package com.atguigu.gulimall.seckill.interceptor;

import com.atguigu.common.constant.AuthServerConstant;
import com.atguigu.common.vo.MemberResVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

@Slf4j
@Component
public class LoginUserInterceptor implements HandlerInterceptor {
    public static ThreadLocal<MemberResVo> loginUser = new ThreadLocal<>();
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        AntPathMatcher antPathMatcher = new AntPathMatcher();
        boolean match = antPathMatcher.match("/kill", request.getRequestURI());
        boolean match2 = antPathMatcher.match("/seckill", request.getRequestURI());
        if (match||match2) {
            HttpSession session = request.getSession();
            MemberResVo attribute = (MemberResVo) session.getAttribute(AuthServerConstant.LOGIN_USER);
            if (attribute != null) {
                //说明登录了 放行
                loginUser.set(attribute);
                return true;
            } else {
                //没登录 去登录页 来拦截
                session.setAttribute("msg", "请先登录～～");
                response.sendRedirect("http://auth.gulimall.com/login.html");
                return false;
            }
        } else {
            return true;
        }
    }

}
