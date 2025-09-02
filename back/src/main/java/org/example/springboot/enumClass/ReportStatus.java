package org.example.springboot.enumClass;

import lombok.Getter;

/**
 * 举报状态枚举
 */
@Getter
public enum ReportStatus {
    PENDING(0, "未处理"),
    PROCESSED(1, "已处理"),
    REJECTED(2, "已驳回");
    
    private final Integer value;
    private final String desc;
    
    ReportStatus(Integer value, String desc) {
        this.value = value;
        this.desc = desc;
    }
} 