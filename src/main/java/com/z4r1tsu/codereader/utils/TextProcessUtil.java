package com.z4r1tsu.codereader.utils;

import java.util.ArrayList;
import java.util.List;

public class TextProcessUtil {

    /**
     * 清洗并处理 EPUB 的原始 HTML 内容，提取出纯文本
     */
    public static String cleanHtmlContent(String rawContent) {
        if (rawContent == null || rawContent.isEmpty()) {
            return "";
        }

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
        return plainText.replaceAll("[ \t\f\r]+", " ") // 合并横向空白
                .replaceAll("\n\\s*\n", "\n\n") // 合并多个空行为双换行
                .trim();
    }

    /**
     * 在纯文本中安全地去除正文开头的重复章节名
     */
    public static String removeDuplicateTitle(String plainText, String chapterTitle) {
        if (plainText == null || plainText.isEmpty() || chapterTitle == null || chapterTitle.isEmpty()) {
            return plainText;
        }

        String cleanTitle = chapterTitle.trim();
        if (cleanTitle.isEmpty()) {
            return plainText;
        }

        // 去除纯文本开头可能存在的空格或换行
        String trimmedText = plainText.trim();
        
        // 简单粗暴且健壮：如果纯文本确实以这个标题开头，直接截取掉
        // 注意：epub的章节标题在正文中可能包含换行或空格，我们采用更宽容的正则匹配
        // 将标题中的空白字符替换为允许任意空白的正则
        String flexibleTitleRegex = "^\\s*" + java.util.regex.Pattern.quote(cleanTitle).replaceAll("\\s+", "\\\\E\\\\s+\\\\Q") + "\\s*";
        
        return trimmedText.replaceFirst(flexibleTitleRegex, "").trim();
    }

    /**
     * 将处理好的纯文本分页
     */
    public static List<String> paginateText(String text, int wordCount) {
        List<String> pages = new ArrayList<>();
        if (text == null || text.isEmpty()) {
            return pages;
        }

        // 为了在单行显示的状态栏体现分段感，我们将换行符替换为一段较长的空格（如4个空格）
        String displayableText = text.replace("\n\n", "    ").replace("\n", "    ");

        for (int i = 0; i < displayableText.length(); i += wordCount) {
            int end = Math.min(i + wordCount, displayableText.length());
            String pageContent = displayableText.substring(i, end);

            // 如果刚好翻页，通过 trim() 忽略掉页首页尾的多余空格
            String trimmedPage = pageContent.trim();
            if (!trimmedPage.isEmpty()) {
                pages.add(trimmedPage);
            }
        }
        
        return pages;
    }

    /**
     * 生成居中显示或分页的章节标题页
     */
    public static List<String> generateTitlePages(String chapterTitle, int wordCount) {
        List<String> pages = new ArrayList<>();
        if (chapterTitle == null) {
            chapterTitle = "";
        } else {
            chapterTitle = chapterTitle.trim();
        }

        if (chapterTitle.isEmpty()) {
            pages.add(" ");
            return pages;
        }

        // Calculate visual width. Chinese chars = 2, English chars = 1.
        int titleVisualWidth = 0;
        for (char c : chapterTitle.toCharArray()) {
            titleVisualWidth += (c > 255) ? 2 : 1;
        }

        int targetWidth = wordCount * 2;

        if (titleVisualWidth >= targetWidth) {
            // 如果标题过长，不能强行塞在一页里，直接复用分页逻辑，避免出现 `...` 截断
            return paginateText(chapterTitle, wordCount);
        } else {
            // 如果标题较短，直接返回即可，UI层会居中对齐
            pages.add(chapterTitle);
            return pages;
        }
    }
}
