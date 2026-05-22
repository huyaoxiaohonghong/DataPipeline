package com.antigravity.module.sync.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.util.List;

/**
 * 同步配置创建请求 DTO
 *
 * @author Antigravity Team
 * @since 1.0.0
 */
@Data
public class SyncConfigCreateRequest {

    @NotBlank(message = "配置名称不能为空")
    @Size(max = 100, message = "配置名称长度不能超过 100 个字符")
    private String name;

    @Size(max = 500, message = "描述长度不能超过 500 个字符")
    private String description;

    @NotNull(message = "源数据库连接ID不能为空")
    private Long sourceDbId;

    @NotBlank(message = "源表名不能为空")
    @Size(max = 200, message = "源表名长度不能超过 200 个字符")
    private String sourceTable;

    @NotNull(message = "目标数据库连接ID不能为空")
    private Long targetDbId;

    @NotBlank(message = "目标表名不能为空")
    @Size(max = 200, message = "目标表名长度不能超过 200 个字符")
    private String targetTable;

    @NotBlank(message = "同步模式不能为空")
    @Pattern(regexp = "^(FULL|INCREMENTAL)$", message = "同步模式必须是 FULL 或 INCREMENTAL")
    private String syncMode;

    /** 增量字段（增量模式必填） */
    @Size(max = 100, message = "增量字段长度不能超过 100 个字符")
    private String incrementalField;

    /** 字段映射列表 */
    @Valid
    private List<FieldMappingDTO> fieldMappings;

}
