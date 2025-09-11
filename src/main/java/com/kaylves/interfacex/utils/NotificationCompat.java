package com.kaylves.interfacex.utils;

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationGroup;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

public class NotificationCompat {

    public static void notifySuccess(String notifyGroup,@NotNull Project project, @NotNull String path) {


        Notification notification = createNotification(notifyGroup,path, NotificationType.INFORMATION);

        if (notification != null) {
            notification.notify(project);
        }
    }

    private static Notification createNotification(String notifyGroup ,@NotNull String content, @NotNull NotificationType type) {

        // 尝试使用新 API（2020.3+）
        try {
            Class<?> groupManagerClass = Class.forName("com.intellij.notification.NotificationGroupManager");
            Object groupManager = groupManagerClass.getMethod("getInstance").invoke(null);
            Object group = groupManagerClass.getMethod("getNotificationGroup", String.class).invoke(groupManager, notifyGroup);

            if (group != null) {
                return (Notification) group.getClass()
                        .getMethod("createNotification", String.class, NotificationType.class)
                        .invoke(group, content, type);
            }
        } catch (Exception ignored) {
            // Fallback to old API
        }

        // 降级使用旧版 API（2020.2 及更早）
        NotificationGroup group = NotificationGroup.balloonGroup(notifyGroup);
        return group.createNotification(content, type);
    }
}