package com.leyou.item.mapper;

import com.leyou.item.pojo.Brand;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import tk.mybatis.mapper.additional.idlist.SelectByIdListMapper;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

public interface BrandMapper extends Mapper<Brand> ,SelectByIdListMapper<Brand,Long>{
    @Insert("insert into tb_category_brand values(#{cid},#{bid})")
    int insertCategoryBrand(@Param("cid") Long cid, @Param("bid") Long bid);

    @Delete("delete from tb_category_brand where brand_id=#{id}")
    void deleteCidByBid(@Param("id") Long id);
    @Select("select brand_id from tb_category_brand where category_id=#{cid}")
    List<Long> selectBidByCid(@Param("cid") Long cid);
}
