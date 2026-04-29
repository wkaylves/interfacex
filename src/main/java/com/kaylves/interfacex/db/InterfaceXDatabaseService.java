package com.kaylves.interfacex.db;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.Service;
import com.intellij.openapi.diagnostic.Logger;
import com.kaylves.interfacex.db.dao.ConfigDao;
import com.kaylves.interfacex.db.dao.ScanMetaDao;
import com.kaylves.interfacex.db.dao.ScanResultDao;
import com.kaylves.interfacex.db.dao.TagDao;
import lombok.Getter;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * InterfaceX 数据库服务（应用级单例）
 * <p>管理 SQLite 数据库连接的整个生命周期，包括：</p>
 * <ul>
 *   <li>数据库目录和文件的创建与权限检查</li>
 *   <li>JDBC 驱动加载和连接建立</li>
 *   <li>WAL 模式配置以提升并发性能</li>
 *   <li>Schema 初始化和 DAO 实例管理</li>
 * </ul>
 * <p>数据库文件位置: ~/.interfacex/interfacex.db</p>
 */
@Service(Service.Level.APP)
public final class InterfaceXDatabaseService {

    private static final Logger LOG = Logger.getInstance(InterfaceXDatabaseService.class);

    /** 数据库目录 */
    private static final String DB_DIR = System.getProperty("user.home") + File.separator + ".interfacex";
    /** 数据库文件完整路径 */
    private static final String DB_PATH = DB_DIR + File.separator + "interfacex.db";
    /** JDBC 连接 URL */
    private static final String JDBC_URL = "jdbc:sqlite:" + DB_PATH;

    /** 数据库连接（单连接模式，SQLite 不支持多连接并发写入） */
    private Connection connection;

    /** 扫描结果 DAO */
    @Getter
    private ScanResultDao scanResultDao;

    /** 标签 DAO */
    @Getter
    private TagDao tagDao;

    /** 配置项 DAO */
    @Getter
    private ConfigDao configDao;

    /** 扫描元数据 DAO */
    @Getter
    private ScanMetaDao scanMetaDao;

    /** Schema 版本管理器 */
    private final SchemaManager schemaManager = new SchemaManager();

    /**
     * 获取数据库服务单例
     */
    public static InterfaceXDatabaseService getInstance() {
        return ApplicationManager.getApplication().getService(InterfaceXDatabaseService.class);
    }

    /**
     * 初始化数据库连接和 Schema
     * <p>幂等操作，重复调用时如果连接已存在则直接返回</p>
     * <p>初始化流程：</p>
     * <ol>
     *   <li>确保数据库目录存在且可写</li>
     *   <li>加载 SQLite JDBC 驱动</li>
     *   <li>建立连接并配置 WAL 模式</li>
     *   <li>执行 Schema 迁移</li>
     *   <li>初始化各 DAO 实例</li>
     * </ol>
     */
    public synchronized void initialize() {
        if (connection != null) {
            return;
        }
        try {
            File dbDir = new File(DB_DIR);
            if (!dbDir.exists()) {
                boolean created = dbDir.mkdirs();
                if (created) {
                    LOG.info("Created database directory: " + DB_DIR);
                } else {
                    LOG.error("Failed to create database directory: " + DB_DIR);
                    throw new RuntimeException("Cannot create database directory: " + DB_DIR);
                }
            }

            if (!dbDir.canWrite()) {
                LOG.warn("Database directory is not writable according to Java canWrite(): " + DB_DIR + ", attempting to create/write a temporary file to verify...");

                try {
                    File testFile = new File(dbDir, ".write_test");
                    testFile.createNewFile();
                    if (testFile.exists()) {
                        testFile.delete();
                        LOG.info("Verified actual write permission on database directory: " + DB_DIR);
                    } else {
                        LOG.error("Database directory is not writable: " + DB_DIR);
                        throw new RuntimeException("Database directory is not writable: " + DB_DIR);
                    }
                } catch (Exception e) {
                    LOG.error("Failed to write to database directory: " + DB_DIR, e);
                    throw new RuntimeException("Database directory is not writable: " + DB_DIR);
                }
            }

            File dbFile = new File(DB_PATH);
            if (dbFile.exists()) {
                if (!dbFile.canWrite()) {
                    LOG.warn("Database file is not writable, attempting to fix permissions: " + DB_PATH);
                    boolean success = dbFile.setWritable(true);
                    if (!success) {
                        LOG.error("Failed to set database file as writable: " + DB_PATH);
                        throw new RuntimeException("Database file is not writable: " + DB_PATH);
                    }
                }
            }

            try {
                Class.forName("org.sqlite.JDBC");
                LOG.info("SQLite JDBC driver loaded successfully");
            } catch (ClassNotFoundException e) {
                LOG.error("SQLite JDBC driver class not found. The driver may not be included in the plugin.", e);
                throw new RuntimeException("SQLite JDBC driver not found", e);
            }

            connection = DriverManager.getConnection(JDBC_URL);
            connection.setAutoCommit(true);

            try (var stmt = connection.createStatement()) {
                stmt.execute("PRAGMA journal_mode=WAL");
                stmt.execute("PRAGMA synchronous=NORMAL");
                LOG.info("Database connection established and configured with WAL mode");
            }

            schemaManager.initialize(connection);
            scanResultDao = new ScanResultDao(connection);
            tagDao = new TagDao(connection);
            configDao = new ConfigDao(connection);
            scanMetaDao = new ScanMetaDao(connection);
            LOG.info("InterfaceX database initialized at " + DB_PATH);
        } catch (SQLException e) {
            LOG.error("Failed to initialize database at " + DB_PATH + ". Error: " + e.getMessage(), e);
            throw new RuntimeException("Failed to initialize InterfaceX database: " + e.getMessage(), e);
        }
    }

    /**
     * 获取数据库连接，未初始化时自动初始化
     */
    public synchronized Connection getConnection() {
        if (connection == null) {
            initialize();
        }
        return connection;
    }

    /**
     * 关闭数据库连接并释放资源
     */
    public synchronized void close() {
        if (connection != null) {
            try {
                connection.close();
                connection = null;
                LOG.info("InterfaceX database connection closed");
            } catch (SQLException e) {
                LOG.error("Failed to close database connection", e);
            }
        }
    }
}
