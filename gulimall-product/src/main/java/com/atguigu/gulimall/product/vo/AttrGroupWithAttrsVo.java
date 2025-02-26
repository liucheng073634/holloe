package com.atguigu.gulimall.product.vo;

import com.atguigu.gulimall.product.entity.AttrEntity;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.util.List;
@Data
public class AttrGroupWithAttrsVo {
    /**
     * 属性分组id
     */

    private Long attrGroupId;
    /**
     * 属性分组名
     */
    private String attrGroupName;
    /**
     * 排序
     */
    private Integer sort;
    /**
     * 描述
     */
    private String descript;
    /**
     * 图标
     */
    private String icon;
    /**
     * 分类id
     */
    private Long catelogId;

    private List<AttrEntity> attrs;
}
