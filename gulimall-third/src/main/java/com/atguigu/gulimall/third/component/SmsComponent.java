package com.atguigu.gulimall.third.component;



import com.atguigu.gulimall.third.util.HttpUtils;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import java.util.HashMap;

@Component
@ConfigurationProperties(prefix = "spring.cloud.alicloud.sms")
@Data
public class SmsComponent {
    private String host;
    private String path;
    private String skin;
    private String sign;
    private String appCode;
    public void sendSmsCode(String phone, String code) {
        String method="GET";
        HashMap<String, String> hereaders = new HashMap<String, String>();
        hereaders.put("Authorization", "APPCODE " + appCode);
        HashMap<String, String> query =  new HashMap<String, String>();
        query.put("code", code);
        query.put("phone", phone);
        query.put("skin", skin);
        query.put("sign", sign);

        try {
            HttpUtils.doGet(host, path, method, hereaders, query);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
