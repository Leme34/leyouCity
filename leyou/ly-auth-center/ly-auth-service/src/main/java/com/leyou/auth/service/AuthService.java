package com.leyou.auth.service;

import com.leyou.auth.client.UserClient;
import com.leyou.auth.config.JwtProperties;
import com.leyou.auth.entiy.UserInfo;
import com.leyou.auth.utils.JwtUtils;
import com.leyou.user.pojo.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AuthService {
    @Autowired
    private JwtProperties jwtProperties;
    @Autowired
    private UserClient userClient;

    /**
     * 登录授权
     * @return  登录失败返回null，登录成功返回token
     */
    public String authentication(User user) {
        try {
            //调用验证用户名密码的服务
            User loginUser = userClient.login(user.getUsername(), user.getPassword());
            //登录失败返回null
            if (loginUser==null){
                return null;
            }
            //登录成功,生成token并返回
            String token = generateToken(loginUser.getId(),loginUser.getUsername());
            return token;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public String generateToken(Long id,String username) throws Exception {
        UserInfo userInfo=new UserInfo();
        userInfo.setId(id);
        userInfo.setUsername(username);
        return JwtUtils.generateToken(userInfo, jwtProperties.getPrivateKey(), jwtProperties.getExpire());
    }
}
