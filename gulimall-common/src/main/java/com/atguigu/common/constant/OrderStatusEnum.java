package com.atguigu.common.constant;

public enum OrderStatusEnum {
    CREATED(0, "待支付"),
    PAYED(1, "已支付"),
    CANCELED(2, "已取消"),
    REFUND(3, "已退款"),
    CLOSED(4, "已完成"),
    REFUNDING(5, "退款中");




    private int code;
    private String msg;

    OrderStatusEnum(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public int getCode() {
        return code;
    }

    public String getMsg() {
        return msg;
    }
}
