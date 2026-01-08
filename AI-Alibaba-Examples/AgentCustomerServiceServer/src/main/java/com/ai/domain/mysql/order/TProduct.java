package com.ai.domain.mysql.order;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Entity
@Table(name = "t_product")
public class TProduct {
    // 主键 ID（自增）
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    /**
     * 商品名称
     * 用途：用于客服向用户确认商品，生成订单明细与展示。
     * 约束：展示给用户时应使用标准化名称，避免包含内部备注或敏感供应链信息。
     */
    @Column(name = "name", nullable = false)
    private String name;

    /**
     * SKU 或商品编码
     * 用途：用于精准匹配商品规格、库存与条目核对。
     * 约束：如对外展示应保证外部用户能理解该编码的上下文，否则以人类可读名称为主。
     */
    @Column(name = "sku", nullable = false, length = 100)
    private String sku;

    /**
     * 商品描述（可能较长）
     * 用途：存储商品详情、参数与卖点。
     * 约束：不得包含违反政策的内容或未经授权的第三方版权材料。
     */
    @Lob
    @Column(name = "description")
    private String description;

    /**
     * 商品价格（当前或标准价，视业务而定）
     * 用途：用于展示、计算订单金额与价保逻辑。
     * 约束：与订单明细中的 unit_price 可能不同，价保校验需基于业务规则。
     */
    @Column(name = "price", nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

}