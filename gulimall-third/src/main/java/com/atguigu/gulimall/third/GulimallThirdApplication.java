package com.atguigu.gulimall.third;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.commons.util.UtilAutoConfiguration;

@SpringBootApplication(exclude = {UtilAutoConfiguration.class})
public class GulimallThirdApplication {

	public static void main(String[] args) {
		SpringApplication.run(GulimallThirdApplication.class, args);
	}

}
