package com.atguigu.gulimall.order.vo;

import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
public class SpuInfoVo {
    private static final long serialVersionUID = 1L;

    /**
     * 产品id
     */
    @TableId
    private Long id;
    /**
     * 产品名称
     */
    private String spuName;
    /**
     * 产品描述
     */
    private String spuDescription;
    /**
     * 分类id
     */
    private Long catalogId;
    /**
     * 品牌id
     */
    private Long brandId;
    /**
     *
     */
    private BigDecimal weight;
    /**
     * 发布状态[0 - 未发布，1 - 已发布]
     */
    private Integer publishStatus;
    /**
     *
     */
    private Date createTime;
    /**
     *
     */
    private Date updateTime;
}
