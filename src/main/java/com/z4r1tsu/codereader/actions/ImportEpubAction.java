package com.z4r1tsu.codereader.actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.fileChooser.FileChooser;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.WindowManager;
import com.z4r1tsu.codereader.services.CodeReaderService;
import org.jetbrains.annotations.NotNull;

import java.io.File;

public class ImportEpubAction extends AnAction {
    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Project project = e.getProject();
        if (project == null) {
            return;
        }

        FileChooserDescriptor descriptor = new FileChooserDescriptor(true, false, false, false, false, false)
                .withTitle("Choose an Epub File to Import")
                .withFileFilter(virtualFile -> {
                    String name = virtualFile.getName().toLowerCase();
                    return name.endsWith(".epub");
                });

        FileChooser.chooseFile(descriptor, project, null, virtualFile -> {
            if (virtualFile != null) {
                File file = new File(virtualFile.getPath());
                CodeReaderService.getInstance(project).loadFile(file);
                WindowManager.getInstance().getStatusBar(project).updateWidget("CodeReaderStatusBar");
            }
        });
    }
}