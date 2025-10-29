package com.z4r1tsu.codereader.actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.popup.JBPopup;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.ui.components.JBList;
import com.z4r1tsu.codereader.epub.TOCEntry;
import com.z4r1tsu.codereader.services.CodeReaderService;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.util.List;

public class SelectChapterAction extends AnAction {
    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Project project = e.getProject();
        if (project == null) {
            return;
        }

        CodeReaderService codeReaderService = CodeReaderService.getInstance(project);
        List<TOCEntry> toc = codeReaderService.getToc();
        int currentChapterIndex = codeReaderService.getCurrentChapterIndex();

        if (toc.isEmpty()) {
            return;
        }

        final JBList<TOCEntry> list = new JBList<>(toc);
        if (currentChapterIndex >= 0 && currentChapterIndex < toc.size()) {
            list.setSelectedIndex(currentChapterIndex);
        }

        var builder = JBPopupFactory.getInstance().createListPopupBuilder(list)
                .setTitle("Select Chapter")
                .setMovable(true)
                .setResizable(true)
                .setItemChosenCallback(() -> {
                    TOCEntry selectedValue = list.getSelectedValue();
                    if (selectedValue != null) {
                        codeReaderService.jumpToChapter(selectedValue);
                    }
                });

        JBPopup popup = builder.createPopup();

        popup.showInBestPositionFor(e.getDataContext());

        if (currentChapterIndex >= 0 && currentChapterIndex < toc.size()) {
            SwingUtilities.invokeLater(() -> list.ensureIndexIsVisible(currentChapterIndex));
        }
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        Project project = e.getProject();
        if (project != null) {
            CodeReaderService service = CodeReaderService.getInstance(project);
            e.getPresentation().setEnabled(!service.getToc().isEmpty());
        } else {
            e.getPresentation().setEnabled(false);
        }
    }
}