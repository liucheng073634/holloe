package com.atguigu.gulimall.auth.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.atguigu.common.constant.AuthServerConstant;
import com.atguigu.common.utils.HttpUtils;
import com.atguigu.common.utils.R;
import com.atguigu.gulimall.auth.feign.MemberFeignService;
import com.atguigu.common.vo.MemberResVo;
import com.atguigu.gulimall.auth.vo.SocialUser;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Map;

@Controller
public class OAuth2Controller {
    @Autowired
    MemberFeignService memberFeignService;

        @GetMapping("/oauth2.0/weibo/success")
        public String weibo(@RequestParam("code") String code, HttpSession session){
            Map<String, String> map = new HashMap<>();
            map.put("client_id","2286854788");
            map.put("client_secret","90c09776865a845affd1250a7109c74d");
            map.put("grant_type","authorization_code");
            map.put("redirect_uri","http://auth.gulimall.com/oauth2.0/weibo/success");
            map.put("code",code);
            try {
                HttpResponse post = HttpUtils.doPost("https://api.weibo.com", "/oauth2/access_token", "POST", null, null, map);

                if( post.getStatusLine().getStatusCode()==200){
                   String string = EntityUtils.toString(post.getEntity());
                   SocialUser socialUser = JSON.parseObject(string, SocialUser.class);
                   //远程调用
                    R oauthlogin = memberFeignService.oauthlogin(socialUser);
                    if(oauthlogin.getCode()==0){
                        MemberResVo data = oauthlogin.getData("data", new TypeReference<MemberResVo>() {
                        });

                        session.setAttribute(AuthServerConstant.LOGIN_USER,data);
                        return "redirect:http://gulimall.com";
                    }else {
                        return "redirect:http://auth.gulimall.com/login.html";
                    }
                }else {
                   return "redirect:http://auth.gulimall.com/login.html";
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

        }
}
