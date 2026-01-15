package com.ai.repo;

import com.ai.domain.mysql.order.TProduct;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface ProductRepository extends JpaRepository<TProduct, Long> {

}
