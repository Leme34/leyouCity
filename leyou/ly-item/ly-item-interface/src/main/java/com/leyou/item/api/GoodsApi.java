package com.leyou.item.api;

import com.leyou.common.dto.CartDTO;
import com.leyou.common.vo.PageResult;
import com.leyou.item.bo.SpuBo;
import com.leyou.item.pojo.Sku;
import com.leyou.item.pojo.Spu;
import com.leyou.item.pojo.SpuDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 作为服务提供方提供调用接口
 */
public interface GoodsApi {
    /**
     * 分页查询所有商品的api
     */
    @GetMapping("spu/page")
    PageResult<SpuBo> queryGoodsByPage(@RequestParam(value = "key", required = false) String key,
                                       @RequestParam(value = "saleable", required = false) Boolean saleable,
                                       @RequestParam(value = "page", defaultValue = "1") Integer page,
                                       @RequestParam(value = "rows", defaultValue = "5") Integer rows);
    /**
     * 提供根据spuId查询商品详情API
     */
    @GetMapping("spu/detail/{spuId}")
    SpuDetail querySpuDetailBySpuId(@PathVariable("spuId") Long spuId);

    /**
     * 提供根据spuId查询skuAPI
     */
    @GetMapping("sku/list")
    List<Sku> querySkuBySpuId(@RequestParam("id") Long id);

    @GetMapping("spu/{id}")
    Spu querySpuBySpuId(@PathVariable("id") Long spuId);

    @GetMapping("sku/{id}")
    Sku querySkuById(@PathVariable("id") Long skuId);

    @GetMapping("sku/page")
    List<Sku> queryGoodsByPage();

    /**
     * 减库存接口
     */
    @PostMapping("stock/decrease")
    void decreaseStock(@RequestBody List<CartDTO> carts);
}
