package com.z4r1tsu.codereader.history;

import com.intellij.openapi.components.Service;
import com.intellij.openapi.project.Project;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service(Service.Level.PROJECT)
public final class HistoryService {
    private final List<ReadingHistory> histories = new ArrayList<>();

    public HistoryService() {
    }

    public static HistoryService getInstance(Project project) {
        return project.getService(HistoryService.class);
    }

    public void addHistory(ReadingHistory history) {
        histories.removeIf(h -> h.getFilePath().equals(history.getFilePath()));
        histories.add(0, history);
    }

    public List<ReadingHistory> getHistories() {
        return histories;
    }

    public void clearHistory() {
        histories.clear();
    }
}