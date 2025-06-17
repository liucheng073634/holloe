package com.atguigu.gulimall.seckill;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.time.LocalDateTime;
import java.util.Date;

@SpringBootTest
class GulimallSeckillApplicationTests {


	@Autowired
	StringRedisTemplate stringRedisTemplate;
	@Test
	void contextLoads() {
		stringRedisTemplate.opsForValue().set("hello","world");
	}

	@Test
	void testSeckill() {
		Date date = new Date();
		//1748239200000
		System.out.println(date.getTime());

	}


}
