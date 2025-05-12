package com.atguigu.gulimall.product.web;

import com.atguigu.gulimall.product.app.CategoryController;
import com.atguigu.gulimall.product.entity.CategoryEntity;
import com.atguigu.gulimall.product.service.CategoryService;
import com.atguigu.gulimall.product.vo.Catelog2Vo;
import org.redisson.api.RLock;
import org.redisson.api.RReadWriteLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.Map;

@Controller
public class IndexController {
    @Autowired
    CategoryService categoryService;

    @Autowired
    RedissonClient redissonClient;
    @GetMapping({"/", "index.html"})
    public String getIndex(Model model) {
        //获取所有的一级分类
        List<CategoryEntity> category = categoryService.getLevel1Catagories();
        model.addAttribute("catagories", category);
        return "index";
    }

    @ResponseBody
    @GetMapping("/index/json/catalog.json")
    public Map<String, List<Catelog2Vo>> getCatalogJson() {
        Map<String, List<Catelog2Vo>> map = categoryService.getCatelogJson();

        return map;
    }






    @GetMapping("/read")
    @ResponseBody
    public String indexRead() {
        RReadWriteLock lock = redissonClient.getReadWriteLock("lock");
        lock.readLock().lock();
        try {
            Thread.sleep(20000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        lock.readLock().unlock();
        return "index";
    }

    @GetMapping("/write")
    @ResponseBody
    public String indexWrite() {
        RReadWriteLock lock = redissonClient.getReadWriteLock("lock");
        lock.writeLock().lock();
        try {

            Thread.sleep(20000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }finally {
            lock.writeLock().unlock();
        }

        return "index";
    }

    }
