package com.leyou.item.mapper;

import com.leyou.item.pojo.Sku;
import org.apache.ibatis.annotations.Select;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

public interface SkuMapper extends Mapper<Sku>{
    @Select("select s.*,stock.seckill_stock,stock.seckill_total from tb_sku s left join tb_stock stock on s.id=stock.sku_id where stock.seckill_stock>0")
    List<Sku> querySkuAndStock();
}
