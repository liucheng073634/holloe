package com.atguigu.gulimall.member.exception;

public class PhoneExsitException extends RuntimeException {
    public PhoneExsitException() {
        super("手机号已经存在");
    }
}
