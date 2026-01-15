package com.ai.domain.mysql.order;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Data
@Entity
@Table(name = "t_inventory")
public class TInventory {
    // 主键 ID（自增）
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    /**
     * 关联的商品
     * 用途：标识库存对应的商品实体，用于扣减/查询库存。
     * 约束：通常作为外键存在，删除商品时根据规则决定库存处理；不可用于暴露敏感商品供应链信息。
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "product_id", nullable = false)
    private TProduct product;

    /**
     * 仓库信息（关联仓库实体）
     * 用途：指明该库存位于哪个仓库，便于分配发货与查询库存分布。
     * 约束：仓库位置信息对外暴露时需注意权限与安全，不应泄露详细安全或内部管理信息。
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "warehouse_id", nullable = false)
    private TWarehouse warehouse;

    /**
     * 库存数量
     * 用途：用于判断是否可以发货、是否触发补货或售罄状态。
     * 约束：显示给用户的可用库存通常为业务上处理后的可售数量（扣除预留/占用）。
     */
    @ColumnDefault("0")
    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @PrePersist
    public void prePersist() {
        if (quantity == null) quantity = 0;
    }

}