package com.atguigu.gulimall.auth.vo;

import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Pattern;

@Data
public class UserRegistVo {
    @NotEmpty(message = "用户名不能为空")
    @Length( min = 6,max = 18,message = "密码长度必须是6-18位")
    private String userName;
    @Length( min = 6,max = 18,message = "密码长度必须是6-18位")
    @NotEmpty(message = "密码不能为空")
    private String password;
    @Pattern(regexp = "^1[3-9]\\d{9}$",message = "手机号格式不正确")
    private String phone;
    @NotEmpty(message = "验证码不能为空")
    private String code;

}
