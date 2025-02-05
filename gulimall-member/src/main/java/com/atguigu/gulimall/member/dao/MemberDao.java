package com.atguigu.gulimall.member.dao;

import com.atguigu.gulimall.member.entity.MemberEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 会员
 * 
 * @author chenshun
 * @email 3268144062@qq.com
 * @date 2025-02-03 21:11:44
 */
@Mapper
public interface MemberDao extends BaseMapper<MemberEntity> {
	
}
