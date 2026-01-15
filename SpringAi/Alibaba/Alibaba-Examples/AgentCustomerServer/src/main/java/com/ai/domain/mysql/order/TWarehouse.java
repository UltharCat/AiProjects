package com.ai.domain.mysql.order;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "t_warehouse")
public class TWarehouse {
    // 主键 ID（自增）
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    /**
     * 仓库名称
     * 用途：用于在发货与库存分配中标识仓库。
     * 约束：对外展示需遵循权限策略，避免暴露过于详细的内部管理或安全信息。
     */
    @Column(name = "name", nullable = false)
    private String name;

    /**
     * 仓库地址/位置描述
     * 用途：用于物流分配与异常处理（例如判断离用户的距离或配送时效）。
     * 约束：对外展示需最小化敏感信息，遵守隐私与安全策略。
     */
    @Column(name = "location", length = 500)
    private String location;

}