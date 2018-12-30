package com.leyou.item.service;

import com.leyou.item.pojo.Category;

import java.util.List;

public interface CategoryService {
    List<Category> queryByParentId(Long parentId);

    List<Category> queryCategoryByBid(Long bid);

    List<String> queryCategoryNamesByCid(List<Long> longs);
}
