package com.leyou.controller;

import com.leyou.service.UserService;
import com.leyou.user.pojo.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
public class UserController {
    /**
     * 校验用户名和手机号
     */
    //GET /check/{data}/{type}
    @Autowired
    private UserService userService;
    @GetMapping("check/{data}/{type}")
    public ResponseEntity<Boolean> checkUser(@PathVariable("data")String data,@PathVariable("type") Integer type){
        Boolean result=userService.checkUser(data,type);
        if(result==null){
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);//400，参数异常
        }
        return ResponseEntity.ok(result);
    }

    /**
     * 发送短信验证码
     * @param phone
     * @return
     */
    @PostMapping("code")
    public ResponseEntity<Void> sendVerifyCode(@RequestParam("phone") String phone){
        Boolean result=this.userService.sendVerifyCode(phone);
        if(result==null||!result){
        return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return new ResponseEntity<>(HttpStatus.ACCEPTED);//202，请求被接受
    }
    /**
     * 保存注册信息
     * post /register
     */
    @PostMapping("register")
    public ResponseEntity<Void> register(@Valid User user, @RequestParam("code")String code){
            Boolean result=this.userService.register(user,code);
            if(result==null){
                return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
            }else if(!result){
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
            return new ResponseEntity<>(HttpStatus.CREATED);//201，创建成功
    }

    /**
     * 对用户名和密码进行验证
     * 登录操作
     * @param username
     * @param password
     * @return
     */
    @GetMapping("query")
    public ResponseEntity<User> login(@RequestParam("username")String username,@RequestParam("password")String password){
        User user=this.userService.login(username,password);
        if(user==null){
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);//400,表示用户名或密码错误
        }
        return ResponseEntity.ok(user);
    }
}
