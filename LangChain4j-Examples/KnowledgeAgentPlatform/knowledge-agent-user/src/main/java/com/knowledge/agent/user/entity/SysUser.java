package com.knowledge.agent.user.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 系统用户实体类
 */
@Data
@TableName("sys_user")
public class SysUser {
    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 用户名
     */
    private String username;

    /**
     * 密码哈希
     */
    private String password;

    /**
     * 学习风格 (SOCRATIC / DIRECT / ELABORATE)
     * 默认为 SOCRATIC
     */
    private String learningStyle;

    /**
     * Structured user preferences encoded as JSON for future evolution.
     */
    private String preferencesJson;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

    /**
     * 删除标志
     */
    private Integer deleted;
}
