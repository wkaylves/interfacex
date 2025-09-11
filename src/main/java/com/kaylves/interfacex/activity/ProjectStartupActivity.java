//package com.kaylves.interfacex.activity;
//
//import com.kaylves.interfacex.navigator.InterfaceXNavigator;
//import com.kaylves.interfacex.utils.ToolkitUtil;
//import com.intellij.openapi.application.ApplicationManager;
//import com.intellij.openapi.project.Project;
//import com.intellij.openapi.startup.ProjectActivity;
//import kotlin.Unit;
//import kotlin.coroutines.Continuation;
//import lombok.extern.slf4j.Slf4j;
//import org.jetbrains.annotations.NotNull;
//import org.jetbrains.annotations.Nullable;
//
///**
// * 初始化ToolWindow ui Component
// * @author kaylves
// * @since 1.0
// */
//@Slf4j
//public class ProjectStartupActivity implements ProjectActivity {
//
//    private void init(@NotNull Project project) {
//
//        log.info("project opened>>>>>>>>>");
//
//        if (ApplicationManager.getApplication().isUnitTestMode()) {
//            return;
//        }
//
//        InterfaceXNavigator interfaceXNavigator = InterfaceXNavigator.getInstance(project);
//        ToolkitUtil.runWhenInitialized(project, () -> {
//
//            if (project.isDisposed()) {
//                return;
//            }
//
//            interfaceXNavigator.initToolWindow();
//
//        });
//    }
//
//    @Nullable
//    @Override
//    public Object execute(@NotNull Project project, @NotNull Continuation<? super Unit> continuation) {
//        init(project);
//        return "success";
//    }
//}
