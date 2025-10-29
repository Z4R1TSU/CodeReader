package com.z4r1tsu.codereader.reader;

import com.z4r1tsu.codereader.epub.TOCEntry;

import java.util.List;

public interface IReader {
    void loadFile(String filePath, int wordCount);

    String getPageContent(int page);

    int getPageCount();

    List<TOCEntry> getToc();

    boolean isEpub();

    void loadChapter(int chapterIndex);

    int getTotalPageCount();

    int getChapterPageCount(int chapterIndex);

    String getChapterTitle(int chapterIndex);
}