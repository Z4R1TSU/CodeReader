package com.z4r1tsu.codereader.services;

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.Service;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.project.Project;
import com.z4r1tsu.codereader.epub.TOCEntry;
import com.z4r1tsu.codereader.listeners.CodeReaderListener;
import nl.siegmann.epublib.domain.Book;
import nl.siegmann.epublib.domain.Resource;
import nl.siegmann.epublib.domain.TOCReference;
import nl.siegmann.epublib.epub.EpubReader;
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
    private Book book;
    private List<TOCEntry> toc = new ArrayList<>();
    private int currentChapterIndex = -1;

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
        toc.clear();
        currentFile = file.getAbsolutePath();
        String fileName = file.getName().toLowerCase();

        try {
            if (fileName.endsWith(".txt")) {
                book = null;
                currentChapterIndex = -1;
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
                }
            } else if (fileName.endsWith(".epub")) {
                try (FileInputStream fileInputStream = new FileInputStream(file)) {
                    this.book = (new EpubReader()).readEpub(fileInputStream);
                    if (book != null) {
                        extractToc(book.getTableOfContents().getTocReferences(), 0);
                        if (!toc.isEmpty()) {
                            currentChapterIndex = 0;
                            loadResource(toc.get(0).getResource());
                        }
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

    private void extractToc(List<TOCReference> tocReferences, int depth) {
        if (tocReferences == null) {
            return;
        }
        for (TOCReference tocReference : tocReferences) {
            StringBuilder indent = new StringBuilder();
            for (int i = 0; i < depth; i++) {
                indent.append("  ");
            }
            toc.add(new TOCEntry(indent + tocReference.getTitle(), tocReference.getResource()));
            extractToc(tocReference.getChildren(), depth + 1);
        }
    }

    public void loadResource(Resource resource) {
        if (resource == null) {
            return;
        }
        pages.clear();
        try {
            String rawContent = new String(resource.getData(), StandardCharsets.UTF_8);
            String plainText = rawContent.replaceAll("<[^>]*>", "");
            if (!plainText.isEmpty()) {
                for (int i = 0; i < plainText.length(); i += myState.wordCount) {
                    int end = Math.min(i + myState.wordCount, plainText.length());
                    pages.add(plainText.substring(i, end));
                }
            } else {
                pages.add(" ");
            }
        } catch (IOException e) {
            e.printStackTrace();
            pages.add("Error reading resource.");
        }
        currentPage = 0;
    }

    public List<TOCEntry> getToc() {
        return toc;
    }

    public void jumpToChapter(TOCEntry entry) {
        if (entry != null) {
            for (int i = 0; i < toc.size(); i++) {
                if (toc.get(i).getResource().getHref().equals(entry.getResource().getHref())) {
                    currentChapterIndex = i;
                    break;
                }
            }
            loadResource(entry.getResource());
            project.getMessageBus().syncPublisher(CodeReaderListener.TOPIC).contentUpdated();
        }
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
        } else if (book != null && currentChapterIndex != -1 && currentChapterIndex < toc.size() - 1) {
            currentChapterIndex++;
            loadResource(toc.get(currentChapterIndex).getResource());
            project.getMessageBus().syncPublisher(CodeReaderListener.TOPIC).contentUpdated();
        }
    }

    public void prevPage() {
        if (currentPage > 0) {
            currentPage--;
            project.getMessageBus().syncPublisher(CodeReaderListener.TOPIC).contentUpdated();
        } else if (book != null && currentChapterIndex != -1 && currentChapterIndex > 0) {
            currentChapterIndex--;
            loadResource(toc.get(currentChapterIndex).getResource());
            if (!pages.isEmpty()) {
                currentPage = pages.size() - 1;
            }
            project.getMessageBus().syncPublisher(CodeReaderListener.TOPIC).contentUpdated();
        }
    }

    public String getCurrentFile() {
        return currentFile;
    }
}