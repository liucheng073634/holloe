package com.atguigu.gulimall.seckill.controller;


import com.atguigu.common.utils.R;
import com.atguigu.gulimall.seckill.service.SeckillService;
import com.atguigu.gulimall.seckill.to.SecKillSkuRedisTo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
public class SeckillController {

    @Autowired
    SeckillService seckillService;

    @ResponseBody
    @GetMapping("/currentSeckillSkus")
    public R getCurrentSeckillSkus() {
        List<SecKillSkuRedisTo> currentSeckillSkus =seckillService.getCurrentSeckillSkus();
        return R.ok().setData(currentSeckillSkus);
    }
    @ResponseBody
    @GetMapping("/sku/seckill/{skuId}")
    public R getSkuSeckillInfo(@PathVariable("skuId") Long skuId) throws InterruptedException {

        SecKillSkuRedisTo to = seckillService.getSkuSeckillInfo(skuId);
        return R.ok().setData(to);
    }

    //http://seckill.gulimall.com/seckill?killId=undefined_11&key=undefined&num=1

    @GetMapping("/seckill")
    public String seckill(@RequestParam("killId") String killId,
                     @RequestParam("key") String key,
                     @RequestParam("num") Integer num,
                          Model model) {
        String orderSn = seckillService.kill(killId, key, num);
        model.addAttribute("orderSn",orderSn);
        return "success";
    }

}
