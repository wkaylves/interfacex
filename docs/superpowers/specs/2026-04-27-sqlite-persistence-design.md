# SQLite 持久化方案设计

## 概述

将 InterfaceX 插件的扫描结果和标签数据持久化到 SQLite 数据库，替代现有的 XML 持久化方案，解决 12 工程同时打开时全量 PSI 扫描耗时严重的问题。

## 需求

1. 持久化扫描结果缓存，避免重复 PSI 扫描
2. 持久化接口标签数据
3. 将现有 `InterfaceX.xml` 配置迁移到 SQLite
4. 全局单库存储，跨项目管理
5. 全量扫描更新策略（手动刷新时触发）

## 技术选型

**方案：JDBC 直连 SQLite**

- 零额外依赖（IntelliJ 平台自带 SQLite JDBC 驱动）
- 实现简单直接，标准 JDBC 操作
- 性能优异，SQLite 单文件数据库适合本场景
- 如平台不包含驱动，fallback 引入 `org.xerial:sqlite-jdbc:3.45.1.0`

## 数据库设计

### 文件位置

```
${user.home}/.interfacex/interfacex.db
```

### 表结构

#### scan_result — 扫描结果缓存

```sql
CREATE TABLE IF NOT EXISTS scan_result (
    id              INTEGER PRIMARY KEY AUTOINCREMENT,
    project_path    TEXT NOT NULL,
    module_name     TEXT NOT NULL,
    category        TEXT NOT NULL,
    url             TEXT NOT NULL,
    http_method     TEXT,
    class_name      TEXT,                  -- 类全限定名 (如 com.example.service.UserService)
    method_name     TEXT,
    psi_element_hash INTEGER,              -- PSI 元素哈希（用于变更检测）
    partner         TEXT,
    scan_time       INTEGER NOT NULL,
    UNIQUE(project_path, module_name, category, url, http_method, method_name)
);
```

#### tag — 接口标签

```sql
CREATE TABLE IF NOT EXISTS tag (
    id              INTEGER PRIMARY KEY AUTOINCREMENT,
    project_path    TEXT NOT NULL,
    module_name     TEXT NOT NULL,
    category        TEXT NOT NULL,
    url             TEXT NOT NULL,
    http_method     TEXT,
    method_name     TEXT NOT NULL,
    tag_name        TEXT NOT NULL,
    tag_value       TEXT,
    created_time    INTEGER NOT NULL,
    updated_time    INTEGER NOT NULL,
    UNIQUE(project_path, module_name, category, url, http_method, method_name, tag_name)
);
```

#### scan_meta — 扫描元数据

```sql
CREATE TABLE IF NOT EXISTS scan_meta (
    project_path    TEXT PRIMARY KEY,
    last_scan_time  INTEGER NOT NULL,
    scan_version    INTEGER NOT NULL DEFAULT 1
);
```

#### config — 配置存储（替代 InterfaceX.xml）

```sql
CREATE TABLE IF NOT EXISTS config (
    project_path    TEXT NOT NULL,
    config_key      TEXT NOT NULL,
    config_value    TEXT NOT NULL,
    updated_time    INTEGER NOT NULL,
    UNIQUE(project_path, config_key)
);
```

### 索引

```sql
CREATE INDEX IF NOT EXISTS idx_scan_project ON scan_result(project_path);
CREATE INDEX IF NOT EXISTS idx_scan_category ON scan_result(project_path, category);
CREATE INDEX IF NOT EXISTS idx_tag_project ON tag(project_path);
CREATE INDEX IF NOT EXISTS idx_tag_name ON tag(tag_name);
```

### 配置数据映射

| 原 XML 字段 | config 表存储 |
|---|---|
| `showPort` | `config_key="showPort"`, `config_value="true"` |
| `interfaceItemConfigEntities[i].itemCategory` + `enabled` | `config_key="itemCategory.HTTP"`, `config_value="true"` |

## 架构设计

### 包结构

```
com.kaylves.interfacex.db/
├── InterfaceXDatabaseService.java      -- Application 级 Service
├── SchemaManager.java                  -- Schema 创建与版本迁移
├── dao/
│   ├── ScanResultDao.java              -- 扫描结果 CRUD
│   ├── TagDao.java                     -- 标签 CRUD
│   ├── ConfigDao.java                  -- 配置 CRUD
│   └── ScanMetaDao.java               -- 扫描元数据 CRUD
├── model/
│   ├── ScanResultEntity.java           -- 扫描结果实体
│   ├── TagEntity.java                  -- 标签实体
│   └── ConfigEntity.java              -- 配置实体
└── migration/
    └── XmlToSqliteMigrator.java        -- XML → SQLite 数据迁移
```

### 核心类

#### InterfaceXDatabaseService

Application 级别 Service，管理数据库连接和 DAO 实例。

```java
@Service(Service.Level.APP)
public final class InterfaceXDatabaseService implements Disposable {
    private Connection connection;
    private SchemaManager schemaManager;
    private ScanResultDao scanResultDao;
    private TagDao tagDao;
    private ConfigDao configDao;
    private ScanMetaDao scanMetaDao;

    public void initialize();
    public Connection getConnection();
    public ScanResultDao getScanResultDao();
    public TagDao getTagDao();
    public ConfigDao getConfigDao();
    public ScanMetaDao getScanMetaDao();

    @Override
    public void dispose();
}
```

### 数据流

**当前流程**：
```
ProjectInitService.getServiceProjects()
  → InterfaceXHelper.getInterfaceProjectUsingResolver()
    → 各 IServiceResolver.findServiceItemsInModule()  (每次全量 PSI 扫描)
      → 构建 InterfaceProject 列表
        → 树结构展示
```

**新流程**：
```
ProjectInitService.getServiceProjects()
  → 检查 SQLite 是否有缓存 (scan_meta)
    → 有缓存: 从 SQLite 加载 → 构建 InterfaceProject 列表
    → 无缓存/手动刷新:
        → 各 IServiceResolver.findServiceItemsInModule()  (PSI 扫描)
        → 扫描结果写入 SQLite
        → 构建 InterfaceProject 列表
  → 合并标签数据 (从 SQLite tag 表)
  → 树结构展示
```

## 标签系统

### 标签类型

- **系统标签**：由插件在扫描时自动推荐（基于 `@partner` 注释、URL 前缀模式等代码特征），推荐结果以 `tag_name="system:partner"` 格式存储，用户可手动移除
- **用户标签**：由用户手动添加（自定义分类、业务域等）

### 标签操作

- 添加标签：右键菜单 → "添加标签"
- 删除标签：右键菜单 → "移除标签"
- 按标签筛选：工具栏下拉筛选
- 标签持久化：自动保存到 SQLite `tag` 表

### 标签与扫描结果关联

- 通过 `project_path + module_name + category + url + http_method + method_name` 唯一标识接口
- 一个接口可有多个标签
- 扫描结果更新时，标签数据保留（基于唯一键匹配）

## 缓存策略

### 三级缓存

```
L1: 内存缓存 (ConcurrentHashMap)
  ├── project_path → List<ScanResultEntity>
  └── project_path → List<TagEntity>

L2: SQLite 数据库
  └── ${user.home}/.interfacex/interfacex.db

L3: PSI 实时扫描 (最慢，仅在无缓存或手动刷新时触发)
```

### 缓存失效

| 事件 | 处理方式 |
|---|---|
| 项目打开 | 优先从 SQLite 加载，后台异步检查是否需要更新 |
| 手动刷新 | 全量 PSI 扫描 → 更新 SQLite → 刷新内存缓存 |
| 添加/删除标签 | 更新 SQLite → 刷新内存缓存 |
| 项目关闭 | 内存缓存清除，SQLite 数据保留 |

### 性能预估（12 工程场景）

| 操作 | 当前耗时 | 优化后耗时 |
|---|---|---|
| 首次打开（无缓存） | ~30-60s | ~30-60s（需全量扫描） |
| 再次打开（有缓存） | ~30-60s | **<2s**（从 SQLite 加载） |
| 手动刷新 | ~30-60s | ~30-60s（全量扫描+写入） |
| 添加标签 | N/A | **<100ms** |
| 按标签筛选 | N/A | **<50ms** |

## XML 迁移策略

1. `InterfaceXNavigator` 的 `@State` 注解保留但标记 `@Deprecated`
2. 首次启动时检测 `InterfaceX.xml` 存在则自动迁移到 SQLite
3. 迁移完成后删除 XML 文件
4. `XmlToSqliteMigrator` 负责读取 XML 数据写入 config 表

## Schema 版本迁移

```java
public class SchemaManager {
    private static final int CURRENT_VERSION = 1;

    public void initialize(Connection conn) {
        // 创建 schema_version 表
        // 读取当前版本
        // 按版本顺序执行迁移脚本
    }
}
```

后续版本升级时，新增迁移脚本按版本号顺序执行。

## UI 变更

1. **右键菜单增强**：`InterfaceXPopupMenu` 添加标签操作项（添加标签、移除标签）
2. **工具栏增加筛选**：标签筛选下拉框
3. **树节点展示**：ServiceNode 名称后追加标签标识
