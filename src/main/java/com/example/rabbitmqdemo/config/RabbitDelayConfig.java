package com.example.rabbitmqdemo.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.CustomExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class RabbitDelayConfig {

    @Component
    public class RabbitmqDelayedConfig {

        /**
         * 初始化延迟交换机
         *
         * @return
         */
        @Bean
        public CustomExchange delayedExchangeInit() {
            Map<String, Object> args = new HashMap<>();
            // 设置类型，可以为fanout、direct、topic
            args.put("x-delayed-type", "direct");
            // 第一个参数是延迟交换机名字，第二个是交换机类型，第三个设置持久化，第四个设置自动删除，第五个放参数
            return new CustomExchange("delayed_exchange", "x-delayed-message", true, false, args);
        }

        /**
         * 初始化队列
         *
         * @return
         */
        @Bean
        public Queue delayedSmsQueueInit() {
            return new Queue("sms_delayed_queue", true);
        }


        /**
         * 短信队列绑定到交换机
         *
         * @param delayedSmsQueueInit
         * @param customExchange
         * @return
         */
        @Bean
        public Binding delayedBindingSmsQueue(Queue delayedSmsQueueInit, CustomExchange customExchange) {
            // 延迟队列绑定延迟交换机并设置RoutingKey为sms
            return BindingBuilder.bind(delayedSmsQueueInit).to(customExchange).with("sms").noargs();
        }
    }
}
