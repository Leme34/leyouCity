package com.leyou.item.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.leyou.common.dto.CartDTO;
import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exception.LyException;
import com.leyou.common.vo.PageResult;
import com.leyou.item.bo.SpuBo;
import com.leyou.item.mapper.GoodsMapper;
import com.leyou.item.mapper.SkuMapper;
import com.leyou.item.mapper.SpuDetailMapper;
import com.leyou.item.mapper.StockMapper;
import com.leyou.item.pojo.Sku;
import com.leyou.item.pojo.Spu;
import com.leyou.item.pojo.SpuDetail;
import com.leyou.item.pojo.Stock;
import com.leyou.item.service.BrandService;
import com.leyou.item.service.CategoryService;
import com.leyou.item.service.GoodsService;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import tk.mybatis.mapper.entity.Example;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class GoodsServiceImpl implements GoodsService {
    @Autowired
    private GoodsMapper goodsMapper;
    @Autowired
    private CategoryService categoryService;
    @Autowired
    private BrandService brandService;
    @Autowired
    private SpuDetailMapper spuDetailMapper;
    @Autowired
    private SkuMapper skuMapper;
    @Autowired
    private StockMapper stockMapper;
    @Autowired
    private AmqpTemplate amqpTemplate;
    private Logger logger = LoggerFactory.getLogger(GoodsServiceImpl.class);

    @Override
    public PageResult<SpuBo> queryGoodsByPage(String key, Boolean saleable, Integer page, Integer rows) {
        PageHelper.startPage(page, rows);
        Example example = new Example(Spu.class);
        Example.Criteria criteria = example.createCriteria();
        //判断是否有搜索条件
        if (StringUtils.isNotBlank(key)) {
            //有搜索条件时对标题进行模糊查询
            criteria.andLike("title", "%" + key + "%");
        }
        //判断上架下架条件是否存在
        if (saleable != null) {
            //如果存在，加入根据上下架条件查询
            criteria.andEqualTo("saleable", saleable);

        }
        Page<Spu> pageInfo = (Page<Spu>) goodsMapper.selectByExample(example);
        List<Spu> src = pageInfo.getResult();
        List<SpuBo> dest = new ArrayList<>();
        for (Spu spu : src) {
            SpuBo spuBo = new SpuBo();
            //属性拷贝，将spu中封装的属性拷贝到spuBo中
            BeanUtils.copyProperties(spu, spuBo);
            //根据spu中的cid和bid去查询分类名称以及品牌名称，并封装到spuBo中
            List<String> cname = categoryService.queryCategoryNamesByCid(Arrays.asList(spu.getCid1(), spu.getCid2(), spu.getCid3()));
            //对查询的cnames集合进行处理，转为字符串"手机，充电宝，.."形式返回页面
            spuBo.setCname(StringUtils.join(cname, ","));
            //查询spu对应的品牌名称
            String bname = brandService.queryBrandNameByBid(spu.getBrandId());
            spuBo.setBname(bname);
            dest.add(spuBo);
        }
        return new PageResult<>(pageInfo.getTotal(), dest);
    }

    @Override
    @Transactional
    public void saveGoods(SpuBo spuBo) {
        //先将数据写入spu表中，数据中缺少saleable，valid,createtime,lastupdatetime
        spuBo.setCreateTime(new Date());
        spuBo.setSaleable(true);
        spuBo.setValid(true);
        spuBo.setLastUpdateTime(spuBo.getCreateTime());
        goodsMapper.insert(spuBo);
        //将数据写入spu_detail中
        SpuDetail spuDetail = spuBo.getSpuDetail();
        spuDetail.setSpuId(spuBo.getId());
        spuDetailMapper.insert(spuDetail);
        //将数据写入sku和stock表中
        saveSkuAndStock(spuBo.getSkus(), spuBo.getId());
        sendMessage(spuBo.getId(), "insert");
    }

    @Override
    public SpuDetail querySpuDetailBySpuId(Long supId) {
        return spuDetailMapper.selectByPrimaryKey(supId);
    }

    @Override
    public List<Sku> querySkuBySpuId(Long id) {
        Sku sku = new Sku();
        sku.setSpuId(id);
        List<Sku> skus = skuMapper.select(sku);
        for (Sku skus1 : skus) {
            Integer stock = stockMapper.selectByPrimaryKey(skus1.getId()).getStock();
            skus1.setStock(stock);
        }
        return skus;
    }

    @Override
    @Transactional
    public void updateGoods(SpuBo spuBo) {
        //删除原有的Sku和对应的库存
        deleteStockAndSku(spuBo.getId());

        //新增sku和库存
        saveSkuAndStock(spuBo.getSkus(), spuBo.getId());
        //修改spu
        spuBo.setLastUpdateTime(new Date());
        goodsMapper.updateByPrimaryKeySelective(spuBo);
        spuDetailMapper.updateByPrimaryKeySelective(spuBo.getSpuDetail());
        this.sendMessage(spuBo.getId(), "update");
    }

    /**
     * 根据spuId删除sku和其库存信息
     *
     * @param spuId
     */
    private void deleteStockAndSku(Long spuId) {
        //查询sku
        Sku querySku = new Sku();
        querySku.setSpuId(spuId);
        List<Sku> skus = skuMapper.select(querySku);
        //如果查询的skus不为空，则拿到其id进行库存删除
        //JDK1.8新特性写法
        if (!CollectionUtils.isEmpty(skus)) {
            List<Long> ids = skus.stream().map(sku -> sku.getId()).collect(Collectors.toList());
            Example example = new Example(Stock.class);
            example.createCriteria().andIn("skuId", ids);
            stockMapper.deleteByExample(example);

        }
        skuMapper.delete(querySku);
    }

    @Override
    public void changeSaleable(Long spuId) {
        Spu spu = goodsMapper.selectByPrimaryKey(spuId);
        spu.setSaleable(!spu.getSaleable());
        goodsMapper.updateByPrimaryKeySelective(spu);

    }

    @Override
    @Transactional
    public void deleteGoods(Long spuId) {
        //先删除sku和库存信息
        deleteStockAndSku(spuId);
        //再删除spu和spu_detail信息
        goodsMapper.deleteByPrimaryKey(spuId);
        spuDetailMapper.deleteByPrimaryKey(spuId);
        this.sendMessage(spuId, "delete");
    }

    @Override
    public Spu querySpuBySpuId(Long spuId) {
        return this.goodsMapper.selectByPrimaryKey(spuId);
    }

    @Override
    public Sku querySkuById(Long skuId) {
        return this.skuMapper.selectByPrimaryKey(skuId);
    }

    @Override
    public PageResult<Sku> querySeckillSkuByPage(Integer page, Integer rows) {
        PageHelper.startPage(page, rows);
        Page<Sku> pageInfo = (Page<Sku>) this.skuMapper.querySkuAndStock();
        return new PageResult<>(pageInfo.getTotal(), (long) pageInfo.getPages(), pageInfo.getResult());
    }

    @Override
    public List<Sku> querySkuBySpuIds(List<Long> skuIds) {
        List<Sku> skus = new ArrayList<>();
        for (Long skuId : skuIds) {
            skus.add(skuMapper.selectByPrimaryKey(skuId));
        }
        if (CollectionUtils.isEmpty(skus)) {
            //TODO 抛异常
        }
        //查询并加上库存信息,在页面提示
        for (Sku skus1 : skus) {
            Integer stock = stockMapper.selectByPrimaryKey(skus1.getId()).getStock();
            skus1.setStock(stock);
        }
        return skus;
    }

    private void saveSkuAndStock(List<Sku> skus, Long spuId) {
        skus.forEach(sku -> {
            if (sku.getEnable()) {
                sku.setSpuId(spuId);
                sku.setCreateTime(new Date());
                sku.setLastUpdateTime(sku.getCreateTime());
                this.skuMapper.insert(sku);
                Stock stock = new Stock();
                stock.setSkuId(sku.getId());
                stock.setStock(sku.getStock());
                this.stockMapper.insert(stock);
            }
        });
    }

    @Override
    public void decreaseStock(List<CartDTO> carts) {
        for (CartDTO cart : carts) {
            //减库存
            int affectedRow = stockMapper.decreaseStock(cart.getSkuId(), cart.getNum());
            if (affectedRow != 1)
                throw new LyException(ExceptionEnum.STOCK_NOT_ENOUGH);
        }
    }

    /**
     * 发送消息,发到配置文件中的默认交换机
     *
     * @param id   变更的商品id
     * @param type 变更类型：CRUD
     */
    private void sendMessage(Long id, String type) {
        try {
            this.amqpTemplate.convertAndSend("item." + type, id);
        } catch (Exception e) {
            logger.error("{}商品消息发送异常，商品id：{}", type, id, e);
        }
    }
}
