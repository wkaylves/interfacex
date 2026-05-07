package com.kaylves.interfacex.db.storage;

/**
 * 存储类型枚举
 * <p>定义 InterfaceX 支持的存储后端类型</p>
 * <ul>
 *   <li>SQLITE - SQLite 数据库存储（默认），数据保存在 ~/.interfacex/interfacex.db</li>
 *   <li>XML - XML 文件存储，数据保存在项目 .idea/InterfaceX-data.xml</li>
 * </ul>
 */
public enum StorageType {

    SQLITE,
    XML;

    /**
     * 从字符串解析存储类型
     * <p>空值或无效值默认返回 SQLITE</p>
     *
     * @param value 存储类型字符串（不区分大小写）
     * @return 对应的 StorageType，无效值返回 SQLITE
     */
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
