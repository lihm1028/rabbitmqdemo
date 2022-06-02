//package com.example.rabbitmqdemo.controller;
//
//
//import org.springframework.amqp.core.Message;
//import org.springframework.amqp.rabbit.annotation.RabbitHandler;
//import org.springframework.amqp.rabbit.annotation.RabbitListener;
//import org.springframework.stereotype.Component;
//
//
//@Component
//@RabbitListener(queues = "bookQueue")
//public class BookConsumer {
//
//    @RabbitHandler
//    public void consumer(Message message) {
//        System.out.println("消费者收到消息：" + message);
//    }
//
//
//
//
//
//}
