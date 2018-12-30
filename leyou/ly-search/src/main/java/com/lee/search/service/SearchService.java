package com.lee.search.service;

import com.lee.search.Repository.GoodsRepository;
import com.lee.search.client.BrandClient;
import com.lee.search.client.CategoryClient;
import com.lee.search.client.SpecificationClient;
import com.lee.search.pojo.Goods;
import com.lee.search.pojo.SearchRequest;
import com.lee.search.pojo.SearchResult;
import com.leyou.item.pojo.Brand;
import com.leyou.item.pojo.Category;
import com.leyou.item.pojo.SpecParam;
import org.apache.commons.lang.StringUtils;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.terms.LongTerms;
import org.elasticsearch.search.aggregations.bucket.terms.StringTerms;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.aggregation.AggregatedPage;
import org.springframework.data.elasticsearch.core.query.FetchSourceFilter;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 聚合查询：根据页面请求的过滤条件被选项,动态显示 SearchResult 对象的数据(页面过滤可选项数据 和 分页商品搜索结果)
 */
@Service
public class SearchService {
    @Autowired
    private GoodsRepository goodsRepository;
    @Autowired
    private CategoryClient categoryClient;
    @Autowired
    private BrandClient brandClient;
    @Autowired
    private SpecificationClient specClient;
    private Logger logger = LoggerFactory.getLogger(SearchService.class);

    public SearchResult search(SearchRequest searchRequest) {
        //定义分类和品牌存放的集合
        List<Category> categories = null;
        List<Brand> brands = null;
        List<Map<String, Object>> specs = null;

        //1、创建原生查询过滤器
        NativeSearchQueryBuilder queryBuilder = new NativeSearchQueryBuilder();
        //2、指定查询的字段,即指定结果集返回的字段
        queryBuilder.withSourceFilter(new FetchSourceFilter(new String[]{"id", "skus", "subTitle", "price"}, null));
        //3、加入请求中的过滤条件
        queryBuilder.withQuery(getFilterQueryBuilder(searchRequest));
        //3、加入分页,es的分页从1开始
        queryBuilder.withPageable(PageRequest.of(searchRequest.getPage() - 1, searchRequest.getSize()));
        //4、加入请求中的排序条件
        String sortBy = searchRequest.getSortBy();
        Boolean descending = searchRequest.getDescending();
        if (StringUtils.isNotBlank(sortBy)) {
            queryBuilder.withSort(SortBuilders.fieldSort(sortBy).order(descending ? SortOrder.DESC : SortOrder.ASC));
        }
        //5、构建聚合，品牌和分类都需要进行聚合,会得到2个聚合结果
        String aggCname = "aggCname";  //聚合名称
        String aggBname = "aggBname";  //聚合名称
        queryBuilder.addAggregation(AggregationBuilders.terms(aggCname).field("cid3"));  //field：聚合字段 ,根据最小的3级分类聚合
        queryBuilder.addAggregation(AggregationBuilders.terms(aggBname).field("brandId")); //根据品牌id聚合,得到每个品牌在页面的过滤栏的信息
        //进行聚合查询
        AggregatedPage<Goods> resultPage = (AggregatedPage<Goods>) goodsRepository.search(queryBuilder.build());
        //获得分类聚合结果
        categories = getCategoryAggResult(aggCname, resultPage);
        //获得品牌聚合结果
        brands = getBrandAggResult(aggBname, resultPage);

        //选中一个商品分类后才显示对应的规格参数过滤项，即仅当分类数为1时进行规格参数的聚合
        if (categories != null && categories.size() == 1) {
            //获取规格参数聚合结果
            specs = getSpecAggResult(categories);
        }
        return new SearchResult(resultPage.getTotalElements(), (long) resultPage.getTotalPages(), resultPage.getContent(), categories, brands, specs);
    }

    private QueryBuilder getFilterQueryBuilder(SearchRequest searchRequest) {
        try {
            //查询字段中包含布尔类型的查询构建器
            BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery();
            //获取searchRequest中的属性
            String key = searchRequest.getKey();
            Map<String, String> filter = searchRequest.getFilter();
            //判断搜索条件是否为空
            if (StringUtils.isBlank(key)) {
                return null;
            }
            //构建搜索条件
            // 添加must的条件 此处为匹配Goods中的all字段
            queryBuilder.must(QueryBuilders.matchQuery("all", key));
            BoolQueryBuilder filterQueryBuilder = QueryBuilders.boolQuery();
            if (!filter.isEmpty()) {
                for (Map.Entry<String, String> filterEntry : filter.entrySet()) {
                    String name = filterEntry.getKey();
                    String value = filterEntry.getValue();
                    //过滤条件中，分类和品牌传入的都是id，要进行额外的判断
                    if (name != "cid2" && name != "brandId") {
                        name = "specs." + name + ".keyword";
                    }
                    filterQueryBuilder.must(QueryBuilders.termQuery(name, value)); //分词精确查询
                }
            }
            queryBuilder.filter(filterQueryBuilder);
            return queryBuilder;
        } catch (Exception e) {
            logger.error("构建过滤参数出现异常：{}", e);
            e.printStackTrace();
        }
        return null;
    }

    private List<Map<String, Object>> getSpecAggResult(List<Category> categories) {
        try {
            //传入的categories只有1个,查询该分类对应的所有规格参数
            List<SpecParam> specParams = this.specClient.querySpecParam(null, categories.get(0).getId(), true, null);
            //进行聚合之后将每一个SpecParam对象拼为Map集合以{k:specparam.name,options:同名聚合结果,即一个Aggregation中的所有桶信息}
            NativeSearchQueryBuilder query = new NativeSearchQueryBuilder();
            //对每一个规格参数都做聚合
            specParams.forEach(specParam -> {
                String key = specParam.getName();
                query.addAggregation(AggregationBuilders.terms(key)   //聚合名称
                                            .field("specs." + key + ".keyword"));  //聚合的字段
            });
            AggregatedPage<Goods> search = (AggregatedPage<Goods>) this.goodsRepository.search(query.build());
            //解析聚合结果,封装成集合返回
            List<Map<String, Object>> specs = new ArrayList<>();
            //将所有的聚合转为map形式，方便获取，key为specs中的每一个规格参数名称
            Map<String, Aggregation> aggMap = search.getAggregations().asMap();
            for (SpecParam specParam : specParams) {
                Map<String, Object> specMap = new HashMap<>();
                String key = specParam.getName();
                //根据key获取聚合Map中对应的每一个聚合，即每一个Aggregation对象
                StringTerms aggregation = (StringTerms) aggMap.get(key);
                //将每一个聚合对象中的桶中的数据取出，封装为一个list集合，即为每一个规格参数所对应的所有条件选项
                List<String> options = aggregation.getBuckets().stream()
                        .map(bucket -> bucket.getKeyAsString()).collect(Collectors.toList());
                specMap.put("k", key);   //此规格参数名称
                specMap.put("options", options);  //此规格参数的所有条件选项
                //将每一个封装好的规格参数键值对放入集合
                specs.add(specMap);
            }
            return specs;
        } catch (Exception e) {
            logger.error("聚合规格参数失败了：{}", e);
            e.printStackTrace();
        }
        return null;

    }

    private List<Brand> getBrandAggResult(String aggBname, AggregatedPage<Goods> resultPage) {
        try {
            //取得搜索结果集page中aggCname字段的聚合结果
            LongTerms aggBrand = (LongTerms) resultPage.getAggregation(aggBname);
            List<Long> bids = new ArrayList<>();
            for (LongTerms.Bucket bucket : aggBrand.getBuckets()) {
                bids.add(bucket.getKeyAsNumber().longValue());
            }
            if (!bids.isEmpty()) {
                return this.brandClient.queryBrandByIds(bids);
            }
        } catch (Exception e) {
            logger.error("聚合品牌失败：{}", e);
            e.printStackTrace();
        }
        return null;
    }

    private List<Category> getCategoryAggResult(String aggCname, AggregatedPage<Goods> resultPage) {
        try {
            //1、取得搜索结果集page中aggCname字段的聚合结果,
            LongTerms aggCategory = (LongTerms) resultPage.getAggregation(aggCname);

            //2、取出每个桶(组)中的key(分类id)

//            List<Long> cids = new ArrayList<>();
//            for (LongTerms.Bucket bucket : aggCategory.getBuckets()) {
//                cids.add(bucket.getKeyAsNumber().longValue());
//            }
            List<Long> cids = aggCategory.getBuckets().stream()
                    .map(bucket -> bucket.getKeyAsNumber().longValue()).collect(Collectors.toList());

            if (!cids.isEmpty()) {
                List<String> cnames = this.categoryClient.queryNameByIds(cids);
                List<Category> categories = new ArrayList<>();
                //封装Category
                for (int i = 0; i < cnames.size(); i++) {
                    Category c = new Category();
                    c.setId(cids.get(i));
                    c.setName(cnames.get(i));
                    categories.add(c);
                }
                return categories;
            }
        } catch (Exception e) {
            logger.error("聚合分类失败:{}", e);
            e.printStackTrace();
        }
        return null;
    }
}
