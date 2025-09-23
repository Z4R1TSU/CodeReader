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
        public boolean showChapterInfo = false;
        public boolean isVisible = false;

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
    private int totalPageCount = 0;
    private boolean justLoaded = false;

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
        justLoaded = false;

        try {
            if (fileName.endsWith(".txt")) {
                book = null;
                currentChapterIndex = -1;
                totalPageCount = 0;
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
                justLoaded = true;
            } else if (fileName.endsWith(".epub")) {
                try (FileInputStream fileInputStream = new FileInputStream(file)) {
                    this.book = (new EpubReader()).readEpub(fileInputStream);
                    if (book != null) {
                        extractToc(book.getTableOfContents().getTocReferences(), 0);
                        if (!toc.isEmpty()) {
                            calculateTotalPageCount();
                            currentChapterIndex = 0;
                            loadResource(toc.get(0).getResource());
                            justLoaded = true;
                        }
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            pages.add("Error reading file.");
            justLoaded = false;
        }

        currentPage = 0;
        myState.isVisible = true;
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

            for (TOCEntry entry : toc) {
                if (entry.getResource().getHref().equals(resource.getHref())) {
                    entry.setPageCount(pages.size());
                    break;
                }
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
            justLoaded = true;
            project.getMessageBus().syncPublisher(CodeReaderListener.TOPIC).contentUpdated();
        }
    }

    public void refreshContent() {
        if (currentFile != null && !currentFile.isEmpty()) {
            loadFile(new File(currentFile));
        }
    }

    public String getCurrentPageContent() {
        if (justLoaded) {
            return "导入成功，请翻页阅读。";
        }
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
        if (justLoaded) {
            justLoaded = false;
            project.getMessageBus().syncPublisher(CodeReaderListener.TOPIC).contentUpdated();
            return;
        }

        boolean changed = false;
        if (currentPage < pages.size() - 1) {
            currentPage++;
            changed = true;
        } else if (isEpub() && hasNextChapter()) {
            loadNextChapter();
            changed = true;
        }

        if (changed) {
            project.getMessageBus().syncPublisher(CodeReaderListener.TOPIC).contentUpdated();
        }
    }

    public void prevPage() {
        if (justLoaded) {
            justLoaded = false;
            project.getMessageBus().syncPublisher(CodeReaderListener.TOPIC).contentUpdated();
            return;
        }

        boolean changed = false;
        if (currentPage > 0) {
            currentPage--;
            changed = true;
        } else if (isEpub() && hasPrevChapter()) {
            loadPrevChapter();
            changed = true;
        }

        if (changed) {
            project.getMessageBus().syncPublisher(CodeReaderListener.TOPIC).contentUpdated();
        }
    }

    public String getCurrentFile() {
        return currentFile;
    }

    public void toggleChapterInfo() {
        myState.showChapterInfo = !myState.showChapterInfo;
        project.getMessageBus().syncPublisher(CodeReaderListener.TOPIC).contentUpdated();
    }

    public void toggleVisibility() {
        myState.isVisible = !myState.isVisible;
        project.getMessageBus().syncPublisher(CodeReaderListener.TOPIC).contentUpdated();
    }

    public boolean isVisible() {
        return myState.isVisible;
    }

    public boolean getShowChapterInfo() {
        return myState.showChapterInfo;
    }

    public String getCurrentChapterTitle() {
        if (isEpub() && currentChapterIndex >= 0 && currentChapterIndex < toc.size()) {
            return toc.get(currentChapterIndex).getTitle();
        }
        return "";
    }

    public String getCurrentChapterProgress() {
        if (isEpub()) {
            TOCEntry currentChapter = toc.get(currentChapterIndex);
            return String.format("(%d/%d)", currentPage + 1, currentChapter.getPageCount());
        }
        return "";
    }

    public String getBookProgress() {
        if (isEpub() && totalPageCount > 0) {
            int pagesRead = 0;
            for (int i = 0; i < currentChapterIndex; i++) {
                pagesRead += toc.get(i).getPageCount();
            }
            pagesRead += currentPage;
            int progress = (int) (((double) pagesRead / totalPageCount) * 100);
            return String.format("[%d%%]", progress);
        }
        return "";
    }

    private void calculateTotalPageCount() {
        totalPageCount = 0;
        for (TOCEntry entry : toc) {
            try {
                Resource resource = entry.getResource();
                String rawContent = new String(resource.getData(), StandardCharsets.UTF_8);
                String plainText = rawContent.replaceAll("<[^>]*>", "");
                int pageCount = (int) Math.ceil((double) plainText.length() / myState.wordCount);
                entry.setPageCount(pageCount);
                totalPageCount += pageCount;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private boolean isEpub() {
        return book != null && currentChapterIndex != -1;
    }

    private boolean hasNextChapter() {
        return currentChapterIndex < toc.size() - 1;
    }

    private void loadNextChapter() {
        currentChapterIndex++;
        loadResource(toc.get(currentChapterIndex).getResource());
    }

    private boolean hasPrevChapter() {
        return currentChapterIndex > 0;
    }

    private void loadPrevChapter() {
        currentChapterIndex--;
        loadResource(toc.get(currentChapterIndex).getResource());
        if (!pages.isEmpty()) {
            currentPage = pages.size() - 1;
        }
    }
}