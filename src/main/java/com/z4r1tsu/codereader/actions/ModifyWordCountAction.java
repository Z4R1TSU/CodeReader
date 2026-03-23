package com.z4r1tsu.codereader.actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.z4r1tsu.codereader.services.CodeReaderService;
import org.jetbrains.annotations.NotNull;

public class ModifyWordCountAction extends AnAction {
    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Project project = e.getProject();
        if (project == null) {
            return;
        }
        String currentWordCount = String.valueOf(CodeReaderService.getInstance(project).getState().getWordCount());
        String newWordCount = Messages.showInputDialog("请输入单页显示的字数:", "修改字数", null, currentWordCount, null);
        if (newWordCount != null) {
            try {
                int wordCount = Integer.parseInt(newWordCount);
                if (wordCount <= 0) {
                    Messages.showErrorDialog("请输入一个正数。", "无效数字");
                } else if (wordCount > 150) {
                    Messages.showErrorDialog("请输入一个小于150的数字。", "无效数字");
                } else {
                    CodeReaderService.getInstance(project).getState().setWordCount(wordCount);
                    CodeReaderService.getInstance(project).refreshContent();
                }
            } catch (NumberFormatException ex) {
                Messages.showErrorDialog("请输入一个有效的数字。", "无效数字");
            }
        }
    }
}