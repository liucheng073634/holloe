package com.atguigu.gulimall.auth.feign;

import com.atguigu.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient("gulimall-third")
public interface ThirdPartFeignService {

    @GetMapping("/ssm/sendCodes")
    R sendCode(@RequestParam("phone") String phone, @RequestParam("code")String code);
}
