package com.z4r1tsu.codereader.epub;

import nl.siegmann.epublib.domain.Resource;

public class TOCEntry {
    private final String title;
    private final Resource resource;
    private int pageCount;

    public TOCEntry(String title, Resource resource) {
        this.title = title;
        this.resource = resource;
        this.pageCount = 0;
    }

    public String getTitle() {
        return title;
    }

    public Resource getResource() {
        return resource;
    }

    public int getPageCount() {
        return pageCount;
    }

    public void setPageCount(int pageCount) {
        this.pageCount = pageCount;
    }

    @Override
    public String toString() {
        return title;
    }
}