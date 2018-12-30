package com.leyou.cart.service;

import com.leyou.auth.entiy.UserInfo;
import com.leyou.cart.client.GoodsClient;
import com.leyou.cart.interceptor.LoginInterceptor;
import com.leyou.cart.pojo.Cart;
import com.leyou.common.utils.JsonUtils;
import com.leyou.item.pojo.Sku;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CartService {
    @Autowired
    private StringRedisTemplate redisTemplate;
    @Autowired
    private GoodsClient goodsClient;
    static final String KEY_PREFIX = "ly:cart:uid:";
    static final Logger logger = LoggerFactory.getLogger(CartService.class);


    public Boolean addCart(List<Cart> carts) {
        try {
            if (CollectionUtils.isEmpty(carts)) {
                return false;
            }
            for (Cart cart : carts) {
                //将传过来的skuId和(旧的)num取出
                Long skuId = cart.getSkuId();
                Integer num = cart.getNum();
                //获取用户信息
                UserInfo loginUser = LoginInterceptor.getLoginUser();
                String userKey = KEY_PREFIX + loginUser.getId();
                //判断redis中是否已经存在该商品
                BoundHashOperations<String, Object, Object> cartMap = redisTemplate.boundHashOps(userKey);  //绑定哈希key的操作对象
                Boolean haskey = cartMap.hasKey(skuId.toString());
                if (haskey) {
                    //存在则直接更新缓存中的数量，在原有的num基础上加上新的num
                    String carString = cartMap.get(skuId.toString()).toString();
                    cart = JsonUtils.parse(carString, Cart.class);
                    cart.setNum(cart.getNum() + num);
                    cart.setUserId(loginUser.getId());
                }
                //没有，则远程调用goodsClient查询当前要存入的sku
                else {
                    Sku sku = this.goodsClient.querySkuById(skuId);
                    if (sku == null) {
                        logger.error("要加入购物车的商品不存在：{}", skuId);
                        return false;
                    }
                    BeanUtils.copyProperties(sku, cart);
                    cart.setImage(sku.getImages().split(",")[0]);
                    cart.setUserId(loginUser.getId());
                    cart.setEnable(sku.getEnable());
                }
                //不管新增还是修改都存入redis
                cartMap.put(skuId.toString(), JsonUtils.serialize(cart));
            }
            return true;
        } catch (BeansException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 加载购物车列表
     */
    public List<Cart> loadCart() {
        UserInfo loginUser = LoginInterceptor.getLoginUser();
        String userKey = KEY_PREFIX + loginUser.getId();
        //若redis中没有
        if (!this.redisTemplate.hasKey(userKey)) {
            return null;
        }
        //从redis中取出cart对象
        BoundHashOperations<String, Object, Object> cartMap = this.redisTemplate.boundHashOps(userKey);  //绑定哈希key的操作对象,此时相当于map键值对
        List<Object> stringCarts = cartMap.values();
        if (CollectionUtils.isEmpty(stringCarts)) {
            return null;
        }
        List<Cart> carts = stringCarts.stream().map(cart -> JsonUtils.parse(cart.toString(), Cart.class)).collect(Collectors.toList());
        return carts;
    }

    public Boolean updateCart(Cart cart) {
        try {
            UserInfo loginUser = LoginInterceptor.getLoginUser();
            String key = KEY_PREFIX + loginUser.getId();
            BoundHashOperations<String, Object, Object> cartMap = this.redisTemplate.boundHashOps(key);
            String stringCart = cartMap.get(cart.getSkuId().toString()).toString();
            Cart parse = JsonUtils.parse(stringCart, Cart.class);
            parse.setNum(cart.getNum());
            cartMap.put(cart.getSkuId().toString(), JsonUtils.serialize(parse));
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public Boolean deleteCart(Long skuId) {
        try {
            UserInfo user = LoginInterceptor.getLoginUser();
            BoundHashOperations<String, Object, Object> ops = redisTemplate.boundHashOps(KEY_PREFIX + user.getId());
            Long delete = ops.delete(skuId.toString());
            return delete == 1;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}
