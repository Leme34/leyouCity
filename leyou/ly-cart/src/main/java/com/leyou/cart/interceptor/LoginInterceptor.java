package com.leyou.cart.interceptor;

import com.leyou.auth.entiy.UserInfo;
import com.leyou.auth.utils.JwtUtils;
import com.leyou.cart.config.JwtProperties;
import com.leyou.common.utils.CookieUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.http.HttpStatus;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 解析token
 */
public class LoginInterceptor extends HandlerInterceptorAdapter {
    private JwtProperties jwtProperties;
    //存放登录用户信息的请求线程共享容器,放行的请求可以在Controller层取得用户信息
    private static ThreadLocal<UserInfo> tl=new ThreadLocal();

    //构造方法：用于传入jwtProperties的内容
    public LoginInterceptor(JwtProperties jwtProperties){
        this.jwtProperties=jwtProperties;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler){
        //判断用户是否登录
        try {
            //没有异常，表示登录成功，则将当前用户信息存入ThreadLocal对象中
            String token = CookieUtils.getCookieValue(request, jwtProperties.getCookieName());
            UserInfo info = JwtUtils.getInfoFromToken(token, jwtProperties.getPublicKey());
            tl.set(info);
            //放行
            return true;
        } catch (Exception e) {
            //出现异常(没有登录)，返回状态码，并进行拦截
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            e.printStackTrace();
            return false;
        }

    }


    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        /**
         * 请求线程结束时清空线程共享容器
         */
        tl.remove();
    }

    /**
     * 提供给其他层获取线程容器内容的方法
     */
    public static UserInfo getLoginUser() {
        return tl.get();
    }

}
