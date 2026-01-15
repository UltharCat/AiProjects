package com.ai.service;

import com.ai.dto.OrderDTO;

import java.util.List;
import java.util.Optional;

public interface OrderService {

    /**
     * 获取所有订单信息
     *
     * @return 包含所有订单信息的列表的 Optional 对象
     */
    Optional<List<OrderDTO>> findAllOrder();

    /**
     * 根据客户姓名查找订单信息
     *
     * @param customerId 客户姓名
     * @return 包含匹配订单信息的列表的 Optional 对象
     */
    Optional<List<OrderDTO>> findOrdersByCustomerId(Long customerId);

    /**
     * 根据用户姓名和订单ID取消订单
     *
     * @param customerId 客户ID
     * @param orderId 订单ID
     * @return 取消操作是否成功的布尔值
     */
    boolean cancelOrderById(Long customerId, Long orderId);

}
