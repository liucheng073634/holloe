package com.atguigu.gulimall.product.service.impl;


import com.alibaba.cloud.commons.io.IOUtils;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.atguigu.gulimall.product.service.CategoryBrandRelationService;
import com.atguigu.gulimall.product.vo.Catelog2Vo;
import javassist.runtime.Inner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;

import com.atguigu.gulimall.product.dao.CategoryDao;
import com.atguigu.gulimall.product.entity.CategoryEntity;
import com.atguigu.gulimall.product.service.CategoryService;
import org.springframework.transaction.annotation.Transactional;
import org.thymeleaf.util.StringUtils;


@Service("categoryService")
public class CategoryServiceImpl extends ServiceImpl<CategoryDao, CategoryEntity> implements CategoryService {

    @Autowired
    private StringRedisTemplate redisTemplate;



    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        // 查询所有分类
        IPage<CategoryEntity> page = this.page(
                new Query<CategoryEntity>().getPage(params),
                new QueryWrapper<CategoryEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public List<CategoryEntity> listWithTree() {
        // 1 查出所有分类
        List<CategoryEntity> entities=baseMapper.selectList(null);
        List<CategoryEntity>  collect =entities.stream().filter(entity->entity.getParentCid()==0)
                .map(item->{ item.setChildren(getChildren(item,entities));
                    return item;
                }).sorted(Comparator.comparingInt(entity -> (entity.getSort() == null ? 0 : entity.getSort()))).collect(Collectors.toList());

        return collect;

    }

    private List<CategoryEntity> getChildren(CategoryEntity root, List<CategoryEntity> all) {
        // 递归查找所有子菜单
        List<CategoryEntity> children = all.stream().filter(categoryEntity -> categoryEntity.getParentCid().equals(root.getCatId()))
                .map(categoryEntity -> {
                    categoryEntity.setChildren(getChildren(categoryEntity, all));
                    return categoryEntity;
                }).sorted(Comparator.comparingInt(entity -> (entity.getSort() == null ? 0 : entity.getSort()))).collect(Collectors.toList());

        return children;

    }
    @Override
    public void removeMenuByIds(List<Long> list) {
        //TODO 检查当前删除的菜单是否被其他地方引用
        baseMapper.deleteBatchIds(list);
    }

    @Override
    public Long[] findCatelogPath(Long catelogId) {
        List<Long> paths=new ArrayList<>();
        List<Long> path = findParentPath(catelogId,paths);
        Collections.reverse(path);
        return  path.toArray(new Long[path.size()]);
    }

    // 递归更新所有关联的数据
   /* @Caching(evict = {
            @CacheEvict(value = {"category"},key = "'getLevel1Catagories'"),
            @CacheEvict(value = {"category"},key = "'getCatelogJson'")
    })*/
    @CacheEvict(value = {"category"},allEntries = true )
    @Transactional
    @Override
    public void updateCascade(CategoryEntity category) {
        this.updateById(category);

    }


    @Override
    // 缓存逻辑
    // 1、空结果缓存：解决空数据的问题
    //  1)、缓存数据空
    //  2)、解决缓存穿透：查询一个不存在的key，给缓存中插入一个空值
    //  2、与spring cache的区别：
    //  1)、spring cache的注解，还是需要自己写业务逻辑
    //  2)、spring cache只支持单机，redis支持集群
    //  3)、spring cache的注解，默认是单机使用的，可以自己指定redisTemplate
    @Cacheable(value = {"category"}, key = "#root.method.name")
    public List<CategoryEntity> getLevel1Catagories() {
//        long start = System.currentTimeMillis();
        List<CategoryEntity> parent_cid = this.list(new QueryWrapper<CategoryEntity>().eq("parent_cid", 0));
//        System.out.println("查询一级菜单时间:"+(System.currentTimeMillis()-start));
        return parent_cid;
    }

    @Cacheable(value = {"category"},key = "#root.method.name",sync = true)
    public Map<String, List<Catelog2Vo>> getCatelogJson() {
        String catelogJson = redisTemplate.opsForValue().get("catelogJson");
        if (StringUtils.isEmpty(catelogJson)) {
            System.out.println("缓存不命中...将要查数据库");
            Map<String, List<Catelog2Vo>> catelogJsonDb = getCatelogJsonDbRedisLock();
            return catelogJsonDb;
        }
        System.out.println("命中缓存");
        // 从redis中获取到数据,序列化
        Map<String, List<Catelog2Vo>> result = JSON.parseObject(catelogJson, new TypeReference<>() {});
        return result;
    }


    public Map<String, List<Catelog2Vo>> getCatelogJsonDbRedisLock() {
        String string = UUID.randomUUID().toString();
        Boolean b = redisTemplate.opsForValue().setIfAbsent("lock", string,300, TimeUnit.SECONDS);
        if(b){
           /* if(string.equals(redisTemplate.opsForValue().get("lock"))){
             redisTemplate.delete("lock");
            }*/


            Map<String, List<Catelog2Vo>> stringListMap ;
            try {
                 stringListMap = getStringListMap();
            }finally {
               /* String script="if redis.call('get',KEYS[1]) == ARGV[1] then return redis.call('del',KEYS[1]) else return 0 end";*/
                // 加载Lua脚本

                DefaultRedisScript<Long> longDefaultRedisScript = new DefaultRedisScript<>();

                longDefaultRedisScript.setLocation(new ClassPathResource("lock.lua"));

                Long  lock =redisTemplate.execute(longDefaultRedisScript,
                        Arrays.asList("lock"),string);
            }



            return stringListMap;
        }else {
            try{
                Thread.sleep(200);
            }catch (Exception e){
                e.printStackTrace();
            }
            return getCatelogJsonDbRedisLock();
        }

    }


    // redis分布式锁
    private Map<String, List<Catelog2Vo>> getStringListMap() {
        //得到锁后，去查缓存
        String catelogJson = redisTemplate.opsForValue().get("catelogJson");
        if(!StringUtils.isEmpty(catelogJson)){
            System.out.println("查询数据库");
            // 如果有，直接返回
            Map<String, List<Catelog2Vo>> result= JSON.parseObject(catelogJson, new TypeReference<>() {
            });
            return result;
        }

        List<CategoryEntity> list = this.list();


        // 使用 collect 方法收集结果
        Map<String, List<Catelog2Vo>> collect = list.stream()
                // 过滤出一级分类（ParentCid 为 0 的）
                .filter(categoryEntity -> categoryEntity.getParentCid() == 0)
                .collect(Collectors.toMap(
                        // 以一级分类的 CatId 为键
                        categoryEntity -> categoryEntity.getCatId().toString(),

                        // 以一级分类为起点，构建包含二级分类和三级分类的数据结构
                        categoryEntity2 -> {
                            List<Catelog2Vo> catelog2VoList = list.stream()
                                    // 过滤出当前一级分类的子分类
                                    .filter(c -> c.getParentCid().equals(categoryEntity2.getCatId()))
                                    .map(categoryEntity3 -> {
                                        Catelog2Vo catelog2Vo = new Catelog2Vo();
                                        List<Catelog2Vo.Category3Vo> category3VoList = list.stream()
                                                // 过滤出当前二级分类的子分类
                                                .filter(categoryEntity4 -> categoryEntity4.getParentCid().equals(categoryEntity3.getCatId()))
                                                .map(categoryEntity4 -> {
                                                    Catelog2Vo.Category3Vo category3Vo = new Catelog2Vo.Category3Vo();
                                                    category3Vo.setCatalog2Id(categoryEntity3.getCatId().toString());
                                                    category3Vo.setId(categoryEntity4.getCatId().toString());
                                                    category3Vo.setName(categoryEntity4.getName());
                                                    return category3Vo;
                                                }).collect(Collectors.toList());
                                        catelog2Vo.setCatalog3List(category3VoList);
                                        catelog2Vo.setCatalog1Id(categoryEntity2.getCatId().toString());
                                        catelog2Vo.setName(categoryEntity3.getName());
                                        catelog2Vo.setId(categoryEntity3.getCatId().toString());
                                        return catelog2Vo;
                                    }).collect(Collectors.toList());
                            return catelog2VoList;
                        }
                ));
        String jsonString = JSON.toJSONString(collect);
        redisTemplate.opsForValue().set("catelogJson",jsonString,1, TimeUnit.DAYS);
        return collect;
    }


    public Map<String, List<Catelog2Vo>> getCatelogJsonDbLock() {

        // TODO 本地锁 ：synchronized，JUC (Lock)
        synchronized (this){

            //得到锁后，去查缓存
            return getStringListMap();
        }
    }




    private List<Long> findParentPath(Long catelogId,List<Long> paths){
        paths.add(catelogId);
        CategoryEntity byId = this.getById(catelogId);
        if(byId.getParentCid()!=0){
            findParentPath(byId.getParentCid(),paths);
        }
        return paths;
    }
}