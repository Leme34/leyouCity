package com.leyou.auth.controller;

import com.leyou.auth.config.JwtProperties;
import com.leyou.auth.entiy.UserInfo;
import com.leyou.auth.service.AuthService;
import com.leyou.auth.utils.JwtUtils;
import com.leyou.common.utils.CookieUtils;
import com.leyou.user.pojo.User;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@RestController
@EnableConfigurationProperties(JwtProperties.class)
public class AuthController {
    @Autowired
    private AuthService authService;
    @Autowired
    private JwtProperties jwtProperties;
    private Logger logger= LoggerFactory.getLogger(AuthController.class);

    /**
     * 登录表单提交的登录认证请求
     */
    @PostMapping("accredit")
    public ResponseEntity<Void> authentication(User user, HttpServletRequest request, HttpServletResponse response) {
        //登录操作返回token
        String token = authService.authentication(user);
        //若登录失败
        if (StringUtils.isBlank(token)) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);  //401
        }
        //登录成功，把token写入cookie保存在客户端,以后每次请求都由cookie携带过来
        CookieUtils.setCookie(request, response, jwtProperties.getCookieName(),
                token, jwtProperties.getCookieMaxAge(), null, true);  //指定httpOnly防止通过js获取和修改
        return ResponseEntity.ok().build();
    }

    /**
     * 验证是否已登录,已登录则刷新token
     */
    @GetMapping("verify")
    public ResponseEntity<UserInfo> verify(HttpServletRequest request, HttpServletResponse response) {
        try {
            //从cookie取出token,并从中取得JWT中的用户数据
            String token = CookieUtils.getCookieValue(request, jwtProperties.getCookieName());
            UserInfo userInfo = JwtUtils.getInfoFromToken(token, jwtProperties.getPublicKey());
            //重新生成token(刷新过期时间),由cookie带回客户端
            String newToken = JwtUtils.generateToken(userInfo, jwtProperties.getPrivateKey(), jwtProperties.getExpire());
            CookieUtils.setCookie(request, response, jwtProperties.getCookieName(), newToken,
                    jwtProperties.getCookieMaxAge(), null, true);  //指定httpOnly防止通过js获取和修改
            return ResponseEntity.ok(userInfo);
        } catch (Exception e) {
           logger.error("鉴权失败，用户未登录:"+request.getRemoteUser());
        }
        return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
    }

    /**
     * 移除用户权限
     * 即退出登录状态
     */
    @PutMapping()
    public ResponseEntity<Void> removeAuthentication(HttpServletRequest request,HttpServletResponse response){
        String token = CookieUtils.getCookieValue(request, jwtProperties.getCookieName());
        CookieUtils.setCookie(request,response,jwtProperties.getCookieName(),token,0,null,true);
        return ResponseEntity.ok().build();
    }
}
