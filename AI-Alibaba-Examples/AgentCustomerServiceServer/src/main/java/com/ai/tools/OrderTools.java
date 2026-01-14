package com.ai.tools;

import com.ai.dto.OrderDTO;
import com.ai.service.OrderService;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Objects;

@Component
public class OrderTools {

    private final OrderService orderService;

    public OrderTools(OrderService orderService) {
        this.orderService = orderService;
    }

    @Tool(description = """
            获取指定订单ID的订单信息。
            输入订单ID，返回对应的订单信息。
            返回类型为 OrderDTO 对象，包含订单的详细信息。
            OrderDTO 包含以下字段：
            - orderId: 订单ID
            - customerName: 客户名称
            - orderDate: 订单日期
            - items: 订单项列表
            - totalAmount: 订单总金额
            """)
    public OrderDTO getOrderById(@ToolParam(description = "订单ID，类型为Long") Long orderId) {
        var orderList = orderService.findAllOrder().orElseGet(Collections::emptyList);
        return orderList.stream().filter(e-> Objects.equals(e.getId(), orderId)).findFirst().orElse(null);
    }

}
