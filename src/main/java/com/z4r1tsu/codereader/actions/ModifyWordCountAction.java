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
        String newWordCount = Messages.showInputDialog("Enter the number of words to display in the status bar:", "Modify Word Count", null, currentWordCount, null);
        if (newWordCount != null) {
            try {
                int wordCount = Integer.parseInt(newWordCount);
                if (wordCount <= 0) {
                    Messages.showErrorDialog("Please enter a positive number.", "Invalid Number");
                } else if (wordCount > 150) {
                    Messages.showErrorDialog("Please enter a number less than 150.", "Invalid Number");
                } else {
                    CodeReaderService.getInstance(project).getState().setWordCount(wordCount);
                    CodeReaderService.getInstance(project).refreshContent();
                }
            } catch (NumberFormatException ex) {
                Messages.showErrorDialog("Please enter a valid number.", "Invalid Number");
            }
        }
    }
}