package com.antigravity.module.sync.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 字段映射实体
 *
 * @author Antigravity Team
 * @since 1.0.0
 */
@Data
@TableName("sys_field_mapping")
public class FieldMapping implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 关联同步配置ID */
    private Long configId;

    /** 源字段名 */
    private String sourceField;

    /** 源字段类型 */
    private String sourceType;

    /** 目标字段名 */
    private String targetField;

    /** 目标字段类型 */
    private String targetType;

    /** 转换表达式（可选） */
    private String transformExpr;

    /** 排序 */
    private Integer sortOrder;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

}
