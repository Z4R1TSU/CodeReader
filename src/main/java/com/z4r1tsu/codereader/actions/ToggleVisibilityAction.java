package com.z4r1tsu.codereader.actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.CustomStatusBarWidget;
import com.intellij.openapi.wm.StatusBar;
import com.intellij.openapi.wm.StatusBarWidget;
import com.intellij.openapi.wm.WindowManager;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public class ToggleVisibilityAction extends AnAction {
    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Project project = e.getProject();
        if (project == null) return;

        StatusBar statusBar = WindowManager.getInstance().getStatusBar(project);
        if (statusBar == null) return;

        StatusBarWidget widget = statusBar.getWidget("CodeReaderStatusBar");
        if (widget instanceof CustomStatusBarWidget) {
            JComponent component = ((CustomStatusBarWidget) widget).getComponent();
            if (component != null) {
                component.setVisible(!component.isVisible());
            }
        }
    }
}