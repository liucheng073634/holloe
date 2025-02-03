package com.atguigu.gulimall.product.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * 商品评论
 * 
 * @author chenshun
 * @email 3268144062@qq.com
 * @date 2025-02-03 13:09:15
 */
@Data
@TableName("pms_spu_comment")
public class SpuCommentEntity implements Serializable {
	private static final long serialVersionUID = 1L;

	/**
	 * id
	 */
	@TableId
	private Long id;
	/**
	 * sku_id
	 */
	private Long skuId;
	/**
	 * spu_id
	 */
	private Long spuId;
	/**
	 * 产品名称
	 */
	private String spuName;
	/**
	 * 会员昵称
	 */
	private String memberNickName;
	/**
	 * 星级
	 */
	private Integer star;
	/**
	 * 会员ip
	 */
	private String memberIp;
	/**
	 * 创建时间
	 */
	private Date createTime;
	/**
	 * 显示状态[0-不显示，1-显示]
	 */
	private Integer showStatus;
	/**
	 * 创建时属性
	 */
	private String spuAttributes;
	/**
	 * 点赞数
	 */
	private Integer likesCount;
	/**
	 * 回复数
	 */
	private Integer replyCount;
	/**
	 * 资源图片/视频[json数据，[{type:文件类型,url:资源路径}]]
	 */
	private String resources;
	/**
	 * 内容
	 */
	private String content;
	/**
	 * 会员头像
	 */
	private String memberIcon;
	/**
	 * 评论类型[0 - 商品直接评论，1 - 评论的回复]
	 */
	private Integer commentType;

}
