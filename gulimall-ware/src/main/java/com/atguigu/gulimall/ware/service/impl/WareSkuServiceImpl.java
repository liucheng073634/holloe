package com.atguigu.gulimall.ware.service.impl;

import ch.qos.logback.classic.spi.EventArgUtil;
import com.alibaba.fastjson.TypeReference;
import com.atguigu.common.constant.WareConstant;
import com.atguigu.common.exception.NoStockException;
import com.atguigu.common.to.mq.OrderTo;
import com.atguigu.common.to.mq.StockDetailTo;
import com.atguigu.common.to.mq.StockLockedTo;
import com.atguigu.common.utils.R;
import com.atguigu.gulimall.ware.entity.SkuWareHasStock;

import com.atguigu.gulimall.ware.entity.WareOrderTaskDetailEntity;
import com.atguigu.gulimall.ware.entity.WareOrderTaskEntity;
import com.atguigu.gulimall.ware.feign.OrderFeignService;
import com.atguigu.gulimall.ware.feign.ProductFeignService;
import com.atguigu.gulimall.ware.service.WareOrderTaskDetailService;
import com.atguigu.gulimall.ware.service.WareOrderTaskService;
import com.atguigu.gulimall.ware.vo.*;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;

import com.atguigu.gulimall.ware.dao.WareSkuDao;
import com.atguigu.gulimall.ware.entity.WareSkuEntity;
import com.atguigu.gulimall.ware.service.WareSkuService;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service("wareSkuService")
public class WareSkuServiceImpl extends ServiceImpl<WareSkuDao, WareSkuEntity> implements WareSkuService {

    @Autowired
    ProductFeignService productFeignService;
    @Autowired
    WareSkuDao wareSkuDao;

    @Autowired
    WareOrderTaskService orderTaskService;
    @Autowired
    WareOrderTaskDetailService orderTaskDetailService;

    @Autowired
    OrderFeignService orderFeignService;

    @Autowired
    RabbitTemplate rabbitTemplate;



    public void unLockStock(Long skuId, Long wareId, Integer num,Long taskDetailId) {
        wareSkuDao.unlockStock(skuId,wareId,num);
        WareOrderTaskDetailEntity entity = new WareOrderTaskDetailEntity();
        entity.setId(taskDetailId);
        entity.setLockStatus(2);
        orderTaskDetailService.updateById(entity);
    }

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        QueryWrapper<WareSkuEntity> wrapper = new QueryWrapper<>();
        String skuId = (String)params.get("skuId");
        if(!StringUtils.isEmpty(skuId)){
                wrapper.eq("sku_id", skuId);
        }
        String wareId = (String)params.get("wareId");
        if(!StringUtils.isEmpty(wareId)){
            wrapper.eq("ware_id", wareId);
        }

        IPage<WareSkuEntity> page = this.page(
                new Query<WareSkuEntity>().getPage(params),
                wrapper
        );

        return new PageUtils(page);
    }

    @Transactional
    @Override
    public void addStock(Long skuId, Long wareId, Integer skuNum) {
        List<WareSkuEntity> wareSkuEntities = this.baseMapper.selectList(new QueryWrapper<WareSkuEntity>().eq("sku_id", skuId).eq("ware_id", wareId));
        if(wareSkuEntities.size() == 0 || wareSkuEntities == null ){
            WareSkuEntity wareSkuEntity = new WareSkuEntity();
            wareSkuEntity.setSkuId(skuId);
            wareSkuEntity.setWareId(wareId);
            wareSkuEntity.setStock(skuNum);
            wareSkuEntity.setStockLocked(0);
           try {
               R r =productFeignService.info(skuId);
               Map< String,Object> skuInfo = (Map< String,Object>) r.get("skuInfo");
               if(r.getCode()==0){
                   wareSkuEntity.setSkuName((String)skuInfo.get("skuName"));
               }
           }catch (Exception e){

           }
            this.baseMapper.insert(wareSkuEntity);
        }else {
            this.baseMapper.addStock(skuId,wareId,skuNum);
        }

    }

    @Override
    public List<SkuHasStockVo> getSkusHasStock(List<Long> skuIds) {
        List<SkuHasStockVo> collect = skuIds.stream().map(skuId -> {
            SkuHasStockVo skuHasStockVo = new SkuHasStockVo();
           Long count= this.baseMapper.getSkuStock(skuId);
            skuHasStockVo.setSkuId(skuId);
            skuHasStockVo.setHasStock(count==null?false:count>0);

            return skuHasStockVo;
        }).collect(Collectors.toList());
        return collect;
    }
    /**
     * 锁库存
     * 默认运行时异常都会回滚
     * @param vo
     * @return
     */
    //@Transactional(rollbackFor = NoStockException.class)
    @Override
    public Boolean orderLockStock(WareSkuLockVo vo) {
        WareOrderTaskEntity wareOrderTaskEntity = new WareOrderTaskEntity();
        wareOrderTaskEntity.setOrderSn(vo.getOrderSn());
        orderTaskService.save(wareOrderTaskEntity);

        List<OrderItemVo> locks = vo.getLocks();

        List<SkuWareHasStock> collect = locks.stream().map(item -> {
            SkuWareHasStock skuWareHasStock = new SkuWareHasStock();
            Long skuId = item.getSkuId();
            skuWareHasStock.setSkuId(skuId);
            skuWareHasStock.setNum(item.getCount());
            List<Long> wareIds=wareSkuDao.listWareHasStock(skuId);
            skuWareHasStock.setWareId(wareIds);
            return skuWareHasStock;
        }).collect(Collectors.toList());

        boolean allLock = true;
        for( SkuWareHasStock skuWareHasStock:collect){
            boolean skuLock =false ;
            Long skuId = skuWareHasStock.getSkuId();
            List<Long> wareIds = skuWareHasStock.getWareId();
            if(wareIds==null|| wareIds.size()==0){
                throw new NoStockException(skuId);

            }
            //1、如果每一个商品都锁定成功，将当前商品锁定了几件记录一下
            //2、如果某一个商品锁定失败，前面商品锁定成功过，也要回滚
            for(Long wareId:wareIds){
                Long cont =wareSkuDao.lockStock(skuId,wareId,skuWareHasStock.getNum());
                if(cont == 1){
                    //成功
                    skuLock= true;
                    WareOrderTaskDetailEntity entity = new WareOrderTaskDetailEntity(null,skuId,"",skuWareHasStock.getNum(),wareOrderTaskEntity.getId(),wareId,1);
                    orderTaskDetailService.save(entity);

                    StockLockedTo stockLockedTo = new StockLockedTo();
                    stockLockedTo.setId(wareOrderTaskEntity.getId());
                    StockDetailTo stockDetailTo = new StockDetailTo();
                    BeanUtils.copyProperties(entity,stockDetailTo);
                    stockLockedTo.setDetail(stockDetailTo);
                    rabbitTemplate.convertAndSend("stock-event-exchange","stock.locked",stockLockedTo);
                    break;
                }else{
                    //
                }
            }
            if( !skuLock){
                throw new NoStockException(skuId);
            }

        }
        return true;
    }

    @Override
    public void unlockStock(StockLockedTo to) {


        StockDetailTo detail = to.getDetail();
        Long id1 = detail.getId();
        WareOrderTaskDetailEntity byId = orderTaskDetailService.getById(id1);
        if(byId!=null){
            //解锁
            Long id2 = to.getId();
            WareOrderTaskEntity byId1 = orderTaskService.getById(id2);
            String orderSn = byId1.getOrderSn();
            R r = orderFeignService.getOrderByOrderSn(orderSn);
            if(r.getCode()==0){
                OrderVo data = r.getData(new TypeReference<OrderVo>() {});
                if(data ==null||data.getStatus()== 4){
                    if(byId.getLockStatus()==1){
                        log.info("解锁库存成功,{}",detail.getSkuNum());
                        unLockStock(detail.getSkuId(),detail.getWareId(),detail.getSkuNum(),detail.getId());
                    }

                }
            }else {
                //消息拒绝以后重新放回队列，让别人消费解锁
                throw new RuntimeException("远程服务失败");
            }
        }else {

        }
    }
    //防止订单服务卡顿，导致订单
    @Transactional
    @Override
    public void unlockStock(OrderTo to) {
        String orderSn = to.getOrderSn();
        //查询数据库关于这个订单的解锁信息
        WareOrderTaskEntity task = orderTaskService.getOrderTaskByOrderSn(orderSn);
        Long id = task.getId();
        //按照id查询出所有的订单详情
        List<WareOrderTaskDetailEntity> list = orderTaskDetailService.list(new QueryWrapper<WareOrderTaskDetailEntity>()
                .eq("task_id", id)
                .eq("lock_status", 1));
        for (WareOrderTaskDetailEntity entity : list) {
            unLockStock(entity.getSkuId(),entity.getWareId(),entity.getSkuNum(),entity.getId());
        }
    }

}