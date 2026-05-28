package com.antigravity.module.sync.engine.dialect;

import com.antigravity.common.BusinessException;

import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * 数据库方言工厂
 * <p>
 * 根据数据库类型获取对应的 {@link DatabaseDialect} 实例。
 * <p>
 * 扩展新数据库类型只需：
 * <ol>
 *   <li>创建新的 {@link DatabaseDialect} 实现类</li>
 *   <li>在 {@link #DIALECT_REGISTRY} 中注册</li>
 * </ol>
 *
 * @author Antigravity Team
 * @since 1.0.0
 */
public final class DatabaseDialectFactory {

    /**
     * 方言注册表 — 新增数据库类型时只需在此添加一行
     */
    private static final Map<String, Supplier<DatabaseDialect>> DIALECT_REGISTRY = Map.of(
            "MYSQL", MySqlDialect::new,
            "POSTGRESQL", PostgreSqlDialect::new,
            "ORACLE", OracleDialect::new,
            "SQLSERVER", SqlServerDialect::new
    );

    private DatabaseDialectFactory() {
        // 工具类不允许实例化
    }

    /**
     * 根据数据库类型获取方言实例
     *
     * @param dbType 数据库类型（不区分大小写）
     * @return 对应的方言实例
     * @throws BusinessException 如果不支持该数据库类型
     */
    public static DatabaseDialect getDialect(String dbType) {
        if (dbType == null || dbType.isBlank()) {
            throw BusinessException.of("数据库类型不能为空");
        }

        Supplier<DatabaseDialect> supplier = DIALECT_REGISTRY.get(dbType.toUpperCase());
        if (supplier == null) {
            throw BusinessException.of("不支持的数据库类型: " + dbType
                    + "，当前支持: " + String.join(", ", getSupportedTypes()));
        }

        return supplier.get();
    }

    /**
     * 获取所有支持的数据库类型
     *
     * @return 支持的数据库类型列表
     */
    public static List<String> getSupportedTypes() {
        return DIALECT_REGISTRY.keySet().stream()
                .sorted()
                .collect(Collectors.toList());
    }
}
