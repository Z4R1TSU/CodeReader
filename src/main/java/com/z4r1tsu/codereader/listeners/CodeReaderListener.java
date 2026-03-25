package com.z4r1tsu.codereader.listeners;

import com.intellij.util.messages.Topic;

import java.util.EventListener;

public interface CodeReaderListener extends EventListener {
    Topic<CodeReaderListener> TOPIC = Topic.create("CodeReader Content Update", CodeReaderListener.class);

    /**
     * 仅当小说内容（页码、章节、正文）发生变化时调用
     */
    void contentUpdated();

    /**
     * 仅当外观设置（可见度、主题切换、字数设置）发生变化时调用
     */
    default void appearanceUpdated() {}
}