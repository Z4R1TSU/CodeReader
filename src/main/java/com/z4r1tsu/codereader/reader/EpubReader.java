package com.z4r1tsu.codereader.reader;

import com.z4r1tsu.codereader.epub.TOCEntry;
import com.z4r1tsu.codereader.utils.TextProcessUtil;
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
    private int currentTitlePageCount = 0;

    @Override
    public void loadFile(String filePath, int wordCount) {
        this.wordCount = wordCount;
        this.toc.clear();
        this.totalPageCountCache = null;
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
            TOCEntry entry = toc.get(chapterIndex);
            loadResource(entry.getResource(), entry.getTitle());
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

    @Override
    public boolean isTitlePage(int page) {
        return page >= 0 && page < currentTitlePageCount;
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

    private void loadResource(Resource resource, String chapterTitle) {
        if (resource == null) {
            return;
        }
        pages.clear();
        
        // Add chapter title pages
        List<String> titlePages = TextProcessUtil.generateTitlePages(chapterTitle, this.wordCount);
        this.currentTitlePageCount = titlePages.size();
        pages.addAll(titlePages);

        try {
            String rawContent = new String(resource.getData(), StandardCharsets.UTF_8);
            
            // 1. 获取干净的纯文本
            String plainText = TextProcessUtil.cleanHtmlContent(rawContent);
            
            // 2. 去除正文开头的重复章节名
            if (chapterTitle != null && !chapterTitle.trim().isEmpty()) {
                plainText = TextProcessUtil.removeDuplicateTitle(plainText, chapterTitle.trim());
            }
            
            // 3. 分页正文
            List<String> contentPages = TextProcessUtil.paginateText(plainText, this.wordCount);
            
            if (contentPages.isEmpty() && titlePages.isEmpty()) {
                pages.add(" ");
            } else {
                pages.addAll(contentPages);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}