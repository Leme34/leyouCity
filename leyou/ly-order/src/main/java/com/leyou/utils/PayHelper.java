package com.leyou.utils;

import com.github.wxpay.sdk.WXPay;
import com.github.wxpay.sdk.WXPayConstants;
import com.github.wxpay.sdk.WXPayUtil;
import com.leyou.config.PayConfig;
import com.leyou.config.PayProperties;
import com.leyou.order.mapper.OrderMapper;
import com.leyou.order.mapper.OrderStatusMapper;
import com.leyou.order.pojo.Order;
import com.leyou.order.pojo.OrderStatus;
import com.leyou.order.service.OrderService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;


@Component
public class PayHelper {
    @Autowired
    private PayProperties payProperties;
    @Autowired
    private OrderMapper orderMapper;
    private static final Logger logger = LoggerFactory.getLogger(PayHelper.class);
    @Autowired
    private StringRedisTemplate redisTemplate;
    @Autowired
    private OrderService orderService;
    @Autowired
    private OrderStatusMapper statusMapper;

    private WXPay wxPay;

    public PayHelper(PayConfig payConfig) {
        // 真实开发时
        wxPay = new WXPay(payConfig);
        // 测试时
        // wxPay = new WXPay(payConfig, WXPayConstants.SignType.MD5, true);
    }

    /**
     * 向微信发起下单请求，取得返回的二维码付款地址
     * 将二维码付款地址缓存到redis，时间为10分钟
     */
    public String createPayUrl(Long orderId, Long totalPay, String desc) {
        String key = "ly.pay.url." + orderId;
        try {
            String url = redisTemplate.opsForValue().get(key);
            if (StringUtils.isNotBlank(url)) {
                return url;
            }
        } catch (Exception e) {
            logger.error("查询缓存付款链接异常,订单编号：{}", orderId, e);
        }

        try {
            Map<String, String> data = new HashMap<>();
            // 商品描述
            data.put("body", desc);
            // 订单号
            data.put("out_trade_no", orderId.toString());
            //货币
            data.put("fee_type", "CNY");
            //金额，单位是分
            data.put("total_fee", totalPay.toString());
            //调用微信支付的终端IP（estore商城的IP）
            data.put("spbill_create_ip", "127.0.0.1");
            //回调地址
            data.put("notify_url", payProperties.getNotifyUrl());
            // 交易类型为扫码支付
            data.put("trade_type", "NATIVE");
            //商品id,使用假数据
            data.put("product_id", "1234567");

            //向微信请求下单,返回二维码付款地址
            Map<String, String> result = wxPay.unifiedOrder(data);

            //根据通信、业务标识判断微信下单是否成功,若不成功则抛异常
            isSuccess(orderId, result);

            //微信下单成功,取得二维码付款地址
            String url = result.get("code_url");
            // 将二维码付款地址缓存，时间为10分钟
            try {
                redisTemplate.opsForValue().set(key, url, 10, TimeUnit.MINUTES);
            } catch (Exception e) {
                logger.error("缓存付款链接异常,订单编号：{}", orderId, e);
            }
            return url;
        } catch (Exception e) {
            logger.error("创建预交易订单异常", e);
            return null;
        }
    }

    /**
     * 根据通信、业务标识判断微信下单是否成功,若不成功则抛异常
     */
    public void isSuccess(Long orderId, Map<String, String> result) {
        //判断通信标识
        String resultCode = result.get("result_code");
        if (WXPayConstants.FAIL.equals(result.get("return_code"))) {  //通信失败
            logger.error("【微信下单】付款链接异常,订单编号：{},失败原因：{}",
                    orderId, result.get("result_msg"));
            //TODO 抛出异常
        }

        //判断业务标识
        if (WXPayConstants.FAIL.equals(result.get("return_code"))) {  //业务失败
            logger.error("【微信下单】创建预交易订单失败，错误码：{},失败原因：{}",
                    result.get("err_code"), result.get("err_code_des"));
            //TODO 抛出异常
        }
    }


    /**
     * 查询订单状态
     *
     * @param orderId
     * @return
     */
    public PayState queryOrder(Long orderId) {
        Map<String, String> data = new HashMap<>();
        // 订单号
        data.put("out_trade_no", orderId.toString());
        try {
            //请求微信查询是否付款成功
            Map<String, String> result = wxPay.orderQuery(data);

            //校验微信返回的数据
            //1、检验通信和业务标识
            isSuccess(orderId, result);
            //2、校验签名
            validSign(result);
            //3、校验应付金额
            String totalFeeStr = result.get("total_fee");
            if (StringUtils.isBlank(totalFeeStr)) {
                //TODO 抛出解析数据失败的异常
            }
            Long totalFee = Long.parseLong(totalFeeStr); //应付金额
            Order order = orderMapper.selectByPrimaryKey(orderId);
            if (totalFee != order.getActualPay()) {  //应付金额不符
                //TODO 抛出应付金额不符异常
            }

            //4、校验支付状态
            String state = result.get("trade_state");
            if (WXPayConstants.SUCCESS.equals(state)) {  //支付成功
                //修改订单状态
                OrderStatus os = new OrderStatus();
                os.setStatus(2);
                os.setOrderId(orderId);
                os.setPaymentTime(new Date());
                int effectedRow = statusMapper.updateByPrimaryKeySelective(os);
                //修改订单状态失败
                if (effectedRow != 1) {
                    //TODO 抛出修改订单状态失败异常
                }
                //返回成功
                return PayState.SUCCESS;
            }

            if ("NOTPAY".equals(state) || "USERPAYING".equals(state)) {
                return PayState.NOT_PAY;
            }else {
                // 其它状态认为是付款失败
                return PayState.FAIL;
            }
        } catch (Exception e) {
            logger.error("查询订单状态异常", e);
            return PayState.NOT_PAY;
        }
    }

    /**
     * 校验签名
     */
    public void validSign(Map<String, String> result) {
        //重新生成签名，再与传过来的签名比较
        try {
            //取出微信传过来的签名
            String sign = result.get("sign");
            //因为不知道微信使用哪种加密,所以2种都生成来尝试匹配
            String sign1 = WXPayUtil.generateSignature(result, payProperties.getKey(), WXPayConstants.SignType.MD5);
            String sign2 = WXPayUtil.generateSignature(result, payProperties.getKey(), WXPayConstants.SignType.HMACSHA256);

            //若2种都不匹配,则是伪造信息,抛出异常
            if (!StringUtils.equals(sign, sign1) && !StringUtils.equals(sign, sign2)) {
                //TODO 抛异常
            }

        } catch (Exception e) {
            //TODO 解析签名失败,抛异常
        }
    }
}
