package com.antigravity.module.sync.dto;

import com.antigravity.module.sync.entity.FieldMapping;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 字段映射 DTO（创建和响应共用）
 *
 * @author Antigravity Team
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FieldMappingDTO {

    private Long id;

    @NotBlank(message = "源字段名不能为空")
    @Size(max = 100, message = "源字段名长度不能超过 100 个字符")
    private String sourceField;

    @Size(max = 50, message = "源字段类型长度不能超过 50 个字符")
    private String sourceType;

    @NotBlank(message = "目标字段名不能为空")
    @Size(max = 100, message = "目标字段名长度不能超过 100 个字符")
    private String targetField;

    @Size(max = 50, message = "目标字段类型长度不能超过 50 个字符")
    private String targetType;

    @Size(max = 500, message = "转换表达式长度不能超过 500 个字符")
    private String transformExpr;

    private Integer sortOrder;

    /**
     * 从实体转换
     */
    public static FieldMappingDTO fromEntity(FieldMapping entity) {
        if (entity == null) return null;
        return FieldMappingDTO.builder()
                .id(entity.getId())
                .sourceField(entity.getSourceField())
                .sourceType(entity.getSourceType())
                .targetField(entity.getTargetField())
                .targetType(entity.getTargetType())
                .transformExpr(entity.getTransformExpr())
                .sortOrder(entity.getSortOrder())
                .build();
    }

    /**
     * 转换为实体
     */
    public FieldMapping toEntity(Long configId) {
        FieldMapping entity = new FieldMapping();
        entity.setConfigId(configId);
        entity.setSourceField(this.sourceField);
        entity.setSourceType(this.sourceType);
        entity.setTargetField(this.targetField);
        entity.setTargetType(this.targetType);
        entity.setTransformExpr(this.transformExpr);
        entity.setSortOrder(this.sortOrder != null ? this.sortOrder : 0);
        return entity;
    }

}
