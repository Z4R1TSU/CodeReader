package com.z4r1tsu.codereader.actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.z4r1tsu.codereader.ui.AdjustDisplayDialog;
import org.jetbrains.annotations.NotNull;

public class AdjustDisplayAction extends AnAction {
    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Project project = e.getProject();
        if (project == null) {
            return;
        }
        AdjustDisplayDialog dialog = new AdjustDisplayDialog(project);
        dialog.show();
    }
}