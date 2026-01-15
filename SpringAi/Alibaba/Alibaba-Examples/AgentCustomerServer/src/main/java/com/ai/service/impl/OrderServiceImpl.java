package com.ai.service.impl;

import com.ai.dto.OrderDTO;
import com.ai.domain.mysql.order.TInventory;
import com.ai.domain.mysql.order.TOrder;
import com.ai.domain.mysql.order.TOrderItem;
import com.ai.repo.InventoryRepository;
import com.ai.repo.OrderItemRepository;
import com.ai.repo.OrderRepository;
import com.ai.service.OrderService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final InventoryRepository inventoryRepository;

    public OrderServiceImpl(OrderRepository orderRepository,
                            OrderItemRepository orderItemRepository,
                            InventoryRepository inventoryRepository) {
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.inventoryRepository = inventoryRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<List<OrderDTO>> findAllOrder() {
        return Optional.of(orderRepository.findAll().stream().map(e -> OrderDTO.pojoToDTO(e, true)).toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<List<OrderDTO>> findOrdersByCustomerId(Long customerId) {
        return Optional.of(orderRepository.findByCustomerId(customerId).stream().map(e -> OrderDTO.pojoToDTO(e, true)).toList());
    }

    @Override
    @Transactional
    public boolean cancelOrderById(Long customerId, Long orderId) {
        TOrder order = orderRepository.findByIdAndCustomerId(orderId, customerId);
        if (order.getCustomer() == null || !order.getCustomer().getId().equals(customerId)) {
            return false;
        }
        // 仅允许PENDING状态的订单取消
        if (!"PENDING".equalsIgnoreCase(order.getStatus())) {
            return false;
        }

        // 设置状态为CANCELLED
        order.setStatus("CANCELLED");
        orderRepository.save(order);

        // 重设订单商品库存信息
        List<TOrderItem> items = orderItemRepository.findByOrderId(orderId);
        for (TOrderItem item : items) {
            // 获取购买数量
            Integer qty = item.getQuantity();
            // 获取库存信息
            TInventory inventory = item.getInventory();
            if (Objects.nonNull(inventory)) {
                // 重设库存资料
                inventory.setQuantity(inventory.getQuantity() + (qty == null ? 0 : qty));
                inventoryRepository.save(inventory);
            }
        }
        return true;
    }

}
