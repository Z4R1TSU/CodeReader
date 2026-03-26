package com.z4r1tsu.codereader.epub;

import nl.siegmann.epublib.domain.Resource;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

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
                
                String processedContent = rawContent.replaceAll("(?i)<p[^>]*>|<div[^>]*>", "\n")
                                                    .replaceAll("(?i)<br\\s*/?>", "\n");

                String plainText = processedContent.replaceAll("<[^>]*>", "");
                
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
                
                plainText = plainText.replaceAll("[ \t\f\r]+", " ")
                                     .replaceAll("\n\\s*\n", "\n\n")
                                     .trim();

                if (!plainText.isEmpty()) {
                    String displayableText = plainText.replace("\n\n", "    ").replace("\n", "    ");
                    this.pageCount = Math.max(1, (int) Math.ceil((double) displayableText.length() / wordCount));
                } else {
                    this.pageCount = 1;
                }
            } catch (IOException e) {
                e.printStackTrace();
                this.pageCount = 0;
            }
        }
        return pageCount;
    }

    @Override
    public String toString() {
        return title;
    }
}