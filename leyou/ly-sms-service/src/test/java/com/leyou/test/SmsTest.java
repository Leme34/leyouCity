package com.leyou.test;

import com.aliyuncs.dysmsapi.model.v20170525.SendSmsResponse;
import com.aliyuncs.exceptions.ClientException;
import com.leyou.SMSApplication;
import com.leyou.config.SmsProperties;
import com.leyou.utils.SmsUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = SMSApplication.class)
public class SmsTest {
    @Autowired
    private SmsUtils smsUtils;
    @Autowired
    private SmsProperties prop;
    @Test
    public void testsms() throws ClientException {
        SendSmsResponse sendSmsResponse = smsUtils.sendSms("17737773237", "12345", prop.getSignName(), prop.getVerifyCodeTemplate());
        System.out.println(sendSmsResponse.getMessage());
    }
}
