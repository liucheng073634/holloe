package com.atguigu.gulimall.order.config;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

@Configuration
public class GuliFeignConfig implements RequestInterceptor {



    @Override
    public void apply(RequestTemplate requestTemplate) {
        ServletRequestAttributes attributes  = (ServletRequestAttributes)RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = attributes.getRequest();

        if(request != null){
            String cookie = request.getHeader("Cookie");

            requestTemplate.header("Cookie", cookie);

        }
    }
}
