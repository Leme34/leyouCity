package com.leyou.item.controller;

import com.leyou.item.pojo.Category;
import com.leyou.item.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 提供分类的增删改查接口
 * 操作的表
 * tb_category
 * tb_category_brand
 */
@RestController
@RequestMapping("category")
public class CategoryController {
    @Autowired
    private CategoryService categoryService;

    /**
     * 根据父分类id查询第一节子分类
     * @param parentId
     * @return
     */
   @GetMapping("list")
   public ResponseEntity<List<Category>> queryByParentId(@RequestParam(value="pid",defaultValue = "0")Long parentId){
       List<Category> categories=categoryService.queryByParentId(parentId);
       if(categories==null||categories.size()<1){
           return new ResponseEntity<>(HttpStatus.NOT_FOUND);
       }
       return ResponseEntity.ok(categories);
   }

    /**
     * 根据品牌id查询分类
     * @param bid
     * @return
     */
   @GetMapping("bid/{bid}")
    public ResponseEntity<List<Category>> queryCategoryByBid(@PathVariable("bid")Long bid){
       List<Category> categories=categoryService.queryCategoryByBid(bid);
       if(categories.size()==0||categories==null){
           return new ResponseEntity<>(HttpStatus.NOT_FOUND);
       }
       return ResponseEntity.ok(categories);
   }
    @GetMapping("names")
    public ResponseEntity queryNameByIds(@RequestParam("ids") List<Long> ids){
        List<String> names = categoryService.queryCategoryNamesByCid(ids);
        if (names == null||names.size()==0) {
            return new ResponseEntity(HttpStatus.NO_CONTENT);
        }
        return ResponseEntity.ok(names);
    }
}
