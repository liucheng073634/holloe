package com.atguigu.common.to.es;

import com.atguigu.common.to.Attrs;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class SkuEsModel {
    private  Long skuId;
    private  Long spuId;
    private  String skuTitle;
    private BigDecimal skuPrice;
    private  String skuImg;
    private  Long saleCount;
    private  Boolean hasStock;
    private  Long hotScore;
    private  Long brandId;
    private  Long catalogId;
    private  String brandName;
    private  String brandImg;
    private  String catalogName;
    private List<Attrs> attrs;





}
