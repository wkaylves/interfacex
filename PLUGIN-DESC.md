<!-- Plugin description -->
A simple and easy-to-use component interface viewer for IntelliJ IDEA. Supports viewing entry points of HTTP, OpenFeign, RabbitMQ consumers, RocketMQ consumers, and XXL-JOB scheduled tasks.
When projects grow large, code navigation becomes difficult — this plugin helps you quickly locate all interface entry points.
Also supports exporting RabbitMQ interfaces and RabbitMQ producers.<br/>
一个在 IntelliJ IDEA 平台上简单易用的组件接口查看工具，支持查看 HTTP、OpenFeign、RabbitMQ 消费者、RocketMQ 消费者、XXL-JOB 定时任务等接口方法入口。
当项目规模变大时，代码查找困难，本插件助你快速定位所有接口入口，同时支持导出 RabbitMQ 接口和 RabbitMQ 生产者。


## HTTP
方便查找HTTP接口代码

### 扫描规则
Spring-MVC注解

## OpenFeign
当微服务较多时，一个接口可能被多个服务调用，此插件功能方便查找HTTP接口被依赖服务调用处

### 扫描规则
Spring OpenFeign包装注解

## RabbitMQ 生产者
### 规则
自动扫描当前工程下方法中调用org.springframework.amqp.rabbit.core.RabbitTemplate#convertAndSend的方法
建议方法名称为队列名称，每个工程创建一个类即可，这样搜索可以按队列名称进行搜索，示例

```java
public void fileNotifyQueue() {
    rabbitTemplate.convertAndSend(exchange,routingKey,body);
}
```


## RabbitMQ 消费者
### 扫描规则
自动扫描Spring Bean方法有
org.springframework.amqp.rabbit.annotation.RabbitListener的方法

## RocketMQ生产者
### 规则

自动扫描当前工程下方法中调用org.apache.rocketmq.spring.core.RocketMQTemplate#convertAndSend的方法
建议方法名称为tag名称，每个工程创建一个类即可，这样搜索可以按Tag名称进行搜索,示例

```java
public void fileNotifyQueue() {
  rocketMQTemplate.convertAndSend();
}
```

## RocketMQ消费者
### 扫描规则
自动扫描类上有此注解的类
org.apache.rocketmq.spring.annotation.RocketMQMessageListener

## XXL-JOB 
### 扫描规则
自动扫描类上有此注解的类com.xxl.job.core.handler.annotation.JobHandler

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
* 加载树形菜单异步加载，兼容2024.3版本
* RocketMQ生产者支持Spring RocketMQTemplate，独立菜单目录

## [1.0.9]
### Added
* 增加使用说明
* 增加接口名称按字母排序
* 导出rocketmq、rabbitmq生产者excel文件
* Find Action / Search Everywhere支持以下类型
  RocketMQProducer、RocketMQDeliver、RocketMQListener、RabbitMQListener
  RabbitMQProducer、XXLJob 、HTTP、OpenFeign, Mission

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

<!-- Plugin description end -->