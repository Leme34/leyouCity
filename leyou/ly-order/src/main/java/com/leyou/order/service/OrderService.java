package com.leyou.order.service;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.leyou.auth.entiy.UserInfo;
import com.leyou.common.dto.CartDTO;
import com.leyou.common.utils.IdWorker;
import com.leyou.common.vo.PageResult;
import com.leyou.order.interceptor.LoginInterceptor;
import com.leyou.order.mapper.OrderDetailMapper;
import com.leyou.order.mapper.OrderMapper;
import com.leyou.order.mapper.OrderStatusMapper;
import com.leyou.order.pojo.Order;
import com.leyou.order.pojo.OrderDetail;
import com.leyou.order.pojo.OrderStatus;
import com.leyou.order.service.api.GoodsClient;
import com.leyou.utils.PayHelper;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;


@Service
public class OrderService {

    @Autowired
    private IdWorker idWorker;
    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private OrderDetailMapper detailMapper;
    @Autowired
    private OrderStatusMapper statusMapper;
    @Autowired
    private GoodsClient goodsClient;
    @Autowired
    private PayHelper payHelper;

    private static final Logger logger = LoggerFactory.getLogger(OrderService.class);

    /**
     * 不在此判断库存（线程不安全会造成超卖），在sql中判断库存：若库存不足sql抛出异常使事务回滚
     */
    @Transactional
    public Long createOrder(Order order) {
        // 生成orderId
        long orderId = idWorker.nextId();
        // 获取登录用户
        UserInfo user = LoginInterceptor.getLoginUser();
        // 初始化数据
        order.setBuyerNick(user.getUsername());
        order.setBuyerRate(false);
        order.setCreateTime(new Date());
        order.setOrderId(orderId);
        order.setUserId(user.getId());
        // 保存数据
        orderMapper.insertSelective(order);

        // 保存订单状态
        OrderStatus orderStatus = new OrderStatus();
        orderStatus.setOrderId(orderId);
        orderStatus.setCreateTime(order.getCreateTime());
        orderStatus.setStatus(1);// 初始状态为未付款

        statusMapper.insertSelective(orderStatus);

        // 订单详情中添加orderId
        order.getOrderDetails().forEach(od -> od.setOrderId(orderId));
        // 保存订单详情,使用批量插入功能
        detailMapper.insertList(order.getOrderDetails());

        //对订单中每一个商品减库存
        List<CartDTO> cartDTOS = new ArrayList<>();
        CartDTO cartDTO = new CartDTO();
        order.getOrderDetails().forEach(cart -> {
                    cartDTO.setSkuId(cart.getSkuId());
                    cartDTO.setNum(cart.getNum());
                    cartDTOS.add(cartDTO);
                }
        );
        goodsClient.decreaseStock(cartDTOS);

        logger.debug("生成订单，订单编号：{}，用户id：{}", orderId, user.getId());
        return orderId;
    }

    public Order queryById(Long id) {
        // 查询订单
        Order order = this.orderMapper.selectByPrimaryKey(id);

        // 查询订单详情
        OrderDetail detail = new OrderDetail();
        detail.setOrderId(id);
        List<OrderDetail> details = this.detailMapper.select(detail);
        order.setOrderDetails(details);

        // 查询订单状态
        OrderStatus status = this.statusMapper.selectByPrimaryKey(order.getOrderId());
        order.setStatus(status.getStatus());
        return order;
    }

    public PageResult<Order> queryUserOrderList(Integer page, Integer rows, Integer status) {
        try {
            // 分页
            PageHelper.startPage(page, rows);
            // 获取登录用户
            UserInfo user = LoginInterceptor.getLoginUser();
            // 创建查询条件
            Page<Order> pageInfo = (Page<Order>) this.orderMapper.queryOrderList(user.getId(), status);

            return new PageResult<>(pageInfo.getTotal(), pageInfo);
        } catch (Exception e) {
            logger.error("查询订单出错", e);
            return null;
        }
    }

    @Transactional
    public Boolean updateStatus(Long id, Integer status) {
        OrderStatus record = new OrderStatus();
        record.setOrderId(id);
        record.setStatus(status);
        // 根据状态判断要修改的时间
        switch (status) {
            case 2:
                record.setPaymentTime(new Date());// 付款
                break;
            case 3:
                record.setConsignTime(new Date());// 发货
                break;
            case 4:
                record.setEndTime(new Date());// 确认收获，订单结束
                break;
            case 5:
                record.setCloseTime(new Date());// 交易失败，订单关闭
                break;
            case 6:
                record.setCommentTime(new Date());// 评价时间
                break;
            default:
                return null;
        }
        int count = this.statusMapper.updateByPrimaryKeySelective(record);
        return count == 1;
    }


    public String createPayUrl(Long orderId) {
        //1、查询订单
        Order order = queryById(orderId);
        //2、判断订单状态
        Integer status = order.getStatus();
        if (status != 1) {  //若非未付款状态抛出异常
            //抛异常
        }
        //计算总支付金额
        Long actualPay = order.getActualPay();
        //商品描述
        String desc = "乐优商城支付测试";
        //向微信发起下单请求，取得返回的二维码付款地址
        return payHelper.createPayUrl(orderId, actualPay, desc);
    }

    /**
     * 完成对xml数据的校验
     *
     * @param result 支付成功后微信返回xml数据序列化后的map
     */
    public void handleNotify(Map<String, String> result) {
        String outTradeNoStr = result.get("out_trade_no");
        String totalFeeStr = result.get("total_fee");
        if (StringUtils.isBlank(outTradeNoStr) || StringUtils.isBlank(totalFeeStr)) {
            //TODO 抛出解析数据失败的异常
        }
        Long orderId = Long.parseLong(outTradeNoStr); //订单编号
        Long totalFee = Long.parseLong(totalFeeStr); //应付金额

        //1、根据通信、业务标识判断微信下单是否成功,若不成功则抛异常
        payHelper.isSuccess(orderId, result);
        //2、校验签名,若不成功则抛异常
        payHelper.validSign(result);

        //3、校验应付金额
        Order order = orderMapper.selectByPrimaryKey(orderId);
        if (totalFee != order.getActualPay()) {  //应付金额不符
            //TODO 抛出应付金额不符异常
        }

        //4、修改订单状态为已付款
        OrderStatus os = new OrderStatus();
        os.setStatus(2);
        os.setOrderId(orderId);
        os.setPaymentTime(new Date());
        int effectedRow = statusMapper.updateByPrimaryKeySelective(os);
        //修改订单状态失败
        if (effectedRow != 1) {
            //TODO 抛出修改订单状态失败异常
        }
    }
}
