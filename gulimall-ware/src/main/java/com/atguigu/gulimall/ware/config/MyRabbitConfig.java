package com.atguigu.gulimall.ware.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;

@Configuration
public class MyRabbitConfig {

//    @RabbitListener(queues = "stock.release.stock.queue")
//    public void handle(Message message) {
//
//
//    }

    @Bean
    public Exchange stockEventExchange() {

        return new TopicExchange("stock-event-exchange",true,false);
    }

    @Bean
    public Queue stockRelaeaseStockQuery() {

      return new Queue("stock.release.stock.queue",true,false,false,null);
    }

    @Bean
    public Queue stockDelayQuery() {
        /*
        * x-dead-letter-exchange: stock-event-exchange
        * x-dead-letter-routing-key: stock.release
        * x-message-ttl: 60000
         */

        HashMap<String, Object> args = new HashMap<>();
        args.put("x-dead-letter-exchange","stock-event-exchange");
        args.put("x-dead-letter-routing-key","stock.release");
        args.put("x-message-ttl",60000);
        return new Queue("stock.delay.queue",true,false,false,args);
    }

    @Bean
    public Binding stockLockedBinding() {
        return new Binding("stock.delay.queue",
                Binding.DestinationType.QUEUE,
                "stock-event-exchange",
                "stock.locked",
                null);
    }

    @Bean
    public Binding stockReleaseBinding() {
        return new Binding("stock.release.stock.queue",
                Binding.DestinationType.QUEUE,
                "stock-event-exchange",
                "stock.release.#",
                null);
    }


}
