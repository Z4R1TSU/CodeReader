package com.z4r1tsu.codereader.actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.fileChooser.FileChooser;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.WindowManager;
import com.z4r1tsu.codereader.services.CodeReaderService;
import org.jetbrains.annotations.NotNull;

import java.io.File;

public class OpenFileAction extends AnAction {
    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Project project = e.getProject();
        if (project == null) {
            return;
        }

        FileChooserDescriptor descriptor = new FileChooserDescriptor(true, false, false, false, false, false) {
            @Override
            public boolean isFileVisible(VirtualFile file, boolean showHiddenFiles) {
                // 如果是目录，必须可见以便导航
                if (file.isDirectory()) return true;
                
                // 让所有文件在列表中可见，这样不支持的文件就会被系统置灰（不可选中）
                return true;
            }

            @Override
            public boolean isFileSelectable(VirtualFile file) {
                // 只有符合后缀的文件才允许被选中（点击 OK）
                if (file == null || file.isDirectory()) return false;
                String name = file.getName().toLowerCase();
                return name.endsWith(".txt") || name.endsWith(".epub");
            }
        }.withFileFilter(file -> {
            String name = file.getName().toLowerCase();
            return name.endsWith(".txt") || name.endsWith(".epub");
        }).withTitle("选择 Txt 或 Epub 文件");

        FileChooser.chooseFile(descriptor, project, null, virtualFile -> {
            if (virtualFile != null) {
                File file = new File(virtualFile.getPath());
                CodeReaderService.getInstance(project).loadFile(file);
                WindowManager.getInstance().getStatusBar(project).updateWidget("CodeReaderStatusBar");
            }
        });
    }
}