package com.atguigu.common.exception;

import lombok.Data;

@Data
public class NoStockException extends RuntimeException {
    private Long skuId;
    public NoStockException(Long skuId){
        super(skuId+"没有足够的库存了");
    }

    public NoStockException(){
        super();
    }


}
