package com.kaylves.interfacex.ui.navigator;

import com.intellij.util.xmlb.annotations.Tag;
import com.kaylves.interfacex.entity.InterfaceItemConfigEntity;
import lombok.Data;
import org.jdom.Element;

import java.util.List;

/**
 * @author kaylves
 */
@Data
public class InterfaceXNavigatorState {

    public boolean showPort = true;

    @Tag("treeState")
    public Element treeState;


    private List<InterfaceItemConfigEntity> interfaceItemConfigEntities;
}
