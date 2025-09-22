package com.z4r1tsu.codereader.epub;

import nl.siegmann.epublib.domain.Book;
import nl.siegmann.epublib.epub.EpubReader;

import java.io.InputStream;

public class EpubParser {

    public String parse(InputStream in) {
        try {
            Book book = (new EpubReader()).readEpub(in);
            StringBuilder content = new StringBuilder();
            book.getContents().forEach(item -> {
                try {
                    content.append(new String(item.getData()));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
            return content.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}