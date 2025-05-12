package com.atguigu.gulimall.cart.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.atguigu.common.utils.R;
import com.atguigu.gulimall.cart.feign.ProductFeignService;
import com.atguigu.gulimall.cart.interceptor.CartInterceptor;
import com.atguigu.gulimall.cart.service.CartService;
import com.atguigu.gulimall.cart.vo.Cart;
import com.atguigu.gulimall.cart.vo.CartItem;
import com.atguigu.gulimall.cart.vo.SkuInfoVo;
import com.atguigu.gulimall.cart.vo.UserInfoTo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Collectors;

@Service
@Slf4j
public class CartServiceImpl implements CartService {
    
    @Autowired
    StringRedisTemplate redisTemplate;
    @Autowired
    ProductFeignService productFeignService;
    @Autowired
    ThreadPoolExecutor executor;


    private final String CART_PREFIX = "gulimall:cart:";

    @Override
    public CartItem addToCart(Long skuId, Integer num) {
        // 1.先查询redis中购物车数据
        BoundHashOperations<String, Object, Object> cartOps = getCartOps();
        String o = (String)cartOps.get(skuId.toString());


            if(StringUtils.isEmpty(o)){
                CartItem cartItem = new CartItem();
                CompletableFuture<Void> voidCompletableFuture = CompletableFuture.runAsync(() -> {
                    R info = productFeignService.info(skuId);
                    SkuInfoVo skuInfo = info.getData("skuInfo", new TypeReference<SkuInfoVo>() {
                    });

                    cartItem.setCheck(true);
                    cartItem.setCount(num);
                    cartItem.setImage(skuInfo.getSkuDefaultImg());
                    cartItem.setTitle(skuInfo.getSkuTitle());
                    cartItem.setSkuId(skuId);
                    cartItem.setPrice(skuInfo.getPrice());
                },executor);

                    // 远程查询当前sku的组合信息
                CompletableFuture<Void> voidCompletableFuture2 = CompletableFuture.runAsync(() ->{
                    List<String> skuSaleAttrValues = productFeignService.getSkuSaleAttrValues(skuId);
                    cartItem.setSkuAttr(skuSaleAttrValues);
                },executor);

                try {
                    CompletableFuture.allOf(voidCompletableFuture, voidCompletableFuture2).get();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
                /// 添加到redis
                cartOps.put( skuId.toString(), JSON.toJSONString(cartItem));
                return cartItem;
            } else {
                CartItem  cartItem = JSON.parseObject(o, CartItem.class);
                cartItem.setCount(cartItem.getCount()+num);
                cartOps.put( skuId.toString(), JSON.toJSONString(cartItem));
                return cartItem;
        }





    }

    /**
     *
     * @param skuId
     * @return
     */
    @Override
    public CartItem getCartItem(Long skuId) {
        BoundHashOperations<String, Object, Object> cartOps = getCartOps();
        String o = (String)cartOps.get(skuId.toString());
        CartItem cartItem=null;
        if(!StringUtils.isEmpty(o)){
             cartItem = JSON.parseObject(o, CartItem.class);

        }
        return cartItem;
    }


    @Override
    public Cart getCart() {
        Cart cart = new Cart();
        UserInfoTo userInfoTo = CartInterceptor.threadLocal.get();
        if(userInfoTo.getUserId()!=null){
            String cartKey = CART_PREFIX+userInfoTo.getUserId();
           /* List<CartItem> cartItems = getCartItems(CART_PREFIX+userInfoTo.getUserKey());
            if(cartItems!=null){
                //合并购物车
                for (CartItem cartItem : cartItems) {
                   addToCart(cartItem.getSkuId(),cartItem.getCount());
                }
            }*/
            List<CartItem> cartItem = getCartItems(cartKey);
            cart.setItems(cartItem);
            return cart;
            //登录
        }else{
            return null;
        }
        /*else{
            //没有登录
            String cartKey = CART_PREFIX+userInfoTo.getUserKey();
            List<CartItem> cartItems = getCartItems(cartKey);
            cart.setItems(cartItems);
            }*/



    }

    // 选中状态
    @Override
    public void checkItem(Long skuId, Integer check) {
        BoundHashOperations<String, Object, Object> cartOps = getCartOps();
        CartItem cartItem = getCartItem(skuId);
        cartItem.setCheck(check==1?true:false);
        String jsonString = JSON.toJSONString(cartItem);
        cartOps.put(skuId.toString(),jsonString);
    }

    @Override
    public void checkItemCount(Long skuId, Integer num) {
        CartItem cartItem = getCartItem(skuId);
        cartItem.setCount(num);
        BoundHashOperations<String, Object, Object> cartOps = getCartOps();
        cartOps.put(skuId.toString(),JSON.toJSONString(cartItem));
    }

    private BoundHashOperations<String, Object, Object> getCartOps() {
        UserInfoTo userInfoTo = CartInterceptor.threadLocal.get();
        String cartKey = "";
        if(userInfoTo.getUserId()!=null){
            cartKey = CART_PREFIX+userInfoTo.getUserId();
        }else{
            cartKey = CART_PREFIX+userInfoTo.getUserKey();
        }
        // 获取到redis中购物车数据
        BoundHashOperations<String, Object, Object> operations = redisTemplate.boundHashOps(cartKey);
    return  operations;
    }

    // 获取到redis中购物车数据
    private List<CartItem> getCartItems(String cartKey) {
        BoundHashOperations<String, Object, Object> operations = redisTemplate.boundHashOps(cartKey);
        List<Object> values = operations.values();
        if(values!=null&&values.size()>0){
            List<CartItem> collect = values.stream().map(o -> {
                CartItem cartItem = JSON.parseObject(o.toString(), CartItem.class);
                return cartItem;
            }).collect(Collectors.toList());
            return collect;
        }
        return null;
    }
    public void deleteItem(Long skuId) {
        BoundHashOperations<String, Object, Object> cartOps = getCartOps();
        cartOps.delete(skuId.toString());
    }

    @Override
    public List<CartItem> getUserCartItems() {
        UserInfoTo userInfoTo = CartInterceptor.threadLocal.get();
        String cartKey = CART_PREFIX+userInfoTo.getUserId();
        if(userInfoTo.getUserId()==null) {
        return null;
        }else{
            List<CartItem> cartItems = getCartItems(cartKey);
            List<CartItem> collect = cartItems.stream()
             .filter(item -> item.getCheck())
                    .map(item -> {
                        //TODO 远程查询商品信息，得到价格，更新价格
                        R price = productFeignService.getPrice(item.getSkuId());
                        String string = (String) price.get("data");
                        item.setPrice(new BigDecimal(string));
                        return item;
                    }).collect(Collectors.toList());

         return collect;
        }

    }
}
