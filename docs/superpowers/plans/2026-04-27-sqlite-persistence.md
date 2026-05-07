# SQLite 持久化实施计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 将 InterfaceX 插件的扫描结果、标签数据和配置从 XML 持久化迁移到 SQLite，解决 12 工程场景下全量 PSI 扫描耗时问题。

**Architecture:** 使用 JDBC 直连 SQLite，全局单库存储在 `${user.home}/.interfacex/interfacex.db`。新增 `InterfaceXDatabaseService`（Application 级）管理连接和 DAO，修改 `ProjectInitService` 和 `InterfaceXHelper` 支持从 SQLite 加载缓存，新增标签系统 UI 和持久化。

**Tech Stack:** Java 17, IntelliJ Platform SDK, JDBC (SQLite), Lombok

---

## File Structure

### 新增文件

| 文件 | 职责 |
|---|---|
| `src/main/java/com/kaylves/interfacex/db/InterfaceXDatabaseService.java` | Application 级 Service，管理 SQLite 连接和 DAO 实例 |
| `src/main/java/com/kaylves/interfacex/db/SchemaManager.java` | Schema 创建与版本迁移 |
| `src/main/java/com/kaylves/interfacex/db/dao/ScanResultDao.java` | 扫描结果 CRUD |
| `src/main/java/com/kaylves/interfacex/db/dao/TagDao.java` | 标签 CRUD |
| `src/main/java/com/kaylves/interfacex/db/dao/ConfigDao.java` | 配置 CRUD |
| `src/main/java/com/kaylves/interfacex/db/dao/ScanMetaDao.java` | 扫描元数据 CRUD |
| `src/main/java/com/kaylves/interfacex/db/model/ScanResultEntity.java` | 扫描结果实体 |
| `src/main/java/com/kaylves/interfacex/db/model/TagEntity.java` | 标签实体 |
| `src/main/java/com/kaylves/interfacex/db/model/ConfigEntity.java` | 配置实体 |
| `src/main/java/com/kaylves/interfacex/db/migration/XmlToSqliteMigrator.java` | XML → SQLite 数据迁移 |

### 修改文件

| 文件 | 变更 |
|---|---|
| `build.gradle.kts` | 添加 sqlite-jdbc 依赖 |
| `src/main/java/com/kaylves/interfacex/service/InterfaceXNavigator.java` | 配置读写从 XML 迁移到 SQLite |
| `src/main/java/com/kaylves/interfacex/ui/navigator/InterfaceXNavigatorState.java` | 添加从 SQLite 加载配置的方法 |
| `src/main/java/com/kaylves/interfacex/service/ProjectInitService.java` | 优先从 SQLite 加载缓存 |
| `src/main/java/com/kaylves/interfacex/utils/InterfaceXHelper.java` | 支持从 SQLite 加载 + 写入扫描结果 |
| `src/main/java/com/kaylves/interfacex/ui/navigator/InterfaceXPopupMenu.java` | 添加标签操作菜单项 |
| `src/main/java/com/kaylves/interfacex/ui/navigator/InterfaceXSimpleTreeStructure.java` | ServiceNode 展示标签 |
| `src/main/java/com/kaylves/interfacex/action/toolbar/RefreshProjectAction.java` | 刷新时更新 SQLite |
| `src/main/java/com/kaylves/interfacex/action/toolbar/SettingProjectAction.java` | 配置保存到 SQLite |

---

### Task 1: 添加 SQLite JDBC 依赖

**Files:**
- Modify: `build.gradle.kts`

- [ ] **Step 1: 在 build.gradle.kts 的 dependencies 块中添加 sqlite-jdbc 依赖**

在 `dependencies` 块中添加：

```kotlin
implementation("org.xerial:sqlite-jdbc:3.45.1.0")
```

- [ ] **Step 2: 同步 Gradle 并验证依赖**

Run: `cd /Users/kaylves/github-src/interfacex && ./gradlew dependencies --configuration runtimeClasspath 2>&1 | grep sqlite`
Expected: 输出包含 `sqlite-jdbc`

- [ ] **Step 3: Commit**

```bash
git add build.gradle.kts
git commit -m "feat: add sqlite-jdbc dependency"
```

---

### Task 2: 创建数据库 Model 实体类

**Files:**
- Create: `src/main/java/com/kaylves/interfacex/db/model/ScanResultEntity.java`
- Create: `src/main/java/com/kaylves/interfacex/db/model/TagEntity.java`
- Create: `src/main/java/com/kaylves/interfacex/db/model/ConfigEntity.java`

- [ ] **Step 1: 创建 ScanResultEntity**

```java
package com.kaylves.interfacex.db.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScanResultEntity {
    private Long id;
    private String projectPath;
    private String moduleName;
    private String category;
    private String url;
    private String httpMethod;
    private String className;
    private String methodName;
    private Integer psiElementHash;
    private String partner;
    private Long scanTime;
}
```

- [ ] **Step 2: 创建 TagEntity**

```java
package com.kaylves.interfacex.db.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TagEntity {
    private Long id;
    private String projectPath;
    private String moduleName;
    private String category;
    private String url;
    private String httpMethod;
    private String methodName;
    private String tagName;
    private String tagValue;
    private Long createdTime;
    private Long updatedTime;
}
```

- [ ] **Step 3: 创建 ConfigEntity**

```java
package com.kaylves.interfacex.db.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConfigEntity {
    private Long id;
    private String projectPath;
    private String configKey;
    private String configValue;
    private Long updatedTime;
}
```

- [ ] **Step 4: Commit**

```bash
git add src/main/java/com/kaylves/interfacex/db/model/
git commit -m "feat: add database model entities"
```

---

### Task 3: 创建 SchemaManager

**Files:**
- Create: `src/main/java/com/kaylves/interfacex/db/SchemaManager.java`

- [ ] **Step 1: 创建 SchemaManager**

```java
package com.kaylves.interfacex.db;

import lombok.extern.slf4j.Slf4j;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

@Slf4j
public class SchemaManager {

    private static final int CURRENT_VERSION = 1;

    public void initialize(Connection conn) throws SQLException {
        createSchemaVersionTable(conn);
        int currentVersion = getCurrentVersion(conn);
        if (currentVersion < CURRENT_VERSION) {
            applyMigrations(conn, currentVersion, CURRENT_VERSION);
        }
    }

    private void createSchemaVersionTable(Connection conn) throws SQLException {
        String sql = "CREATE TABLE IF NOT EXISTS schema_version ("
                + "version INTEGER PRIMARY KEY"
                + ")";
        try (Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        }
    }

    private int getCurrentVersion(Connection conn) throws SQLException {
        String sql = "SELECT MAX(version) FROM schema_version";
        try (var rs = conn.createStatement().executeQuery(sql)) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        return 0;
    }

    private void applyMigrations(Connection conn, int fromVersion, int toVersion) throws SQLException {
        for (int v = fromVersion + 1; v <= toVersion; v++) {
            applyMigration(conn, v);
        }
    }

    private void applyMigration(Connection conn, int version) throws SQLException {
        try (Statement stmt = conn.createStatement()) {
            switch (version) {
                case 1 -> applyV1(stmt);
                default -> throw new SQLException("Unknown migration version: " + version);
            }
            stmt.execute("INSERT INTO schema_version (version) VALUES (" + version + ")");
        }
        log.info("Applied schema migration v{}", version);
    }

    private void applyV1(Statement stmt) throws SQLException {
        stmt.execute("CREATE TABLE IF NOT EXISTS scan_result ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
                + "project_path TEXT NOT NULL,"
                + "module_name TEXT NOT NULL,"
                + "category TEXT NOT NULL,"
                + "url TEXT NOT NULL,"
                + "http_method TEXT,"
                + "class_name TEXT,"
                + "method_name TEXT,"
                + "psi_element_hash INTEGER,"
                + "partner TEXT,"
                + "scan_time INTEGER NOT NULL,"
                + "UNIQUE(project_path, module_name, category, url, http_method, method_name)"
                + ")");

        stmt.execute("CREATE TABLE IF NOT EXISTS tag ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
                + "project_path TEXT NOT NULL,"
                + "module_name TEXT NOT NULL,"
                + "category TEXT NOT NULL,"
                + "url TEXT NOT NULL,"
                + "http_method TEXT,"
                + "method_name TEXT NOT NULL,"
                + "tag_name TEXT NOT NULL,"
                + "tag_value TEXT,"
                + "created_time INTEGER NOT NULL,"
                + "updated_time INTEGER NOT NULL,"
                + "UNIQUE(project_path, module_name, category, url, http_method, method_name, tag_name)"
                + ")");

        stmt.execute("CREATE TABLE IF NOT EXISTS scan_meta ("
                + "project_path TEXT PRIMARY KEY,"
                + "last_scan_time INTEGER NOT NULL,"
                + "scan_version INTEGER NOT NULL DEFAULT 1"
                + ")");

        stmt.execute("CREATE TABLE IF NOT EXISTS config ("
                + "project_path TEXT NOT NULL,"
                + "config_key TEXT NOT NULL,"
                + "config_value TEXT NOT NULL,"
                + "updated_time INTEGER NOT NULL,"
                + "UNIQUE(project_path, config_key)"
                + ")");

        stmt.execute("CREATE INDEX IF NOT EXISTS idx_scan_project ON scan_result(project_path)");
        stmt.execute("CREATE INDEX IF NOT EXISTS idx_scan_category ON scan_result(project_path, category)");
        stmt.execute("CREATE INDEX IF NOT EXISTS idx_tag_project ON tag(project_path)");
        stmt.execute("CREATE INDEX IF NOT EXISTS idx_tag_name ON tag(tag_name)");
    }
}
```

- [ ] **Step 2: Commit**

```bash
git add src/main/java/com/kaylves/interfacex/db/SchemaManager.java
git commit -m "feat: add SchemaManager for SQLite schema creation and migration"
```

---

### Task 4: 创建 DAO 层

**Files:**
- Create: `src/main/java/com/kaylves/interfacex/db/dao/ScanResultDao.java`
- Create: `src/main/java/com/kaylves/interfacex/db/dao/TagDao.java`
- Create: `src/main/java/com/kaylves/interfacex/db/dao/ConfigDao.java`
- Create: `src/main/java/com/kaylves/interfacex/db/dao/ScanMetaDao.java`

- [ ] **Step 1: 创建 ScanResultDao**

```java
package com.kaylves.interfacex.db.dao;

import com.kaylves.interfacex.db.model.ScanResultEntity;
import lombok.extern.slf4j.Slf4j;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class ScanResultDao {

    private final Connection connection;

    public ScanResultDao(Connection connection) {
        this.connection = connection;
    }

    public void batchUpsert(String projectPath, List<ScanResultEntity> entities) throws SQLException {
        String sql = "INSERT OR REPLACE INTO scan_result "
                + "(project_path, module_name, category, url, http_method, class_name, method_name, psi_element_hash, partner, scan_time) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            for (ScanResultEntity entity : entities) {
                ps.setString(1, entity.getProjectPath());
                ps.setString(2, entity.getModuleName());
                ps.setString(3, entity.getCategory());
                ps.setString(4, entity.getUrl());
                ps.setString(5, entity.getHttpMethod());
                ps.setString(6, entity.getClassName());
                ps.setString(7, entity.getMethodName());
                ps.setInt(8, entity.getPsiElementHash() != null ? entity.getPsiElementHash() : 0);
                ps.setString(9, entity.getPartner());
                ps.setLong(10, entity.getScanTime());
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    public void deleteByProjectPath(String projectPath) throws SQLException {
        String sql = "DELETE FROM scan_result WHERE project_path = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, projectPath);
            ps.executeUpdate();
        }
    }

    public List<ScanResultEntity> findByProjectPath(String projectPath) throws SQLException {
        String sql = "SELECT * FROM scan_result WHERE project_path = ?";
        List<ScanResultEntity> results = new ArrayList<>();
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, projectPath);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    results.add(mapRow(rs));
                }
            }
        }
        return results;
    }

    private ScanResultEntity mapRow(ResultSet rs) throws SQLException {
        return ScanResultEntity.builder()
                .id(rs.getLong("id"))
                .projectPath(rs.getString("project_path"))
                .moduleName(rs.getString("module_name"))
                .category(rs.getString("category"))
                .url(rs.getString("url"))
                .httpMethod(rs.getString("http_method"))
                .className(rs.getString("class_name"))
                .methodName(rs.getString("method_name"))
                .psiElementHash(rs.getInt("psi_element_hash"))
                .partner(rs.getString("partner"))
                .scanTime(rs.getLong("scan_time"))
                .build();
    }
}
```

- [ ] **Step 2: 创建 TagDao**

```java
package com.kaylves.interfacex.db.dao;

import com.kaylves.interfacex.db.model.TagEntity;
import lombok.extern.slf4j.Slf4j;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class TagDao {

    private final Connection connection;

    public TagDao(Connection connection) {
        this.connection = connection;
    }

    public void insert(TagEntity entity) throws SQLException {
        String sql = "INSERT OR REPLACE INTO tag "
                + "(project_path, module_name, category, url, http_method, method_name, tag_name, tag_value, created_time, updated_time) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, entity.getProjectPath());
            ps.setString(2, entity.getModuleName());
            ps.setString(3, entity.getCategory());
            ps.setString(4, entity.getUrl());
            ps.setString(5, entity.getHttpMethod());
            ps.setString(6, entity.getMethodName());
            ps.setString(7, entity.getTagName());
            ps.setString(8, entity.getTagValue());
            ps.setLong(9, entity.getCreatedTime());
            ps.setLong(10, entity.getUpdatedTime());
            ps.executeUpdate();
        }
    }

    public void delete(String projectPath, String moduleName, String category,
                       String url, String httpMethod, String methodName, String tagName) throws SQLException {
        String sql = "DELETE FROM tag WHERE project_path = ? AND module_name = ? AND category = ? "
                + "AND url = ? AND http_method = ? AND method_name = ? AND tag_name = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, projectPath);
            ps.setString(2, moduleName);
            ps.setString(3, category);
            ps.setString(4, url);
            ps.setString(5, httpMethod);
            ps.setString(6, methodName);
            ps.setString(7, tagName);
            ps.executeUpdate();
        }
    }

    public List<TagEntity> findByProjectPath(String projectPath) throws SQLException {
        String sql = "SELECT * FROM tag WHERE project_path = ?";
        List<TagEntity> results = new ArrayList<>();
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, projectPath);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    results.add(mapRow(rs));
                }
            }
        }
        return results;
    }

    public List<TagEntity> findByInterface(String projectPath, String moduleName, String category,
                                            String url, String httpMethod, String methodName) throws SQLException {
        String sql = "SELECT * FROM tag WHERE project_path = ? AND module_name = ? AND category = ? "
                + "AND url = ? AND http_method = ? AND method_name = ?";
        List<TagEntity> results = new ArrayList<>();
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, projectPath);
            ps.setString(2, moduleName);
            ps.setString(3, category);
            ps.setString(4, url);
            ps.setString(5, httpMethod);
            ps.setString(6, methodName);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    results.add(mapRow(rs));
                }
            }
        }
        return results;
    }

    public List<TagEntity> findByTagName(String projectPath, String tagName) throws SQLException {
        String sql = "SELECT * FROM tag WHERE project_path = ? AND tag_name = ?";
        List<TagEntity> results = new ArrayList<>();
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, projectPath);
            ps.setString(2, tagName);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    results.add(mapRow(rs));
                }
            }
        }
        return results;
    }

    private TagEntity mapRow(ResultSet rs) throws SQLException {
        return TagEntity.builder()
                .id(rs.getLong("id"))
                .projectPath(rs.getString("project_path"))
                .moduleName(rs.getString("module_name"))
                .category(rs.getString("category"))
                .url(rs.getString("url"))
                .httpMethod(rs.getString("http_method"))
                .methodName(rs.getString("method_name"))
                .tagName(rs.getString("tag_name"))
                .tagValue(rs.getString("tag_value"))
                .createdTime(rs.getLong("created_time"))
                .updatedTime(rs.getLong("updated_time"))
                .build();
    }
}
```

- [ ] **Step 3: 创建 ConfigDao**

```java
package com.kaylves.interfacex.db.dao;

import com.kaylves.interfacex.db.model.ConfigEntity;
import lombok.extern.slf4j.Slf4j;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class ConfigDao {

    private final Connection connection;

    public ConfigDao(Connection connection) {
        this.connection = connection;
    }

    public void upsert(ConfigEntity entity) throws SQLException {
        String sql = "INSERT OR REPLACE INTO config (project_path, config_key, config_value, updated_time) "
                + "VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, entity.getProjectPath());
            ps.setString(2, entity.getConfigKey());
            ps.setString(3, entity.getConfigValue());
            ps.setLong(4, entity.getUpdatedTime());
            ps.executeUpdate();
        }
    }

    public String getValue(String projectPath, String configKey) throws SQLException {
        String sql = "SELECT config_value FROM config WHERE project_path = ? AND config_key = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, projectPath);
            ps.setString(2, configKey);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("config_value");
                }
            }
        }
        return null;
    }

    public List<ConfigEntity> findByProjectPath(String projectPath) throws SQLException {
        String sql = "SELECT * FROM config WHERE project_path = ?";
        List<ConfigEntity> results = new ArrayList<>();
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, projectPath);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    results.add(mapRow(rs));
                }
            }
        }
        return results;
    }

    public void deleteByProjectPathAndKey(String projectPath, String configKey) throws SQLException {
        String sql = "DELETE FROM config WHERE project_path = ? AND config_key = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, projectPath);
            ps.setString(2, configKey);
            ps.executeUpdate();
        }
    }

    private ConfigEntity mapRow(ResultSet rs) throws SQLException {
        return ConfigEntity.builder()
                .id(rs.getLong("id"))
                .projectPath(rs.getString("project_path"))
                .configKey(rs.getString("config_key"))
                .configValue(rs.getString("config_value"))
                .updatedTime(rs.getLong("updated_time"))
                .build();
    }
}
```

- [ ] **Step 4: 创建 ScanMetaDao**

```java
package com.kaylves.interfacex.db.dao;

import com.kaylves.interfacex.db.model.ScanResultEntity;
import lombok.extern.slf4j.Slf4j;

import java.sql.*;

@Slf4j
public class ScanMetaDao {

    private final Connection connection;

    public ScanMetaDao(Connection connection) {
        this.connection = connection;
    }

    public Long getLastScanTime(String projectPath) throws SQLException {
        String sql = "SELECT last_scan_time FROM scan_meta WHERE project_path = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, projectPath);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getLong("last_scan_time");
                }
            }
        }
        return null;
    }

    public void updateScanTime(String projectPath, long scanTime) throws SQLException {
        String sql = "INSERT OR REPLACE INTO scan_meta (project_path, last_scan_time, scan_version) "
                + "VALUES (?, ?, 1)";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, projectPath);
            ps.setLong(2, scanTime);
            ps.executeUpdate();
        }
    }

    public void deleteByProjectPath(String projectPath) throws SQLException {
        String sql = "DELETE FROM scan_meta WHERE project_path = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, projectPath);
            ps.executeUpdate();
        }
    }
}
```

- [ ] **Step 5: Commit**

```bash
git add src/main/java/com/kaylves/interfacex/db/dao/
git commit -m "feat: add DAO layer for scan_result, tag, config, scan_meta"
```

---

### Task 5: 创建 InterfaceXDatabaseService

**Files:**
- Create: `src/main/java/com/kaylves/interfacex/db/InterfaceXDatabaseService.java`

- [ ] **Step 1: 创建 InterfaceXDatabaseService**

```java
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
public final class InterfaceXDatabaseService implements AutoCloseable {

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
            File dbDir = new File(DB_DIR);
            if (!dbDir.exists()) {
                dbDir.mkdirs();
            }
            connection = DriverManager.getConnection(JDBC_URL);
            connection.setAutoCommit(true);
            schemaManager.initialize(connection);
            scanResultDao = new ScanResultDao(connection);
            tagDao = new TagDao(connection);
            configDao = new ConfigDao(connection);
            scanMetaDao = new ScanMetaDao(connection);
            LOG.info("InterfaceX database initialized at " + DB_PATH);
        } catch (SQLException e) {
            LOG.error("Failed to initialize database", e);
        }
    }

    public synchronized Connection getConnection() {
        if (connection == null) {
            initialize();
        }
        return connection;
    }

    @Override
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
```

- [ ] **Step 2: Commit**

```bash
git add src/main/java/com/kaylves/interfacex/db/InterfaceXDatabaseService.java
git commit -m "feat: add InterfaceXDatabaseService as Application-level SQLite manager"
```

---

### Task 6: 创建 XML 迁移器

**Files:**
- Create: `src/main/java/com/kaylves/interfacex/db/migration/XmlToSqliteMigrator.java`

- [ ] **Step 1: 创建 XmlToSqliteMigrator**

```java
package com.kaylves.interfacex.db.migration;

import com.intellij.openapi.diagnostic.Logger;
import com.kaylves.interfacex.db.InterfaceXDatabaseService;
import com.kaylves.interfacex.db.dao.ConfigDao;
import com.kaylves.interfacex.db.model.ConfigEntity;
import com.kaylves.interfacex.entity.InterfaceItemConfigEntity;

import java.io.File;
import java.sql.SQLException;
import java.util.List;

public class XmlToSqliteMigrator {

    private static final Logger LOG = Logger.getInstance(XmlToSqliteMigrator.class);

    public static boolean needsMigration(String projectPath) {
        File xmlFile = new File(projectPath, ".idea/InterfaceX.xml");
        return xmlFile.exists();
    }

    public static void migrate(String projectPath, boolean showPort,
                                List<InterfaceItemConfigEntity> configEntities) {
        InterfaceXDatabaseService dbService = InterfaceXDatabaseService.getInstance();
        dbService.initialize();
        ConfigDao configDao = dbService.getConfigDao();

        try {
            ConfigEntity showPortEntity = ConfigEntity.builder()
                    .projectPath(projectPath)
                    .configKey("showPort")
                    .configValue(String.valueOf(showPort))
                    .updatedTime(System.currentTimeMillis())
                    .build();
            configDao.upsert(showPortEntity);

            if (configEntities != null) {
                for (InterfaceItemConfigEntity entity : configEntities) {
                    ConfigEntity configEntity = ConfigEntity.builder()
                            .projectPath(projectPath)
                            .configKey("itemCategory." + entity.getItemCategory())
                            .configValue(String.valueOf(entity.getEnabled()))
                            .updatedTime(System.currentTimeMillis())
                            .build();
                    configDao.upsert(configEntity);
                }
            }

            File xmlFile = new File(projectPath, ".idea/InterfaceX.xml");
            if (xmlFile.exists()) {
                boolean deleted = xmlFile.delete();
                LOG.info("XML migration completed, file deleted: " + deleted);
            }
        } catch (SQLException e) {
            LOG.error("Failed to migrate XML to SQLite", e);
        }
    }
}
```

- [ ] **Step 2: Commit**

```bash
git add src/main/java/com/kaylves/interfacex/db/migration/XmlToSqliteMigrator.java
git commit -m "feat: add XmlToSqliteMigrator for config migration"
```

---

### Task 7: 修改 InterfaceXNavigator — 配置读写迁移到 SQLite

**Files:**
- Modify: `src/main/java/com/kaylves/interfacex/service/InterfaceXNavigator.java`
- Modify: `src/main/java/com/kaylves/interfacex/ui/navigator/InterfaceXNavigatorState.java`

- [ ] **Step 1: 在 InterfaceXNavigatorState 中添加从 SQLite 加载配置的方法**

在 `InterfaceXNavigatorState.java` 中添加方法：

```java
public void loadFromDatabase(String projectPath) {
    InterfaceXDatabaseService dbService = InterfaceXDatabaseService.getInstance();
    dbService.initialize();
    ConfigDao configDao = dbService.getConfigDao();

    try {
        List<ConfigEntity> configs = configDao.findByProjectPath(projectPath);
        List<InterfaceItemConfigEntity> entities = new ArrayList<>();

        for (ConfigEntity config : configs) {
            if ("showPort".equals(config.getConfigKey())) {
                this.showPort = Boolean.parseBoolean(config.getConfigValue());
            } else if (config.getConfigKey().startsWith("itemCategory.")) {
                String category = config.getConfigKey().substring("itemCategory.".length());
                boolean enabled = Boolean.parseBoolean(config.getConfigValue());
                entities.add(new InterfaceItemConfigEntity(category, enabled));
            }
        }

        if (!entities.isEmpty()) {
            this.interfaceItemConfigEntities = entities;
        }
    } catch (SQLException e) {
        log.error("Failed to load config from SQLite", e);
    }
}
```

需要在 `InterfaceXNavigatorState.java` 顶部添加导入：

```java
import com.kaylves.interfacex.db.InterfaceXDatabaseService;
import com.kaylves.interfacex.db.dao.ConfigDao;
import com.kaylves.interfacex.db.model.ConfigEntity;
import com.kaylves.interfacex.entity.InterfaceItemConfigEntity;
import java.sql.SQLException;
import java.util.ArrayList;
import lombok.extern.slf4j.Slf4j;
```

并在类声明上添加 `@Slf4j` 注解。

- [ ] **Step 2: 修改 InterfaceXNavigator — 初始化时从 SQLite 加载配置**

在 `InterfaceXNavigator` 构造函数中添加数据库初始化和迁移逻辑：

```java
public InterfaceXNavigator(Project project) {
    this.project = project;
    String projectPath = project.getBasePath();

    InterfaceXDatabaseService.getInstance().initialize();

    if (XmlToSqliteMigrator.needsMigration(projectPath)) {
        XmlToSqliteMigrator.migrate(projectPath, xNavigatorState.isShowPort(),
                xNavigatorState.getInterfaceItemConfigEntities());
    }

    xNavigatorState.loadFromDatabase(projectPath);
}
```

需要在 `InterfaceXNavigator.java` 顶部添加导入：

```java
import com.kaylves.interfacex.db.InterfaceXDatabaseService;
import com.kaylves.interfacex.db.migration.XmlToSqliteMigrator;
```

- [ ] **Step 3: 修改 InterfaceXNavigator — loadState 时同步到 SQLite**

修改 `loadState` 方法：

```java
@Override
public void loadState(@NotNull InterfaceXNavigatorState state) {
    this.xNavigatorState = state;
    xNavigatorState.loadFromDatabase(project.getBasePath());
    scheduleStructureUpdate();
}
```

- [ ] **Step 4: Commit**

```bash
git add src/main/java/com/kaylves/interfacex/service/InterfaceXNavigator.java
git add src/main/java/com/kaylves/interfacex/ui/navigator/InterfaceXNavigatorState.java
git commit -m "feat: migrate InterfaceXNavigator config from XML to SQLite"
```

---

### Task 8: 修改 SettingProjectAction — 配置保存到 SQLite

**Files:**
- Modify: `src/main/java/com/kaylves/interfacex/action/toolbar/SettingProjectAction.java`

- [ ] **Step 1: 修改 SettingProjectAction 的 OK 操作，保存配置到 SQLite**

修改 `getDialogBuilder` 方法中的 `setOkOperation`：

```java
dialogBuilder.setOkOperation(() -> {
    InterfaceXNavigatorState xNavigatorState = xNavigator.getXNavigatorState();
    xNavigatorState.setInterfaceItemConfigEntities(settingForm.getInterfaceItemConfigEntities());

    InterfaceXDatabaseService dbService = InterfaceXDatabaseService.getInstance();
    ConfigDao configDao = dbService.getConfigDao();
    String projectPath = project.getBasePath();

    try {
        configDao.upsert(ConfigEntity.builder()
                .projectPath(projectPath)
                .configKey("showPort")
                .configValue(String.valueOf(xNavigatorState.isShowPort()))
                .updatedTime(System.currentTimeMillis())
                .build());

        for (InterfaceItemConfigEntity entity : xNavigatorState.getInterfaceItemConfigEntities()) {
            configDao.upsert(ConfigEntity.builder()
                    .projectPath(projectPath)
                    .configKey("itemCategory." + entity.getItemCategory())
                    .configValue(String.valueOf(entity.getEnabled()))
                    .updatedTime(System.currentTimeMillis())
                    .build());
        }
    } catch (SQLException ex) {
        LOG.error("Failed to save config to SQLite", ex);
    }

    dialogBuilder.getDialogWrapper().doCancelAction();
});
```

添加导入：

```java
import com.kaylves.interfacex.db.InterfaceXDatabaseService;
import com.kaylves.interfacex.db.dao.ConfigDao;
import com.kaylves.interfacex.db.model.ConfigEntity;
import java.sql.SQLException;
```

- [ ] **Step 2: Commit**

```bash
git add src/main/java/com/kaylves/interfacex/action/toolbar/SettingProjectAction.java
git commit -m "feat: save settings to SQLite via ConfigDao"
```

---

### Task 9: 修改 InterfaceXHelper — 支持从 SQLite 加载和写入扫描结果

**Files:**
- Modify: `src/main/java/com/kaylves/interfacex/utils/InterfaceXHelper.java`

- [ ] **Step 1: 在 InterfaceXHelper 中添加从 SQLite 加载缓存的方法**

在 `InterfaceXHelper` 类中添加新方法：

```java
public static List<InterfaceProject> getInterfaceProjectFromCache(Project project, InterfaceXNavigatorState navigatorState) {
    InterfaceXDatabaseService dbService = InterfaceXDatabaseService.getInstance();
    dbService.initialize();
    ScanResultDao scanResultDao = dbService.getScanResultDao();
    ScanMetaDao scanMetaDao = dbService.getScanMetaDao();
    TagDao tagDao = dbService.getTagDao();

    String projectPath = project.getBasePath();

    try {
        Long lastScanTime = scanMetaDao.getLastScanTime(projectPath);
        if (lastScanTime == null) {
            return null;
        }

        List<ScanResultEntity> cachedResults = scanResultDao.findByProjectPath(projectPath);
        if (cachedResults.isEmpty()) {
            return null;
        }

        Map<String, List<InterfaceItem>> serviceItemMap = new LinkedHashMap<>();
        Map<String, List<ScanResultEntity>> groupedByModule = new LinkedHashMap<>();

        for (ScanResultEntity entity : cachedResults) {
            groupedByModule.computeIfAbsent(entity.getModuleName(), k -> new ArrayList<>()).add(entity);
        }

        List<InterfaceProject> projects = new ArrayList<>();
        Module[] modules = ModuleManager.getInstance(project).getModules();

        for (Module module : modules) {
            List<ScanResultEntity> moduleResults = groupedByModule.get(module.getName());
            if (moduleResults == null || moduleResults.isEmpty()) {
                continue;
            }

            Map<String, List<InterfaceItem>> moduleServiceMap = new LinkedHashMap<>();
            for (ScanResultEntity entity : moduleResults) {
                HttpItem httpItem = HttpItem.builder().url(entity.getUrl()).build();
                InterfaceItem item = new InterfaceItem(
                        null,
                        InterfaceItemCategoryEnum.valueOf(entity.getCategory()),
                        entity.getHttpMethod(),
                        httpItem,
                        true
                );
                item.setModule(module);
                moduleServiceMap.computeIfAbsent(entity.getCategory(), k -> new ArrayList<>()).add(item);
            }

            projects.add(new InterfaceProject(module, moduleServiceMap));
        }

        return projects;
    } catch (SQLException e) {
        log.error("Failed to load from SQLite cache", e);
        return null;
    }
}
```

- [ ] **Step 2: 在 InterfaceXHelper 中添加写入扫描结果到 SQLite 的方法**

```java
public static void saveScanResultsToDatabase(Project project, List<InterfaceProject> projects) {
    InterfaceXDatabaseService dbService = InterfaceXDatabaseService.getInstance();
    dbService.initialize();
    ScanResultDao scanResultDao = dbService.getScanResultDao();
    ScanMetaDao scanMetaDao = dbService.getScanMetaDao();

    String projectPath = project.getBasePath();
    long scanTime = System.currentTimeMillis();

    try {
        scanResultDao.deleteByProjectPath(projectPath);

        for (InterfaceProject interfaceProject : projects) {
            List<ScanResultEntity> entities = new ArrayList<>();
            Map<String, List<InterfaceItem>> serviceItemMap = interfaceProject.getServiceItemMap();

            for (Map.Entry<String, List<InterfaceItem>> entry : serviceItemMap.entrySet()) {
                String category = entry.getKey();
                for (InterfaceItem item : entry.getValue()) {
                    ScanResultEntity entity = ScanResultEntity.builder()
                            .projectPath(projectPath)
                            .moduleName(interfaceProject.getModuleName())
                            .category(category)
                            .url(item.getUrl())
                            .httpMethod(item.getMethod() != null ? item.getMethod().name() : null)
                            .className(item.getPsiMethod() != null && item.getPsiMethod().getContainingClass() != null
                                    ? item.getPsiMethod().getContainingClass().getQualifiedName() : null)
                            .methodName(item.getPsiMethod() != null ? item.getPsiMethod().getName() : null)
                            .psiElementHash(item.getPsiElement() != null ? item.getPsiElement().hashCode() : 0)
                            .partner(null)
                            .scanTime(scanTime)
                            .build();
                    entities.add(entity);
                }
            }

            if (!entities.isEmpty()) {
                scanResultDao.batchUpsert(projectPath, entities);
            }
        }

        scanMetaDao.updateScanTime(projectPath, scanTime);
        log.info("Saved scan results to SQLite for project: {}", projectPath);
    } catch (SQLException e) {
        log.error("Failed to save scan results to SQLite", e);
    }
}
```

添加导入：

```java
import com.kaylves.interfacex.db.InterfaceXDatabaseService;
import com.kaylves.interfacex.db.dao.ScanMetaDao;
import com.kaylves.interfacex.db.dao.ScanResultDao;
import com.kaylves.interfacex.db.dao.TagDao;
import com.kaylves.interfacex.db.model.ScanResultEntity;
import com.kaylves.interfacex.module.http.HttpItem;
import java.sql.SQLException;
```

- [ ] **Step 3: Commit**

```bash
git add src/main/java/com/kaylves/interfacex/utils/InterfaceXHelper.java
git commit -m "feat: add SQLite cache load/save methods to InterfaceXHelper"
```

---

### Task 10: 修改 ProjectInitService — 优先从 SQLite 加载缓存

**Files:**
- Modify: `src/main/java/com/kaylves/interfacex/service/ProjectInitService.java`

- [ ] **Step 1: 修改 getServiceProjects 方法，优先从缓存加载**

```java
public List<InterfaceProject> getServiceProjects() {
    InterfaceXNavigator navigator = InterfaceXNavigator.getInstance(project);

    List<InterfaceProject> cachedProjects = DumbService
            .getInstance(project)
            .runReadActionInSmartMode(() ->
                    InterfaceXHelper.getInterfaceProjectFromCache(project, navigator.xNavigatorState)
            );

    if (cachedProjects != null && !cachedProjects.isEmpty()) {
        return cachedProjects;
    }

    List<InterfaceProject> projects = DumbService
            .getInstance(project)
            .runReadActionInSmartMode(() ->
                    InterfaceXHelper.getInterfaceProjectUsingResolver(project, navigator.xNavigatorState)
            );

    InterfaceXHelper.saveScanResultsToDatabase(project, projects);

    return projects;
}
```

- [ ] **Step 2: Commit**

```bash
git add src/main/java/com/kaylves/interfacex/service/ProjectInitService.java
git commit -m "feat: load scan results from SQLite cache first in ProjectInitService"
```

---

### Task 11: 修改 RefreshProjectAction — 刷新时更新 SQLite

**Files:**
- Modify: `src/main/java/com/kaylves/interfacex/action/toolbar/RefreshProjectAction.java`

- [ ] **Step 1: 修改 RefreshProjectAction，刷新时强制 PSI 扫描并更新 SQLite**

```java
@Override
public void actionPerformed(AnActionEvent e) {
    LOG.warn("trigger:refresh project");

    final Project project = getProject(e.getDataContext());

    assert project != null;

    InterfaceXNavigator servicesNavigator = InterfaceXNavigator.getInstance(project);
    InterfaceXNavigatorState navigatorState = servicesNavigator.getXNavigatorState();

    List<InterfaceProject> projects = InterfaceXHelper.getInterfaceProjectUsingResolver(project, navigatorState);
    InterfaceXHelper.saveScanResultsToDatabase(project, projects);

    servicesNavigator.scheduleStructureUpdate(true);
}
```

添加导入：

```java
import com.kaylves.interfacex.common.InterfaceProject;
import com.kaylves.interfacex.ui.navigator.InterfaceXNavigatorState;
import com.kaylves.interfacex.utils.InterfaceXHelper;
import java.util.List;
```

- [ ] **Step 2: Commit**

```bash
git add src/main/java/com/kaylves/interfacex/action/toolbar/RefreshProjectAction.java
git commit -m "feat: refresh action now updates SQLite cache"
```

---

### Task 12: 添加标签操作到右键菜单

**Files:**
- Modify: `src/main/java/com/kaylves/interfacex/ui/navigator/InterfaceXPopupMenu.java`

- [ ] **Step 1: 在 InterfaceXPopupMenu 中添加标签操作菜单项**

在 `installPopupMenu` 方法中，在现有菜单项之后添加：

```java
popupGroup.addSeparator();

popupGroup.addAction(new AnAction("添加标签") {
    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        addTag();
    }
});

popupGroup.addAction(new AnAction("移除标签") {
    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        removeTag();
    }
});
```

在类中添加 `addTag` 和 `removeTag` 方法：

```java
private void addTag() {
    SimpleNode simpleNode = simpleTree.getSelectedNode();
    if (!(simpleNode instanceof InterfaceXSimpleTreeStructure.ServiceNode serviceNode)) {
        return;
    }

    InterfaceItem item = serviceNode.interfaceItem;
    String tagName = JOptionPane.showInputDialog(null, "请输入标签名称:", "添加标签", JOptionPane.PLAIN_MESSAGE);

    if (tagName == null || tagName.trim().isEmpty()) {
        return;
    }

    InterfaceXDatabaseService dbService = InterfaceXDatabaseService.getInstance();
    dbService.initialize();
    TagDao tagDao = dbService.getTagDao();

    try {
        TagEntity tagEntity = TagEntity.builder()
                .projectPath(project.getBasePath())
                .moduleName(item.getModule() != null ? item.getModule().getName() : "")
                .category(item.getInterfaceItemCategoryEnum().name())
                .url(item.getUrl())
                .httpMethod(item.getMethod() != null ? item.getMethod().name() : null)
                .methodName(item.getPsiMethod() != null ? item.getPsiMethod().getName() : "")
                .tagName(tagName.trim())
                .createdTime(System.currentTimeMillis())
                .updatedTime(System.currentTimeMillis())
                .build();
        tagDao.insert(tagEntity);

        InterfaceXNavigator.getInstance(project).scheduleStructureUpdate();
    } catch (SQLException ex) {
        log.error("Failed to add tag", ex);
    }
}

private void removeTag() {
    SimpleNode simpleNode = simpleTree.getSelectedNode();
    if (!(simpleNode instanceof InterfaceXSimpleTreeStructure.ServiceNode serviceNode)) {
        return;
    }

    InterfaceItem item = serviceNode.interfaceItem;
    InterfaceXDatabaseService dbService = InterfaceXDatabaseService.getInstance();
    dbService.initialize();
    TagDao tagDao = dbService.getTagDao();

    try {
        List<TagEntity> tags = tagDao.findByInterface(
                project.getBasePath(),
                item.getModule() != null ? item.getModule().getName() : "",
                item.getInterfaceItemCategoryEnum().name(),
                item.getUrl(),
                item.getMethod() != null ? item.getMethod().name() : null,
                item.getPsiMethod() != null ? item.getPsiMethod().getName() : ""
        );

        if (tags.isEmpty()) {
            JOptionPane.showMessageDialog(null, "当前接口没有标签", "提示", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        String[] tagNames = tags.stream().map(TagEntity::getTagName).toArray(String[]::new);
        String selected = (String) JOptionPane.showInputDialog(
                null, "选择要移除的标签:", "移除标签",
                JOptionPane.PLAIN_MESSAGE, null, tagNames, tagNames[0]);

        if (selected == null) {
            return;
        }

        tagDao.delete(
                project.getBasePath(),
                item.getModule() != null ? item.getModule().getName() : "",
                item.getInterfaceItemCategoryEnum().name(),
                item.getUrl(),
                item.getMethod() != null ? item.getMethod().name() : null,
                item.getPsiMethod() != null ? item.getPsiMethod().getName() : "",
                selected
        );

        InterfaceXNavigator.getInstance(project).scheduleStructureUpdate();
    } catch (SQLException ex) {
        log.error("Failed to remove tag", ex);
    }
}
```

添加导入：

```java
import com.kaylves.interfacex.db.InterfaceXDatabaseService;
import com.kaylves.interfacex.db.dao.TagDao;
import com.kaylves.interfacex.db.model.TagEntity;
import java.sql.SQLException;
import java.util.List;
```

- [ ] **Step 2: Commit**

```bash
git add src/main/java/com/kaylves/interfacex/ui/navigator/InterfaceXPopupMenu.java
git commit -m "feat: add tag operations to context menu"
```

---

### Task 13: 修改 ServiceNode 展示标签

**Files:**
- Modify: `src/main/java/com/kaylves/interfacex/ui/navigator/InterfaceXSimpleTreeStructure.java`

- [ ] **Step 1: 在 ServiceNode 的 getName 方法中追加标签标识**

修改 `ServiceNode` 类中的 `getName` 方法：

```java
@Override
public String getName() {
    String baseName = interfaceItem.getName();
    try {
        InterfaceXDatabaseService dbService = InterfaceXDatabaseService.getInstance();
        dbService.initialize();
        TagDao tagDao = dbService.getTagDao();
        List<TagEntity> tags = tagDao.findByInterface(
                interfaceItem.getModule() != null ? interfaceItem.getModule().getProject().getBasePath() : "",
                interfaceItem.getModule() != null ? interfaceItem.getModule().getName() : "",
                interfaceItem.getInterfaceItemCategoryEnum().name(),
                interfaceItem.getUrl(),
                interfaceItem.getMethod() != null ? interfaceItem.getMethod().name() : null,
                interfaceItem.getPsiMethod() != null ? interfaceItem.getPsiMethod().getName() : ""
        );
        if (!tags.isEmpty()) {
            String tagStr = tags.stream()
                    .map(TagEntity::getTagName)
                    .reduce((a, b) -> a + "," + b)
                    .orElse("");
            return baseName + " [" + tagStr + "]";
        }
    } catch (Exception e) {
        log.debug("Failed to load tags for node display", e);
    }
    return baseName;
}
```

添加导入：

```java
import com.kaylves.interfacex.db.InterfaceXDatabaseService;
import com.kaylves.interfacex.db.dao.TagDao;
import com.kaylves.interfacex.db.model.TagEntity;
import java.util.List;
```

- [ ] **Step 2: Commit**

```bash
git add src/main/java/com/kaylves/interfacex/ui/navigator/InterfaceXSimpleTreeStructure.java
git commit -m "feat: display tags on ServiceNode in tree"
```

---

### Task 14: 编译验证

**Files:**
- All modified/created files

- [ ] **Step 1: 运行 Gradle 编译**

Run: `cd /Users/kaylves/github-src/interfacex && ./gradlew build 2>&1 | tail -30`
Expected: BUILD SUCCESSFUL

- [ ] **Step 2: 修复编译错误（如有）**

根据编译输出修复所有错误。

- [ ] **Step 3: Commit**

```bash
git add -A
git commit -m "fix: resolve compilation errors for SQLite persistence"
```

---

### Task 15: 在 plugin.xml 中注册 Application Service

**Files:**
- Modify: `src/main/resources/META-INF/plugin.xml`

- [ ] **Step 1: 在 plugin.xml 的 extensions 块中注册 InterfaceXDatabaseService**

在 `<extensions defaultExtensionNs="com.intellij">` 块内添加：

```xml
<applicationService serviceImplementation="com.kaylves.interfacex.db.InterfaceXDatabaseService"/>
```

- [ ] **Step 2: Commit**

```bash
git add src/main/resources/META-INF/plugin.xml
git commit -m "feat: register InterfaceXDatabaseService in plugin.xml"
```
