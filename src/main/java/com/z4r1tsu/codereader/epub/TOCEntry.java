package com.z4r1tsu.codereader.epub;

import nl.siegmann.epublib.domain.Resource;

public class TOCEntry {
    private final String title;
    private final Resource resource;

    public TOCEntry(String title, Resource resource) {
        this.title = title;
        this.resource = resource;
    }

    public String getTitle() {
        return title;
    }

    public Resource getResource() {
        return resource;
    }

    @Override
    public String toString() {
        return title;
    }
}