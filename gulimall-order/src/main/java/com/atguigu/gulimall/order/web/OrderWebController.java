package com.atguigu.gulimall.order.web;

import com.atguigu.common.exception.NoStockException;
import com.atguigu.gulimall.order.controller.OrderController;
import com.atguigu.gulimall.order.feign.MemberFeignService;
import com.atguigu.gulimall.order.service.OrderService;
import com.atguigu.gulimall.order.vo.OrderConfirmVo;
import com.atguigu.gulimall.order.vo.OrderSubmitVo;
import com.atguigu.gulimall.order.vo.SubmitOrderResponseVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class OrderWebController {
    @Autowired
    OrderService service;


    @GetMapping("/toTrade")
    public String toTrade( Model model)  {
        OrderConfirmVo orderConfirm =service.confirmOrder();
        model.addAttribute("orderConfirm",orderConfirm);
    return "confirm";
    }

    @PostMapping("/submit")
    public String submitOrder(OrderSubmitVo vo, Model model, RedirectAttributes redirectAttributes){
        SubmitOrderResponseVo submitOrder= null;
        String msg="下单失败；";
        try {
            submitOrder = service.submitOrder(vo);
            System.out.println("提交订单成功"+submitOrder);
            if(submitOrder.getCode()==0){
                model.addAttribute("submitOrder",submitOrder);
                return "pay";
            }else{

                switch (submitOrder.getCode()){

                    case 1:msg+="订单信息过期，请刷新页面后重试";
                        break;
                    case 2:msg+="订单商品价格发生变化，请刷新页面后重试";
                        break;

                }
            }
        } catch (NoStockException e) {
            msg+="没有库存；";
        }

            redirectAttributes.addFlashAttribute("msg",msg);
            return "redirect:http://order.gulimall.com/toTrade";
        }




}
