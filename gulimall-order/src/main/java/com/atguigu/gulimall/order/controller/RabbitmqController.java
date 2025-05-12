package com.atguigu.gulimall.order.controller;

import com.atguigu.gulimall.order.entity.OrderReturnReasonEntity;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class RabbitmqController {
    @Lazy
    @Autowired
    RabbitTemplate rabbitTemplate;
    @ResponseBody
    @GetMapping("/sendMsg")
    public String sendMsg() {
        for (int i = 0; i < 5; i++) {
            OrderReturnReasonEntity orderReturnReasonEntity = new OrderReturnReasonEntity();
            orderReturnReasonEntity.setId(1L);
            orderReturnReasonEntity.setName("哈哈哈");
            rabbitTemplate.convertAndSend("hello-java-exchange", "hello.java", orderReturnReasonEntity);
            System.out.println("发送消息成功");
        }
            return "ok";
    }
}
