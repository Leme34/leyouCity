package com.leyou.service;

import com.leyou.client.BrandClient;
import com.leyou.client.CategoryClient;
import com.leyou.client.GoodsClient;
import com.leyou.client.SpecificationClient;
import com.leyou.item.pojo.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class GoodsPageService {
    @Autowired
    private GoodsClient goodsClient;
    @Autowired
    private CategoryClient categoryClient;
    @Autowired
    private BrandClient brandClient;
    @Autowired
    private SpecificationClient specificationClient;
    private Logger logger= LoggerFactory.getLogger(GoodsPageService.class);

    public Map<String, Object> loadItem(Long spuId) {
        try {
            HashMap<String, Object> goodsMap = new HashMap<>();
            //查询spuDetail
            SpuDetail spuDetail = goodsClient.querySpuDetailBySpuId(spuId);
            goodsMap.put("spuDetail", spuDetail);
            //查询spu
            Spu spu = goodsClient.querySpuBySpuId(spuId);
            goodsMap.put("spu", spu);
            //查询sku
            List<Sku> skus = goodsClient.querySkuBySpuId(spuId);
            goodsMap.put("skus", skus);
            //查询对应的规格参数组
            List<SpecGroup> specGroups = this.specificationClient.querySpecGroupByCid(spu.getCid3());
            goodsMap.put("group",specGroups);
            //查询spu对应的分类名称
            List<Category> categories = getCategoriesBySpu(spu);
            goodsMap.put("categories",categories);
            //查询spu对应的品牌名称
            List<Brand> brands = brandClient.queryBrandByIds(Arrays.asList(spu.getBrandId()));
            goodsMap.put("brand",brands.get(0));
            //封装特有的规格参数
            List<SpecParam> params = this.specificationClient.querySpecParam(null, spu.getCid3(), null, null);
            Map<Long, String> paramsMap = new HashMap<>();
            params.forEach(param->paramsMap.put(param.getId(),param.getName()));
            goodsMap.put("paramMap",paramsMap);
            return goodsMap;
        } catch (Exception e) {
            logger.error("封装商品详情出现异常，{}",e);
        }
        return null;
    }

    private List<Category> getCategoriesBySpu(Spu spu) {
        List<String> cnames = categoryClient.queryNameByIds(Arrays.asList(spu.getCid1(), spu.getCid2(), spu.getCid3()));
        Category category = new Category();
        category.setId(spu.getCid1());
        category.setName(cnames.get(0));
        Category category1 = new Category();
        category1.setId(spu.getCid2());
        category1.setName(cnames.get(1));
        Category category2 = new Category();
        category2.setId(spu.getCid3());
        category2.setName(cnames.get(2));
       return Arrays.asList(category,category1,category2);
    }

    public Map<String,Object> loadSeckillItem(Long id) {
        try {
            Sku sku = this.goodsClient.querySkuById(id);
            SpuDetail spuDetail = this.goodsClient.querySpuDetailBySpuId(sku.getSpuId());
            Spu spu = this.goodsClient.querySpuBySpuId(sku.getSpuId());
            Map<String,Object> skuMap = new HashMap<>();
            skuMap.put("spuDetail",spuDetail);
            List<SpecParam> params = this.specificationClient.querySpecParam(null, spu.getCid3(), null, null);
            Map<Long, String> paramsMap = new HashMap<>();
            //查询对应的规格参数组
            List<SpecGroup> specGroups = this.specificationClient.querySpecGroupByCid(spu.getCid3());
            skuMap.put("group",specGroups);
            params.forEach(param->paramsMap.put(param.getId(),param.getName()));
            skuMap.put("paramMap",paramsMap);
            skuMap.put("sku",sku);
            return skuMap;
        } catch (Exception e) {
            logger.error("封装秒杀商品详情出现异常，{}",e);
        }
        return null;
    }
}
