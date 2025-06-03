package com.atguigu.gulimall.product.service.impl;

import com.alibaba.fastjson.TypeReference;
import com.atguigu.common.utils.R;
import com.atguigu.gulimall.product.entity.SkuImagesEntity;
import com.atguigu.gulimall.product.entity.SpuInfoDescEntity;
import com.atguigu.gulimall.product.entity.SpuInfoEntity;
import com.atguigu.gulimall.product.feign.SeckillFeignService;
import com.atguigu.gulimall.product.service.AttrGroupService;
import com.atguigu.gulimall.product.service.SkuImagesService;
import com.atguigu.gulimall.product.service.SkuSaleAttrValueService;
import com.atguigu.gulimall.product.vo.SeckillInfoVo;
import com.atguigu.gulimall.product.vo.SkuItemSaleAttrVo;
import com.atguigu.gulimall.product.vo.SkuItemVo;
import com.atguigu.gulimall.product.vo.SpuItemAttrGroupVo;
import io.swagger.annotations.Authorization;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;

import com.atguigu.gulimall.product.dao.SkuInfoDao;
import com.atguigu.gulimall.product.entity.SkuInfoEntity;
import com.atguigu.gulimall.product.service.SkuInfoService;


@Service("skuInfoService")
public class SkuInfoServiceImpl extends ServiceImpl<SkuInfoDao, SkuInfoEntity> implements SkuInfoService {

    @Autowired
    private SkuImagesService skuImagesService;

    @Autowired
    private SpuInfoDescServiceImpl spuInfoDescService;

    @Autowired
    private AttrGroupService attrGroupService;

    @Autowired
    private SkuSaleAttrValueService skuSaleAttrValueService;

    @Autowired
    ThreadPoolExecutor executor;

    @Autowired
    SeckillFeignService seckillFeignService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<SkuInfoEntity> page = this.page(
                new Query<SkuInfoEntity>().getPage(params),
                new QueryWrapper<SkuInfoEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public void saveSkuInfo(SkuInfoEntity skuInfoEntity) {
        this.baseMapper.insert(skuInfoEntity);
    }


    @Override
    public PageUtils queryPageByCondition(Map<String, Object> params) {

        QueryWrapper<SkuInfoEntity> wrapper = new QueryWrapper<>();

        //catelogId: 225
        //brandId: 8
        //min: 0
        //max: 0
        String key = (String) params.get("key");
        if(!StringUtils.isEmpty(key)){
            wrapper.and((obj)->{
                obj.eq("id",key).or().like("sku_name",key);
            });
        }
        String catelogId = (String) params.get("catelogId");
        if(!StringUtils.isEmpty(catelogId)&&!"0".equalsIgnoreCase(catelogId)){
            wrapper.and((obj)->{
                obj.eq("catalog_id",catelogId);
            });
        }
        String brandId = (String) params.get("brandId");
        if(!StringUtils.isEmpty(brandId)&&!"0".equalsIgnoreCase(brandId)){
            wrapper.and((obj)->{
                obj.eq("brand_id",brandId);
            });
        }
        String min = (String) params.get("min");
        if(!StringUtils.isEmpty(min)){
            wrapper.and((obj)->{
                obj.ge("price",min);
            });
        }
        String max = (String) params.get("max");
        if(!StringUtils.isEmpty(max)){
            try {
                BigDecimal bigDecimal = new BigDecimal(max);
                if(bigDecimal.compareTo(new BigDecimal("0"))==1){
                    wrapper.and((obj)->{
                        obj.le("price",max);
                    });
                }


            }catch (Exception e){

            }

        }

        IPage<SkuInfoEntity> page = this.page(
                new Query<SkuInfoEntity>().getPage(params),
                wrapper
        );

        return new PageUtils(page);
    }

    //根据spuId查询所有sku
    @Override
    public List<SkuInfoEntity> getSkusBySpuId(Long spuId) {
        return this.list(new QueryWrapper<SkuInfoEntity>().eq("spu_id",spuId));
    }

    @Override
    public SkuItemVo item(Long skuId) {
        SkuItemVo skuItemVo = new SkuItemVo();
        CompletableFuture<SkuInfoEntity> infoFuture =  CompletableFuture.supplyAsync(() -> {
            SkuInfoEntity byId = getById(skuId);
            skuItemVo.setInfo(byId);
            return byId;
        }, executor);
        //sku基本信息获取  sku_info
        CompletableFuture<Void> voidCompletableFuture2 = infoFuture.thenAcceptAsync(res -> {
            List<SkuItemSaleAttrVo> saleAttrsBySpuId = skuSaleAttrValueService.getSaleAttrsBySpuId(res.getSpuId());
            List<SkuItemSaleAttrVo> saleAttrsBySpuIds = saleAttrsBySpuId.stream().filter(item -> {
                if (item != null) {
                    return true;
                }
                return false;
            }).collect(Collectors.toList());

            skuItemVo.setSaleAttr(saleAttrsBySpuIds);
        }, executor);

        CompletableFuture<Void> voidCompletableFuture = infoFuture.thenAcceptAsync((res) -> {
            //spu销售属性组合

            SpuInfoDescEntity spuInfoDescEntity = spuInfoDescService.getById(res.getSpuId());
            skuItemVo.setDesc(spuInfoDescEntity);
        }, executor);

        CompletableFuture<Void> attrFuture = infoFuture.thenAcceptAsync((res) -> {
            List<SpuItemAttrGroupVo> groupAttrs = attrGroupService.getAttrGroupWithAttrsBySpuId(res.getSpuId(), res.getCatalogId());
            skuItemVo.setGroupAttrs(groupAttrs);
        }, executor);
        CompletableFuture<Void> voidCompletableFuture1 = CompletableFuture.runAsync(() -> {
            //sku图片信息 sku_images
            List<SkuImagesEntity> images = skuImagesService.getImagesBySkuId(skuId);
            skuItemVo.setImages(images);
        }, executor);


        CompletableFuture<Void> seckillFuture = CompletableFuture.runAsync(() -> {
            //sku图片信息 sku_images
            R r = seckillFeignService.getSkuSeckillInfo(skuId);
            if (r.getCode() == 0) {
                SeckillInfoVo data = r.getData(new TypeReference<SeckillInfoVo>() {
                });
                skuItemVo.setSeckillInfo(data);
            }
        }, executor);


        try {
            CompletableFuture.allOf(voidCompletableFuture, voidCompletableFuture1, voidCompletableFuture2, attrFuture,seckillFuture).get();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }
        return skuItemVo;
    }

    @Override
    public void remove(Long spuId) {
        this.remove(new QueryWrapper<SkuInfoEntity>().eq("spu_id",spuId));
    }

    @Override
    public List<Long> getBySkuId(Long spuId) {
        List<Long> skuIds=this.baseMapper.selectBySkuId(spuId);
        return skuIds;
    }



}