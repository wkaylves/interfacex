package com.kaylves.interfacex.utils;

import com.kaylves.interfacex.common.InterfaceItem;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class PartnerGrouper {

    private static final String DEFAULT_PARTNER = "Spring-MVC";
    private static final String PARTNER_SUFFIX = "-HTTP";

    private PartnerGrouper() {
    }

    public static Map<String, List<InterfaceItem>> groupByPartner(List<InterfaceItem> itemList) {
        Map<String, List<InterfaceItem>> groupedMap = new LinkedHashMap<>();

        for (InterfaceItem item : itemList) {
            String partner = extractPartner(item);
            groupedMap.computeIfAbsent(partner, k -> new ArrayList<>()).add(item);
        }

        return groupedMap;
    }

    public static String extractPartner(InterfaceItem item) {
        String partner = getPartnerFromItem(item);
        if (partner == null || partner.isEmpty()) {
            return DEFAULT_PARTNER;
        }
        return partner + PARTNER_SUFFIX;
    }

    private static String getPartnerFromItem(InterfaceItem item) {
        if (item.getPsiMethod() == null) {
            return null;
        }

        com.intellij.psi.javadoc.PsiDocComment docComment = item.getPsiMethod().getDocComment();
        if (docComment == null) {
            return null;
        }

        com.intellij.psi.javadoc.PsiDocTag partnerTag = docComment.findTagByName("partner");
        if (partnerTag == null) {
            return null;
        }

        com.intellij.psi.javadoc.PsiDocTagValue psiDocTagValue = partnerTag.getValueElement();
        return psiDocTagValue != null ? psiDocTagValue.getText() : null;
    }
}