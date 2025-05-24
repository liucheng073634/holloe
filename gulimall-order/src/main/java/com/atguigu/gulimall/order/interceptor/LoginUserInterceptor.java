package com.atguigu.gulimall.order.interceptor;

import com.atguigu.common.constant.AuthServerConstant;
import com.atguigu.common.vo.MemberResVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.servlet.HandlerInterceptor;
import org.yaml.snakeyaml.tokens.AnchorToken;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Slf4j
@Component
public class LoginUserInterceptor implements HandlerInterceptor {
    public static ThreadLocal<MemberResVo> loginUser = new ThreadLocal<>();
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String uri = request.getRequestURI();
        AntPathMatcher antPathMatcher = new AntPathMatcher();
        boolean match= antPathMatcher.match("/order/order/status/**",uri);
        boolean match1= antPathMatcher.match("/payed/**",uri);
        if(match||match1){

            return true;
        }
        MemberResVo attribute = (MemberResVo)request.getSession().getAttribute(AuthServerConstant.LOGIN_USER);
        if(attribute!=null){
           log.info("用户已登录"+attribute);
            loginUser.set(attribute);
            return true;
        }else {
            request.getSession().setAttribute("msg","请先进行登录");
            response.sendRedirect("http://auth.gulimall.com/login.html");
            return false;
        }

    }

}
