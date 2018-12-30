package com.leyou.item.service;

import com.leyou.common.dto.CartDTO;
import com.leyou.common.vo.PageResult;
import com.leyou.item.bo.SpuBo;
import com.leyou.item.pojo.Sku;
import com.leyou.item.pojo.Spu;
import com.leyou.item.pojo.SpuDetail;

import java.util.List;

public interface GoodsService {
    PageResult<SpuBo> queryGoodsByPage(String key, Boolean saleable, Integer page, Integer rows);

    void saveGoods(SpuBo spuBo);

    SpuDetail querySpuDetailBySpuId(Long supId);

    List<Sku> querySkuBySpuId(Long id);

    void updateGoods(SpuBo spuBo);

    void changeSaleable(Long spuId);

    void deleteGoods(Long spuId);

    Spu querySpuBySpuId(Long spuId);

    Sku querySkuById(Long skuId);

    PageResult<Sku> querySeckillSkuByPage(Integer page, Integer rows);

    List<Sku> querySkuBySpuIds(List<Long> skuIds);

    //减库存接口(在数据库端where条件解决线程不安全的超卖问题)
    void decreaseStock(List<CartDTO> carts);

}
