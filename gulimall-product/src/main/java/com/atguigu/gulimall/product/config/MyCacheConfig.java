package com.atguigu.gulimall.product.config;

import org.springframework.boot.autoconfigure.cache.CacheProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
// 开启缓存功能
@EnableConfigurationProperties(CacheProperties.class)// 引入缓存配置

@Configuration
//
@EnableCaching
public class MyCacheConfig {
    @Bean
    RedisCacheConfiguration redisCacheConfiguration(CacheProperties cacheProperties){
        RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig();


        config = config.serializeKeysWith( RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()));
        config = config.serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(new GenericJackson2JsonRedisSerializer()));
        CacheProperties.Redis redis = cacheProperties.getRedis();
        if(redis.getTimeToLive() != null){
            config = config.entryTtl(redis.getTimeToLive());
        }
        if(redis.getKeyPrefix() != null){
            config = config.prefixCacheNameWith(redis.getKeyPrefix());
        }
        if(!redis.isCacheNullValues()){
            config = config.disableCachingNullValues();
        }
        if(!redis.isUseKeyPrefix()){
            config = config.disableKeyPrefix();
        }

        return config;
    }
}
