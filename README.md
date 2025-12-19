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