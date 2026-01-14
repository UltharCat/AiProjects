package com.ai.repo;

import com.ai.domain.mysql.order.TInventory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InventoryRepository extends JpaRepository<TInventory, Long> {

    List<TInventory> findByProductId(Long productId);

}

