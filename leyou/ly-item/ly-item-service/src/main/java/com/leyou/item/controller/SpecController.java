package com.leyou.item.controller;

import com.leyou.item.pojo.SpecGroup;
import com.leyou.item.pojo.SpecParam;
import com.leyou.item.service.SpecService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 提供规格模板、分组以及参数的增删改查接口
 * 操作的表：
 * tb_spec_group
 * tb_spec_param
 */
@RestController
@RequestMapping("spec")
public class SpecController {
    //spec/groups/3
    @Autowired
    private SpecService specService;

    /**
     * 查询某一分类对应的规格模板
     *
     * @param cid
     * @return
     */
    @GetMapping("groups/{cid}")
    public ResponseEntity<List<SpecGroup>> querySpecGroupByCid(@PathVariable("cid") Long cid) {
        //根据分类id查询对应的规格模板PRIMARY
        List<SpecGroup> specGroups = specService.querySpecByCid(cid);
        if (specGroups == null || specGroups.size() == 0) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        return ResponseEntity.ok(specGroups);
    }

    /**
     * 查询规格模板中某一组的规格参数
     *
     * @param gid
     * @return
     */
    //----------根据不同条件查询规格参数
    @GetMapping("params")
    public ResponseEntity<List<SpecParam>> querySpecParam(@RequestParam(value = "gid", required = false) Long gid,
                                                               @RequestParam(value = "cid", required = false) Long cid,
                                                               @RequestParam(value = "searching", required = false) Boolean searching,
                                                               @RequestParam(value = "generic", required = false) Boolean generic) {
        List<SpecParam> specParams = specService.querySpecParams(gid, cid,searching,generic);
        if (specParams.size() == 0 || specParams == null) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        return ResponseEntity.ok(specParams);
    }

    //http://api.leyou.com/api/item/spec/group

    /**
     * 新增规格模板的分组
     *
     * @param specGroup
     * @return
     */
    @PostMapping("group")
    public ResponseEntity<Void> saveSpecGroup(@RequestBody SpecGroup specGroup) {
        specService.saveSpecGroup(specGroup);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    /**
     * 修改规格模板中某一分组信息
     *
     * @param specGroup
     * @return
     */
    @PutMapping("group")
    public ResponseEntity<Void> updateSpecGroup(SpecGroup specGroup) {
        specService.updateSpecGroup(specGroup);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    /**
     * 删除规格模板中的某一分组
     *
     * @param id
     * @return
     */
    //http://api.leyou.com/api/item/spec/group/15
    @DeleteMapping("group/{id}")
    public ResponseEntity<Void> deleteSpecGroup(@PathVariable("id") Long id) {
        specService.deleteSpecGroup(id);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    /**
     * 新增规格模板中某一组的规格参数
     *
     * @param specParam
     * @return
     */
    //http://api.leyou.com/api/item/spec/param
    @PostMapping("param")
    public ResponseEntity<Void> saveSpecParam(@RequestBody SpecParam specParam) {
        specService.saveSpecParam(specParam);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    /**
     * 修改规格参数中某一分组的某个规格参数
     *
     * @param specParam
     * @return
     */
    @PutMapping("param")
    public ResponseEntity<Void> updateSpecParam(@RequestBody SpecParam specParam) {
        specService.updateSpecParam(specParam);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    /**
     * 删除规格参数中某一分组中的某个模板参数
     *
     * @param paramId
     * @return
     */
    @DeleteMapping("param/{paramId}")
    public ResponseEntity<Void> deleteSpecParam(@PathVariable("paramId") Long paramId) {
        specService.deleteSpecParam(paramId);
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
