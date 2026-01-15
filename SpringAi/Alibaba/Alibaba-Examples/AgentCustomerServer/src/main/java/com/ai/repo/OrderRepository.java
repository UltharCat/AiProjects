package com.ai.repo;

import com.ai.domain.mysql.order.TOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface OrderRepository extends JpaRepository<TOrder, Long> {

    List<TOrder> findByCustomerId(Long customerId);

    TOrder findByIdAndCustomerId(Long id, Long customerId);

}
