package com.z4r1tsu.codereader.epub;

import com.z4r1tsu.codereader.utils.TextProcessUtil;
import nl.siegmann.epublib.domain.Resource;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class TOCEntry {
    private final String title;
    private final Resource resource;
    private Integer pageCount;
    private int lastWordCount = -1;

    public TOCEntry(String title, Resource resource) {
        this.title = title;
        this.resource = resource;
        this.pageCount = null;
    }

    public String getTitle() {
        return title;
    }

    public Resource getResource() {
        return resource;
    }

    public int getPageCount(int wordCount) {
        if (pageCount == null || this.lastWordCount != wordCount) {
            this.lastWordCount = wordCount;
            try {
                String rawContent = new String(resource.getData(), StandardCharsets.UTF_8);
                
                // 1. 获取干净的纯文本
                String plainText = TextProcessUtil.cleanHtmlContent(rawContent);
                
                // 2. 去除正文开头的重复章节名
                if (title != null && !title.trim().isEmpty()) {
                    plainText = TextProcessUtil.removeDuplicateTitle(plainText, title.trim());
                }
                
                // 3. 计算正文页数
                List<String> contentPages = TextProcessUtil.paginateText(plainText, wordCount);
                int contentPagesCount = contentPages.size();
                
                // 4. 计算章节标题页数
                List<String> titlePages = TextProcessUtil.generateTitlePages(title, wordCount);
                int titlePagesCount = titlePages.size();
                
                this.pageCount = titlePagesCount + contentPagesCount;
            } catch (IOException e) {
                e.printStackTrace();
                this.pageCount = 1; // At least show the title page even on error
            }
        }
        return pageCount;
    }

    @Override
    public String toString() {
        return title;
    }
}