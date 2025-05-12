package com.atguigu.common.exception;

public enum BizCodeEnume {
    UNKNOW_EXCEPTION(10000,"系统未知异常"),
    VALID_EXCEPTION(10001,"参数格式校验失败"),
    SMS_CODE_EXCEPTION(10002,"获取验证码频率太高，稍后再试"),
    PRODUCT_UP_EXCEPTION(11000,"商品上架异常"),
    USER_EXIST_EXCEPTION(15001,"用户存在"),
    PHONE_EXIST_EXCEPTION(15002,"手机号存在"),
    LOGINACCT_PASSWORD_INVAILD_EXCEPTION(20001,"账号或密码错误"),
    ACCOUNT_LOCKED(20002,"账号已锁定"),
    INNER_SERVER_EXCEPTION(20003,"内部服务异常"),
    USER_NOT_EXIST_EXCEPTION(20004,"用户不存在"),
    USER_PASSWORD_NOT_EXIST_EXCEPTION(20005,"密码错误"),
    TOKEN_INVALID_EXCEPTION(20006,"token失效"),
    USER_NOT_LOGIN_EXCEPTION(20007,"用户未登录"),
    NOT_LOGIN_EXCEPTION(20008,"未登录"),
    CAPTCHA_WRONG_EXCEPTION(20009,"验证码错误"),
    CAPTCHA_NOT_EXIST_EXCEPTION(20010,"验证码不存在"),
    CAPTCHA_EXPIRE_EXCEPTION(20011,"验证码过期"),
    SESSION_NOT_EXIST_EXCEPTION(20012,"session不存在"),
    NO_STOCK_EXCEPTION(21000,"没有库存了");

    private int  code;
    private String msg;
    BizCodeEnume(int code,String msg){
        this.code=code;
        this.msg=msg;
    }
    public int getCode() {
        return code;

    }
    public String getMsg() {
        return msg;
    }
}
