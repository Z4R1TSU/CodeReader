package com.z4r1tsu.codereader.reader;

import com.z4r1tsu.codereader.epub.TOCEntry;
import nl.siegmann.epublib.domain.Book;
import nl.siegmann.epublib.domain.Resource;
import nl.siegmann.epublib.domain.TOCReference;


import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class EpubReader implements IReader {
    private Book book;
    private List<TOCEntry> toc = new ArrayList<>();
    private List<String> pages = new ArrayList<>();
    private int wordCount;
    private Integer totalPageCountCache = null;

    @Override
    public void loadFile(String filePath, int wordCount) {
        this.wordCount = wordCount;
        try (FileInputStream fileInputStream = new FileInputStream(filePath)) {
            this.book = (new nl.siegmann.epublib.epub.EpubReader()).readEpub(fileInputStream);
            if (book != null) {
                extractToc(book.getTableOfContents().getTocReferences(), 0);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String getPageContent(int page) {
        if (page < 0 || page >= pages.size()) {
            return "";
        }
        return pages.get(page);
    }

    @Override
    public int getPageCount() {
        return pages.size();
    }

    @Override
    public List<TOCEntry> getToc() {
        return toc;
    }

    @Override
    public boolean isEpub() {
        return true;
    }

    @Override
    public void loadChapter(int chapterIndex) {
        if (chapterIndex >= 0 && chapterIndex < toc.size()) {
            loadResource(toc.get(chapterIndex).getResource());
        }
    }

    @Override
    public int getTotalPageCount() {
        if (totalPageCountCache == null) {
            totalPageCountCache = toc.stream().mapToInt(entry -> entry.getPageCount(this.wordCount)).sum();
        }
        return totalPageCountCache;
    }

    @Override
    public int getChapterPageCount(int chapterIndex) {
        if (chapterIndex < 0 || chapterIndex >= toc.size()) {
            return 0;
        }
        return toc.get(chapterIndex).getPageCount(wordCount);
    }

    @Override
    public String getChapterTitle(int chapterIndex) {
        if (chapterIndex < 0 || chapterIndex >= toc.size()) {
            return "";
        }
        return toc.get(chapterIndex).getTitle();
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

    private void loadResource(Resource resource) {
        if (resource == null) {
            return;
        }
        pages.clear();
        try {
            String rawContent = new String(resource.getData(), StandardCharsets.UTF_8);
            String plainText = rawContent.replaceAll("<[^>]*>", "");
            if (!plainText.isEmpty()) {
                for (int i = 0; i < plainText.length(); i += this.wordCount) {
                    int end = Math.min(i + this.wordCount, plainText.length());
                    pages.add(plainText.substring(i, end));
                }
            } else {
                pages.add(" ");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}