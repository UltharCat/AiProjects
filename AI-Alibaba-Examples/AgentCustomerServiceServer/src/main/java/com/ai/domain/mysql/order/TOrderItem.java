package com.ai.domain.mysql.order;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.math.BigDecimal;

@Data
@Entity
@Table(name = "t_order_item")
public class TOrderItem {
    // 主键 ID（自增）
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    /**
     * 所属订单
     * 用途：将明细与订单关联，便于客服查看具体购买的商品与数量。
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "order_id", nullable = false)
    private TOrder order;

    /**
     * 关联商品
     * 用途：指明该订单行对应的商品，用于核对商品名称/规格/SKU 等。
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "product_id", nullable = false)
    private TProduct product;

    /**
     * 购买数量
     * 用途：用于处理少件/补发/退款时的数量判断。展示给用户时应与订单明细一致。
     */
    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    /**
     * 单价（下单时的价格）
     * 用途：用于退款/价保等金额计算，注意与商品当前价格可能不同。
     */
    @Column(name = "unit_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal unitPrice;

}