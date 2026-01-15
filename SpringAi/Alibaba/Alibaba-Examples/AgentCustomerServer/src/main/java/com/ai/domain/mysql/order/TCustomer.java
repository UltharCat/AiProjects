package com.ai.domain.mysql.order;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "t_customer")
public class TCustomer {
    // 主键 ID（自增）
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    /**
     * 客户姓名/收件人姓名
     * 用途：用于在订单与包裹上显示、人工客服核实身份。
     * 隐私/约束：如非必要，返回给用户或外部系统时可做部分脱敏（例如只显示姓或首字母）。
     */
    @Column(name = "name", nullable = false, length = 100)
    private String name;

    /**
     * 客户邮箱
     * 用途：用于发送电子发票、订单通知与客服沟通记录。
     * 隐私/约束：不得在公开场景泄露完整邮箱；外呼/展示时可做脱敏处理（如****@domain）。
     */
    @Column(name = "email", nullable = false)
    private String email;

    /**
     * 收货地址（可能包含省市区与详细街道门牌）
     * 用途：用于发货、配送地址核验。
     * 隐私/约束：严禁收集或存储超过业务需要的敏感地址信息；展示给用户或第三方时应遵循最小暴露原则与脱敏规则。
     */
    @Column(name = "address", length = 500)
    private String address;

}