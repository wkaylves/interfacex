package com.kaylves.interfacex.action.search;

import com.kaylves.interfacex.common.constants.InterfaceXItemCategoryEnum;
import com.kaylves.interfacex.common.InterfaceXItem;
import com.intellij.featureStatistics.FeatureUsageTracker;
import com.intellij.ide.actions.GotoActionBase;
import com.intellij.ide.util.gotoByName.ChooseByNameFilter;
import com.intellij.ide.util.gotoByName.ChooseByNameItemProvider;
import com.intellij.ide.util.gotoByName.ChooseByNameModel;
import com.intellij.ide.util.gotoByName.ChooseByNamePopup;
import com.intellij.navigation.ChooseByNameContributor;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.DataKeys;
import com.intellij.openapi.fileEditor.ex.FileEditorManagerEx;
import com.intellij.openapi.ide.CopyPasteManager;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Pair;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.datatransfer.DataFlavor;
import java.util.Arrays;
import java.util.List;

public class GotoRequestMappingAction extends GotoActionBase implements DumbAware {

    public GotoRequestMappingAction() {
    }

    @Override
    protected void gotoActionPerformed(AnActionEvent e) {
        Project project = e.getProject();
        if (project == null) {
            return;
        }

        FeatureUsageTracker.getInstance().triggerFeatureUsed("navigation.popup.service");

        ChooseByNameContributor[] chooseByNameContributors = {new GotoRequestMappingContributor(e.getData(DataKeys.MODULE)),};

        final GotoRequestMappingModel model = new GotoRequestMappingModel(project, chooseByNameContributors);

        GotoActionCallback<InterfaceXItemCategoryEnum> callback = new GotoActionCallback<>() {
            @Override
            protected ChooseByNameFilter<InterfaceXItemCategoryEnum> createFilter(@NotNull ChooseByNamePopup popup) {
                return new GotoRequestMappingFilter(popup, model, project);
            }

            @Override
            public void elementChosen(ChooseByNamePopup chooseByNamePopup, Object element) {
                if (element instanceof InterfaceXItem) {
                    InterfaceXItem navigationItem = (InterfaceXItem) element;
                    if (navigationItem.canNavigate()) {
                        navigationItem.navigate(true);
                    }
                }
            }
        };

        GotoRequestMappingProvider provider = new GotoRequestMappingProvider(getPsiContext(e));
        showNavigationPopup(e, model, callback, "Request Mapping Url matching pattern", true, true, (ChooseByNameItemProvider) provider);
    }

    @Override
    protected <T> void showNavigationPopup(AnActionEvent e, ChooseByNameModel model, final GotoActionCallback<T> callback, @Nullable final String findUsagesTitle, boolean useSelectionFromEditor, final boolean allowMultipleSelection, final ChooseByNameItemProvider itemProvider) {
        final Project project = e.getData(CommonDataKeys.PROJECT);
        boolean mayRequestOpenInCurrentWindow = model.willOpenEditor() && FileEditorManagerEx.getInstanceEx(project).hasSplitOrUndockedWindows();
        Pair<String, Integer> start = getInitialText(useSelectionFromEditor, e);
        String copiedURL = tryFindCopiedURL();

        String predefinedText = start.first == null ? copiedURL : start.first;

        showNavigationPopup(callback, findUsagesTitle, RestServiceChooseByNamePopup.createPopup(project, model, itemProvider, predefinedText, mayRequestOpenInCurrentWindow, start.second), allowMultipleSelection);
    }

    private String tryFindCopiedURL() {
        String contents = CopyPasteManager.getInstance().getContents(DataFlavor.stringFlavor);
        if (contents == null) {
            return null;
        }

        contents = contents.trim();
        if (contents.startsWith("http")) {
            if (contents.length() <= 120) {
                return contents;
            } else {
                return contents.substring(0, 120);
            }
        }

        return null;
    }

    protected static class GotoRequestMappingFilter extends ChooseByNameFilter<InterfaceXItemCategoryEnum> {

        GotoRequestMappingFilter(final ChooseByNamePopup popup, GotoRequestMappingModel model, final Project project) {
            super(popup, model, GotoRequestMappingConfiguration.getInstance(project), project);
        }

        /**
         * 自动在 Popup 右侧生成复选过滤器（就是你图中的 POST/PUT/DELETE 列表！）
         * @return HttpMethod.values()
         */
        @Override
        @NotNull
        protected List<InterfaceXItemCategoryEnum> getAllFilterValues() {
            return Arrays.asList(InterfaceXItemCategoryEnum.values());
        }

        @Override
        protected String textForFilterValue(@NotNull InterfaceXItemCategoryEnum value) {
            return value.name();
        }

        @Override
        protected Icon iconForFilterValue(@NotNull InterfaceXItemCategoryEnum value) {
            return null;
        }
    }
}
