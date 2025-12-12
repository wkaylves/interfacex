<!-- Plugin description -->
A simple and easy-to-use component interface viewer for IntelliJ IDEA. Supports viewing entry points of HTTP, OpenFeign, RabbitMQ consumers, RocketMQ consumers, and XXL-JOB scheduled tasks.
When projects grow large, code navigation becomes difficult — this plugin helps you quickly locate all interface entry points.
Also supports exporting RabbitMQ interfaces and RabbitMQ producers.<br/>
一个在 IntelliJ IDEA 平台上简单易用的组件接口查看工具，支持查看 HTTP、OpenFeign、RabbitMQ 消费者、RocketMQ 消费者、XXL-JOB 定时任务等接口方法入口。
当项目规模变大时，代码查找困难，本插件助你快速定位所有接口入口，同时支持导出 RabbitMQ 接口和 RabbitMQ 生产者。


## HTTP扫描规则
Spring-MVC注解

## OpenFeign扫描规则
Spring OpenFeign包装注解

## RabbitMQ 消费者扫描规则
自动扫描Spring Bean方法有
org.springframework.amqp.rabbit.annotation.RabbitListener的方法

## RocketMQ消费者扫描规则
自动扫描类上有此注解的类
org.apache.rocketmq.spring.annotation.RocketMQMessageListener

## XXL-JOB 扫描规则
自动扫描类上有此注解的类com.xxl.job.core.handler.annotation.JobHandler

# 变更历史
## [1.0.8]
### Added
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