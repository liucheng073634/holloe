package com.atguigu.gulimall.order;

import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;
/**
 * 1、引入消息队列
 * 2、给容器中自动配置一个RabbitTemplate
 * 3、给容器中自动配置一个AmqpAdmin
 * 4、给容器中自动配置了连接工厂ConnectionFactory
 * 5、给容器中自动配置了RabbitMQ的自动配置类
 * 6、给容器中自动配置了RabbitMQ的ListenerContainer
 * 7、给容器中自动配置了RabbitMQ的ListenerContainerFactory
 *
 * @理aspectj 自动代理
 *exposeProxy = true对外暴露代理对象，AOP功能才生效
 *
 * Seata 分布式事务
 * 1.每一个微服务先创建undo_log表
 * 2.安装事务协调器
 * */


@EnableAspectJAutoProxy(exposeProxy = true) //开启AOP
@EnableFeignClients
@EnableRedisHttpSession
@EnableRabbit
@SpringBootApplication
public class GulimallOrderApplication {

    public static void main(String[] args) {
        SpringApplication.run(GulimallOrderApplication.class, args);
    }

}
