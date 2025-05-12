package com.atguigu.gulimall.order.vo;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;


public class OrderConfirmVo {
    //收获地址
    @Getter @Setter
    List<MemberAddressVo> address;
    //所以选中的购物项
    @Getter @Setter
    List<OrderItemVo> items;
    //防重令牌
    @Getter @Setter
    String orderToken;

    //优惠券信息
    @Getter @Setter
    Integer integration;
    //订单的总额
    @Getter @Setter
    Map<Long, Boolean> stocks;





    public BigDecimal getTotal() {
        BigDecimal totalNum = BigDecimal.ZERO;
        if (items != null && items.size() > 0) {
            for (OrderItemVo item : items) {
                //计算当前商品的总价格
                BigDecimal itemPrice = item.getPrice().multiply(new BigDecimal(item.getCount().toString()));
                //再计算全部商品的总价格
                totalNum = totalNum.add(itemPrice);
            }
        }
        return totalNum;
    }

    public Integer getCount(){
        Integer i=0;
        if(items!=null){
            for (OrderItemVo item : items){
                i+=item.getCount();
            }
        }
        return i;
    }







    //应付价格


    public BigDecimal getPayPrice() {
        return getTotal();
    }
}
