package com.ai.alibaba.dto;

import com.ai.alibaba.entity.TCustomer;
import com.ai.alibaba.entity.TOrder;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderDTO {

    private Long id;

    private String orderNumber;

    private CustomerDTO customer;

    private Instant orderDate;

    private BigDecimal totalAmount;

    private String status;

    /**
     * Convert TOrder to OrderDTO
     * @param order
     * @param flag true: with customer info; false: without customer info
     * @return
     */
    public static OrderDTO pojoToDTO(TOrder order, boolean flag) {
        if (flag && order.getCustomer() != null) {
            TCustomer customer = order.getCustomer();
            return new OrderDTO(
                    order.getId(),
                    order.getOrderNumber(),
                    new CustomerDTO(
                            customer.getId(),
                            customer.getName(),
                            customer.getEmail(),
                            customer.getAddress()
                    ),
                    order.getOrderDate(),
                    order.getTotalAmount(),
                    order.getStatus()
            );
        } else {
            return new OrderDTO(
                    order.getId(),
                    order.getOrderNumber(),
                    null,
                    order.getOrderDate(),
                    order.getTotalAmount(),
                    order.getStatus()
            );
        }
    }

}
