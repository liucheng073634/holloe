package com.atguigu.gulimall.order.dao;

import com.atguigu.gulimall.order.entity.OrderEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 订单
 * 
 * @author chenshun
 * @email 3268144062@qq.com
 * @date 2025-02-03 21:19:05
 */
@Mapper
public interface OrderDao extends BaseMapper<OrderEntity> {
	
}
