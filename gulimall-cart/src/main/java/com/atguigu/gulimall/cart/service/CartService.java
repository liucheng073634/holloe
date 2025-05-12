package com.atguigu.gulimall.cart.service;

import com.atguigu.gulimall.cart.vo.Cart;
import com.atguigu.gulimall.cart.vo.CartItem;

import java.util.List;

public interface CartService {
    CartItem addToCart(Long skuId, Integer num);

    CartItem getCartItem(Long skuId);

    Cart getCart();

    /**
     * 修改购物车选中状态
     * @param skuId
     * @param check
     */
    void checkItem(Long skuId, Integer check);
    /**
     * 修改购物车商品数量
     * @param skuId
     * @param num
     */
    void checkItemCount(Long skuId, Integer num);

    /**
     * 删除购物车商品
     * @param skuId
     */
    void deleteItem(Long skuId);

    List<CartItem> getUserCartItems();
}
