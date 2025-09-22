package com.z4r1tsu.codereader.actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.popup.JBPopup;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.z4r1tsu.codereader.epub.TOCEntry;
import com.z4r1tsu.codereader.services.CodeReaderService;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class SelectChapterAction extends AnAction {
    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Project project = e.getProject();
        if (project == null) {
            return;
        }

        CodeReaderService service = CodeReaderService.getInstance(project);
        List<TOCEntry> toc = service.getToc();

        if (toc == null || toc.isEmpty()) {
            return;
        }

        JBPopup popup = JBPopupFactory.getInstance()
                .createPopupChooserBuilder(toc)
                .setTitle("Select Chapter")
                .setMovable(true)
                .setResizable(true)
                .setItemChosenCallback((selectedValue) -> {
                    if (selectedValue != null) {
                        service.jumpToChapter(selectedValue);
                    }
                })
                .createPopup();

        popup.showInBestPositionFor(e.getDataContext());
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