package com.baiguiren.yaml;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.ApplicationComponent;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.openapi.vfs.newvfs.BulkFileListener;
import com.intellij.openapi.vfs.newvfs.events.VFileEvent;
import com.intellij.util.messages.MessageBusConnection;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.List;

public class YamlApplicationComponent implements ApplicationComponent, BulkFileListener {

    @SuppressWarnings("WeakerAccess")
    public static HashSet<String > dirtyProjects = new HashSet<>();

    private final MessageBusConnection connection;

    public YamlApplicationComponent() {
        connection = ApplicationManager.getApplication().getMessageBus().connect();
    }

    @Override
    public void initComponent() {
        connection.subscribe(VirtualFileManager.VFS_CHANGES, this);
    }

    @Override
    public void disposeComponent() {
        connection.disconnect();
    }

    @Override
    public void before(@NotNull List<? extends VFileEvent> events) {

    }

    @Override
    public void after(@NotNull List<? extends VFileEvent> events) {
        for (VFileEvent event : events) {
            if (isLaravelProjectEvent(event)) {
                String projectName = getProjectName(event);
                if (!projectName.equals("")) {
                    dirtyProjects.add(projectName);
                }
            }
        }
    }

    private boolean isLaravelProjectEvent(VFileEvent event) {
        return event.getPath().endsWith("code-gen/fields.txt");
    }

    private String getProjectName(VFileEvent event) {
        String[] paths = event.getPath().split("/");
        if (paths.length > 3) {
            return paths[paths.length - 3];
        }

        return "";
    }

    @SuppressWarnings("WeakerAccess")
    public static boolean projectIsDirty(String projectName) {
        return dirtyProjects.contains(projectName);
    }
}
