package com.atguigu.gulimall.product.exception;

import com.atguigu.common.exception.BizCodeEnume;
import com.atguigu.common.utils.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

/*@ResponseBody
@ControllerAdvice(basePackages = "com.atguigu.gulimall.product.controller")*/

@Slf4j
@RestControllerAdvice(basePackages = "com.atguigu.gulimall.product.controller")
public class GulimallExceptionControllerAdvice {
    // 自定义异常
    @ExceptionHandler(value = MethodArgumentNotValidException.class)
    public R handleBrandException(MethodArgumentNotValidException exception ){
        // 1.获取到异常信息
        BindingResult bindingResult = exception.getBindingResult();
        Map<String, String> map=new HashMap<>();
        bindingResult.getFieldErrors().forEach((item)->{
               String message=item.getDefaultMessage();
                String field = item.getField();
                map.put(field,message);
            });
            return R.error(BizCodeEnume.VALID_EXCEPTION.getCode(),BizCodeEnume.VALID_EXCEPTION.getMsg()).put("data",map);

    }
    // 处理所有异常
    @ExceptionHandler(value = Throwable .class)
    public  R handleException(Throwable e){
        // 打印异常信息
        log.error("错误：{}",e);
        return R.error(BizCodeEnume.UNKNOW_EXCEPTION.getCode(),BizCodeEnume.UNKNOW_EXCEPTION.getMsg());
    }

}
