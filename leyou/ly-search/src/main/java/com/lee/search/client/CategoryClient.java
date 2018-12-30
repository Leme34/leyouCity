package com.lee.search.client;

import com.leyou.item.api.CategoryApi;
import org.springframework.cloud.openfeign.FeignClient;

/**
 * 继承自GoodsApi由服务提供方维护接口
 */
@FeignClient(value = "item-service")
public interface CategoryClient extends CategoryApi {
}
