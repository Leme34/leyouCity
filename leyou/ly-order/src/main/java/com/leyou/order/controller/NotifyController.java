package com.leyou.order.controller;

import com.leyou.order.service.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * 微信支付成功后微信回调的接口
 * 微信支付文档要求：
 *      1、不能在url有参数,所以不使用get请求;
 *      2、对微信传入的xml数据完成一系列校验并返回规定的数据给微信
 */
@Slf4j
@RestController
@RequestMapping("notify")
public class NotifyController {

    @Autowired
    private OrderService orderService;

    /**
     * 微信支付成功后微信回调的自己后台的接口
     */
    @PostMapping(value = "pay",produces = "application/xml")    //返回规定的xml数据给微信
    public Map<String,String> success(@RequestBody Map<String,String> result){   //接受支付成功后微信返回的xml数据
        //处理回调 (完成对xml数据的校验)
        orderService.handleNotify(result);

        log.info("【微信支付回调】接收微信支付回调，结果：{}",result);

        //若处理回调成功,返回微信规定的数据
        Map<String,String> returnMsg = new HashMap<>();
        returnMsg.put("return_code","SUCCESS");
        returnMsg.put("return_msg","OK");
        return returnMsg;
    }

}
