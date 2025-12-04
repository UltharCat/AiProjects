package com.ai.alibaba.config;

import com.ai.alibaba.dto.OrderDTO;
import com.ai.alibaba.service.OrderService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Description;

import java.util.Collections;
import java.util.List;
import java.util.function.Function;

@Configuration
public class FunctionConfig {

    private final OrderService orderService;

    public FunctionConfig(OrderService orderService) {
        this.orderService = orderService;
    }

    @Bean
    @Description("""
            获取所有订单信息
            该函数不需要输入参数
            返回值类型: List<TOrder>，包含所有订单的详细信息，包括关联的客户信息
            """)
    public Function<Object, List<OrderDTO>> orderFunction() {
        return input -> {
            // 在这里实现你的函数逻辑
            return orderService.findAllOrder().orElseGet(Collections::emptyList);
        };
    }

}
