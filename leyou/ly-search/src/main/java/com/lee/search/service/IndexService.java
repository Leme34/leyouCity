package com.lee.search.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.lee.search.Repository.GoodsRepository;
import com.lee.search.client.CategoryClient;
import com.lee.search.client.GoodsClient;
import com.lee.search.client.SpecificationClient;
import com.lee.search.pojo.Goods;
import com.leyou.common.utils.JsonUtils;
import com.leyou.item.bo.SpuBo;
import com.leyou.item.pojo.Sku;
import com.leyou.item.pojo.SpecParam;
import com.leyou.item.pojo.SpuDetail;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * 封装数据结构,存入es索引库
 */
@Service
public class IndexService {
    @Autowired
    private CategoryClient categoryClient;
    @Autowired
    private GoodsClient goodsClient;
    @Autowired
    private SpecificationClient specificationClient;
    @Autowired
    private GoodsRepository goodsRepository;

    /**
     * 传入SpuBo构件Goods索引对象
     */
    public Goods buildGoods(SpuBo spuBo) {
        Goods goods = new Goods();
        //先查spuDetail
        SpuDetail spuDetail = goodsClient.querySpuDetailBySpuId(spuBo.getId());
        //再查sku
        List<Sku> skus = goodsClient.querySkuBySpuId(spuBo.getId());
        //进行属性拷贝
        BeanUtils.copyProperties(spuBo, goods);
        //封装all字段
        String title = spuBo.getTitle();
        //封装cnames
        List<String> cnames = this.categoryClient.queryNameByIds(Arrays.asList(spuBo.getCid1(), spuBo.getCid2(), spuBo.getCid3()));
        String all = title + " " + StringUtils.join(cnames, ' ');
        //封装price  private List<Long> price;// 价格
        List<Long> prices = new ArrayList<>();
        skus.forEach(sku -> prices.add(sku.getPrice()));
        //封装 private String skus;// sku信息的json结构
        List<Map<String, Object>> skuList = new ArrayList<>();
        for (Sku sku : skus) {
            Map<String, Object> skuMap = new HashMap<>();
            skuMap.put("id", sku.getId());
            skuMap.put("title", sku.getTitle());
            //使用StringUtils工具类取得","之前第一个字符串 （取得一张图片）
            skuMap.put("image", StringUtils.substringBefore(sku.getImages(), ","));
            skuMap.put("price", sku.getPrice());
            skuList.add(skuMap);
        }
        // 封装规格参数 private Map<String, Object> specs;// 可搜索的规格参数，key是参数名，值是参数值
        List<SpecParam> specParams = this.specificationClient.querySpecParam(null, spuBo.getCid3(), true, null);
        String genericSpec = spuDetail.getGenericSpec();
        Map<Long, String> genericSpecMap = JsonUtils.parseMap(genericSpec, Long.class, String.class);
        Map<Long, List<String>> specialSpec = JsonUtils.nativeRead(spuDetail.getSpecialSpec(), new TypeReference<Map<Long, List<String>>>() {
        });
        Map<String, Object> specs = new HashMap<>();
        //处理规格参数
        for (SpecParam specParam : specParams) {
            Long id = specParam.getId();
            String pname = specParam.getName();
            Object value = null;
            //如果是数字类型,则把数据库中的数字范围解析成前端显示的段
            if (specParam.getGeneric()) {
                value = genericSpecMap.get(id);
                if (specParam.getNumeric()) {
                    value = this.chooseSegment(value.toString(), specParam);
                }
            } else {   //否则是非数字类型的规格参数
                value = specialSpec.get(id);
            }
            if (null == value) {
                value = "其他";
            }
            specs.put(pname, value);
        }
        goods.setSkus(JsonUtils.serialize(skuList));
        goods.setAll(all);
        goods.setPrice(prices);
        goods.setSpecs(specs);
        return goods;
    }

    /**
     * 把tb_spec_param表中的segments字段 解析为前端显示的可搜索范围字段
     */
    private String chooseSegment(String value, SpecParam specParam) {
        String[] split = specParam.getSegments().split(",");
        //使用NumberUtils工具类把字符串解析为double值
        Double val = NumberUtils.toDouble(value);
        String result = "其他";
        //获取每一个区间值，判断数值value是否在对应区间中
        for (String s : split) {
            String[] spl = s.split("-");
            //获取每个数值区间的起始区间
            Double begin = NumberUtils.toDouble(spl[0]);
            //因为有可能不存在结束区间，如1000以上，故end先取一个最大值
            Double end = Double.MAX_VALUE;
            //判断该数值区间是否为一个闭区间，如果为闭区间，则表示存在结束区间，应将end的取值为对应结束区间的值
            if (spl.length == 2) {
                end = NumberUtils.toDouble(spl[1]);
            }
            //获得了起始区间和结束区间后，开始判断传过来的value对应的值属于哪个区间
            if (val < end && val > begin) {
                if (spl.length == 1) {
                    result = spl[0] + specParam.getUnit() + "以上";
                } else {
                    result = s + specParam.getUnit();
                }
                break;
            }
        }
        return result;

    }
}
