package com.kaylves.interfacex.activity;

import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.startup.ProjectActivity;
import com.intellij.util.concurrency.AppExecutorUtil;
import com.kaylves.interfacex.navigator.InterfaceXNavigator;
import com.kaylves.interfacex.utils.ToolkitUtil;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.startup.StartupActivity;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

/**
 * 初始化ToolWindow ui Component
 * @author kaylves
 * @since 1.0
 */
@Slf4j
public class MyStartupActivity implements  ProjectActivity {

    private void init(@NotNull Project project) {

        log.info("project opened>>>>>>>>>");

        if (ApplicationManager.getApplication().isUnitTestMode()) {
            return;
        }

        ApplicationManager.getApplication().executeOnPooledThread(() -> {
            ReadAction.run(() -> {
                // 回到 EDT 更新 UI
                ApplicationManager.getApplication().invokeLater(() -> {
                    InterfaceXNavigator interfaceXNavigator = InterfaceXNavigator.getInstance(project);

                    if (project.isDisposed()) {
                        return;
                    }

                    interfaceXNavigator.initToolWindow();
                });
            });
        });


    }


    @Override
    public @Nullable Object execute(@NotNull Project project, @NotNull Continuation<? super Unit> continuation) {
        return CompletableFuture.runAsync(() -> {
        // 耗时操作
            init(project);
        }, AppExecutorUtil.getAppExecutorService());    }
}
