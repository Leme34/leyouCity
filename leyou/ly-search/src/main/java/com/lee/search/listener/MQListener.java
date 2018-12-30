package com.lee.search.listener;

import com.lee.search.Repository.GoodsRepository;
import com.lee.search.client.GoodsClient;
import com.lee.search.service.IndexService;
import com.leyou.item.bo.SpuBo;
import com.leyou.item.pojo.Spu;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class MQListener {
    @Autowired
    private IndexService indexService;
    @Autowired
    private GoodsClient goodsClient;
    @Autowired
    private GoodsRepository goodsRepository;

    /**
     * 监听商品的新增和修改,从队列中拿到商品服务传来的消息,进行索引的修改
     * 若抛出异常spring会回滚事务，而且消息不会丢失
     */
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = "ly-search-queue", durable = "true"),
            exchange = @Exchange(value = "ly.item.exchange",
                    ignoreDeclarationExceptions = "true",
                    type = ExchangeTypes.TOPIC),
            key = {"item.insert", "item.update"}))
    public void listenCreate(Long id) {
        //消息为null
        if (id == null) {
            return;
        }
        createIndex(id);
    }

    /**
     * 监听消息队列删除索引的消息
     */
    @RabbitListener(bindings = @QueueBinding(value =
    @Queue(value = "ly-search-queue", durable = "true"),
            exchange = @Exchange(value = "ly.item.exchange",
                    ignoreDeclarationExceptions = "true",
                    type = ExchangeTypes.TOPIC),
            key = "item.delete"))
    public void listenDelete(Long id) {
        if (id == null) {
            return;
        }
        goodsRepository.deleteById(id);
    }

    /**
     * 新增或修改商品的索引
     * @param id  变更的商品id
     */
    private void createIndex(Long id) {
        Spu spu = goodsClient.querySpuBySpuId(id);
        SpuBo spuBo = new SpuBo();
        BeanUtils.copyProperties(spu, spuBo);
        goodsRepository.save(indexService.buildGoods(spuBo));
    }

}
