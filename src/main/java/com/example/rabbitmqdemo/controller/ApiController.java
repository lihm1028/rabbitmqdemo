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
    final AmqpAdmin amqpAdmin;
    final AmqpTemplate amqpTemplate;
    final RabbitTemplate rabbitTemplate;


    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    public ApiController(AmqpAdmin amqpAdmin, AmqpTemplate amqpTemplate, RabbitTemplate rabbitTemplate) {
        this.amqpAdmin = amqpAdmin;
        this.amqpTemplate = amqpTemplate;
        this.rabbitTemplate = rabbitTemplate;
    }

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
//                logger.info("??????????????????:{}", message.getMessageProperties());
//                if (StringUtils.equalsIgnoreCase(MessageProperties.CONTENT_TYPE_JSON, properties.getContentType())) {
//                    try {
//                        final Book book = new ObjectMapper().readValue(message.getBody(), Book.class);
//                        logger.info("??????book??????:{}", book);
//                    } catch (IOException e) {
//                        logger.info("??????Rabbitmq????????????:{}", e);
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
     * ????????????????????? ???????????????
     *
     * @param connectionFactory
     * @return
     */
    @Bean
    SimpleMessageListenerContainer container(ConnectionFactory connectionFactory) {
        /**
         * .??????Queue
         * ????????????????????? ??????????????????????????????????????????
         */
        final Queue msqQueue = new Queue("bookQueue");
        amqpAdmin.declareQueue(msqQueue);

        SimpleMessageListenerContainer container = new SimpleMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        container.setQueueNames("bookQueue");
        container.setAcknowledgeMode(AcknowledgeMode.MANUAL);
        container.setMessageListener((ChannelAwareMessageListener) (message, channel) -> {
            final MessageProperties properties = message.getMessageProperties();
            logger.info("??????????????????:{}", message.getMessageProperties());
            if (StringUtils.equalsIgnoreCase(MessageProperties.CONTENT_TYPE_JSON, properties.getContentType())) {
                try {
                    final Book book = new ObjectMapper().readValue(message.getBody(), Book.class);

                    channel.basicAck(properties.getDeliveryTag(), false);//????????????
                    logger.info("??????book??????:{}", book);
                } catch (IOException e) {
                    logger.info("??????Rabbitmq????????????:{}", e);
                    channel.basicReject(properties.getDeliveryTag(), false);//????????????
                }
            }
        });
        return container;
    }

    /**
     * ????????????
     */
    @GetMapping("/produce")
    public void produce() throws JsonProcessingException {


        String exchange = "eventTopic";
        String routingKey = "foo.bar.baz";


        final TopicExchange topicExchange = new TopicExchange(exchange, true, false);

        /**
         * 1.?????????????????????
         *  ????????????????????? ??????????????????????????????????????????
         */
        amqpAdmin.declareExchange(topicExchange);


        /**
         * 2.??????Queue
         * ????????????????????? ??????????????????????????????????????????
         */
        final Queue bookQueue = new Queue("bookQueue");
        amqpAdmin.declareQueue(bookQueue);


        /**
         * 3.???????????????????????????
         * ????????????????????? ??????????????????????????????????????????
         */
        final Binding binding = BindingBuilder.bind(bookQueue).to(topicExchange).with("#");
        amqpAdmin.declareBinding(binding);


        /**
         * 4.???????????????????????????
         */
        Book book = new Book("??????", "zhuangzi@qq.com");
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

        logger.info("?????????????????????{}", content);
    }
}
