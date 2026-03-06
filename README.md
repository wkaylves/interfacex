# InterfaceX
A simple and easy-to-use component interface viewer for IntelliJ IDEA. Supports viewing entry points of HTTP, OpenFeign, RabbitMQ consumers, RocketMQ consumers, and XXL-JOB scheduled tasks.
When projects grow large, code navigation becomes difficult — this plugin helps you quickly locate all interface entry points.
Also supports exporting RabbitMQ interfaces and RabbitMQ producers.Support XXLJOB task execution,
Solves the problem of xxl-job debugging in a container environment, where the local environment cannot be called<br/>
一个在 IntelliJ IDEA 平台上简单易用的组件接口查看工具，支持查看 HTTP、OpenFeign、RabbitMQ 消费者、RocketMQ 消费者、XXL-JOB 定时任务等接口方法入口。
当项目规模变大时，代码查找困难，本插件助你快速定位所有接口入口，同时支持导出 RabbitMQ 接口和 RabbitMQ 生产者。支持XXLJOB任务执行，
解决容器环境下xxl-job调试问题无法调用本地环境的痛点
支持RocketMQ消息发送

<div align="left">
  <h3>🚀 让代码导航变得前所未有的简单</h3>
  <p>一款专为 IntelliJ IDEA 打造的强大组件接口查看器，让大型项目的复杂性变得一目了然。</p>
  <img src="https://img.shields.io/badge/Platform-IntelliJ_IDEA-orange?style=for-the-badge" alt="Platform"/>
  <img src="https://img.shields.io/badge/Language-Java-brightgreen?style=for-the-badge&logo=java" alt="Language"/>
</div>

---

## ✨ 核心特性

InterfaceX 是您在大型项目中的得力助手，它能自动扫描并清晰地展示以下组件的入口点，彻底解决代码跳转困难、调用链路模糊的痛点。

- **🌐 HTTP 接口**: 快速定位 Spring-MVC 控制器。
- **🔄 OpenFeign 调用**: 清晰展示远程服务间的调用关系。
- **📮 RabbitMQ 生产者/消费者**: 可视化消息的发送与消费逻辑。
- **🚀 RocketMQ 生产者/消费者**: 支持阿里云生态的消息队列管理。
- **⏰ XXL-JOB 任务**: 直接在 IDE 内执行和调试定时任务，告别繁琐的容器环境配置。

---

## 🎯 解决的核心痛点

| 场景 | 传统方式的困扰 | InterfaceX 如何解决 |
| :--- | :--- | :--- |
| **大型项目** | 代码量庞大，`Ctrl+Shift+F` 全局搜索效率低下。 | 自动分类扫描，一键展示所有接口入口，清晰明了。 |
| **微服务调用** | 一个 HTTP 接口被 N 个 Feign 客户端调用，难以追溯。 | 清晰列出所有 OpenFeign 调用方，轻松掌握服务依赖。 |
| **消息队列调试** | 想知道谁是消息的生产者/消费者？需要翻阅大量代码。 | 自动识别 `RabbitTemplate` / `RocketMQTemplate` 和监听注解，可视化呈现。 |
| **XXL-JOB 调试** | 在容器环境中调试 Job，本地断点无法生效。 | 支持在 IDE 内直接触发 XXL-JOB 任务，本地调试畅通无阻。 |

---

## 🔍 功能详解

### **🌐 HTTP 接口**
轻松查找所有控制器中的 HTTP 接口方法。

*   **扫描规则**: 识别所有 Spring-MVC 注解（如 `@GetMapping`, `@PostMapping` 等）。

### **🔄 OpenFeign 客户端**
当您的微服务架构日益复杂时，此功能帮助您快速找到哪个服务调用了哪个远程接口。

*   **扫描规则**: 识别所有被 Spring OpenFeign 注解（如 `@FeignClient`）标记的接口及其实现。

### **📮 RabbitMQ 生产者**
让消息的“发送”过程变得透明可见。

*   **扫描规则**: 自动扫描项目中调用 `org.springframework.amqp.rabbit.core.RabbitTemplate#convertAndSend` 的方法。
*   **最佳实践**: 建议将方法名命名为队列或交换机名称，便于通过搜索快速定位。
    ```java
    public void fileNotifyQueue() { // 方法名即队列名，方便搜索
        rabbitTemplate.convertAndSend(exchange, routingKey, body);
    }
    ```

### **📮 RabbitMQ 消费者**
清晰展示哪些方法是消息的“接收者”。

*   **扫描规则**: 自动扫描被 `org.springframework.amqp.rabbit.annotation.RabbitListener` 注解标记的方法。

### **🚀 RocketMQ 生产者**
支持阿里云主流的消息队列，同样可以扫描其生产者。

*   **扫描规则**: 自动扫描项目中调用 `org.apache.rocketmq.spring.core.RocketMQTemplate#syncSend` 或 `convertAndSend` 的方法。
*   **最佳实践**: 建议将方法名命名为 Topic 或 Tag 名称，便于管理。
    ```java
    public void orderPaidEvent() { // 方法名即事件名，逻辑清晰
        rocketMQTemplate.syncSend("OrderTopic:PayTag", messageBody);
    }
    ```

### **🚀 RocketMQ 消费者**
识别处理 RocketMQ 消息的消费者方法。

*   **扫描规则**: 自动扫描被 `org.apache.rocketmq.spring.annotation.RocketMQMessageListener` 注解标记的类。

### **⏰ XXL-JOB 任务**
将 XXL-JOB 的调度中心部分能力引入 IDE，提升开发效率。

*   **扫描规则**: 自动扫描被 `com.xxl.job.core.handler.annotation.JobHandler` 注解标记的类。
*   **核心优势**: **本地调试** - 在开发环境即可直接运行任务，无需部署到调度中心，极大简化了调试流程。

---
<div align="center">
    <p>让 InterfaceX 成为您代码世界的指南针，从此告别迷航！</p>
</div>

# 变更历史
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
