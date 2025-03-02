package com.atguigu.gulimall.search.config;

import net.minidev.json.JSONUtil;
import org.apache.http.HttpHost;
import org.elasticsearch.client.HttpAsyncResponseConsumerFactory;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


import java.io.IOException;

@Configuration
public class GulimallElasticSearchConfig {
    public static final RequestOptions COMMON_OPTIONS ;

    static {
        RequestOptions.Builder builder = RequestOptions.DEFAULT.toBuilder();
        // 添加header
      /*  builder.addHeader("Authorization", "Bearer " + "");
        // 添加endpoint
        builder.setHttpAsyncResponseConsumerFactory(
                new HttpAsyncResponseConsumerFactory
                        .HeapBufferedResponseConsumerFactory(30 * 1024 * 1024 * 1024));*/
        COMMON_OPTIONS = builder.build();

    }
    

    
    // 创建一个RestHighLevelClient对象
    @Bean(destroyMethod = "close") //程序开始时交给bean对象注入, 指定了当bean被销毁时应该调用其close方法
    @ConditionalOnMissingBean//保证spring容器里面只有一个utils对象(当没有这个bean对象再去创建，有就没必要去创建了)
    public RestHighLevelClient client(){
        return new RestHighLevelClient(RestClient.builder(
                new HttpHost(
                        "10.211.55.16",
                        9200,
                        "http"
                )
        ));
    }




}
