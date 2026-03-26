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
            
            // 1. 处理段落标签，将其转换为明显的换行标识
            // 将 <p>, <div>, <br> 等块级或换行标签替换为自定义的换行符
            String processedContent = rawContent.replaceAll("(?i)<p[^>]*>|<div[^>]*>", "\n")
                                                .replaceAll("(?i)<br\\s*/?>", "\n");

            // 2. 过滤掉剩余的所有 HTML 标签
            String plainText = processedContent.replaceAll("<[^>]*>", "");
            
            // 3. 处理常用的 HTML 实体转义字符
            plainText = plainText.replace("&nbsp;", " ")
                                 .replace("&quot;", "\"")
                                 .replace("&amp;", "&")
                                 .replace("&lt;", "<")
                                 .replace("&gt;", ">")
                                 .replace("&apos;", "'")
                                 .replace("&ldquo;", "“")
                                 .replace("&rdquo;", "”")
                                 .replace("&lsquo;", "‘")
                                 .replace("&rsquo;", "’")
                                 .replace("&hellip;", "…")
                                 .replace("&mdash;", "—");
            
            // 4. 清洗多余的空白字符，但保留显式的换行（将多个连续换行缩减为两个，以保持分段感）
            plainText = plainText.replaceAll("[ \t\f\r]+", " ") // 合并横向空白
                                 .replaceAll("\n\\s*\n", "\n\n") // 合并多个空行为双换行
                                 .trim();

            if (!plainText.isEmpty()) {
                // 为了在单行显示的状态栏体现分段感，我们将换行符替换为一段较长的空格（如4个空格）
                String displayableText = plainText.replace("\n\n", "    ").replace("\n", "    ");
                
                for (int i = 0; i < displayableText.length(); i += this.wordCount) {
                    int end = Math.min(i + this.wordCount, displayableText.length());
                    String pageContent = displayableText.substring(i, end);
                    
                    // 如果刚好翻页，通过 trim() 忽略掉页首页尾的多余空格
                    pages.add(pageContent.trim());
                }
            } else {
                pages.add(" ");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}