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
    private String filePath;
    private List<Long> lineByteOffsets = new ArrayList<>();
    private List<int[]> pageMap = new ArrayList<>();
    private int wordCount;

    @Override
    public void loadFile(String filePath, int wordCount) {
        this.filePath = filePath;
        this.wordCount = wordCount;
        lineByteOffsets.clear();
        pageMap.clear();

        try (FileInputStream fis = new FileInputStream(filePath)) {
            long currentByteOffset = 0;
            lineByteOffsets.add(0L);
            
            byte[] buffer = new byte[8192];
            int bytesRead;
            int lineNum = 0;
            
            // 用于存储当前行的字节，以便正确计算字符长度（处理多字节字符）
            java.io.ByteArrayOutputStream lineBuffer = new java.io.ByteArrayOutputStream();

            while ((bytesRead = fis.read(buffer)) != -1) {
                for (int i = 0; i < bytesRead; i++) {
                    byte b = buffer[i];
                    lineBuffer.write(b);
                    if (b == '\n') {
                        processLine(lineBuffer.toByteArray(), lineNum, wordCount);
                        lineNum++;
                        lineBuffer.reset();
                        lineByteOffsets.add(currentByteOffset + i + 1);
                    }
                }
                currentByteOffset += bytesRead;
            }
            
            // 处理最后一行（如果没有以换行符结尾）
            if (lineBuffer.size() > 0) {
                processLine(lineBuffer.toByteArray(), lineNum, wordCount);
            }
            
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void processLine(byte[] lineBytes, int lineNum, int wordCount) {
        String line = new String(lineBytes, StandardCharsets.UTF_8);
        // 去掉末尾的换行符，以免影响字数计算和显示
        if (line.endsWith("\n")) {
            line = line.substring(0, line.length() - 1);
            if (line.endsWith("\r")) {
                line = line.substring(0, line.length() - 1);
            }
        }
        
        if (line.isEmpty()) {
            pageMap.add(new int[]{lineNum, 0});
        } else {
            for (int i = 0; i < line.length(); i += wordCount) {
                pageMap.add(new int[]{lineNum, i});
            }
        }
    }

    @Override
    public String getPageContent(int page) {
        if (page < 0 || page >= pageMap.size()) {
            return "";
        }
        int[] pageInfo = pageMap.get(page);
        int lineIndex = pageInfo[0];
        int charOffset = pageInfo[1];

        try (java.io.RandomAccessFile raf = new java.io.RandomAccessFile(filePath, "r")) {
            raf.seek(lineByteOffsets.get(lineIndex));
            
            // 读取这一行直到换行符
            java.io.ByteArrayOutputStream lineBuffer = new java.io.ByteArrayOutputStream();
            int b;
            while ((b = raf.read()) != -1) {
                if (b == '\n') break;
                lineBuffer.write(b);
            }
            
            String line = new String(lineBuffer.toByteArray(), StandardCharsets.UTF_8);
            if (line.endsWith("\r")) {
                line = line.substring(0, line.length() - 1);
            }
            
            if (line.isEmpty()) {
                return " ";
            }
            
            int start = charOffset;
            int end = Math.min(start + this.wordCount, line.length());
            return line.substring(start, end);
        } catch (IOException e) {
            e.printStackTrace();
            return "读取错误";
        }
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