package com.atguigu.gulimall.order;


import com.atguigu.common.utils.LuaUtils;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;

@SpringBootTest
class GulimallOrderApplicationTests {
        @Test
        void contextLoads() throws IOException {
            String lua = LuaUtils.getLua("Token.lua");
            System.out.println(lua);
        }
}
