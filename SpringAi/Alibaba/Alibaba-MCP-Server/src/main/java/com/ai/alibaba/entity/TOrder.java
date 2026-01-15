package com.ai.alibaba.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@Entity
@Table(name = "t_order")
public class TOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "order_number", nullable = false, length = 100)
    private String orderNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @OnDelete(action = OnDeleteAction.SET_NULL)
    @JoinColumn(name = "customer_id")
    private TCustomer customer;

    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "order_date", nullable = false)
    private Instant orderDate;

    @Column(name = "total_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalAmount;

    @ColumnDefault("'PENDING'")
    @Column(name = "status", nullable = false, length = 50)
    private String status;

}