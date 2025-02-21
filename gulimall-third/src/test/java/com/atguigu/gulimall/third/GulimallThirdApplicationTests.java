package com.atguigu.gulimall.third;

import com.aliyun.oss.OSS;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.FileInputStream;
import java.io.InputStream;

@SpringBootTest
class GulimallThirdApplicationTests {
	@Autowired
	OSS ossClient;
	@Test
	void test() throws Exception {
      /* String endpoint = "oss-cn-hangzhou.aliyuncs.com";
       String accessKeyId = "LTAI5tMojnJCwYBBhvcg8g67";
       String accessKeySecret = "7EkRWJlxhOIayIO9qKzi3h5BUFmlr0";
        OSS ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);*/
		InputStream inputStream = new FileInputStream("/Users/liuchengdediannao/Desktop/Src/背景/11.jpeg");
		ossClient.putObject("web-tiase", "13.jpeg", inputStream);
		ossClient.shutdown();
		System.out.println("上传完成");
	}

}
