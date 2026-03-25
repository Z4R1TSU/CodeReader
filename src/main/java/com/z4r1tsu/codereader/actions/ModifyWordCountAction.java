package com.z4r1tsu.codereader.actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.z4r1tsu.codereader.ui.ModifyWordCountDialog;
import org.jetbrains.annotations.NotNull;

public class ModifyWordCountAction extends AnAction {
    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Project project = e.getProject();
        if (project == null) {
            return;
        }
        ModifyWordCountDialog dialog = new ModifyWordCountDialog(project);
        dialog.show();
    }
}