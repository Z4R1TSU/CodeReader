package com.z4r1tsu.codereader.reader;

import com.z4r1tsu.codereader.epub.TOCEntry;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TxtReader implements IReader {
    private List<String> lines = new ArrayList<>();
    private List<int[]> pageMap = new ArrayList<>();
    private int wordCount;

    @Override
    public void loadFile(String filePath, int wordCount) {
        this.wordCount = wordCount;
        lines.clear();
        pageMap.clear();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(filePath), StandardCharsets.UTF_8))) {
            String line;
            int lineNum = 0;
            while ((line = reader.readLine()) != null) {
                lines.add(line);
                if (line.isEmpty()) {
                    pageMap.add(new int[]{lineNum, 0});
                } else {
                    for (int i = 0; i < line.length(); i += wordCount) {
                        pageMap.add(new int[]{lineNum, i});
                    }
                }
                lineNum++;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String getPageContent(int page) {
        if (page < 0 || page >= pageMap.size()) {
            return "";
        }
        int[] pageInfo = pageMap.get(page);
        String line = lines.get(pageInfo[0]);
        if (line.isEmpty()) {
            return " ";
        }
        int start = pageInfo[1];
        int end = Math.min(start + this.wordCount, line.length());
        return line.substring(start, end);
    }

    @Override
    public int getPageCount() {
        return pageMap.size();
    }

    @Override
    public List<TOCEntry> getToc() {
        return Collections.emptyList();
    }

    @Override
    public boolean isEpub() {
        return false;
    }

    @Override
    public void loadChapter(int chapterIndex) {
        // Not applicable for TXT files
    }

    @Override
    public int getTotalPageCount() {
        return pageMap.size();
    }

    @Override
    public int getChapterPageCount(int chapterIndex) {
        return 0;
    }

    @Override
    public String getChapterTitle(int chapterIndex) {
        return "";
    }
}