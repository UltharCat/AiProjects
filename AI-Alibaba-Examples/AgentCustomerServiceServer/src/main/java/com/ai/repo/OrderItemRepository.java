package com.ai.repo;

import com.ai.domain.mysql.order.TOrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderItemRepository extends JpaRepository<TOrderItem, Long> {

    List<TOrderItem> findByOrderId(Long orderId);

}

