package com.atguigu.common.to;

import lombok.Data;

@Data
public class AttrResponseVo {
    private Long attrId;
    /**
     * 属性名
     */
    private String attrName;
    /**
     * 是否需要检索[0-不需要，1-需要]
     */
    private Integer searchType;
    /**
     * 属性图标
     */
    private String icon;
    /**
     * 可选值列表[用逗号分隔]
     */
    private String valueSelect;
    /**
     * 属性类型[0-销售属性，1-基本属性，2-既是销售属性又是基本属性]
     */
    private Integer attrType;
    /**
     * 状态[0 - 禁用，1 - 启用]
     */
    private Long enable;
    /**
     * 分类id
     */
    private Long catelogId;
    /**
     * 是否显示在介绍页面，如果需要显示在sku页面，这个字段可以不用，或者设置为0-不显示 1-显示
     */
    private Integer showDesc;

    private Long attrGroupId;
    //完整路径
    private String catelogName;
    //分组名称
    private String groupName;
    //完整路径
    private Long[] catelogPath;
}
