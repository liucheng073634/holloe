package com.atguigu.gulimall.product;



import com.atguigu.gulimall.product.dao.AttrGroupDao;
import com.atguigu.gulimall.product.entity.BrandEntity;
import com.atguigu.gulimall.product.service.AttrGroupService;
import com.atguigu.gulimall.product.service.BrandService;
import com.atguigu.gulimall.product.service.CategoryService;
import com.atguigu.gulimall.product.service.SkuSaleAttrValueService;
import com.atguigu.gulimall.product.vo.SkuItemSaleAttrVo;
import com.atguigu.gulimall.product.vo.SpuItemAttrGroupVo;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.extern.slf4j.Slf4j;


import org.junit.jupiter.api.Test;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.Arrays;
import java.util.List;
@Slf4j
@SpringBootTest

class GulimallProductApplicationTests {
    @Autowired
    BrandService brandService;
    @Autowired
    CategoryService categoryService;


    @Autowired
    RedissonClient redissonClient;

    @Autowired
    StringRedisTemplate redisTemplate;

    @Autowired
    AttrGroupDao attrGroupDao;

    @Autowired
    SkuSaleAttrValueService skuSaleAttrValueService;


    @Test
    void contextLoads() {
        /*BrandEntity brandEntity = new BrandEntity();
        brandEntity.setName("华为");
        brandService.save(brandEntity);*/
        List<BrandEntity> brandId = brandService.list(new QueryWrapper<BrandEntity>().eq("brand_id", 1));
        brandId.forEach(item->{
            System.out.println(item);
        });
    }

    @Test
    void testUpload() {
        Long[] path = categoryService.findCatelogPath(226L);
        log.info( "完整路径：{}", Arrays.toString(path));
    }
    @Test
    void testRedisTemplate() {
        redisTemplate.opsForValue().set("hello","world");
        String hello = redisTemplate.opsForValue().get("hello");
        System.out.println("redisTemplate.opsForValue().get(\"hello\") = " + hello);
    }

    @Test
    void testRedisson() {
        System.out.println(redissonClient);
    }
        @Test
    void testRedisson2() {
        System.out.println("你好");
    }

    @Test
    void testAttrGroup() {
        List<SpuItemAttrGroupVo> attrGroupWithAttrsBySpuId = attrGroupDao.getAttrGroupWithAttrsBySpuId(27L, 225L);
        System.out.println(attrGroupWithAttrsBySpuId);
    }


    @Test
    void testAttrGroup2() {
        List<SkuItemSaleAttrVo> saleAttrsBySpuId = skuSaleAttrValueService.getSaleAttrsBySpuId(24L);
        System.out.println(saleAttrsBySpuId);
    }


}
