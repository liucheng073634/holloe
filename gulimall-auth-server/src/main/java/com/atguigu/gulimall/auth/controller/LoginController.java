package com.atguigu.gulimall.auth.controller;

import com.alibaba.fastjson.TypeReference;
import com.atguigu.common.constant.AuthServerConstant;
import com.atguigu.common.exception.BizCodeEnume;
import com.atguigu.common.utils.R;
import com.atguigu.common.vo.MemberResVo;
import com.atguigu.gulimall.auth.feign.MemberFeignService;
import com.atguigu.gulimall.auth.vo.UserLoginVo;
import com.atguigu.gulimall.auth.vo.UserRegistVo;
import com.atguigu.gulimall.auth.feign.ThirdPartFeignService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Controller
public class LoginController {
    @Autowired
    ThirdPartFeignService thirdPartFeignService;

    @Autowired
    StringRedisTemplate redisTemplate;
    @Autowired
    MemberFeignService memberFeignService;


    @ResponseBody
    @GetMapping("/sms/sendCode")
    public R sendCode(@RequestParam("phone") String phone) {
        String s = redisTemplate.opsForValue().get(AuthServerConstant.SMS_CODE_CACHE_PREFIX + phone);
        System.out.println("获取验证码");
       if(s!=null){
           long l = Long.parseLong(s.split("_")[1]);
           if((System.currentTimeMillis()-l) < 60000){
               return R.error(BizCodeEnume.SMS_CODE_EXCEPTION.getCode(), BizCodeEnume.SMS_CODE_EXCEPTION.getMsg());
           }
       }
        String substring = UUID.randomUUID().toString().substring(0, 5);
        String code = substring+"_"+System.currentTimeMillis();
        redisTemplate.opsForValue().set(AuthServerConstant.SMS_CODE_CACHE_PREFIX + phone, code,10, TimeUnit.MINUTES);
        thirdPartFeignService.sendCode(phone, substring);
        return R.ok();
    }

    /**
     * //TODO重定向携带数据，利用session原理，将数据放在session中，
     * //只要跳到下一个页面取出这个数据以后，session里面的数据就会删掉
     *
     *
     * RedirectAttributes 重定向携带数据
     * @param userRegistVo
     * @param result
     * @param model
     * @return
     */

    @PostMapping("/regist")
    public String regist( @Valid UserRegistVo userRegistVo, BindingResult result, RedirectAttributes model, HttpSession session){
        Map<String, String> errors =new HashMap<>();
        if(result.hasErrors()){
             errors = result.getFieldErrors().stream().collect(Collectors.toMap(FieldError::getField, FieldError::getDefaultMessage));
            model.addFlashAttribute("errors",errors);
            //校验失败
            return "redirect:http://auth.gulimall.com/reg.html";
        }else {
            String code = userRegistVo.getCode();
            String redisCode = redisTemplate.opsForValue().get(AuthServerConstant.SMS_CODE_CACHE_PREFIX + userRegistVo.getPhone());
            if(!StringUtils.isEmpty(redisCode)&&code.equals(redisCode.split("_")[0])){

                    //删除验证码
                    redisTemplate.delete(AuthServerConstant.SMS_CODE_CACHE_PREFIX + userRegistVo.getPhone());

                    //注册,调用远程服务进行注册
                    R regist = memberFeignService.regist(userRegistVo);
                    if(regist.getCode()==0){
                        //登录成功，数据放到session中
                        MemberResVo data = regist.getData("data", new TypeReference<MemberResVo>() {
                        });
                        session.setAttribute(AuthServerConstant.LOGIN_USER,data);
                        return "redirect:http://auth.gulimall.com/login.html";

                    }else {

                        errors.put("msg",regist.getData(new TypeReference<String>(){}));
                        model.addFlashAttribute("errors", errors);
                        return "redirect:http://auth.gulimall.com/reg.html";
                    }


            }else {
                HashMap<String, String> stringStringHashMap = new HashMap<>();
                stringStringHashMap.put("msg","验证码错误");
                model.addFlashAttribute("errors",stringStringHashMap);
                return "redirect:http://auth.gulimall.com/reg.html";
            }


        }



    }
    //登录
    @PostMapping("/login")
    public String login(UserLoginVo userLoginVo, RedirectAttributes model, HttpSession session){
        R login = memberFeignService.login(userLoginVo);
       if(login.getCode()==0){
           MemberResVo data = login.getData("data", new TypeReference<MemberResVo>() {
           });
           session.setAttribute(AuthServerConstant.LOGIN_USER,data);
           return "redirect:http://gulimall.com";
          /* return "redirect:https://www.baidu.com/";*/
       }else {
           Map<String, String> errors = new HashMap<>();
           errors.put("msg",login.getData(new TypeReference<String>(){}));
           model.addFlashAttribute("errors",errors);
           return "redirect:http://auth.gulimall.com/login.html";
       }
    }

    @GetMapping("/login.html")
    public String loginPage(HttpSession session){
        Object attribute = session.getAttribute(AuthServerConstant.LOGIN_USER);
        if(attribute==null){
            return "login";
        }else {
            return "redirect:http://gulimall.com";
        }
    }


}
