package com.atguigu.gulimall.third.controller;


import com.atguigu.common.utils.R;
import com.atguigu.gulimall.third.component.SmsComponent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/ssm")
public class SmsSendController {
    @Lazy
    @Autowired
    private SmsComponent smsComponent;

    /**
     * 提供个别的服务调用
     * @param phone
     * @param code
     * @return
     */
    @GetMapping("/sendCodes")
    public R sendCode(@RequestParam("phone") String phone, @RequestParam("code")String code) {
        /*smsComponent.sendSmsCode(phone, code);*/
        System.out.println("手机号"+phone+"发送短信成功"+code);
        return R.ok();
    }

}
