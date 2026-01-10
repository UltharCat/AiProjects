package com.ai.domain.mysql.order;

import jakarta.persistence.*;
import lombok.Data;
import opennlp.tools.util.StringUtil;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@Entity
@Table(name = "t_order")
public class TOrder {

    // 主键 ID（自增）
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    /**
     * 订单号（业务唯一标识）
     * 用途：用于外部查询、客服核实与系统关联。
     * 约束：在向用户或日志输出中应注意脱敏或以受控方式展示，避免泄露关联支付/渠道细节。
     */
    @Column(name = "order_number", nullable = false, length = 100)
    private String orderNumber;

    /**
     * 关联客户（下单人）
     * 用途：指向下单用户/收件人信息，用于售后与查询。
     * 约束：对外共享需遵守隐私策略，不暴露除必要字段外的个人信息。
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @OnDelete(action = OnDeleteAction.SET_NULL)
    @JoinColumn(name = "customer_id")
    private TCustomer customer;

    /**
     * 下单时间
     * 用途：用于判断售后时效、活动优惠核算等。
     */
    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "order_date", nullable = false)
    private Instant orderDate;

    /**
     * 订单总金额（含税/含运费视业务而定）
     * 用途：用于退款/价保/财务核验等。
     * 约束：金额展示需与业务规则一致，金额变更需有审计记录。
     */
    @Column(name = "total_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalAmount;

    /**
     * 订单状态（例如 PENDING/PAID/SHIPPED/CANCELLED）
     * 用途：驱动订单流程与客服应答策略（拦截、取消、退货等）。
     * 约束：状态变更应有完整操作记录，不应允许客服无审批直接进行关键性状态跳变。
     */
    @ColumnDefault("'PENDING'")
    @Column(name = "status", nullable = false, length = 50)
    private String status;

    @PrePersist
    public void prePersist() {
        if (orderDate == null) orderDate = Instant.now();
        if (StringUtil.isEmpty(status)) status = "PENDING";
    }

}