<!-- Keep a Changelog guide -> https://keepachangelog.com -->
# InterfaceX Changelog

## [Unreleased]

## [1.3.0]
### 中文
* **SQLite 持久化存储系统**
  - 新增 SQLite 数据库支持，用于存储扫描结果、标签和配置
  - 添加数据库迁移工具，将现有 XML 配置迁移到 SQLite
  - 实现存储适配器模式，支持 SQLite 和 XML 两种存储方式动态切换
  - 新增 StorageMigrator 用于不同存储类型间的数据迁移
  - 重构项目初始化服务，支持从数据库加载扫描结果

* **标签管理系统重新设计（参考专业 IDEA 插件交互）**
  - 数据结构保持不变，只优化 UI 交互
  - 新增快速标签选择器组件（TagQuickSelectorPanel）
    - 采用彩色便签条样式展示标签
    - 支持一键移除标签（点击 × 按钮）
    - 快速添加标签（支持选择已有标签或新建）
    - 标签颜色基于标签名自动生成独特颜色
  - 新增专业标签管理对话框（TagManagerDialog）
    - 搜索过滤：顶部搜索框支持实时过滤标签
    - 表格展示：清晰展示标签名、使用次数、操作按钮
    - 一键应用：快速将标签应用到当前接口
    - 查看详情：列出所有使用该标签的接口
    - 删除标签：全局删除标签（所有接口）
    - 按标签过滤视图
  - 优化右键菜单交互
    - 改进标签添加对话框，显示更友好
    - 集成新的专业标签管理对话框

* **标签使用场景**

- 接口分类管理：将不同业务线接口打上不同标签（如：用户模块、订单模块）
  - 按接口合作渠道标记 ：按渠道打标签（如：支付宝、网商）
  - 接口状态标记：标记接口状态（如：待测试、开发中、已废弃）
  - 按功能特性组织：按技术特性打标签（如：需要鉴权、异步接口、高并发）
  - 接口文档整理：辅助团队进行接口文档治理
  - 按负责人标记：明确接口归属和职责
  - 快速批量操作：按标签过滤视图，快速定位相关接口

* **行标记功能（Line Marker）**
  - 新增 Controller 行标记，支持从 HTTP Controller 跳转到对应实现
  - 新增 Feign 行标记，支持从 Feign 接口跳转到服务端实现
  - 新增 RocketMQ 消费者行标记，支持从消息生产者跳转到消费者
  - 新增 RocketMQ 生产者行标记，支持从消费者跳转到生产者

### English
* **SQLite Persistent Storage System**
  * New SQLite database support for storing scan results, tags and configurations
  * Added database migration tool to migrate existing XML configuration to SQLite
  * Implemented storage adapter pattern supporting dynamic switching between SQLite and XML
  * New StorageMigrator for data migration between different storage types
  * Refactored project initialization service to support loading scan results from database

* **Redesign tag management system (referencing professional IDEA plugin interaction)**
  * Data structure unchanged, only UI interaction optimized
  * New quick tag selector component (TagQuickSelectorPanel)
    - Display tags with colorful label style
    - One-click remove tag (click × button)
    - Quick add tags (support select existing or create new)
    - Tag colors auto-generated based on tag name
  * New professional tag manager dialog (TagManagerDialog)
    - Search filter: real-time tag filtering via top search box
    - Table display: clearly show tag name, usage count, action buttons
    - One-click apply: quickly apply tag to current interface
    - View details: list all interfaces using the tag
    - Delete tag: global delete (all interfaces)
    - Filter view by tag
  * Optimized right-click menu interaction
    - Improved tag add dialog, more user-friendly
    - Integrated new professional tag manager dialog

* **Tag Use Cases**
  * Interface classification: tag interfaces by business lines (e.g., user module, order module)
  * Status marking: tag interface status (e.g., testing, developing, deprecated)
  * Feature organization: tag by technical features (e.g., need auth, async, high concurrency)
  * Documentation management: assist team in interface documentation governance
  * Owner tagging: clarify interface ownership and responsibilities
  * Quick batch operations: filter view by tag, quickly locate related interfaces

* **Line Marker**
  * New Controller line marker, support navigation from HTTP Controller to implementation
  * New Feign line marker, support navigation from Feign interface to server implementation
  * New RocketMQ consumer line marker, support navigation from message producer to consumer
  * New RocketMQ producer line marker, support navigation from consumer to producer

## [1.2.1]
### 中文
* XXLJOB代码未使用变更背景为灰色，判断条件类或方法上有@Deprecated
* RocketMQ消费者代码未使用变更背景为灰色,判断条件类或方法上有注解@Deprecated
* **可以选择指定"接口"进行扫描**
  * **HTTP**
  * **RocketMQ消费者**
  * **RabbitMQ生产者**
  * **OpenFeign**
  * **XXL-JOB**

### English
* XXLJOB add unused color on tree node 
* rocket listener add unused color on tree node 


## [1.2.0]
### Added
* add rocketmq send message support
* add copy service param as json support

## [1.1.4]
### Added
* XLL-JOB UI布局调整,方便DEBUG调试
* SimpleTree默认初始化所有接口

## [1.1.3]
### Added
* 支持XLL-JOB执行 
解决容器环境不xxl-job调试无法调用本地进行验证的痛点


鼠标右键点击执行，弹出XXL-JOB执行页面设置XLL-JOB端口号即可


## [1.1.2]
### Added
* 刷新按钮点击增加显示进度条

## [1.1.1]
### Added
* 增加idea高版本
* 修复PsiElement未就绪双击末节点自动刷Tree场景，用户自主选择刷新
* 增加文档说明

## [1.1.0]
### Added
* 兼容2024.3版本加载树形菜单异步加载
* RocketMQ生产者支持Spring RocketMQTemplate，独立菜单目录

## [1.0.9]
### Added
*增加使用说明
* 增加接口名称按字母排序
* 导出rocketmq、rabbitmq生产者excel文件
* Find Action / Search Everywhere支持以下类型
RocketMQProducer、RocketMQDeliver、RocketMQListener、RabbitMQListener
RabbitMQProducer、XXLJob 、HTTP、OpenFeign, Mission

## [1.0.7]
### Added
- 增加RestTemplate接口

## [1.0.6]
### Added
- 增加导出国际化文件属性

## [1.0.5]
### Added 
- 修改默认icon(modify default icon)
- 增加导出rabbitmq接口
- 服务增加rabbitmq生产者
- 增加mission支持

## [1.0.1] 
### Added
- add tree display interfaces,e.g rabbit、rocketmq、xxl-job
