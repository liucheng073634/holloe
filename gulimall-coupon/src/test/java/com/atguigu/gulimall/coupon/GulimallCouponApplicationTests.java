package com.atguigu.gulimall.coupon;

import com.alibaba.nacos.common.utils.UuidUtils;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class GulimallCouponApplicationTests {

    @Test
    void contextLoads() {
        System.out.println(UuidUtils.generateUuid().toString().replace("-", ""));
    }

}
