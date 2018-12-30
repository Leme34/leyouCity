package com.leyou.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 请求返回体的错误码和错误信息枚举
 */
@NoArgsConstructor
@AllArgsConstructor
@Getter
public enum ExceptionEnum {

    BRAND_NOT_FOUND(404,"品牌不存在"),
    CATEGORY_NOT_FOUND(404,"没有此商品分类"),
    SPEC_GROUP_NOT_FOUND(404,"商品规格组不存在"),
    BARND_SAVE_ERROR(500,"新增商品失败"),
    UPLOAD_FILE_ERROR(500,"文件上传失败"),
    INVALID_FILE_TYPE(400,"无效的文件类型"),
    STOCK_NOT_ENOUGH(500,"库存不足"),
    ;

    private Integer statusCode;
    private String msg;


}
