package com.ai.repo;

import com.ai.domain.mysql.order.TOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface OrderRepository extends JpaRepository<TOrder, Long> {

}
