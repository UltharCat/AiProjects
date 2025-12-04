package com.ai.alibaba.service.impl;

import com.ai.alibaba.dto.OrderDTO;
import com.ai.alibaba.repository.OrderRepository;
import com.ai.alibaba.service.OrderService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;

    public OrderServiceImpl(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<List<OrderDTO>> findAllOrder() {
        return Optional.of(orderRepository.findAll().stream().map(e -> OrderDTO.pojoToDTO(e, true)).toList());
    }

}
