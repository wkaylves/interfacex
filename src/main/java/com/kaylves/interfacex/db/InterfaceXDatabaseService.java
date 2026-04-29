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

@Service(Service.Level.APP)
public final class InterfaceXDatabaseService {

    private static final Logger LOG = Logger.getInstance(InterfaceXDatabaseService.class);

    private static final String DB_DIR = System.getProperty("user.home") + File.separator + ".interfacex";
    private static final String DB_PATH = DB_DIR + File.separator + "interfacex.db";
    private static final String JDBC_URL = "jdbc:sqlite:" + DB_PATH;

    private Connection connection;

    @Getter
    private ScanResultDao scanResultDao;

    @Getter
    private TagDao tagDao;

    @Getter
    private ConfigDao configDao;

    @Getter
    private ScanMetaDao scanMetaDao;

    private final SchemaManager schemaManager = new SchemaManager();

    public static InterfaceXDatabaseService getInstance() {
        return ApplicationManager.getApplication().getService(InterfaceXDatabaseService.class);
    }

    public synchronized void initialize() {
        if (connection != null) {
            return;
        }
        try {
            // 确保数据库目录存在且可写
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
            
            // 验证目录可写 - 使用更宽松的检查策略
            if (!dbDir.canWrite()) {
                LOG.warn("Database directory is not writable according to Java canWrite(): " + DB_DIR + ", attempting to create/write a temporary file to verify...");
                
                // 尝试创建一个临时文件来验证实际写入权限
                try {
                    File testFile = new File(dbDir, ".write_test");
                    testFile.createNewFile();
                    if (testFile.exists()) {
                        testFile.delete(); // 清理测试文件
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
            
            // 确保数据库文件可写
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
            
            // 显式加载 SQLite JDBC 驱动
            try {
                Class.forName("org.sqlite.JDBC");
                LOG.info("SQLite JDBC driver loaded successfully");
            } catch (ClassNotFoundException e) {
                LOG.error("SQLite JDBC driver class not found. The driver may not be included in the plugin.", e);
                throw new RuntimeException("SQLite JDBC driver not found", e);
            }
            
            // 建立数据库连接
            connection = DriverManager.getConnection(JDBC_URL);
            connection.setAutoCommit(true);
            
            // 配置 WAL 模式和同步模式
            try (var stmt = connection.createStatement()) {
                stmt.execute("PRAGMA journal_mode=WAL");
                stmt.execute("PRAGMA synchronous=NORMAL");
                LOG.info("Database connection established and configured with WAL mode");
            }
            
            // 初始化数据库 schema 和 DAO
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

    public synchronized Connection getConnection() {
        if (connection == null) {
            initialize();
        }
        return connection;
    }

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
