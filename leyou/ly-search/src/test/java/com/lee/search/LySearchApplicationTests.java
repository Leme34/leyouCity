package com.lee.search;

import com.lee.search.Repository.GoodsRepository;
import com.lee.search.client.GoodsClient;
import com.lee.search.pojo.Goods;
import com.lee.search.service.IndexService;
import com.leyou.common.vo.PageResult;
import com.leyou.item.bo.SpuBo;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.client.SpringCloudApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.stream.Collectors;

@RunWith(SpringRunner.class)
@SpringBootTest
public class LySearchApplicationTests {

    @Autowired
    private GoodsRepository goodsRepository;
    @Autowired
    private GoodsClient goodsClient;
    @Autowired
    private IndexService indexService;


    /**
     * 把所有商品信息导入索引库
     */
    @Test
    public void loadData() {
        //从第一页到最后一页,每次查询100条记录并导入
        int page = 1;
        int rows = 100;
        do {
            //查出所有商品
            PageResult<SpuBo> spuBos = goodsClient.queryGoodsByPage(null, true, page, rows);
            if (CollectionUtils.isEmpty(spuBos.getItems())){
                break;
            }
            //取出每一个spu转换为Goods索引对象
            List<Goods> goods = spuBos.getItems().stream().map(indexService::buildGoods).collect(Collectors.toList());
            //导入索引库
            goodsRepository.saveAll(goods);
            //翻页
            page++;
        }while (rows==100);
    }

}

