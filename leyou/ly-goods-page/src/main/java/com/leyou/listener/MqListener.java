package com.leyou.listener;

import com.leyou.service.FileService;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class MqListener {
    @Autowired
    private FileService fileService;

    @RabbitListener(bindings = @QueueBinding(   //绑定消息队列
            value = @Queue(value = "ly.create.page.queue", durable = "true"),  //绑定持久化的队列ly.create.page.queue
            exchange = @Exchange(value = "ly.item.exchange",   //接收此交换机中的消息
                    ignoreDeclarationExceptions = "true",
                    type = ExchangeTypes.TOPIC),     //topic类型的交换机
            key = {"item.insert", "item.update"}))   //指定匹配接收的路由key
    public void listenCreate(Long id) throws Exception {
        if (id == null) {
            return;
        }
        // 创建页面
        fileService.createHtml(id);
    }

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = "ly.create.page.queue", durable = "true"),
            exchange = @Exchange(value = "ly.item.exchange", ignoreDeclarationExceptions = "true", type = ExchangeTypes.TOPIC),
            key = "item.delete"))
    public void listenDelete(Long id){
        if (id == null) {
            return;
        }
        // 删除页面文件
        fileService.deleteHtml(id);
    }
}
