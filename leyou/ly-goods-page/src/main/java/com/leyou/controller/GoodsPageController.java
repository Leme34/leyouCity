package com.leyou.controller;

import com.leyou.item.pojo.Sku;
import com.leyou.service.FileService;
import com.leyou.service.GoodsPageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Map;

@Controller
public class GoodsPageController {
    @Autowired
    private GoodsPageService goodsPageService;
    @Autowired
    private FileService fileService;
    //'item/'+goods.id+'.html'
    @GetMapping("item/{id}.html")
    public String toItemPage(Model model, @PathVariable("id")Long spuId){
        //准备页面的所有数据
        Map<String,Object> spuMap= goodsPageService.loadItem(spuId);
        model.addAllAttributes(spuMap);
        if(!this.fileService.exists(spuId)){
            this.fileService.syncCreateHtml(spuId);
        }
        return "item";
    }

    @GetMapping("seckill-item/{id}.html")
    public String toSeckillItemPage(Model model, @PathVariable("id")Long id){
        Map<String,Object>skuMap=goodsPageService.loadSeckillItem(id);
        model.addAllAttributes(skuMap);
        if(!this.fileService.exists(id)){
            this.fileService.syncCreateHtml(id);
        }
        return "seckill-item";
    }

}
