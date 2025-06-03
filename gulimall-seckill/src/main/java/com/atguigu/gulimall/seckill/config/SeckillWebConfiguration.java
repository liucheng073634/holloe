package com.atguigu.gulimall.seckill.config;


import com.atguigu.gulimall.seckill.interceptor.LoginUserInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class SeckillWebConfiguration implements WebMvcConfigurer {

    @Autowired
    LoginUserInterceptor loginUserInterceptor;

    public void addInterceptors(InterceptorRegistry interceptableChannel){
        interceptableChannel.addInterceptor(loginUserInterceptor).addPathPatterns("/**");
    }
}

