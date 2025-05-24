package com.atguigu.gulimall.order.listener;

import com.alipay.api.AlipayApiException;
import com.alipay.api.domain.AlipayPassTemplateAddModel;
import com.alipay.api.internal.util.AlipaySignature;
import com.atguigu.gulimall.order.config.AlipayTemplate;
import com.atguigu.gulimall.order.service.OrderService;
import com.atguigu.gulimall.order.vo.PayAsyncVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.io.UnsupportedEncodingException;
import java.net.http.HttpRequest;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;


@RestController
public class OrderPayedListener {

    @Autowired
    AlipayTemplate  alipayTemplate;

    @Autowired
    OrderService orderService;

    @PostMapping("/payed/notify")
    public String handlePayResult(PayAsyncVo vo, HttpServletRequest request) throws UnsupportedEncodingException, AlipayApiException {
//        Map<String, String[]> parameterMap = req.getParameterMap();
//        System.out.println("收到支付结果通知"+parameterMap);
//        for (String s : parameterMap.keySet()){
//            String parameter = req.getParameter(s);
//
//            System.out.println(s+"--->"+parameter);
//
//        }
        //验签
        Map<String,String> params = new HashMap<String,String>();
        Map<String,String[]> requestParams = request.getParameterMap();
        for (Iterator<String> iter = requestParams.keySet().iterator(); iter.hasNext();) {
            String name = (String) iter.next();
            String[] values = (String[]) requestParams.get(name);
            String valueStr = "";
            for (int i = 0; i < values.length; i++) {
                valueStr = (i == values.length - 1) ? valueStr + values[i]
                        : valueStr + values[i] + ",";
            }
            //乱码解决，这段代码在出现乱码时使用
            valueStr = new String(valueStr.getBytes("ISO-8859-1"), "utf-8");
            params.put(name, valueStr);
        }

        boolean signVerified = AlipaySignature.rsaCheckV1(params, alipayTemplate.alipay_public_key, alipayTemplate.charset, alipayTemplate.sign_type);
        System.out.println("签名验证结果："+signVerified);
        if(signVerified){
                System.out.println("签名验证成功");
                String result=orderService.handlePayResult(vo);
                return result;
            }else {
                return "error";
            }

    }
}
