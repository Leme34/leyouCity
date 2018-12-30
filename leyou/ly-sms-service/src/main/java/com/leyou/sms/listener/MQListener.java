package com.leyou.sms.listener;

import com.aliyuncs.dysmsapi.model.v20170525.SendSmsResponse;
import com.aliyuncs.exceptions.ClientException;
import com.leyou.config.SmsProperties;
import com.leyou.utils.SmsUtils;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class MQListener {
    @Autowired
    private SmsUtils smsUtils;
    @Autowired
    private SmsProperties prop;
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = "ly.sms.queue", durable = "true"),
            exchange = @Exchange(value = "ly.sms.exchange",
                    ignoreDeclarationExceptions = "true"),
            key = {"sms.verify.code"}))
    public void listenerSendMessage(Map<String, String> msg){
        if (msg == null || msg.isEmpty()) {
            return;
        }
        String phone = msg.get("phone");
        String code = msg.get("code");
        if (phone == null || code == null) {
            return;
        }
        SendSmsResponse resp = null;
        try {
            resp = smsUtils.sendSms(phone, code, prop.getSignName(), prop.getVerifyCodeTemplate());
            if (!"OK".equalsIgnoreCase(resp.getMessage())) {
                throw new RuntimeException("发送短信失败");
            }
        } catch (ClientException e) {
            e.printStackTrace();
        }

    }
}
