package com.atguigu.gulimall.product.dao;

import com.atguigu.gulimall.product.entity.SkuImagesEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * sku图片
 * 
 * @author chenshun
 * @email 3268144062@qq.com
 * @date 2025-02-03 13:09:15
 */
@Mapper
public interface SkuImagesDao extends BaseMapper<SkuImagesEntity> {

    void deleteImages(List<Long> bySkuId);
}
