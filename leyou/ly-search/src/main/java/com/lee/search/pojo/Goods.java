package com.lee.search.pojo;

import lombok.Data;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import javax.persistence.Id;
import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * spu对应的索引对象
 */
@Document(indexName = "goods", type = "docs", shards = 1, replicas = 0)
@Data
public class Goods implements Serializable{
    @Id
    private Long id; // spuId
    @Field(type = FieldType.text, analyzer = "ik_max_word")
    private String all; // 所有需要被搜索的信息，包含标题、分类、品牌等 ,即页面的过滤条件被选项
    @Field(type = FieldType.keyword, index = false)
    private String subTitle;// 卖点
    private Long brandId;// 品牌id
    private Long cid1;// 1级分类id
    private Long cid2;// 2级分类id
    private Long cid3;// 3级分类id
    private Date createTime;// 创建时间
    private List<Long> price;// 价格
    @Field(type = FieldType.keyword, index = false)
    private String skus;// sku集合的json结构,只用于展示
    private Map<String, Object> specs;// 可搜索的规格参数，key是参数名，值是参数值

    public Goods() {
    }

    public Goods(Long id, String all, String subTitle, Long brandId, Long cid1, Long cid2, Long cid3, Date createTime, List<Long> price, String skus, Map<String, Object> specs) {
        this.id = id;
        this.all = all;
        this.subTitle = subTitle;
        this.brandId = brandId;
        this.cid1 = cid1;
        this.cid2 = cid2;
        this.cid3 = cid3;
        this.createTime = createTime;
        this.price = price;
        this.skus = skus;
        this.specs = specs;
    }

}