springboot 集成 rabbitmq

# springboot 2.7.0 集成rabbitmq使用消息中间件MQ 

# springboot 使用rabbitmq
springboot可以通过amqp封装来直接操作Rabbitmq

```
        <!--rabbitmq-->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-amqp</artifactId>
        </dependency>
```


# rabbitmq进阶思考
1. rabbitmq集群模式有哪些? 怎么保证高可用？

2. 消息投递可靠性保证？

3. 消息消费确认机制？



项目地址：https://github.com/lihm1028/rabbitmqdemo.git
