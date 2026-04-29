package com.kaylves.interfacex.db.storage;

public enum StorageType {
    SQLITE,
    XML;

    public static StorageType fromString(String value) {
        if (value == null || value.isEmpty()) {
            return SQLITE;
        }
        try {
            return valueOf(value.toUpperCase());
        } catch (IllegalArgumentException e) {
            return SQLITE;
        }
    }
}
