package com.z4r1tsu.codereader.services;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.Service;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.project.Project;
import com.z4r1tsu.codereader.epub.TOCEntry;
import com.z4r1tsu.codereader.history.HistoryService;
import com.z4r1tsu.codereader.history.ReadingHistory;
import com.z4r1tsu.codereader.listeners.CodeReaderListener;
import com.z4r1tsu.codereader.reader.EpubReader;
import com.z4r1tsu.codereader.reader.IReader;
import com.z4r1tsu.codereader.reader.TxtReader;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

@State(
        name = "com.z4r1tsu.codereader.services.CodeReaderService",
        storages = @Storage("CodeReaderPlugin.xml")
)
@Service(Service.Level.PROJECT)
public final class CodeReaderService implements PersistentStateComponent<CodeReaderService.State>, Disposable {

    public static class State {
        public int wordCount = 30;
        public boolean showChapterInfo = false;
        public boolean isVisible = false;
        public int visibility = 100;

        public int getWordCount() {
            return wordCount;
        }

        public void setWordCount(int wordCount) {
            this.wordCount = wordCount;
        }

        public int getVisibility() {
            return visibility;
        }

        public void setVisibility(int visibility) {
            this.visibility = visibility;
        }
    }

    private State myState = new State();
    private final Project project;
    private IReader reader;
    private int currentPage = 0;
    private String currentFile = "";
    private int currentChapterIndex = -1;

    private enum ReaderState {
        IDLE,
        LOADING,
        JUST_LOADED,
        CHAPTER_JUST_JUMPED,
        CACHE_CLEARED
    }

    private ReaderState readerState = ReaderState.IDLE;

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
        saveCurrentStateToHistory();

        readerState = ReaderState.LOADING;
        project.getMessageBus().syncPublisher(CodeReaderListener.TOPIC).contentUpdated();

        String filePath = file.getAbsolutePath();
        if (filePath.endsWith(".txt")) {
            reader = new TxtReader();
        } else if (filePath.endsWith(".epub")) {
            reader = new EpubReader();
        } else {
            readerState = ReaderState.IDLE;
            project.getMessageBus().syncPublisher(CodeReaderListener.TOPIC).contentUpdated();
            return;
        }

        reader.loadFile(filePath, myState.wordCount);
        if (reader.isEpub()) {
            reader.loadChapter(0);
        }

        currentFile = filePath;
        currentPage = 0;
        currentChapterIndex = 0;
        readerState = ReaderState.JUST_LOADED;
        myState.isVisible = true;
        project.getMessageBus().syncPublisher(CodeReaderListener.TOPIC).contentUpdated();
    }

    public void loadFromHistory(ReadingHistory history) {
        File file = new File(history.getFilePath());
        if (!file.exists()) {
            return;
        }

        loadFile(file);

        if (reader.isEpub()) {
            currentChapterIndex = history.getCurrentChapterIndex();
            reader.loadChapter(currentChapterIndex);
        }

        currentPage = history.getCurrentPage();
        readerState = ReaderState.IDLE;
        project.getMessageBus().syncPublisher(CodeReaderListener.TOPIC).contentUpdated();
    }

    public List<TOCEntry> getToc() {
        return reader != null ? reader.getToc() : new ArrayList<>();
    }

    public void jumpToChapter(TOCEntry entry) {
        if (entry != null && reader != null && reader.isEpub()) {
            List<TOCEntry> toc = reader.getToc();
            for (int i = 0; i < toc.size(); i++) {
                if (toc.get(i).getResource().getHref().equals(entry.getResource().getHref())) {
                    currentChapterIndex = i;
                    break;
                }
            }
            reader.loadChapter(currentChapterIndex);
            currentPage = 0;
            readerState = ReaderState.CHAPTER_JUST_JUMPED;
            project.getMessageBus().syncPublisher(CodeReaderListener.TOPIC).contentUpdated();
            saveCurrentStateToHistory();
        }
    }

    public void refreshContent() {
        if (currentFile != null && !currentFile.isEmpty()) {
            loadFile(new File(currentFile));
        }
    }

    public String getCurrentPageContent() {
        switch (readerState) {
            case CACHE_CLEARED:
                return "缓存已清除";
            case LOADING:
                return "Loading...";
            case JUST_LOADED:
                return "导入成功，请翻页阅读";
            case CHAPTER_JUST_JUMPED:
                String title = getCurrentChapterTitle().trim();
                if (title.isEmpty()) {
                    return "请翻页阅读";
                }
                return "成功跳转至【" + title + "】，请翻页阅读";
            default:
                return getActualCurrentPageContent();
        }
    }

    private String getActualCurrentPageContent() {
        if (reader == null) {
            return "No file loaded.";
        }
        if (currentPage < 0) {
            currentPage = 0;
        }
        if (currentPage >= reader.getPageCount()) {
            currentPage = reader.getPageCount() - 1;
        }
        return reader.getPageContent(currentPage);
    }

    public void nextPage() {
        if (handleStateAfterPageTurn()) return;
        if (reader == null) return;

        boolean changed = false;
        if (currentPage < reader.getPageCount() - 1) {
            currentPage++;
            changed = true;
        } else if (reader.isEpub() && currentChapterIndex < reader.getToc().size() - 1) {
            currentChapterIndex++;
            reader.loadChapter(currentChapterIndex);
            currentPage = 0;
            changed = true;
        }

        if (changed) {
            project.getMessageBus().syncPublisher(CodeReaderListener.TOPIC).contentUpdated();
            saveCurrentStateToHistory();
        }
    }

    public void prevPage() {
        if (handleStateAfterPageTurn()) return;
        if (reader == null) return;

        boolean changed = false;
        if (currentPage > 0) {
            currentPage--;
            changed = true;
        } else if (reader.isEpub() && currentChapterIndex > 0) {
            currentChapterIndex--;
            reader.loadChapter(currentChapterIndex);
            currentPage = reader.getPageCount() - 1;
            changed = true;
        }

        if (changed) {
            project.getMessageBus().syncPublisher(CodeReaderListener.TOPIC).contentUpdated();
            saveCurrentStateToHistory();
        }
    }

    private boolean handleStateAfterPageTurn() {
        if (readerState != ReaderState.IDLE) {
            readerState = ReaderState.IDLE;
            project.getMessageBus().syncPublisher(CodeReaderListener.TOPIC).contentUpdated();
            return true;
        }
        return false;
    }

    public String getCurrentFile() {
        return currentFile;
    }

    public void clearCache() {
        reader = null;
        currentFile = "";
        currentPage = 0;
        currentChapterIndex = -1;
        readerState = ReaderState.CACHE_CLEARED;
        myState.isVisible = false;
        project.getMessageBus().syncPublisher(CodeReaderListener.TOPIC).contentUpdated();

        HistoryService historyService = HistoryService.getInstance(project);
        historyService.clearHistory();
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

    public int getCurrentChapterIndex() {
        return currentChapterIndex;
    }

    public String getCurrentChapterTitle() {
        if (reader != null && reader.isEpub()) {
            return reader.getChapterTitle(currentChapterIndex);
        }
        return "";
    }

    public double getBookProgressValue() {
        if (reader != null && reader.isEpub()) {
            int totalPageCount = reader.getTotalPageCount();
            if (totalPageCount > 0) {
                int pagesRead = 0;
                for (int i = 0; i < currentChapterIndex; i++) {
                    pagesRead += reader.getChapterPageCount(i);
                }
                pagesRead += currentPage;
                return ((double) pagesRead / totalPageCount) * 100;
            }
        }
        return 0;
    }

    public String getCurrentChapterProgress() {
        if (reader != null && reader.isEpub()) {
            return String.format("(%d/%d)", currentPage + 1, reader.getChapterPageCount(currentChapterIndex));
        }
        return "";
    }

    public String getBookProgress() {
        if (reader != null && reader.isEpub()) {
            int totalPageCount = reader.getTotalPageCount();
            if (totalPageCount > 0) {
                int pagesRead = 0;
                for (int i = 0; i < currentChapterIndex; i++) {
                    pagesRead += reader.getChapterPageCount(i);
                }
                pagesRead += currentPage;
                double progress = ((double) pagesRead / totalPageCount) * 100;
                return String.format("[%.2f%%]", progress);
            }
        }
        return "";
    }

    private void saveCurrentStateToHistory() {
        if (currentFile == null || currentFile.isEmpty() || reader == null) {
            return;
        }

        HistoryService historyService = HistoryService.getInstance(project);
        String fileName = new File(currentFile).getName();
        String chapterTitle = getCurrentChapterTitle();
        double progress = getBookProgressValue();
        String currentPageContent = getActualCurrentPageContent();
        int chapterPageCount = 0;
        if (reader.isEpub()) {
            chapterPageCount = reader.getChapterPageCount(currentChapterIndex);
        }

        ReadingHistory history = new ReadingHistory(currentFile, fileName, chapterTitle, progress, currentPageContent, currentChapterIndex, currentPage, chapterPageCount);
        historyService.addHistory(history);
    }

    @Override
    public void dispose() {
        saveCurrentStateToHistory();
    }
}