package com.atguigu.gulimall.ware.vo;

import lombok.Data;

import java.util.List;

@Data
public class WareSkuLockVo {
    private String orderSn; // 订单号
    private List<OrderItemVo> locks; // 锁住的库存信息
}
