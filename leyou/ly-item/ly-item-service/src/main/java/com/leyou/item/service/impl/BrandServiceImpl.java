package com.leyou.item.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.leyou.common.vo.PageResult;
import com.leyou.item.mapper.BrandMapper;
import com.leyou.item.pojo.Brand;
import com.leyou.item.service.BrandService;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tk.mybatis.mapper.entity.Example;

import java.util.ArrayList;
import java.util.List;

/**
 * 使用通用mapper，分页助手进行对品牌的分页查询
 */
@Service
public class BrandServiceImpl implements BrandService {
@Autowired
private BrandMapper brandMapper;
    @Override
    public PageResult<Brand> queryBrandByPage(String key, Integer page, Integer rows, String sortBy, Boolean desc) {
        //开启分页助手,传入当前页及每页行数
        PageHelper.startPage(page,rows);
        //创建模板对象，建立查询模板,传入要查询的对象
        Example example = new Example(Brand.class);
        //进行key和sortBy的值判断
        if(StringUtils.isNotBlank(key)){
            example.createCriteria().andLike("name","%"+key+"%").orEqualTo("letter",key.toUpperCase());
        }
       if(StringUtils.isNotBlank(sortBy)){
           String orderByClause=sortBy+(desc?" desc":" asc");
           example.setOrderByClause(orderByClause);
       }
       //通过设置好的模板进行查询
      Page<Brand> brands= (Page<Brand>) brandMapper.selectByExample(example);
        return new PageResult<Brand>(brands.getTotal(),brands);
    }

    @Override
    @Transactional
    public void saveBrand(Brand brand, List<Long> cids) {
        this.brandMapper.insertSelective(brand);
        for (Long cid : cids) {
            Long bid = brand.getId();
            this.brandMapper.insertCategoryBrand(cid,bid);
        }
    }

    @Override
    @Transactional
    public void updateBrand(Brand brand, List<Long> cids) {
        brandMapper.updateByPrimaryKey(brand);
        brandMapper.deleteCidByBid(brand.getId());
        for (Long cid : cids) {
            brandMapper.insertCategoryBrand(cid,brand.getId());
        }
    }

    @Override
    @Transactional
    public void deleteBrandByBid(Long bid) {
        Brand brand = new Brand();
        brand.setId(bid);
        brandMapper.delete(brand);
        brandMapper.deleteCidByBid(bid);
    }

    @Override
    public String queryBrandNameByBid(Long brandId) {
        Brand brand = brandMapper.selectByPrimaryKey(brandId);
        return brand.getName();

    }

    @Override
    public List<Brand> queryBrandByCid(Long cid) {
        List<Long> bids=brandMapper.selectBidByCid(cid);
       List<Brand> brands = new ArrayList<>();
        bids.forEach(bid->
            brands.add(brandMapper.selectByPrimaryKey(bid))
        );
        return brands;
    }

    @Override
    public List<Brand> queryBrandByIds(List<Long> ids) {

        return  brandMapper.selectByIdList(ids);
    }
}