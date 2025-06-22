package com.coditas.tool.management.system.constant;

public enum ToolCategory {
    SPECIAL, NORMAL;

    public boolean isSpecial() {
        return this == SPECIAL;
    }
}
