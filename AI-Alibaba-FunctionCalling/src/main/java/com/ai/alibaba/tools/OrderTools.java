package com.ai.alibaba.tools;

import com.ai.alibaba.client.AiWebClient;
import com.ai.alibaba.dto.OrderDTO;
import com.ai.alibaba.service.OrderService;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

@Component
public class OrderTools {

    private final OrderService orderService;

    private final AiWebClient aiWebClient;

    public OrderTools(OrderService orderService,
                      AiWebClient aiWebClient) {
        this.orderService = orderService;
        this.aiWebClient = aiWebClient;
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

    @Tool(description = """
            远程调用api获取所有订单信息。
            无输入参数，返回所有订单信息的列表。
            返回类型为 List<OrderDTO>，包含所有订单的详细信息。
            OrderDTO 包含以下字段：
            - orderId: 订单ID
            - customerName: 客户名称
            - orderDate: 订单日期
            - items: 订单项列表
            - totalAmount: 订单总金额
            """)
    public List<OrderDTO> findAllOrder() {
        return aiWebClient.findAllOrder();
    }

}
