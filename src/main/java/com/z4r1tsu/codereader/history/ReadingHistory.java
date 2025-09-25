package com.z4r1tsu.codereader.history;

import java.io.Serializable;

public class ReadingHistory implements Serializable {
    private static final long serialVersionUID = 1L;

    private String filePath;
    private String fileName;
    private String chapterTitle;
    private double progress;
    private String currentPageContent;
    private int currentChapterIndex;
    private int currentPage;
    private int chapterPageCount;

    public ReadingHistory(String filePath, String fileName, String chapterTitle, double progress, String currentPageContent, int currentChapterIndex, int currentPage, int chapterPageCount) {
        this.filePath = filePath;
        this.fileName = fileName;
        this.chapterTitle = chapterTitle;
        this.progress = progress;
        this.currentPageContent = currentPageContent;
        this.currentChapterIndex = currentChapterIndex;
        this.currentPage = currentPage;
        this.chapterPageCount = chapterPageCount;
    }

    public String getFilePath() {
        return filePath;
    }

    public String getFileName() {
        return fileName;
    }

    public String getChapterTitle() {
        return chapterTitle;
    }

    public double getProgress() {
        return progress;
    }

    public String getCurrentPageContent() {
        return currentPageContent;
    }

    public int getCurrentChapterIndex() {
        return currentChapterIndex;
    }

    public int getCurrentPage() {
        return currentPage;
    }

    public int getChapterPageCount() {
        return chapterPageCount;
    }

    @Override
    public String toString() {
        String progressString = String.format("[%.2f%%]", progress);
        String chapterProgressString = "";
        if (chapterPageCount > 0) {
            chapterProgressString = String.format("[%d/%d]", currentPage + 1, chapterPageCount);
        }
        return String.format("<html>%s %s<br/>%s %s<br/><i>%s</i></html>",
                progressString, fileName,
                chapterProgressString, chapterTitle,
                currentPageContent);
    }
}