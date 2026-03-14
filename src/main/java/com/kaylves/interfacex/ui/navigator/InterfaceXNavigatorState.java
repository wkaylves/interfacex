package com.kaylves.interfacex.ui.navigator;

import com.kaylves.interfacex.entity.InterfaceItemConfigEntity;
import lombok.Data;

import java.util.List;

/**
 * @author kaylves
 */
@Data
public class InterfaceXNavigatorState {

    public boolean showPort = true;

    private List<InterfaceItemConfigEntity> interfaceItemConfigEntities;
}
