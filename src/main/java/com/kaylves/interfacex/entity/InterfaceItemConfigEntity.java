package com.kaylves.interfacex.entity;

import lombok.Data;

/**
 * @author kaylves
 */
@Data
public class InterfaceItemConfigEntity {

    private String itemCategory;

    private Boolean enabled;

    public InterfaceItemConfigEntity(String itemCategory, Boolean enabled) {
        this.itemCategory = itemCategory;
        this.enabled = enabled;
    }

    public InterfaceItemConfigEntity() {
    }
}
