package com.z4r1tsu.codereader.services;

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.Service;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.project.Project;
import com.z4r1tsu.codereader.listeners.CodeReaderListener;
import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@State(
        name = "com.z4r1tsu.codereader.services.CodeReaderService",
        storages = @Storage("CodeReaderPlugin.xml")
)
@Service(Service.Level.PROJECT)
public final class CodeReaderService implements PersistentStateComponent<CodeReaderService.State> {

    public static class State {
        public int wordCount = 30;

        public int getWordCount() {
            return wordCount;
        }

        public void setWordCount(int wordCount) {
            this.wordCount = wordCount;
        }
    }

    private State myState = new State();
    private final Project project;
    private List<String> pages = new ArrayList<>();
    private int currentPage = 0;
    private String currentFile = "";

    public CodeReaderService(Project project) {
        this.project = project;
    }

    public static CodeReaderService getInstance(Project project) {
        return project.getService(CodeReaderService.class);
    }

    @Override
    public State getState() {
        return myState;
    }

    @Override
    public void loadState(@NotNull State state) {
        myState = state;
    }

    public void loadFile(File file) {
        pages.clear();
        currentFile = file.getAbsolutePath();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.isEmpty()) {
                    pages.add(" "); // Represent empty line as a space to be visible
                } else {
                    // Split line into chunks of wordCount
                    for (int i = 0; i < line.length(); i += myState.wordCount) {
                        int end = Math.min(i + myState.wordCount, line.length());
                        pages.add(line.substring(i, end));
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            pages.add("Error reading file.");
        }
        currentPage = 0;
        project.getMessageBus().syncPublisher(CodeReaderListener.TOPIC).contentUpdated();
    }

    public void refreshContent() {
        if (currentFile != null && !currentFile.isEmpty()) {
            loadFile(new File(currentFile));
        }
    }

    public String getCurrentPageContent() {
        if (pages.isEmpty()) {
            return "No file loaded.";
        }
        if (currentPage < 0) {
            currentPage = 0;
        }
        if (currentPage >= pages.size()) {
            currentPage = pages.size() - 1;
        }
        return pages.get(currentPage);
    }

    public void nextPage() {
        if (currentPage < pages.size() - 1) {
            currentPage++;
            project.getMessageBus().syncPublisher(CodeReaderListener.TOPIC).contentUpdated();
        }
    }

    public void prevPage() {
        if (currentPage > 0) {
            currentPage--;
            project.getMessageBus().syncPublisher(CodeReaderListener.TOPIC).contentUpdated();
        }
    }

    public String getCurrentFile() {
        return currentFile;
    }
}