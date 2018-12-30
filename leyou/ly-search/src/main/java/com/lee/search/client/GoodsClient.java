package com.lee.search.client;

import com.leyou.item.api.GoodsApi;
import org.springframework.cloud.openfeign.FeignClient;

@FeignClient(value = "item-service")
public interface GoodsClient extends GoodsApi {
    /**
     * 继承自GoodsApi由服务提供方维护接口
     */

}
