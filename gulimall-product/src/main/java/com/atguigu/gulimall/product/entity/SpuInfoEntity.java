package com.atguigu.gulimall.product.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.math.BigDecimal;
import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * spu信息
 * 
 * @author chenshun
 * @email 3268144062@qq.com
 * @date 2025-02-03 13:09:15
 */
@Data
@TableName("pms_spu_info")
public class SpuInfoEntity implements Serializable {
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
