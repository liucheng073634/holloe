/**
  * Copyright 2025 bejson.com 
  */
package com.atguigu.gulimall.product.vo;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * Auto-generated: 2025-02-24 15:58:32
 *
 * @author bejson.com (i@bejson.com)
 * @website http://www.bejson.com/java2pojo/
 */
@Data
public class SpuSaveVo {

    private String spuName; //商品名称
    private String spuDescription; //商品描述
    private Long catalogId; //商品分类id
    private Long brandId; //品牌id
    private BigDecimal  weight; //商品重量
    private int publishStatus;  //上架状态
    private List<String> decript; //商品介绍
    private List<String> images; //商品图片
    private Bounds bounds;  //积分信息
    private List<BaseAttrs> baseAttrs; //商品
    private List<Skus> skus;   //商品sku信息


}