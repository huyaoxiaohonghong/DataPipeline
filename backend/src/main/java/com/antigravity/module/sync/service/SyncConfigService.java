package com.antigravity.module.sync.service;

import com.antigravity.common.PageResult;
import com.antigravity.module.sync.dto.FieldMappingDTO;
import com.antigravity.module.sync.entity.SyncConfig;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;
import java.util.Optional;

/**
 * 同步配置 Service 接口
 *
 * @author Antigravity Team
 * @since 1.0.0
 */
public interface SyncConfigService extends IService<SyncConfig> {

    /**
     * 按 ID 查找（含字段映射）
     */
    Optional<SyncConfig> findById(Long id);

    /**
     * 分页查询同步配置
     */
    PageResult<SyncConfig> pageQuery(int pageNumber, int pageSize, String name, String syncMode);

    /**
     * 检查名称是否存在
     */
    boolean existsByName(String name);

    /**
     * 创建同步配置（含字段映射）
     */
    SyncConfig createConfig(SyncConfig config, List<FieldMappingDTO> mappings);

    /**
     * 更新同步配置（含字段映射）
     */
    boolean updateConfig(SyncConfig config, List<FieldMappingDTO> mappings);

    /**
     * 删除同步配置（级联删除字段映射）
     */
    boolean deleteConfig(Long id);

    /**
     * 获取配置关联的字段映射
     */
    List<FieldMappingDTO> getFieldMappings(Long configId);

}
