package com.example.rabbitmqdemo.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.ReturnedMessage;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

/**
 * Rabbitmq的消息可靠性投递
 * 保证mq节点成功接受消息
 * 消息发送端需要接受到mq服务端接受到消息的确认应答
 * 完善的消息补偿机制，发送失败的消息可以再感知并二次处理
 * 保证消息百分百发送到消息队列中去
 *
 * RabbitMQ消息投递路径
 *
 * 生产者到交换机
 * 交换机到队列
 * 通过confirmCallback
 * 通过returnCallback
 * 生产者-->交换机->队列->消费者
 * 通过两个的点控制消息的可靠性投递
 * 建议:
 * 开启消息确认机制以后，保证了消息的准确送达，但由于频繁的确认交互， rabbitmq 整体效率变低，吞吐量下降严重，不是非常重要的消息真心不建议用消息确认机制.
 *
 *
 *
 *
 */
@Component
public class PublisherReturnCallback implements RabbitTemplate.ReturnsCallback {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @PostConstruct
    public void init() {
        rabbitTemplate.setReturnsCallback(this);             //指定 ReturnCallback
    }

    @Override
    public void returnedMessage(ReturnedMessage returnedMessage) {
        logger.info("错误返回message : " + returnedMessage);
    }
}
