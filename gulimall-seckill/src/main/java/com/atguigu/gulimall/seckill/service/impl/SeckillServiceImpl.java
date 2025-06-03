package com.atguigu.gulimall.seckill.service.impl;

import com.alibaba.csp.sentinel.Entry;
import com.alibaba.csp.sentinel.SphU;
import com.alibaba.csp.sentinel.annotation.SentinelResource;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.atguigu.common.to.mq.SeckillOrderTo;
import com.atguigu.common.utils.R;
import com.atguigu.common.vo.MemberResVo;
import com.atguigu.gulimall.seckill.feign.CouponFeignService;
import com.atguigu.gulimall.seckill.feign.ProductFeignService;
import com.atguigu.gulimall.seckill.interceptor.LoginUserInterceptor;
import com.atguigu.gulimall.seckill.service.SeckillService;
import com.atguigu.gulimall.seckill.to.SecKillSkuRedisTo;
import com.atguigu.gulimall.seckill.vo.SeckillSesssionsWithSkus;
import com.atguigu.gulimall.seckill.vo.SkuInfoVo;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RSemaphore;
import org.redisson.api.RedissonClient;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.thymeleaf.util.StringUtils;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Slf4j
@Service
public class SeckillServiceImpl implements SeckillService {
    @Autowired
    CouponFeignService couponFeignService;

    @Autowired
    StringRedisTemplate stringredisTemplate;

    @Autowired
    ProductFeignService productFeignService;

    @Autowired
    RedissonClient redissonClient;

    @Autowired
    RabbitTemplate rabbitTemplate;



    private  final String SESSION_CACHE_PREFIX = "seckill:sessions:";
    private  final String SKU_CACHE_PREFIX = "seckill:skus:";

    private final String SKU_STOCK_SEMAPHORE =  "seckill:stock:";

    @Override
    public void  uploadSeckillSkuLatest3Days() {
        //  1. 获取最近三天需要参与秒杀的商品信息
        R r = couponFeignService.getLatest3Days();
        if(r.getCode()==0){
            List<SeckillSesssionsWithSkus> data = r.getData(new TypeReference<List<SeckillSesssionsWithSkus>>() {});
            saveSessionInfos(data);

            saveSessionSkuInfos(data);

        }


    }
    public List<SecKillSkuRedisTo> blockHandler(BlockException e){
        log.error("blockHandler被限流了,{}",  e.getMessage());
      return null;
    }

    /**
     * blockHandler 原方法降级被调用
     * fallback
     * @return
     */

    @SentinelResource(value = "getCurrentSeckillSkusResource",blockHandler = "blockHandler")
    @Override
    public List<SecKillSkuRedisTo> getCurrentSeckillSkus() {
        long time = new Date().getTime();
        //  1. 定义受保护的资源
        try(Entry entry=SphU.entry("SeckillSkus")){
            Set<String> keys = stringredisTemplate.keys(SESSION_CACHE_PREFIX + "*");
            // 1. 缓存活动信息
            for  (String key : keys) {

                String replace = key.replace(SESSION_CACHE_PREFIX, "");
                String[] split = replace.split("_");
                long start = Long.parseLong(split[0]);
                long end = Long.parseLong(split[1]);
                if(time>=start&&time<=end){
                    //  当前时间在活动时间段内
                    List<String> range = stringredisTemplate.opsForList().range(key, -100, 100);
                    BoundHashOperations<String, String, String> ops = stringredisTemplate.boundHashOps(SKU_CACHE_PREFIX);
                    List<String> objects = ops.multiGet(range);
                    if (objects != null){
                        List<SecKillSkuRedisTo> collect = objects.stream().map(item -> {
                            SecKillSkuRedisTo secKillSkuRedisTo = JSON.parseObject(item.toString(), SecKillSkuRedisTo.class);
                            return secKillSkuRedisTo;
                        }).collect(Collectors.toList());
                        return collect;
                    }
                    break;

                }
            }
        }catch (BlockException e) {
            log.error("被限流了,{}",  e.getMessage());
        }
        return null;
    }

    @Override
    public SecKillSkuRedisTo getSkuSeckillInfo(Long skuId) {
        BoundHashOperations<String, String, String> ops = stringredisTemplate.boundHashOps(SKU_CACHE_PREFIX);
        Set<String> keys = ops.keys();

            if(keys!=null&& keys.size()>0){
                String regx="\\d_"+skuId;
                for (String   key : keys){
                    if(Pattern.matches(regx,key)){
                        String json=ops.get(key);
                        SecKillSkuRedisTo secKillSkuRedisTo = JSON.parseObject(json, SecKillSkuRedisTo.class);

                        long time = new Date().getTime();
                        if(time>=secKillSkuRedisTo.getStartTime()&&time<=secKillSkuRedisTo.getEndTime()){
                            return secKillSkuRedisTo;
                        }else{
                            secKillSkuRedisTo.setRandomCode(null);
                        }


                        return secKillSkuRedisTo;
                    }
                }

            }


        return null;
    }

    @Override
    public String kill(String killId, String key, Integer num) {
        MemberResVo memberResVo = LoginUserInterceptor.loginUser.get();
        BoundHashOperations<String, String, String> ops = stringredisTemplate.boundHashOps(SKU_CACHE_PREFIX);
        String s = ops.get(killId);
        if (StringUtils.isEmpty(s)) {
            return null;
        }else {
            SecKillSkuRedisTo redis = JSON.parseObject(s, SecKillSkuRedisTo.class);
            Long startTime = redis.getStartTime();
            Long endTime = redis.getEndTime();
            long time = new Date().getTime();
            long ttl = endTime - time;
            if(time>=startTime&&time<=endTime){
                String randomCode = redis.getRandomCode();
                String skuId = redis.getPromotionSessionId() + "_" + redis.getSkuId();
                if(randomCode.equals(key)&&killId.equals(skuId)){
                    if(num<= redis.getSeckillLimit()){
                    //  校验数量
                        String redisKey=memberResVo.getId()+"_"+skuId;
                        Boolean b = stringredisTemplate.opsForValue().setIfAbsent(redisKey, num.toString(),ttl, TimeUnit.MILLISECONDS);
                        if(b){
                            //  校验购买数量，幂等性
                            RSemaphore semaphore = redissonClient.getSemaphore(SKU_STOCK_SEMAPHORE + randomCode);

                                boolean b1 = semaphore.tryAcquire(num);
                                if(b1){
                                    String timeId = IdWorker.getTimeId();
                                    SeckillOrderTo seckillOrderTo = new SeckillOrderTo();
                                    seckillOrderTo.setOrderSn(timeId);
                                    seckillOrderTo.setMemberId(memberResVo.getId());
                                    seckillOrderTo.setNum(num);
                                    seckillOrderTo.setPromotionSessionId(redis.getPromotionSessionId());
                                    seckillOrderTo.setSkuId(redis.getSkuId());
                                    seckillOrderTo.setSeckillPrice(redis.getSeckillPrice());
                                    rabbitTemplate.convertAndSend("order-event-exchange","order.seckill.order",seckillOrderTo);
                                    return timeId;
                                }
                                return null;

                        }else{
                            // 重复下单
                            return null;
                        }
                    }
                }else{
                    return null;
                }
            }else{
                return null;
            }

        }

        return null;
    }

    private void saveSessionInfos(List<SeckillSesssionsWithSkus> data) {
       data.stream().forEach(sesssion->{
           Long startTime = sesssion.getStartTime().getTime();
           Long endTime = sesssion.getEndTime().getTime();
           String key = SESSION_CACHE_PREFIX+startTime+"_"+endTime;
           Boolean b = stringredisTemplate.hasKey(key);
           if(!b){
               List<String> collect = sesssion.getRelationSkus().stream().map(item -> item.getPromotionSessionId()+"_"+item.getSkuId().toString()).collect(Collectors.toList());
               stringredisTemplate.opsForList().leftPushAll(key,collect);

           }

             });
    }
    private void saveSessionSkuInfos(List<SeckillSesssionsWithSkus> data) {
        BoundHashOperations<String, String, String> ops = stringredisTemplate.boundHashOps(SKU_CACHE_PREFIX);
        data.stream().forEach(session->{
            session.getRelationSkus().stream().forEach(sku->{
                SecKillSkuRedisTo secKillSkuRedisTo = new SecKillSkuRedisTo();
                String replace = UUID.randomUUID().toString().replace("_", "");
                //  1. 缓存sku信息
                if(!ops.hasKey(sku.getPromotionSessionId().toString()+"_"+sku.getSkuId().toString())){
                    R info = productFeignService.info(sku.getSkuId());
                    if(info.getCode()==0){
                        SkuInfoVo skuInfo = info.getData("skuInfo", new TypeReference<SkuInfoVo>() {
                        });
                        secKillSkuRedisTo.setSkuInfo(skuInfo);
                    }
                    //2. 缓存秒杀信息
                    BeanUtils.copyProperties(sku,secKillSkuRedisTo);

                    //3. 缓存活动信息
                    secKillSkuRedisTo.setStartTime(session.getStartTime().getTime());
                    secKillSkuRedisTo.setEndTime(session.getEndTime().getTime());

                    //随机码

                    secKillSkuRedisTo.setRandomCode(replace);
                    ops.put(sku.getPromotionSessionId().toString()+"_"+sku.getSkuId().toString(), JSON.toJSONString(secKillSkuRedisTo));
                    RSemaphore semaphore = redissonClient.getSemaphore(SKU_STOCK_SEMAPHORE + replace);
                    semaphore.trySetPermits(sku.getSeckillCount());
                }





            });
        });
    }

}
