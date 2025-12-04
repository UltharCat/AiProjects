package com.ai.alibaba.repository;

import com.ai.alibaba.entity.TOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface OrderRepository extends JpaRepository<TOrder, Long> {

}
