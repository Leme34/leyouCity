package com.leyou.item.controller;

import com.leyou.common.dto.CartDTO;
import com.leyou.common.vo.PageResult;
import com.leyou.item.bo.SpuBo;
import com.leyou.item.pojo.Sku;
import com.leyou.item.pojo.Spu;
import com.leyou.item.pojo.SpuDetail;
import com.leyou.item.service.GoodsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 提供商品的增删改查接口
 * 操作的表有：
 * tb_spu
 * tb_spu_detail
 * tb_sku
 * tb_stock
 */
@Slf4j
@RestController
@RequestMapping
public class GoodsController {
    @Autowired
    private GoodsService goodsService;

    /**
     * 商品的分页查询
     * @param key
     * @param saleable
     * @param page
     * @param rows
     * @return
     */
    //http://api.leyou.com/api/item/spu/page?key=&saleable=true&page=1&rows=5
    @GetMapping("spu/page")
    public ResponseEntity<PageResult<SpuBo>> queryGoodsByPage(@RequestParam(value = "key", required = false) String key,
                                                              @RequestParam(value = "saleable", required = false) Boolean saleable,
                                                              @RequestParam(value = "page", defaultValue = "1") Integer page,
                                                              @RequestParam(value = "rows", defaultValue = "5") Integer rows)
    {
            PageResult<SpuBo> pageResult=goodsService.queryGoodsByPage(key,saleable,page,rows);
            System.out.println(pageResult.getItems());
            if(pageResult==null || pageResult.getItems()==null){
                return new ResponseEntity<>(HttpStatus.NO_CONTENT);
            }
            return ResponseEntity.ok(pageResult);
    }
    @GetMapping("sku/page")
    public ResponseEntity<PageResult<Sku>> queryGoodsByPage(
                                                              @RequestParam(value = "page", defaultValue = "1") Integer page,
                                                              @RequestParam(value = "rows", defaultValue = "12") Integer rows)
    {
            PageResult<Sku> pageResult=goodsService.querySeckillSkuByPage(page,rows);
            if(pageResult==null || pageResult.getItems()==null){
                return new ResponseEntity<>(HttpStatus.NO_CONTENT);
            }
            return ResponseEntity.ok(pageResult);
    }

    /**
     * 新增商品
     * 包括，新增spu信息，spuDetial信息
     * 新增sku信息，库存信息
     * @param spuBo
     * @return
     */
    //http://api.leyou.com/api/item/goods
    @PostMapping("goods")
    public ResponseEntity<Void> saveGoods(@RequestBody SpuBo spuBo){
        goodsService.saveGoods(spuBo);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    /**
     * 修改商品-》先删除原有的商品sku和stock，再新增，最后修改spu
     * @param spuBo
     * @return
     */
    @PutMapping("goods")
    public ResponseEntity<Void> updateGoods(@RequestBody SpuBo spuBo){
        goodsService.updateGoods(spuBo);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    /**
     * 根据spuId查询spuDetail
     * @param spuId
     * @return
     */
    //http://api.leyou.com/api/item/spu/detail/184
    @GetMapping("spu/detail/{spuId}")
    public ResponseEntity<SpuDetail> querySpuDetailBySpuId(@PathVariable("spuId")Long spuId){
        SpuDetail spuDetail=goodsService.querySpuDetailBySpuId(spuId);
        if(spuDetail==null){
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        return ResponseEntity.ok(spuDetail);
    }

    /**
     * 商品修改时的回显
     * 根据SpuId查询Sku作为回显数据
     * * @param id
     * @return
     */
    //http://api.leyou.com/api/item/sku/list?id=184
    @GetMapping("sku/list")
    public ResponseEntity<List<Sku>> querySkuBySpuId(@RequestParam("id")Long id){
        List<Sku> skus=goodsService.querySkuBySpuId(id);
        if(skus==null || skus.size()==0){
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        return ResponseEntity.ok(skus);
    }

    /**
     * 展示购物车页时发起的查询,用于检查商品最新价格、是否下架、库存
     * @param skuIds  购物车商品id集合
     */
    @GetMapping("skus")
    public ResponseEntity<List<Sku>> querySkuBySpuId(@RequestParam("skuIds")List<Long> skuIds){
        List<Sku> skus = goodsService.querySkuBySpuIds(skuIds);
        return ResponseEntity.ok(skus);
    }

    @GetMapping("sku/{id}")
    public ResponseEntity<Sku> querySkuById(@PathVariable("id")Long skuId){
       Sku sku= this.goodsService.querySkuById(skuId);
       if(sku==null){
           return new ResponseEntity<>(HttpStatus.NO_CONTENT);
       }
       return ResponseEntity.ok(sku);
    }


    @PutMapping("spu/{spuId}")
    public ResponseEntity<Void> changeSaleable(@PathVariable("spuId")Long spuId){
        try {
            goodsService.changeSaleable(spuId);
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    @DeleteMapping("spu/{spuId}")
    public ResponseEntity<Void> deleteGoods(@PathVariable("spuId")Long spuId){
        try {
            goodsService.deleteGoods(spuId);
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    @GetMapping("spu/{id}")
    public ResponseEntity<Spu> querySpuBySpuId(@PathVariable("id")Long spuId){
        Spu spu=this.goodsService.querySpuBySpuId(spuId);
        if(spu==null){
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        return ResponseEntity.ok(spu);
    }

    /**
     * 减库存接口
     */
    @PostMapping("stock/decrease")
    public ResponseEntity<Void> decreaseStock(@RequestBody List<CartDTO> carts){
        goodsService.decreaseStock(carts);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

}
