package com.atguigu.gulimall.ware.service;

import com.atguigu.common.vo.AddressVo;
import com.baomidou.mybatisplus.extension.service.IService;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.gulimall.ware.entity.WareInfoEntity;

import java.math.BigDecimal;
import java.util.Map;

/**
 * 仓库信息
 *
 * @author chenshun
 * @email 3268144062@qq.com
 * @date 2025-02-03 21:43:29
 */
public interface WareInfoService extends IService<WareInfoEntity> {

    PageUtils queryPage(Map<String, Object> params);

    AddressVo getFare(Long addrId);
}

