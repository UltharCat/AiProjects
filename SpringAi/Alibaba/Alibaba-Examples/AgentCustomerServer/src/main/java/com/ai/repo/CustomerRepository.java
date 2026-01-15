package com.ai.repo;

import com.ai.domain.mysql.order.TCustomer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface CustomerRepository extends JpaRepository<TCustomer, Long> {

}
