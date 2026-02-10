# InterfaceX

A simple and easy-to-use component interface viewer for IntelliJ IDEA. Supports viewing entry points of HTTP, OpenFeign, RabbitMQ consumers, RocketMQ consumers, and XXL-JOB scheduled tasks.
When projects grow large, code navigation becomes difficult — this plugin helps you quickly locate all interface entry points.
Also supports exporting RabbitMQ interfaces and RabbitMQ producers.Support XXLJOB task execution,
Solves the problem of xxl-job debugging in a container environment, where the local environment cannot be called<br/>
一个在 IntelliJ IDEA 平台上简单易用的组件接口查看工具，支持查看 HTTP、OpenFeign、RabbitMQ 消费者、RocketMQ 消费者、XXL-JOB 定时任务等接口方法入口。
当项目规模变大时，代码查找困难，本插件助你快速定位所有接口入口，同时支持导出 RabbitMQ 接口和 RabbitMQ 生产者。支持XXLJOB任务执行，
解决容器环境下xxl-job调试问题无法调用本地环境的痛点
支持RocketMQ消息发送

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