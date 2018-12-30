package com.leyou.item.service;

import com.leyou.common.vo.PageResult;
import com.leyou.item.pojo.Brand;

import java.util.List;

public interface BrandService {

    PageResult<Brand> queryBrandByPage(String key, Integer page, Integer rows, String sortBy, Boolean desc);

    void saveBrand(Brand brand, List<Long> cids);

    void updateBrand(Brand brand, List<Long> cids);

    void deleteBrandByBid(Long bid);

    String queryBrandNameByBid(Long brandId);

    List<Brand> queryBrandByCid(Long cid);

    List<Brand> queryBrandByIds(List<Long> ids);
}
