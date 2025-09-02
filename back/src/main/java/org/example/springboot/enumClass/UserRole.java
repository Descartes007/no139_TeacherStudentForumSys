package org.example.springboot.enumClass;

import lombok.Getter;

/**
 * 用户角色枚举
 */
@Getter
public enum UserRole {
    ADMIN(1, "管理员"),
    TEACHER(2, "教师"),
    STUDENT(3, "学生");

    private final Integer value;
    private final String label;

    UserRole(Integer value, String label) {
        this.value = value;
        this.label = label;
    }
} 