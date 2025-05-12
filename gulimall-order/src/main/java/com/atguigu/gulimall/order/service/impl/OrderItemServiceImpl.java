package com.atguigu.gulimall.order.service.impl;

import com.atguigu.gulimall.order.entity.OrderReturnReasonEntity;

import com.rabbitmq.client.Channel;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;

import org.springframework.boot.autoconfigure.amqp.RabbitProperties;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Map;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;

import com.atguigu.gulimall.order.dao.OrderItemDao;
import com.atguigu.gulimall.order.entity.OrderItemEntity;
import com.atguigu.gulimall.order.service.OrderItemService;


@Service("orderItemService")
@RabbitListener(queues = "hello-java-queue")
public class OrderItemServiceImpl extends ServiceImpl<OrderItemDao, OrderItemEntity> implements OrderItemService {
    /**
     * 监听队列
     * Message message（有响应头和响应体） , Channel channel（通道） 可以接受的类型
     * T<发送的信息类型>接受并自动转换为对应类型
     * @param o
     */
    @RabbitHandler
    public void  OrderItemServiceImpl(Message message, OrderReturnReasonEntity o, Channel channel) {
        System.out.println("收到消息："+o);
        String name = o.getName();
        System.out.println("name:"+name);
        // 消息的标识，false只确认当前一个消息收到，true确认所有consumer获得的消息
        long deliveryTag = message.getMessageProperties().getDeliveryTag();
        // 手动确认
        try {
            if(true){
                // 确认收到消息
            channel.basicAck(deliveryTag,false);
             }else{
                // 拒绝消息
                channel.basicNack(deliveryTag,false,false);
                // 拒绝消息
                channel.basicReject(deliveryTag,false);
            }


            System.out.println("消息确认成功"+deliveryTag);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<OrderItemEntity> page = this.page(
                new Query<OrderItemEntity>().getPage(params),
                new QueryWrapper<OrderItemEntity>()
        );

        return new PageUtils(page);
    }

}