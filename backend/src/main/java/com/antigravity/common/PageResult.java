package com.antigravity.common;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 分页结果
 *
 * @param <T> 数据类型
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PageResult<T> {

    /**
     * 数据列表
     */
    private List<T> records;

    /**
     * 当前页码
     */
    private int pageNumber;

    /**
     * 每页大小
     */
    private int pageSize;

    /**
     * 总记录数
     */
    private long totalRow;

    /**
     * 总页数
     */
    private int totalPage;

    public static <T> PageResult<T> of(List<T> records, int pageNumber, int pageSize, long totalRow) {
        int totalPage = (int) Math.ceil((double) totalRow / pageSize);
        return new PageResult<>(records, pageNumber, pageSize, totalRow, totalPage);
    }

}
