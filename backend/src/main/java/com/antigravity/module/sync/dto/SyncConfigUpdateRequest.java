package com.antigravity.module.sync.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.util.List;

/**
 * 同步配置更新请求 DTO
 *
 * @author Antigravity Team
 * @since 1.0.0
 */
@Data
public class SyncConfigUpdateRequest {

    @Size(max = 100, message = "配置名称长度不能超过 100 个字符")
    private String name;

    @Size(max = 500, message = "描述长度不能超过 500 个字符")
    private String description;

    private Long sourceDbId;

    @Size(max = 200, message = "源表名长度不能超过 200 个字符")
    private String sourceTable;

    private Long targetDbId;

    @Size(max = 200, message = "目标表名长度不能超过 200 个字符")
    private String targetTable;

    @Pattern(regexp = "^(FULL|INCREMENTAL|REALTIME)$", message = "同步模式必须是 FULL、INCREMENTAL 或 REALTIME")
    private String syncMode;

    @Size(max = 100, message = "增量字段长度不能超过 100 个字符")
    private String incrementalField;

    /** 实时计算SQL表达式（可选） */
    private String transformSql;

    private Boolean enabled;

    /** 字段映射列表（如果提供，则全量替换） */
    @Valid
    private List<FieldMappingDTO> fieldMappings;

}
