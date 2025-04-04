package com.atguigu.gulimall.cart.controller;

import com.atguigu.common.constant.AuthServerConstant;
import com.atguigu.gulimall.cart.interceptor.CartInterceptor;
import com.atguigu.gulimall.cart.service.CartService;
import com.atguigu.gulimall.cart.vo.Cart;
import com.atguigu.gulimall.cart.vo.CartItem;
import com.atguigu.gulimall.cart.vo.UserInfoTo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpSession;

@Controller
public class CartController {
    @Autowired
    private CartService cartService;

    @GetMapping("/cart.html")
    public String cartListPage(HttpSession session,Model model){
        UserInfoTo userInfoTo = CartInterceptor.threadLocal.get();
        Cart cart=cartService.getCart();
        if(cart == null){
            return "redirect:http://auth.gulimall.com/login.html";
        }
        model.addAttribute("cart",cart);
        return "cartList";
    }

    /**
     * RedirectAttributes 封装了重定向携带的数据
     * @param skuId
     * @param num
     * @param re
     * @return
     */
    @GetMapping("/addToCart")
    public String addToCart(@RequestParam("skuId") Long skuId,
                            @RequestParam("num") Integer num ,
                            RedirectAttributes re){
        UserInfoTo userInfoTo = CartInterceptor.threadLocal.get();
            if(userInfoTo.getUserId()!=null){
                cartService.addToCart(skuId,num);
                re.addAttribute("skuId",skuId);
                return "redirect:http://cart.gulimall.com/addToCartSuccess.html";
            }else {
                return "redirect:http://auth.gulimall.com/login.html";
            }

    }
    @GetMapping("/addToCartSuccess.html")
    public String addToCartSuccessPage(@RequestParam("skuId") Long skuId, Model model){
        CartItem cartItem = cartService.getCartItem(skuId);
        model.addAttribute("item",cartItem);
        return "success";
    }

    @GetMapping("/checkItem")
    public String checkItem(@RequestParam("skuId") Long skuId,@RequestParam("check") Integer check){
        cartService.checkItem(skuId,check);
        return "redirect:http://cart.gulimall.com/cart.html";
    }

}
