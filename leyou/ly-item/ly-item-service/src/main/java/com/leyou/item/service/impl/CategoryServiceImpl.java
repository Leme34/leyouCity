package com.leyou.item.service.impl;

import com.leyou.item.mapper.CategoryMapper;
import com.leyou.item.pojo.Category;
import com.leyou.item.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class CategoryServiceImpl implements CategoryService {
    @Autowired
    private CategoryMapper categoryMapper;
    @Override
    public List<Category> queryByParentId(Long parentId) {
        Category category = new Category();
        category.setParentId(parentId);
        return categoryMapper.select(category);
    }

    @Override
    public List<Category> queryCategoryByBid(Long bid) {
        List<Long> cids=categoryMapper.selectCidByBid(bid);
        List<Category> categories = new ArrayList<>();
        for (Long cid : cids) {
            Category category = categoryMapper.selectByPrimaryKey(cid);
            categories.add(category);
        }
        return categories;
    }
    @Override
    public List<String> queryCategoryNamesByCid(List<Long>cids){
        List<Category> categories = categoryMapper.selectByIdList(cids);
        List<String> cnames = new ArrayList<>();
        for (Category category : categories) {
            cnames.add(category.getName());
        }
        return cnames;
    }
}
