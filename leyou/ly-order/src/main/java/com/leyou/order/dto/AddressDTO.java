package com.leyou.order.dto;

import lombok.Data;

@Data
public class AddressDTO {
    Long id;
    String name;
    String phone;
    private String state;  //省份
    private String city;
    private String district;  //区
    private String address;  //街道地址
    private String zipCode;  //邮编
    private Boolean isDefault;  //是否默认地址
}
