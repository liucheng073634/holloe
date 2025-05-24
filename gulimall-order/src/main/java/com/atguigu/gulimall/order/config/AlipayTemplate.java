package com.atguigu.gulimall.order.config;

import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.request.AlipayTradeCloseRequest;
import com.alipay.api.request.AlipayTradePagePayRequest;

import com.atguigu.gulimall.order.vo.PayVo;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.stringtemplate.v4.ST;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.FileWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

/* *
 *类名：AlipayConfig
 *功能：基础配置类
 *详细：设置帐户有关信息及返回路径
 *修改日期：2017-04-05
 *说明：
 *以下代码只是为了方便商户测试而提供的样例代码，商户可以根据自己网站的需要，按照技术文档编写,并非一定要使用该代码。
 *该代码仅供学习和研究支付宝接口使用，只是提供一个参考。
 */

@ConfigurationProperties(prefix = "alipay")
@Component
@Data
public class AlipayTemplate {

//↓↓↓↓↓↓↓↓↓↓请在这里配置您的基本信息↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓

    // 应用ID,您的APPID，收款账号既是您的APPID对应支付宝账号
    public static String app_id = "2021000148694189";

    // 商户私钥，您的PKCS8格式RSA2私钥
    public static String merchant_private_key = "MIIEvAIBADANBgkqhkiG9w0BAQEFAASCBKYwggSiAgEAAoIBAQDEEfoc9YXxUIJ8vYJtnmTuOHYDxL6mdFnyhoR1eva2HOETQMMh4cqxa+U6/Mu+ecNCEV6Ey78xAvPgeFvoXO7Ar56yDcFva1P1dGUNU6Hz+Q4YAkpMuWS1iaQ/IJlaIhWoqmDyxqwcKp6+oX87r30cnQrdPZ7Iy41Hp9TWMFlkHqxSIgIlM4XE6gIMjizbCuKxoqkqRsiB+H5iwP21klt0Tg9r0XgOB9gYvXphoLQc4yv1Kxyn4Fu/XYfgEoC5hW5My8TbdS9vt8ywDYJ8F751Vo8LLR2PpDBCjeW+3+TUont3SqZEbkbNux+BmKCv4ygoOyN9x3ZIMI5sw56uIMjRAgMBAAECggEAEkO+cd4pSQ5/6VDaCo4bxtk5nSZBwE3MZzxCsJLw0owkJ1/8DXb0I8zyQKl17Vt4ZfoqPOKh/9XPlBSCM0Yc85Qoi1uerltoKdBa7X/h8VE9D91+wGDw0bxtsXe+8VeFfv3IJDV/qOwLd6ShrpStafHzJiLsXLYhhhBLUlyUpy/OVFTL1rEjVzhel7ptNbYzKCBDIFQ0czSck4vf5khM48oFOUIGJhaf3xwjUcXNGEloPwoQbme/wO5J2SykPb/L4K5T+0oy2FEEzKXrRT+moFIW73UzuitVCFX1C9JP4D18BbrAovfefoaPGReJoi9Mz3kXb3WwRWSe215em4G4FQKBgQD4jUbuonPuNi7IvQ3IYvcl5lRmgwNtEVPNRyxXNsDxm02T/9B6Iek5P3upTm092S0z3vORAl4WBwa8OZXy/RwRhJpwyXW9w6AW4X0OZGLqJANd/O1KCigcnUbVLqGnH3QeOBQT/KWcTxOS8UQqusd4xCNjVcpdZxRTBBI//6gAIwKBgQDJ8hiVplmxn++bGAYI9z4C5GfppafhJYn9YH/VPnFWjgecM6LAca9qkc2aUIB0ywyJviUeA6626QV0fpZ+Ht5JblJtdKiC+Anoz6qUwFJVmQrDS2GzQm57ZsGTkigyclM2UwPRO7pY9fOBVyYYNQ76y7aDHu0MVobbV112lqPoewKBgA3+lmHVT8W300aGLoJ74AKIR3RGKCk58of6W+rFMHPqFKIiozBdPG52vpW3zrrOwZbMFNazVWzUFHMAowq9aZkq6C81iKWYoCjppUrsKA2a3X2rQgkLLVr/F//k0kFAqh8RdT/y17hHU4jQgNAPGDAaiwuKxwwfQ6uw7P4hfOoVAoGAI0tanJ0MRA0N1W9nbp23is3ck2tOYf0rlssMzHozWMtN7TDhGv1DiT6eToAPU1pBgLlox0whdvpW57tQFg7TDsZnRZYD9Y/gO34/8+uA+FDdHrDIDIppwMt+vYnn2Jk+jdoIasMjmHSz8EEI+WYcjhyYc2YuS9+7hg5I2XcOgo0CgYBNaEpFTYo1foEiiSB5QaYsN3W0bqzboyhrHJQlbg1nlXw5WChjPK4H5n4iIEh6nUkukjjM37vV3WU5zPHdFR0KI5vre1094f+g58FbJsMJPB8KZgaAGdrbPrU95lWuARJYKo0/6/KBGw2g8rLF16orKeXUbLQBdliM8as1iZwcDg==";

    // 支付宝公钥,查看地址：https://openhome.alipay.com/platform/keyManage.htm 对应APPID下的支付宝公钥。
    public static String alipay_public_key = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAqtyPOpESGKT6+md0zlvXG0GSOwQRam7mDgrWLyRAcJkG+NI0dMkaFiw3S77s2lnXeJls3QLheRZ9sM5ikhAUDI0F44AC5DZ+156W/X2kaT0e9IiwAg2FfLw08QDK/dPur1I+jk7oucCgvrt6dqWc8KxmZ1IU9X0T/9u29/ZHE0nW8PMciDD/RthtP7dkS5MM4emQxtT5F57UtxjEx1zSaOsX5PHLQQcqWlnRua6dTsS0loaGbjvgtcOE4pFtWlO/3p2WbAx+u76dLNxIwMqYu+gj3IvCk5nTHKNfLiZLt204AnZLZH9fL4ahdNTVxrm/KSGEwIbd/gFjqbGsGJ2gswIDAQAB";

    // 服务器异步通知页面路径  需http://格式的完整路径，不能加?id=123这类自定义参数，必须外网可以正常访问
    public static String notify_url = " http://e43dc68e.natappfree.cc/payed/notify";

    // 页面跳转同步通知页面路径 需http://格式的完整路径，不能加?id=123这类自定义参数，必须外网可以正常访问
    public static String return_url = "http://member.gulimall.com/memberOrder.html";

    // 签名方式
    public static String sign_type = "RSA2";

    // 字符编码格式
    public static String charset = "utf-8";

    //超时时间
    public static String timeout = "30m";

    // 支付宝网关
    public static String gatewayUrl = "https://openapi-sandbox.dl.alipaydev.com/gateway.do";

    // 日志目录
    public static String log_path = "./";

    public  void showPay(HttpServletRequest request, HttpServletResponse response, PayVo vo) throws Exception {
        //获得初始化的AlipayClient
        AlipayClient alipayClient = new DefaultAlipayClient(AlipayTemplate.gatewayUrl, AlipayTemplate.app_id, AlipayTemplate.merchant_private_key, "json", AlipayTemplate.charset, AlipayTemplate.alipay_public_key, AlipayTemplate.sign_type);

        //设置请求参数
        AlipayTradePagePayRequest alipayRequest = new AlipayTradePagePayRequest();
        alipayRequest.setReturnUrl(AlipayTemplate.return_url);
        alipayRequest.setNotifyUrl(AlipayTemplate.notify_url);

        //商户订单号，商户网站订单系统中唯一订单号，必填
        String out_trade_no = new String(request.getParameter("WIDout_trade_no").getBytes("ISO-8859-1"), "UTF-8");
        //付款金额，必填
        String total_amount = new String(request.getParameter("WIDtotal_amount").getBytes("ISO-8859-1"), "UTF-8");
        //订单名称，必填

        String subject = new String(request.getParameter("WIDsubject").getBytes("ISO-8859-1"), "GBK");
        System.out.println(subject);
        //商品描述，可空
        String body = new String(request.getParameter("WIDbody").getBytes("ISO-8859-1"), "GBK");
        System.out.println(body);
        alipayRequest.setBizContent("{\"out_trade_no\":\"" + out_trade_no + "\","
                + "\"total_amount\":\"" + total_amount + "\","
                + "\"subject\":\"" + subject + "\","
                + "\"body\":\"" + body + "\","
                + "\"timeout_express\":\"" + timeout + "\","
                + "\"product_code\":\"FAST_INSTANT_TRADE_PAY\"}");


        //请求参数可查阅【电脑网站支付的API文档-alipay.trade.page.pay-请求参数】章节

        //构建支付宝官方支付页面
        String top = "<!DOCTYPE html PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\" \"http://www.w3.org/TR/html4/loose.dtd\">\n" +
                "<html>\n" +
                "<head>\n" +
                "<meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\">\n" +
                "<title>付款</title>\n" +
                "</head>";
        String result = alipayClient.pageExecute(alipayRequest).getBody();
        String bottom = "<body>\n" +
                "</body>\n" +
                "</html>";
        //输出
        response.getWriter().println(top + result + bottom);
    }

//收单
    public String verify(HttpServletRequest request) throws UnsupportedEncodingException, AlipayApiException {
        //获得初始化的AlipayClient
        AlipayClient alipayClient = new DefaultAlipayClient(AlipayTemplate.gatewayUrl, AlipayTemplate.app_id, AlipayTemplate.merchant_private_key, "json", AlipayTemplate.charset, AlipayTemplate.alipay_public_key, AlipayTemplate.sign_type);

        //设置请求参数
        AlipayTradeCloseRequest alipayRequest = new AlipayTradeCloseRequest();
        //商户订单号，商户网站订单系统中唯一订单号
        String out_trade_no = new String(request.getParameter("WIDTCout_trade_no").getBytes("ISO-8859-1"),"UTF-8");
        //支付宝交易号
        String trade_no = new String(request.getParameter("WIDTCtrade_no").getBytes("ISO-8859-1"),"UTF-8");
        //请二选一设置

        alipayRequest.setBizContent("{\"out_trade_no\":\""+ out_trade_no +"\"," +"\"trade_no\":\""+ trade_no +"\"}");

        //请求
        String result = alipayClient.execute(alipayRequest).getBody();

        return result;
    }

//↑↑↑↑↑↑↑↑↑↑请在这里配置您的基本信息↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑

    /**
     * 写日志，方便测试（看网站需求，也可以改成把记录存入数据库）
     * @param sWord xx要写入日志里的文本内容
     */
    public static void logResult(String sWord) {
        FileWriter writer = null;
        try {
            writer = new FileWriter(log_path + "alipay_log_" + System.currentTimeMillis()+".txt");
            writer.write(sWord);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (writer != null) {
                try {
                    writer.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}

