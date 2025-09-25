package com.z4r1tsu.codereader.actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.popup.JBPopup;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.ui.components.JBList;
import com.z4r1tsu.codereader.history.HistoryService;
import com.z4r1tsu.codereader.history.ReadingHistory;
import com.z4r1tsu.codereader.history.ReadingHistory;
import com.z4r1tsu.codereader.services.CodeReaderService;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class ViewHistoryAction extends AnAction {
    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Project project = e.getProject();
        if (project == null) {
            return;
        }

        HistoryService historyService = HistoryService.getInstance(project);
        List<ReadingHistory> histories = historyService.getHistories();

        if (histories.isEmpty()) {
            JBPopupFactory.getInstance()
                    .createMessage("No reading history found.")
                    .showInBestPositionFor(e.getDataContext());
            return;
        }

        CodeReaderService codeReaderService = CodeReaderService.getInstance(project);

        JBList<ReadingHistory> historyList = new JBList<>(histories);

        JBPopup popup = JBPopupFactory.getInstance()
                .createListPopupBuilder(historyList)
                .setTitle("Reading History")
                .setMovable(true)
                .setResizable(true)
                .setItemChoosenCallback(() -> {
                    ReadingHistory selectedValue = historyList.getSelectedValue();
                    if (selectedValue != null) {
                        codeReaderService.loadFromHistory(selectedValue);
                    }
                })
                .createPopup();

        popup.showInBestPositionFor(e.getDataContext());
    }
}