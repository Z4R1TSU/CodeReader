package com.z4r1tsu.codereader.epub;

import nl.siegmann.epublib.domain.Resource;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class TOCEntry {
    private final String title;
    private final Resource resource;
    private Integer pageCount;

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
        if (pageCount == null) {
            try {
                String rawContent = new String(resource.getData(), StandardCharsets.UTF_8);
                String plainText = rawContent.replaceAll("<[^>]*>", "");
                this.pageCount = (int) Math.ceil((double) plainText.length() / wordCount);
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