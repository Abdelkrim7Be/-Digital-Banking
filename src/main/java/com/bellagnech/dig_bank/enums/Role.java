package com.bellagnech.dig_bank.enums;

/**
 * Enum representing user roles in the digital banking system
 */
public enum Role {
    ADMIN("ADMIN"),
    CUSTOMER("CUSTOMER");

    private final String value;

    Role(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return value;
    }
}
