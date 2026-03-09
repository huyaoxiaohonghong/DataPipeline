package com.antigravity.module.log.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("sys_operation_log")
public class OperationLog {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String username;
    private String module;
    private String action;
    private String method;
    private String params;
    private String ipAddress;
    private Long duration; // in milliseconds
    private Integer status; // 1: success, 0: fail
    private String errorMsg;
    private LocalDateTime createTime;
}
