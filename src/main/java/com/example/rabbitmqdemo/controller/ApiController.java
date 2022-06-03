package com.example.rabbitmqdemo.controller;

import com.example.rabbitmqdemo.Book;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.rabbit.listener.api.ChannelAwareMessageListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Date;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

@RestController
@RequestMapping("/api")
public class ApiController {
    private static AtomicInteger count = new AtomicInteger();
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    @Autowired
    AmqpAdmin amqpAdmin;


    @Autowired
    private AmqpTemplate amqpTemplate;

    @Autowired
    private RabbitTemplate rabbitTemplate;


//
//    @Bean
//    CommandLineRunner rabbitConsumer(ConnectionFactory connectionFactory) {
//
//        return args -> {
//            SimpleMessageListenerContainer container = new SimpleMessageListenerContainer();
//            container.setConnectionFactory(connectionFactory);
//            container.setQueueNames("bookQueue");
//            container.setMessageListener((ChannelAwareMessageListener) (message, channel) -> {
//                final MessageProperties properties = message.getMessageProperties();
//                logger.info("收到消息参数:{}", message.getMessageProperties());
//                if (StringUtils.equalsIgnoreCase(MessageProperties.CONTENT_TYPE_JSON, properties.getContentType())) {
//                    try {
//                        final Book book = new ObjectMapper().readValue(message.getBody(), Book.class);
//                        logger.info("收到book消息:{}", book);
//                    } catch (IOException e) {
//                        logger.info("处理Rabbitmq消息失败:{}", e);
//                        channel.basicReject(properties.getDeliveryTag(), false);
//                    }
//                }
//
//            });
//            container.afterPropertiesSet();
//            container.start();
//
//        };
//    }


    /**
     * 注册消费侦听器 客户端消费
     *
     * @param connectionFactory
     * @return
     */
    @Bean
    SimpleMessageListenerContainer container(ConnectionFactory connectionFactory) {
        /**
         * .声明Queue
         * 注：非必须步骤 可以手动配置，也可以程序绑定
         */
        final Queue msqQueue = new Queue("bookQueue");
        amqpAdmin.declareQueue(msqQueue);

        SimpleMessageListenerContainer container = new SimpleMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        container.setQueueNames("bookQueue");
        container.setAcknowledgeMode(AcknowledgeMode.MANUAL);
        container.setMessageListener((ChannelAwareMessageListener) (message, channel) -> {
            final MessageProperties properties = message.getMessageProperties();
            logger.info("收到消息参数:{}", message.getMessageProperties());
            if (StringUtils.equalsIgnoreCase(MessageProperties.CONTENT_TYPE_JSON, properties.getContentType())) {
                try {
                    final Book book = new ObjectMapper().readValue(message.getBody(), Book.class);

                    channel.basicAck(properties.getDeliveryTag(), false);//确认消息
                    logger.info("收到book消息:{}", book);
                } catch (IOException e) {
                    logger.info("处理Rabbitmq消息失败:{}", e);
                    channel.basicReject(properties.getDeliveryTag(), false);//拒绝消息
                }
            }
        });
        return container;
    }

    /**
     * 生产数据
     */
    @GetMapping("/produce")
    public void produce() throws JsonProcessingException {


        String exchange = "eventTopic";
        String routingKey = "foo.bar.baz";


        final TopicExchange topicExchange = new TopicExchange(exchange, true, false);

        /**
         * 1.创建一个交换。
         *  注：非必须步骤 可以手动配置，也可以程序绑定
         */
        amqpAdmin.declareExchange(topicExchange);


        /**
         * 2.声明Queue
         * 注：非必须步骤 可以手动配置，也可以程序绑定
         */
        final Queue bookQueue = new Queue("bookQueue");
        amqpAdmin.declareQueue(bookQueue);


        /**
         * 3.将队列和交换机绑定
         * 注：非必须步骤 可以手动配置，也可以程序绑定
         */
        final Binding binding = BindingBuilder.bind(msqQueue).to(topicExchange).with("#");
        amqpAdmin.declareBinding(binding);


        /**
         * 4.向指定交换发送消息
         */
        Book book = new Book("庄子", "zhuangzi@qq.com");
        book.setId(count.incrementAndGet());

        final String content = new ObjectMapper().writeValueAsString(book);

        final Message message = MessageBuilder
                .withBody(content.getBytes())
                .setContentType(MessageProperties.CONTENT_TYPE_JSON)
                .setMessageId(UUID.randomUUID().toString())
                .setContentEncoding("UTF-8")
                .setTimestamp(new Date())
                .setAppId("rabbitmqdemo")
                .setType(book.getClass().getTypeName())
                .setHeader("content_type", MessageProperties.CONTENT_TYPE_JSON)
                .setHeader("content_encoding", Charset.forName("utf-8"))
                .build();

//        rabbitTemplate.setMessageConverter(new Jackson2JsonMessageConverter());
        rabbitTemplate.convertAndSend(exchange, routingKey, message);

        logger.info("发送消息成功：{}", content);
    }
}
