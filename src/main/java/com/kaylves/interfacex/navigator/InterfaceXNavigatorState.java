package com.kaylves.interfacex.navigator;

import com.intellij.util.xmlb.annotations.Tag;
import lombok.Data;
import org.jdom.Element;

@Data
public class InterfaceXNavigatorState {

    public boolean showPort = true;

    @Tag("treeState")
    public Element treeState;

    /**
     * 是否控制显示未使用
     */
    private final boolean displayUseAble=false;

}
