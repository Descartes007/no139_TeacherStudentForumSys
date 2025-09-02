package org.example.springboot.enumClass;

import lombok.Getter;

/**
 * 举报类型枚举
 */
@Getter
public enum ReportType {
    POST(1, "帖子"),
    COMMENT(2, "评论"),
    RESOURCE(3, "资源");
    
    private final Integer value;
    private final String desc;
    
    ReportType(Integer value, String desc) {
        this.value = value;
        this.desc = desc;
    }
} 