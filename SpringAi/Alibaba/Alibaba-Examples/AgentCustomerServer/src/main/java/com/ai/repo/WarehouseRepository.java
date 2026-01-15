package com.ai.repo;

import com.ai.domain.mysql.order.TWarehouse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface WarehouseRepository extends JpaRepository<TWarehouse, Long> {

}
