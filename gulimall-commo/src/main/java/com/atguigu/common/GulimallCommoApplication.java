package com.atguigu.common;

import org.apache.ibatis.annotations.Mapper;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class GulimallCommoApplication {

    public static void main(String[] args) {
        SpringApplication.run(GulimallCommoApplication.class, args);
    }

}
