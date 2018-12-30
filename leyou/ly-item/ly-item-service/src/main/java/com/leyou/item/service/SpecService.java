package com.leyou.item.service;

import com.leyou.item.pojo.SpecGroup;
import com.leyou.item.pojo.SpecParam;

import java.util.List;

public interface SpecService {
    List<SpecGroup> querySpecByCid(Long cid);

    List<SpecParam> querySpecParams(Long gid, Long cid, Boolean searching, Boolean generic);

    void saveSpecGroup(SpecGroup specGroup);

    void updateSpecGroup(SpecGroup specGroup);

    void deleteSpecGroup(Long id);

    void saveSpecParam(SpecParam specParam);

    void updateSpecParam(SpecParam specParam);

    void deleteSpecParam(Long paramId);
}
