package com.atguigu.gulimall.order.service.impl;

import com.alibaba.fastjson.TypeReference;
import com.alibaba.nacos.common.utils.StringUtils;
import com.alibaba.nacos.common.utils.UuidUtils;
import com.atguigu.common.constant.OrderStatusEnum;
import com.atguigu.common.exception.NoStockException;
import com.atguigu.common.utils.LuaUtils;
import com.atguigu.common.utils.R;
import com.atguigu.common.vo.MemberResVo;
import com.atguigu.gulimall.order.constant.OrderConstant;
import com.atguigu.gulimall.order.dao.OrderItemDao;
import com.atguigu.gulimall.order.entity.OrderItemEntity;
import com.atguigu.gulimall.order.feign.CartFeignService;
import com.atguigu.gulimall.order.feign.MemberFeignService;
import com.atguigu.gulimall.order.feign.ProductFeignService;
import com.atguigu.gulimall.order.feign.WmsFeignService;
import com.atguigu.gulimall.order.interceptor.LoginUserInterceptor;
import com.atguigu.gulimall.order.service.OrderItemService;
import com.atguigu.gulimall.order.vo.*;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;

import com.atguigu.gulimall.order.dao.OrderDao;
import com.atguigu.gulimall.order.entity.OrderEntity;
import com.atguigu.gulimall.order.service.OrderService;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;


@Slf4j
@Service("orderService")
public class OrderServiceImpl extends ServiceImpl<OrderDao, OrderEntity> implements OrderService {

    private ThreadLocal<OrderSubmitVo> orderConfirmVoThreadLocal = new ThreadLocal<>();

    @Autowired
    OrderItemService orderItemService;

    @Autowired
    OrderDao OrderDao;

    @Autowired
    MemberFeignService memberFeignService;

    @Autowired
    CartFeignService cartFeignService;

    @Autowired
    WmsFeignService wmsFeignService;

    @Autowired
    ThreadPoolExecutor executor;
    @Autowired
    StringRedisTemplate stringredisTemplate;
    @Autowired
    ProductFeignService productFeignService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<OrderEntity> page = this.page(
                new Query<OrderEntity>().getPage(params),
                new QueryWrapper<OrderEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public OrderConfirmVo confirmOrder() {

        OrderConfirmVo orderConfirmVo = new OrderConfirmVo();
        MemberResVo memberResVo = LoginUserInterceptor.loginUser.get();
        System.out.println("主线程"+Thread.currentThread().getId());
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();

        // 远程查询收货地址
        CompletableFuture<Void> memberFuture = CompletableFuture.runAsync(() -> {
            System.out.println("member"+Thread.currentThread().getId());
            RequestContextHolder.setRequestAttributes(requestAttributes);
            List<MemberAddressVo> address = memberFeignService.getAddress(memberResVo.getId());
            orderConfirmVo.setAddress(address);
        }, executor);
        // 获取购物车选中的购物项
        CompletableFuture<Void> cartFuture = CompletableFuture.runAsync(() -> {
            System.out.println("cart"+Thread.currentThread().getId());
            RequestContextHolder.setRequestAttributes(requestAttributes);
            List<OrderItemVo> currentUserCartItems = cartFeignService.getCurrentUserCartItems();
            orderConfirmVo.setItems(currentUserCartItems);
        },executor).thenRunAsync(() -> {
            RequestContextHolder.setRequestAttributes(requestAttributes);
            List<Long> collect = orderConfirmVo.getItems().stream().map(item -> item.getSkuId()).collect(Collectors.toList());
            R skusHasStock = wmsFeignService.getSkusHasStock(collect);
            List<SkuStockVo> data = skusHasStock.getData(new TypeReference<List<SkuStockVo>>() {
            });
            if(data!=null){
                Map<Long, Boolean> collect1 = data.stream().collect(Collectors.toMap(SkuStockVo::getSkuId, SkuStockVo::getHasStock));
                orderConfirmVo.setStocks(collect1);
            }
        }, executor);
        // 查询用户积分
        Integer integration = memberResVo.getIntegration();
        orderConfirmVo.setIntegration(integration);
        // 其他数据自动计算

        //防重令牌
        String replace = UuidUtils.generateUuid().toString().replace("-", "");

        stringredisTemplate.opsForValue().set(OrderConstant.USER_ORDER_TOKEN_PREFIX+memberResVo.getId(),replace,30, TimeUnit.MINUTES);
        orderConfirmVo.setOrderToken(replace);
        try {
            CompletableFuture.allOf( memberFuture, cartFuture).get();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }

        return orderConfirmVo;
    }
    @GlobalTransactional
    @Transactional
    @Override
    public SubmitOrderResponseVo submitOrder(OrderSubmitVo vo) {
        SubmitOrderResponseVo response = new SubmitOrderResponseVo();
        orderConfirmVoThreadLocal.set(vo);
        MemberResVo memberResVo = LoginUserInterceptor.loginUser.get();
        response.setCode(0);
        String lua;
        String orderToken = vo.getOrderToken();
        try {
            lua = LuaUtils.getLua("Token.lua");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        Long execute = stringredisTemplate.execute(new DefaultRedisScript<Long>(lua, Long.class),
                Arrays.asList(OrderConstant.USER_ORDER_TOKEN_PREFIX + memberResVo.getId()), orderToken);
        if (execute == 0L) {
            response.setCode(1);
            // 令牌不存在，或者令牌与redis中的不匹配
            return response;
        } else {
            // 生成订单
            OrderCreateTo orderCreateTo = orderCreateTo();
            BigDecimal payAmount = orderCreateTo.getOrder().getPayAmount();
            BigDecimal payPrice = vo.getPayPrice();
            //验价
            if (Math.abs(payAmount.subtract(payPrice).doubleValue()) < 0.01) {
                // 金额对比成功
                saveOrder(orderCreateTo);
                //锁定库存
                WareSkuLockVo wareSkuLockVo = new WareSkuLockVo();
                wareSkuLockVo.setOrderSn(orderCreateTo.getOrder().getOrderSn());
                List<OrderItemVo> collect = orderCreateTo.getOrderItems().stream().map(orderItem -> {
                    OrderItemVo orderItemVo = new OrderItemVo();
                    orderItemVo.setSkuId(orderItem.getSkuId());
                    orderItemVo.setCount(orderItem.getSkuQuantity());
                    orderItemVo.setTitle(orderItem.getSkuName());
                    return orderItemVo;
                }).collect(Collectors.toList());
                wareSkuLockVo.setLocks(collect);
                R r = wmsFeignService.orderLockStock(wareSkuLockVo);

                log.info("库存锁定结果：{}", r);
                if (r.getCode() == 0) {
                    response.setOrder(orderCreateTo.getOrder());
                    return response;
                } else {
                    throw new NoStockException();


                }

            } else {
                response.setCode(2);
                return response;
            }

        }

        /*
                String s = stringredisTemplate.opsForValue().get(OrderConstant.USER_ORDER_TOKEN_PREFIX + memberResVo.getId());
        if(orderToken!=null&&orderToken.equals(s)){
            //通过删除令牌，令牌只能使用一次
            stringredisTemplate.delete(OrderConstant.USER_ORDER_TOKEN_PREFIX+memberResVo.getId());
        }else{
           // 令牌不存在，或者令牌与redis中的不匹配

            return null;
        }*/

    }

    /**
     * @param orderCreateTo
     */
    private void saveOrder(OrderCreateTo orderCreateTo) {
        OrderEntity order = orderCreateTo.getOrder();
        order.setModifyTime(new Date());
        OrderDao.insert(order);
        this.save(order);
        List<OrderItemEntity> orderItems = orderCreateTo.getOrderItems();
        orderItemService.saveBatch(orderItems);
    }

    public OrderCreateTo orderCreateTo(){
        OrderCreateTo orderCreateTo = new OrderCreateTo();
        String timeId = IdWorker.getTimeId();
        OrderEntity extracted = extracted(timeId);
        List<OrderItemEntity> orderItemEntities = buildOrderItems(timeId);
        //获取到所有的订单项数据
        computePrice(extracted, orderItemEntities);
        orderCreateTo.setOrder(extracted);
        orderCreateTo.setOrderItems(orderItemEntities);
        return orderCreateTo;
    }

    //计算价格
    private void computePrice(OrderEntity extracted, List<OrderItemEntity> orderItemEntities) {
    BigDecimal total = new BigDecimal("0.0");
    BigDecimal coupon = new BigDecimal("0.0");
    BigDecimal integration = new BigDecimal("0.0");
    BigDecimal promotion= new BigDecimal("0.0");
        BigDecimal gift = new BigDecimal("0.0");
        BigDecimal Growth= new BigDecimal("0.0");
    //叠加所有订单项价格
    for (OrderItemEntity orderItemEntity : orderItemEntities){
        total = total.add(orderItemEntity.getRealAmount());
        coupon = coupon.add(orderItemEntity.getCouponAmount());
        integration = integration.add(orderItemEntity.getIntegrationAmount());
        promotion = promotion.add(orderItemEntity.getPromotionAmount());
         gift = gift.add(new BigDecimal(orderItemEntity.getGiftIntegration().toString()));
        Growth = Growth.add(new BigDecimal(orderItemEntity.getGiftGrowth().toString()));

    }
        //订单总价
        extracted.setTotalAmount(total);
        //应该支付金额
        extracted.setPayAmount(total.add(extracted.getFreightAmount()));
        extracted.setPromotionAmount(promotion);
        extracted.setIntegrationAmount(integration);
        extracted.setCouponAmount(coupon);
        //设置订单的相关状态信息
        extracted.setStatus(OrderStatusEnum.CREATED.getCode());
        extracted.setAutoConfirmDay(7);

    }

    private List<OrderItemEntity> buildOrderItems(String timeId) {
        //最后确认价格
        List<OrderItemEntity> collect=null;
        List<OrderItemVo> currentUserCartItems = cartFeignService.getCurrentUserCartItems();
        if( currentUserCartItems!=null&&currentUserCartItems.size()>0){
             collect = currentUserCartItems.stream().map(item -> {
                OrderItemEntity orderItemEntity = buildOrderItem(item);
                orderItemEntity.setOrderSn(timeId);
                return orderItemEntity;
            }).collect(Collectors.toList());

        }
        return collect;

    }

    private OrderItemEntity buildOrderItem(OrderItemVo item) {


        OrderItemEntity orderItemEntity = new OrderItemEntity();
        R skuInfo = productFeignService.getSkuInfo(item.getSkuId());
        SpuInfoVo data = skuInfo.getData(new TypeReference<SpuInfoVo>() {
        });
        //商品spu信息
        orderItemEntity.setSpuId(data.getId());
        orderItemEntity.setSpuName(data.getSpuName());
        orderItemEntity.setSpuBrand(data.getBrandId().toString());
        orderItemEntity.setCategoryId(data.getCatalogId());
        //商品sku信息
        orderItemEntity.setSkuId(item.getSkuId());
        orderItemEntity.setSkuName(item.getTitle());
        orderItemEntity.setSkuPic(item.getImage());
        orderItemEntity.setSkuPrice(item.getPrice());
        orderItemEntity.setSkuAttrsVals(StringUtils.join(item.getSkuAttr(), ";"));
        orderItemEntity.setSkuQuantity(item.getCount());

        //优惠信息
        //积分信息
        orderItemEntity.setGiftGrowth(item.getPrice().multiply(new BigDecimal(item.getCount().toString())).intValue());
        orderItemEntity.setGiftIntegration(item.getPrice().multiply(new BigDecimal(item.getCount().toString())).intValue());
        //订单项信息
        orderItemEntity.setPromotionAmount(new BigDecimal("0"));
        orderItemEntity.setCouponAmount(new BigDecimal("0"));
        orderItemEntity.setIntegrationAmount(new BigDecimal("0"));

        //订单实际金额
        BigDecimal multiply = orderItemEntity.getSkuPrice().multiply(new BigDecimal(orderItemEntity.getSkuQuantity().toString()));
        multiply.subtract(orderItemEntity.getCouponAmount())
                .subtract(orderItemEntity.getPromotionAmount())
                .subtract(orderItemEntity.getIntegrationAmount());
        orderItemEntity.setRealAmount(multiply);
    return orderItemEntity;
    }

    private OrderEntity extracted(String timeId) {
        MemberResVo memberResVo = LoginUserInterceptor.loginUser.get();
        OrderEntity orderEntity = new OrderEntity();
        orderEntity.setOrderSn(timeId);
        orderEntity.setMemberId(memberResVo.getId());
        OrderSubmitVo orderSubmitVo = orderConfirmVoThreadLocal.get();
        R fare = wmsFeignService.getFare(orderSubmitVo.getAddrId());
        FareVo fareVo = fare.getData(new TypeReference<FareVo>() {
        });
        //运费金额
        orderEntity.setFreightAmount(fareVo.getFare());
        //设置收货信息
        orderEntity.setReceiverCity(fareVo.getAddress().getCity());
        orderEntity.setReceiverDetailAddress(fareVo.getAddress().getDetailAddress());
        orderEntity.setReceiverName(fareVo.getAddress().getName());
        orderEntity.setReceiverPhone(fareVo.getAddress().getPhone());
        orderEntity.setReceiverPostCode(fareVo.getAddress().getPostCode());
        orderEntity.setReceiverProvince(fareVo.getAddress().getProvince());
        orderEntity.setReceiverRegion(fareVo.getAddress().getRegion());
        orderEntity.setDeleteStatus(0);
        return orderEntity;
    }


}