package com.example.rabbitmqdemo.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
public class Runner implements CommandLineRunner {


    private final RabbitTemplate rabbitTemplate;
    private final Receiver receiver;
    private RabbitAdmin rabbitAdmin;

    public Runner(Receiver receiver, RabbitTemplate rabbitTemplate) {
        this.receiver = receiver;
        this.rabbitTemplate = rabbitTemplate;

    }

    public RabbitAdmin getRabbitAdmin() {
        if (rabbitAdmin == null) {
            rabbitAdmin = new RabbitAdmin(rabbitTemplate);
        }
        return rabbitAdmin;
    }

    @Override
    public void run(String... args) throws Exception {
        System.out.println("Sending message...");

        /**
         * 声明exchange
         */
        String topicExchangeName = "messageTopic";
        final TopicExchange exchange = new TopicExchange(topicExchangeName, true, false);
        getRabbitAdmin().declareExchange(exchange);


        /**
         * 声明Queue
         */
        final Queue msqQueue = new Queue("msqQueue");
        rabbitAdmin.declareQueue(msqQueue);


        /**
         * bing
         * 将队列和交换机绑定
         */
        final Binding binding = BindingBuilder.bind(msqQueue).to(exchange).with("#");
        rabbitAdmin.declareBinding(binding);

        rabbitTemplate.convertAndSend(topicExchangeName, "foo.bar.baz", "老子-道德经");
        receiver.getLatch().await(10000, TimeUnit.MILLISECONDS);
    }
}
