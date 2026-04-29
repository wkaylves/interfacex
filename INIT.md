# InterfaceX - 项目初始化指南

> 本文档提供 InterfaceX IntelliJ IDEA 插件的快速启动和开发环境配置指南。

## 📋 目录

- [项目概述](#项目概述)
- [前置要求](#前置要求)
- [快速开始](#快速开始)
- [开发环境设置](#开发环境设置)
- [项目结构](#项目结构)
- [核心功能模块](#核心功能模块)
- [数据库初始化](#数据库初始化)
- [构建与运行](#构建与运行)
- [调试技巧](#调试技巧)
- [常见问题](#常见问题)

---

## 项目概述

**InterfaceX** 是一款专为 IntelliJ IDEA 打造的组件接口查看工具，能够自动扫描并展示以下组件的入口点：

- 🌐 **HTTP 接口** - Spring-MVC 控制器
- 🔄 **OpenFeign 客户端** - 微服务调用关系
- 📮 **RabbitMQ 生产者/消费者** - 消息队列可视化
- 🚀 **RocketMQ 生产者/消费者** - 阿里云消息队列支持
- ⏰ **XXL-JOB 定时任务** - 本地调试支持

### 解决的核心痛点

| 场景 | 传统方式 | InterfaceX 方案 |
|------|---------|----------------|
| 大型项目导航 | 全局搜索效率低 | 自动分类扫描，一键展示 |
| 微服务调用追溯 | 难以定位调用方 | 清晰列出所有 Feign 调用 |
| 消息队列调试 | 需要翻阅大量代码 | 自动识别生产者和消费者 |
| XXL-JOB 调试 | 容器环境无法本地断点 | IDE 内直接触发任务 |

---

## 前置要求

### 必需软件

- **JDK**: 11 或更高版本（推荐 JDK 17）
- **IntelliJ IDEA**: 2020.3 或更高版本（Ultimate 或 Community）
- **Gradle**: 7.0+（项目内置 Gradle Wrapper，无需单独安装）

### 推荐插件

在 IDEA 中安装以下插件可提升开发体验：

- **Lombok** - 简化 Java 代码
- **SQLite** - 查看和管理 SQLite 数据库

---

## 快速开始

### 1️⃣ 克隆项目

```bash
git clone https://github.com/wkaylves/interfacex.git
cd interfacex
```

### 2️⃣ 打开项目

在 IntelliJ IDEA 中：
1. 选择 **File → Open**
2. 导航到项目根目录
3. 点击 **OK**，IDEA 会自动识别为 Gradle 项目

### 3️⃣ 等待索引完成

首次打开时，IDEA 会：
- 下载 Gradle 依赖
- 构建项目索引
- 解析 PSI（Program Structure Interface）

⏱️ 这可能需要 2-5 分钟，请耐心等待右下角进度条完成。

### 4️⃣ 验证配置

检查以下内容是否正常：

```bash
# 验证 Gradle 配置
./gradlew --version

# 编译项目
./gradlew buildPlugin
```

如果编译成功，会在 `build/distributions/` 目录下生成 `.zip` 文件。

---

## 开发环境设置

### Gradle 配置

项目使用 Kotlin DSL 配置 Gradle，主要配置文件：

- [`build.gradle.kts`](file:///Users/kaylves/github-src/interfacex/build.gradle.kts) - 主构建脚本
- [`settings.gradle.kts`](file:///Users/kaylves/github-src/interfacex/settings.gradle.kts) - 项目设置
- [`gradle.properties`](file:///Users/kaylves/github-src/interfacex/gradle.properties) - Gradle 属性

关键配置项：

```kotlin
// build.gradle.kts 中的关键配置
intellij {
    version.set("2023.2")  // 目标 IDEA 版本
    type.set("IC")         // IC = Community, IU = Ultimate
    plugins.set(listOf(
        "com.intellij.java",
        "com.intellij.modules.lang"
    ))
}
```

### Lombok 支持

项目广泛使用 Lombok 注解，确保：

1. 安装 **Lombok Plugin**（IDEA 2020.3+ 已内置）
2. 启用注解处理：**Settings → Build → Compiler → Annotation Processors** → 勾选 "Enable annotation processing"

### 代码风格

项目遵循以下规范：

- **命名**: camelCase（变量/方法）、PascalCase（类/接口）
- **缩进**: 4 空格
- **导入顺序**: 标准 Java 导入顺序
- **日志**: 使用 SLF4J + Lombok `@Slf4j`

---

## 项目结构

```
interfacex/
├── src/main/java/com/kaylves/interfacex/
│   ├── action/              # IDEA 动作（菜单/工具栏操作）
│   │   ├── search/         # 搜索相关动作（如 GotoRequestMapping）
│   │   └── toolbar/        # 工具栏动作（刷新、导出、设置等）
│   ├── bean/               # 数据传输对象（DTO）
│   ├── common/             # 通用常量和接口定义
│   │   └── constants/      # 枚举和常量
│   ├── db/                 # SQLite 数据库层
│   │   ├── dao/           # 数据访问对象
│   │   ├── migration/     # 数据库迁移工具
│   │   └── model/         # 数据库实体模型
│   ├── entity/             # 业务实体类
│   ├── module/             # 各组件扫描器实现
│   │   ├── http/          # HTTP 接口扫描
│   │   ├── openfeign/     # OpenFeign 扫描
│   │   ├── rabbitmq/      # RabbitMQ 扫描
│   │   ├── rocketmq/      # RocketMQ 扫描
│   │   ├── spring/        # Spring 相关工具
│   │   └── xxljob/        # XXL-JOB 扫描和执行
│   ├── service/            # 核心服务层
│   ├── ui/                 # UI 组件
│   │   ├── form/          # 表单对话框
│   │   ├── navigator/     # 导航器状态管理
│   │   └── toolwindow/    # 工具窗口实现
│   └── utils/              # 工具类
├── src/main/resources/
│   ├── META-INF/
│   │   └── plugin.xml     # 插件配置文件（核心！）
│   ├── icons/             # 图标资源
│   └── template/          # HTML 模板
├── docs/                   # 文档
│   └── superpowers/       # 设计文档和计划
├── build.gradle.kts        # Gradle 构建脚本
└── README.md               # 项目说明
```

### 关键文件说明

| 文件 | 作用 |
|------|------|
| [`plugin.xml`](file:///Users/kaylves/github-src/interfacex/src/main/resources/META-INF/plugin.xml) | 插件元数据、依赖声明、扩展点注册 |
| [`InterfaceXToolWindow.java`](file:///Users/kaylves/github-src/interfacex/src/main/java/com/kaylves/interfacex/ui/toolwindow/InterfaceXToolWindow.java) | 工具窗口工厂，插件入口 |
| [`InterfaceXNavigator.java`](file:///Users/kaylves/github-src/interfacex/src/main/java/com/kaylves/interfacex/service/InterfaceXNavigator.java) | 核心导航器，管理树形结构 |
| [`InterfaceXDatabaseService.java`](file:///Users/kaylves/github-src/interfacex/src/main/java/com/kaylves/interfacex/db/InterfaceXDatabaseService.java) | SQLite 数据库服务（单例） |
| [`ProjectInitService.java`](file:///Users/kaylves/github-src/interfacex/src/main/java/com/kaylves/interfacex/service/ProjectInitService.java) | 项目初始化服务，触发扫描 |

---

## 核心功能模块

### 1. 插件生命周期

```
IDEA 启动
  ↓
加载 plugin.xml
  ↓
注册 ToolWindow（InterfaceX）
  ↓
用户打开项目
  ↓
InterfaceXToolWindow.createToolWindowContent()
  ↓
InterfaceXNavigator.initToolWindow()
  ↓
ProjectInitService.getServiceProjects()
  ↓
扫描各模块接口（HTTP/Feign/MQ/Job）
  ↓
缓存到 SQLite 数据库
  ↓
渲染树形结构
```

### 2. 扫描器架构

每个组件类型都有独立的扫描器：

#### HTTP 扫描器
- **位置**: [`module/http/`](file:///Users/kaylves/github-src/interfacex/src/main/java/com/kaylves/interfacex/module/http/)
- **规则**: 查找 `@GetMapping`、`@PostMapping` 等 Spring-MVC 注解
- **输出**: URL 路径、HTTP 方法、Controller 类和方法

#### OpenFeign 扫描器
- **位置**: [`module/openfeign/`](file:///Users/kaylves/github-src/interfacex/src/main/java/com/kaylves/interfacex/module/openfeign/)
- **规则**: 查找 `@FeignClient` 注解的接口
- **输出**: 服务名、接口路径、调用方法

#### RabbitMQ 扫描器
- **生产者**: 查找 `RabbitTemplate.convertAndSend()` 调用
- **消费者**: 查找 `@RabbitListener` 注解方法

#### RocketMQ 扫描器
- **生产者**: 查找 `RocketMQTemplate.syncSend()` 调用
- **消费者**: 查找 `@RocketMQMessageListener` 注解类

#### XXL-JOB 扫描器
- **位置**: [`module/xxljob/`](file:///Users/kaylves/github-src/interfacex/src/main/java/com/kaylves/interfacex/module/xxljob/)
- **规则**: 查找 `@JobHandler` 注解类
- **特色**: 支持在 IDE 内直接执行任务

### 3. 数据存储

#### SQLite 数据库

从 v1.2.0 开始，项目使用 SQLite 替代 XML 存储扫描结果。

**数据库位置**: `~/.interfacex/interfacex.db`

**核心表结构**:

```sql
-- 扫描结果表
CREATE TABLE scan_result (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    project_path TEXT NOT NULL,
    module_name TEXT NOT NULL,
    category TEXT NOT NULL,      -- HTTP/OpenFeign/RabbitMQ/etc
    url TEXT,
    http_method TEXT,
    class_name TEXT,
    method_name TEXT,
    created_time INTEGER
);

-- 配置表
CREATE TABLE config (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    project_path TEXT NOT NULL,
    config_key TEXT NOT NULL,
    config_value TEXT,
    updated_time INTEGER
);

-- 标签表
CREATE TABLE tag (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    project_path TEXT NOT NULL,
    tag_name TEXT NOT NULL,
    color TEXT,
    created_time INTEGER
);
```

**数据库服务**: [`InterfaceXDatabaseService`](file:///Users/kaylves/github-src/interfacex/src/main/java/com/kaylves/interfacex/db/InterfaceXDatabaseService.java)

```java
// 获取数据库实例（单例）
InterfaceXDatabaseService dbService = InterfaceXDatabaseService.getInstance();
dbService.initialize();

// 使用 DAO 操作数据
ScanResultDao dao = dbService.getScanResultDao();
List<ScanResultEntity> results = dao.findByProjectPath(projectPath);
```

---

## 数据库初始化

### 自动初始化流程

数据库在以下时机自动初始化：

1. **首次打开项目**时，`InterfaceXDatabaseService.initialize()` 被调用
2. **Schema 创建**: [`SchemaManager`](file:///Users/kaylves/github-src/interfacex/src/main/java/com/kaylves/interfacex/db/SchemaManager.java) 自动创建表和索引
3. **DAO 初始化**: 创建 `ScanResultDao`、`ConfigDao`、`TagDao` 等

### 手动检查数据库

如果需要查看数据库内容：

```bash
# 安装 SQLite CLI（macOS）
brew install sqlite

# 打开数据库
sqlite3 ~/.interfacex/interfacex.db

# 查看表
.tables

# 查询数据
SELECT * FROM scan_result LIMIT 10;
```

### 数据库迁移

如果已有旧版本的 XML 配置，可以使用迁移工具：

```java
// XmlToSqliteMigrator.java
XmlToSqliteMigrator migrator = new XmlToSqliteMigrator();
migrator.migrate(oldXmlPath);
```

---

## 构建与运行

### 开发模式运行

在 IDEA 中直接运行插件：

1. 打开 **Run → Edit Configurations**
2. 点击 **+** → 选择 **Gradle**
3. 配置如下：
   - **Gradle project**: `interfacex`
   - **Tasks**: `runIde`
4. 点击 **Run**（或按 `Shift+F10`）

这会启动一个**新的 IDEA 实例**，其中包含您的插件。

### 构建发布包

```bash
# 清理并构建
./gradlew clean buildPlugin

# 产物位置
ls -lh build/distributions/InterfaceX-*.zip
```

### 安装到本地 IDEA

1. 打开 **Settings → Plugins**
2. 点击齿轮图标 → **Install Plugin from Disk...**
3. 选择 `build/distributions/InterfaceX-*.zip`
4. 重启 IDEA

---

## 调试技巧

### 1. 启用详细日志

在 `src/main/resources` 下创建或修改 `log.properties`：

```properties
# 设置日志级别
com.kaylves.interfacex.level=DEBUG
```

### 2. 查看 PSI 结构

调试扫描器时，可以使用 IDEA 内置工具：

- **Tools → View PSI Structure of Current File**
- 或使用快捷键 `Ctrl+Shift+A` → 搜索 "PSI Viewer"

### 3. 断点调试扫描器

在以下位置设置断点：

```java
// ProjectInitService.java - 查看扫描触发
public List<InterfaceProject> getServiceProjects() {
    // ← 在此处设置断点
    InterfaceXHelper.getInterfaceProjectUsingResolver(...)
}

// 各扫描器的 getRestServiceItemList() 方法
// 例如：HttpServiceResolver.java
```

### 4. 数据库调试

```java
// 在代码中临时打印 SQL 查询结果
List<ScanResultEntity> results = scanResultDao.findByProjectPath(projectPath);
log.info("Found {} scan results for project: {}", results.size(), projectPath);
results.forEach(r -> log.info("  - {} {}", r.getCategory(), r.getUrl()));
```

### 5. 性能分析

如果扫描速度慢，可以：

```java
// 在关键位置添加计时
long start = System.currentTimeMillis();
// ... 扫描逻辑 ...
long elapsed = System.currentTimeMillis() - start;
log.warn("Scanning took {} ms", elapsed);
```

---

## 常见问题

### Q1: 插件加载后工具窗口不显示？

**检查清单**:

1. 确认 `plugin.xml` 中正确注册了 ToolWindow：
   ```xml
   <toolWindow id="InterfaceX"
               factoryClass="com.kaylves.interfacex.ui.toolwindow.InterfaceXToolWindow"
               anchor="right"/>
   ```

2. 查看 IDEA 日志（**Help → Show Log in Explorer**）：
   ```bash
   grep -i "interfacex" idea.log
   ```

3. 确认没有异常堆栈：
   ```
   com.kaylves.interfacex ERROR - Failed to initialize database
   ```

### Q2: 扫描结果为空？

**可能原因**:

1. **索引未完成**: 等待 IDEA 右下角进度条完成
2. **模块未识别**: 确认项目是 Maven/Gradle 多模块结构
3. **注解未识别**: 检查是否缺少 Spring 相关依赖

**解决方法**:

```java
// 手动触发刷新
// 在工具窗口点击"刷新"按钮
// 或运行 Action: RefreshProjectAction
```

### Q3: 数据库锁定错误？

```
SQLException: database is locked
```

**原因**: 多个线程同时写入数据库

**解决**:

```java
// InterfaceXDatabaseService 已使用 synchronized
// 确保所有数据库操作通过单例服务
InterfaceXDatabaseService dbService = InterfaceXDatabaseService.getInstance();
```

### Q4: Lombok 注解不生效？

**检查**:

1. 安装 Lombok 插件
2. 启用注解处理：**Settings → Build → Compiler → Annotation Processors**
3. 重新构建项目：**Build → Rebuild Project**

### Q5: 如何添加新的扫描器？

**步骤**:

1. 创建新的扫描器类，继承 `BaseServiceResolver`：
   ```java
   public class MyCustomResolver extends BaseServiceResolver {
       @Override
       public List<InterfaceItem> getRestServiceItemList(...) {
           // 实现扫描逻辑
       }
   }
   ```

2. 在 [`InterfaceXHelper`](file:///Users/kaylves/github-src/interfacex/src/main/java/com/kaylves/interfacex/utils/InterfaceXHelper.java) 中注册：
   ```java
   resolvers.add(new MyCustomResolver(module));
   ```

3. 在 [`InterfaceItemCategoryEnum`](file:///Users/kaylves/github-src/interfacex/src/main/java/com/kaylves/interfacex/common/constants/InterfaceItemCategoryEnum.java) 中添加新类别

4. 更新 UI 表单 [`InterfaceXSettingForm`](file:///Users/kaylves/github-src/interfacex/src/main/java/com/kaylves/interfacex/ui/form/InterfaceXSettingForm.java)

### Q6: 如何自定义图标？

项目使用 SVG 图标，位于 [`src/main/resources/icons/`](file:///Users/kaylves/github-src/interfacex/src/main/resources/icons/)

**生成 PNG 图标**（用于不同尺寸）:

```python
# 使用 Python PIL 库
from PIL import Image
import os

svg_files = [f for f in os.listdir('icons') if f.endswith('.svg')]
for svg in svg_files:
    # 转换为 PNG（需要使用 cairosvg 或其他工具）
    pass
```

或在 [`ToolkitIcons.java`](file:///Users/kaylves/github-src/interfacex/src/main/java/com/kaylves/interfacex/common/ToolkitIcons.java) 中引用：

```java
public static final Icon INTERFACE_X = IconLoader.getIcon("/icons/interfacex_icon.svg", ToolkitIcons.class);
```

---

## 贡献指南

### 提交 PR 前检查清单

- [ ] 代码符合阿里巴巴 Java 开发规范
- [ ] 添加了必要的单元测试
- [ ] 更新了相关文档
- [ ] 在本地 IDEA 中测试通过
- [ ] 运行 `./gradlew buildPlugin` 无错误

### 代码审查要点

1. **PSI 操作安全性**: 确保在 Read/Write Action 中执行
2. **线程安全**: 数据库操作使用 synchronized
3. **资源释放**: Closeable 资源使用 try-with-resources
4. **日志规范**: 使用 `@Slf4j`，避免 System.out

---

## 参考资料

### 官方文档

- [IntelliJ Platform SDK](https://plugins.jetbrains.com/docs/intellij/welcome.html)
- [PSI (Program Structure Interface)](https://plugins.jetbrains.com/docs/intellij/psi.html)
- [Tool Windows](https://plugins.jetbrains.com/docs/intellij/tool-windows.html)
- [Actions System](https://plugins.jetbrains.com/docs/intellij/action-system.html)

### 相关技术

- [SQLite JDBC Driver](https://github.com/xerial/sqlite-jdbc)
- [Lombok](https://projectlombok.org/)
- [Gradle IntelliJ Plugin](https://github.com/JetBrains/gradle-intellij-plugin)

### 社区资源

- [IntelliJ Platform Explorer](https://plugins.jetbrains.com/intellij-platform-explorer)
- [JetBrains Plugin Development Forum](https://intellij-support.jetbrains.com/hc/en-us/community/topics/200366979)

---

## 联系与支持

- **GitHub**: [https://github.com/wkaylves/interfacex](https://github.com/wkaylves/interfacex)
- **邮箱**: kaylves@outlook.com
- **问题反馈**: [GitHub Issues](https://github.com/wkaylves/interfacex/issues)

---

**祝您开发愉快！** 🚀

如有任何问题，欢迎提交 Issue 或 Pull Request。
