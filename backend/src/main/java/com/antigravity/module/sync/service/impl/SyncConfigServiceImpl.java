package com.antigravity.module.sync.service.impl;

import com.antigravity.common.PageResult;
import com.antigravity.module.sync.dto.FieldMappingDTO;
import com.antigravity.module.sync.entity.FieldMapping;
import com.antigravity.module.sync.entity.SyncConfig;
import com.antigravity.module.sync.mapper.FieldMappingMapper;
import com.antigravity.module.sync.mapper.SyncConfigMapper;
import com.antigravity.module.sync.service.SyncConfigService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * 同步配置 Service 实现
 *
 * @author Antigravity Team
 * @since 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SyncConfigServiceImpl extends ServiceImpl<SyncConfigMapper, SyncConfig> implements SyncConfigService {

    private final FieldMappingMapper fieldMappingMapper;
    private final JdbcTemplate jdbcTemplate;

    @Override
    public Optional<SyncConfig> findById(Long id) {
        return Optional.ofNullable(this.getById(id));
    }

    @Override
    public PageResult<SyncConfig> pageQuery(int pageNumber, int pageSize, String name, String syncMode) {
        Page<SyncConfig> page = new Page<>(pageNumber, pageSize);
        LambdaQueryWrapper<SyncConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.isNotBlank(name), SyncConfig::getName, name)
                .eq(StringUtils.isNotBlank(syncMode), SyncConfig::getSyncMode, syncMode)
                .orderByDesc(SyncConfig::getCreateTime);

        Page<SyncConfig> resultPage = this.page(page, wrapper);
        return PageResult.of(resultPage.getRecords(), pageNumber, pageSize, resultPage.getTotal());
    }

    @Override
    public boolean existsByName(String name) {
        LambdaQueryWrapper<SyncConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SyncConfig::getName, name);
        return this.count(wrapper) > 0;
    }

    private void cleanupSoftDeletedConfig(String name) {
        if (name == null || name.isBlank()) {
            return;
        }
        try {
            // 查询同名且已软删除的配置 ID
            String querySql = "SELECT id FROM sys_sync_config WHERE name = ? AND is_deleted = 1";
            List<Long> ids = jdbcTemplate.query(querySql, (rs, rowNum) -> rs.getLong("id"), name);
            if (ids != null && !ids.isEmpty()) {
                for (Long oldId : ids) {
                    log.info("发现同名已软删除配置(id={})，执行物理清除以避免唯一键冲突...", oldId);
                    // 删除关联的字段映射
                    jdbcTemplate.update("DELETE FROM sys_field_mapping WHERE config_id = ?", oldId);
                    // 物理删除该配置
                    jdbcTemplate.update("DELETE FROM sys_sync_config WHERE id = ?", oldId);
                }
            }
        } catch (Exception e) {
            log.error("清理软删除配置失败: {}", e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public SyncConfig createConfig(SyncConfig config, List<FieldMappingDTO> mappings) {
        // 自动清理同名的已软删除的配置，防止唯一键冲突
        cleanupSoftDeletedConfig(config.getName());

        // 设置默认值
        if (config.getEnabled() == null) {
            config.setEnabled(true);
        }
        if (config.getSyncMode() == null) {
            config.setSyncMode("FULL");
        }
        config.setIsDeleted(false);
        this.save(config);

        // 保存字段映射
        if (mappings != null && !mappings.isEmpty()) {
            for (int i = 0; i < mappings.size(); i++) {
                FieldMapping entity = mappings.get(i).toEntity(config.getId());
                if (entity.getSortOrder() == null || entity.getSortOrder() == 0) {
                    entity.setSortOrder(i + 1);
                }
                fieldMappingMapper.insert(entity);
            }
        }

        log.info("创建同步配置成功: id={}, name={}", config.getId(), config.getName());
        return config;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateConfig(SyncConfig config, List<FieldMappingDTO> mappings) {
        // 自动清理同名的已软删除的配置，防止唯一键冲突
        cleanupSoftDeletedConfig(config.getName());

        boolean updated = this.updateById(config);

        // 如果提供了字段映射，则全量替换
        if (mappings != null) {
            // 删除原有映射
            LambdaQueryWrapper<FieldMapping> deleteWrapper = new LambdaQueryWrapper<>();
            deleteWrapper.eq(FieldMapping::getConfigId, config.getId());
            fieldMappingMapper.delete(deleteWrapper);

            // 插入新映射
            for (int i = 0; i < mappings.size(); i++) {
                FieldMapping entity = mappings.get(i).toEntity(config.getId());
                if (entity.getSortOrder() == null || entity.getSortOrder() == 0) {
                    entity.setSortOrder(i + 1);
                }
                fieldMappingMapper.insert(entity);
            }
        }

        log.info("更新同步配置: id={}, success={}", config.getId(), updated);
        return updated;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteConfig(Long id) {
        // 先删除字段映射
        LambdaQueryWrapper<FieldMapping> deleteWrapper = new LambdaQueryWrapper<>();
        deleteWrapper.eq(FieldMapping::getConfigId, id);
        fieldMappingMapper.delete(deleteWrapper);

        // 再删除配置（逻辑删除）
        boolean deleted = this.removeById(id);
        log.info("删除同步配置: id={}, success={}", id, deleted);
        return deleted;
    }

    @Override
    public List<FieldMappingDTO> getFieldMappings(Long configId) {
        LambdaQueryWrapper<FieldMapping> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FieldMapping::getConfigId, configId)
                .orderByAsc(FieldMapping::getSortOrder);
        List<FieldMapping> mappings = fieldMappingMapper.selectList(wrapper);
        if (mappings == null || mappings.isEmpty()) {
            return Collections.emptyList();
        }
        return mappings.stream().map(FieldMappingDTO::fromEntity).toList();
    }

}
