package org.example.springboot.enumClass;

import lombok.Getter;

/**
 * 账号状态枚举
 */
@Getter
public enum AccountStatus {
    DISABLED(0, "禁用"),
    NORMAL(1, "正常"),
    PENDING_REVIEW(2, "待审核"),
    REVIEW_FAILED(3, "审核不通过");

    private final Integer value;
    private final String label;

    AccountStatus(Integer value, String label) {
        this.value = value;
        this.label = label;
    }
}