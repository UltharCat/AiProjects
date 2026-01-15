package com.ai.alibaba.service;

import com.ai.alibaba.dto.OrderDTO;

import java.util.List;
import java.util.Optional;

public interface OrderService {

    Optional<List<OrderDTO>> findAllOrder();

}
