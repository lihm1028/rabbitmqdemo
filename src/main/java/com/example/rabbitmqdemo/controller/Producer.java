package com.example.rabbitmqdemo.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;

@Slf4j
@RestController
@RequestMapping("/producer")
public class Producer {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    /**
     * 延迟模式
     *
     * @param msg  消息
     * @param time 延迟时间
     */
    @GetMapping("/publish")
    public void delayedTest(@RequestParam String msg, @RequestParam Integer time) {
        // 第一个参数是延迟交换机名称，第二个是Routingkey，第三个是消息主题，第四个是X，并设置延迟时间，单位		是毫秒
        rabbitTemplate.convertAndSend("delayed_exchange", "sms", msg, a -> {
            a.getMessageProperties().setDelay(time);
            return a;
        });
        log.info("延迟模式默认交换机已发出消息");
    }


    /**
     * 延迟模式短信消费者
     *
     * @param message
     */
    @RabbitListener(queues = {"sms_delayed_queue"})
    public void getSmsConsumer(String message) {
        log.info(new Date().toString() + "延迟模式短信消费者接收到信息:" + message);
    }
}
