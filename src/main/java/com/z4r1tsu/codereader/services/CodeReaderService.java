package com.z4r1tsu.codereader.services;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.components.Service;
import com.intellij.openapi.project.Project;
import com.z4r1tsu.codereader.epub.TOCEntry;
import com.z4r1tsu.codereader.history.HistoryService;
import com.z4r1tsu.codereader.history.ReadingHistory;
import com.z4r1tsu.codereader.listeners.CodeReaderListener;
import com.z4r1tsu.codereader.reader.EpubReader;
import com.z4r1tsu.codereader.reader.IReader;
import com.z4r1tsu.codereader.reader.TxtReader;
import com.z4r1tsu.codereader.settings.CodeReaderSettingsService;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * 核心阅读服务。不再实现 PersistentStateComponent 以减少对项目配置文件的频繁写入。
 * 显隐状态和章节信息显隐状态仅在内存中保留，确保 IDE 重启后默认为关闭状态。
 * 全局设置（如字数、可见度）通过 CodeReaderSettingsService 进行 Application 级别的持久化。
 */
@Service(Service.Level.PROJECT)
public final class CodeReaderService implements Disposable {

    private final Project project;
    private IReader reader;
    private int currentPage = 0;
    private String currentFile = "";
    private int currentChapterIndex = -1;
    
    // 内存中的临时状态，不进行持久化，确保 IDE 重启后默认为关闭状态
    private boolean isVisible = false;
    private boolean showChapterInfo = false;

    private enum ReaderState {
        IDLE,
        LOADING,
        JUST_LOADED,
        CHAPTER_JUST_JUMPED,
        CACHE_CLEARED
    }

    private ReaderState readerState = ReaderState.IDLE;
    private javax.swing.Timer autoPageTimer;
    private boolean isAutoPageRunning = false;

    public CodeReaderService(Project project) {
        this.project = project;
    }

    public static CodeReaderService getInstance(Project project) {
        return project.getService(CodeReaderService.class);
    }

    public void updateAutoPageTimer() {
        if (autoPageTimer != null) {
            autoPageTimer.stop();
        }
        if (isAutoPageRunning) {
            int delay = (int) (CodeReaderSettingsService.getInstance().getAutoPageInterval() * 1000);
            autoPageTimer = new javax.swing.Timer(delay, e -> {
                if (isVisible) {
                    if (readerState != ReaderState.CACHE_CLEARED && readerState != ReaderState.LOADING) {
                        nextPageInternal();
                    }
                }
            });
            autoPageTimer.start();
        }
    }

    public void startAutoPage() {
        if (!isAutoPageRunning) {
            isAutoPageRunning = true;
            updateAutoPageTimer();
            project.getMessageBus().syncPublisher(CodeReaderListener.TOPIC).contentUpdated();
        }
    }

    public void stopAutoPage() {
        if (isAutoPageRunning) {
            isAutoPageRunning = false;
            if (autoPageTimer != null) {
                autoPageTimer.stop();
            }
            project.getMessageBus().syncPublisher(CodeReaderListener.TOPIC).contentUpdated();
        }
    }

    public void toggleAutoPage() {
        if (isAutoPageRunning) {
            stopAutoPage();
        } else {
            startAutoPage();
        }
    }

    public boolean isAutoPageRunning() {
        return isAutoPageRunning;
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

        reader.loadFile(filePath, CodeReaderSettingsService.getInstance().getWordCount());
        if (reader.isEpub()) {
            reader.loadChapter(0);
        }

        currentFile = filePath;
        currentPage = 0;
        currentChapterIndex = 0;
        readerState = ReaderState.JUST_LOADED;
        isVisible = true;
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
        if (currentFile != null && !currentFile.isEmpty() && reader != null) {
            int oldChapterIndex = currentChapterIndex;
            int oldPage = currentPage;
            
            reader.loadFile(currentFile, CodeReaderSettingsService.getInstance().getWordCount());
            if (reader.isEpub()) {
                reader.loadChapter(currentChapterIndex);
            }
            
            currentPage = Math.min(oldPage, reader.getPageCount() - 1);
            if (currentPage < 0) currentPage = 0;
            
            readerState = ReaderState.IDLE;
            project.getMessageBus().syncPublisher(CodeReaderListener.TOPIC).contentUpdated();
        }
    }

    public void updateAppearance() {
        project.getMessageBus().syncPublisher(CodeReaderListener.TOPIC).appearanceUpdated();
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
        stopAutoPage();
        nextPageInternal();
    }

    private void nextPageInternal() {
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
        stopAutoPage();
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
            if (isAutoPageRunning && (readerState == ReaderState.JUST_LOADED || readerState == ReaderState.CHAPTER_JUST_JUMPED)) {
                readerState = ReaderState.IDLE;
                project.getMessageBus().syncPublisher(CodeReaderListener.TOPIC).contentUpdated();
                return false;
            }
            
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
        stopAutoPage();
        reader = null;
        currentFile = "";
        currentPage = 0;
        currentChapterIndex = -1;
        readerState = ReaderState.CACHE_CLEARED;
        isVisible = false;
        project.getMessageBus().syncPublisher(CodeReaderListener.TOPIC).contentUpdated();

        HistoryService historyService = HistoryService.getInstance(project);
        historyService.clearHistory();
    }

    public void toggleChapterInfo() {
        showChapterInfo = !showChapterInfo;
        project.getMessageBus().syncPublisher(CodeReaderListener.TOPIC).contentUpdated();
    }

    public void toggleVisibility() {
        stopAutoPage();
        isVisible = !isVisible;
        project.getMessageBus().syncPublisher(CodeReaderListener.TOPIC).contentUpdated();
    }

    public boolean isVisible() {
        return isVisible;
    }

    public boolean getShowChapterInfo() {
        return showChapterInfo;
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
