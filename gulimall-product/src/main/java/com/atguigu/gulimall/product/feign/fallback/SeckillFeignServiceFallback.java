package com.atguigu.gulimall.product.feign.fallback;

import com.atguigu.common.exception.BizCodeEnume;
import com.atguigu.common.utils.R;
import com.atguigu.gulimall.product.feign.SeckillFeignService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class SeckillFeignServiceFallback implements SeckillFeignService {
    @Override
    public R getSkuSeckillInfo(Long skuId) {
        log.info("熔断器工作了");
        return R.error(BizCodeEnume.TOO_MANY_REQUESTS.getCode(), BizCodeEnume.TOO_MANY_REQUESTS.getMsg());
    }
}
