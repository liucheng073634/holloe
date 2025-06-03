package com.atguigu.gulimall.cart.config;

import com.alibaba.csp.sentinel.adapter.spring.webflux.callback.BlockRequestHandler;
import com.alibaba.csp.sentinel.adapter.spring.webflux.callback.WebFluxCallbackManager;
import com.alibaba.csp.sentinel.adapter.spring.webmvc.callback.BlockExceptionHandler;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.alibaba.fastjson.JSON;
import com.atguigu.common.exception.BizCodeEnume;
import com.atguigu.common.utils.R;
import org.springframework.context.annotation.Configuration;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Configuration
public class SeckillSentinelConfig implements BlockExceptionHandler {

    /**
     * 自定义限流返回信息
     */
    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, BlockException ex) throws IOException {
        // 降级业务处理
        R error = R.error(BizCodeEnume.TOO_MANY_REQUESTS.getCode(), BizCodeEnume.TOO_MANY_REQUESTS.getMsg());
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json");
        response.getWriter().write(JSON.toJSONString(error));

    }


}
















