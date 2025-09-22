package com.z4r1tsu.codereader.listeners;

import com.intellij.util.messages.Topic;

import java.util.EventListener;

public interface CodeReaderListener extends EventListener {
    Topic<CodeReaderListener> TOPIC = Topic.create("CodeReader Content Update", CodeReaderListener.class);

    void contentUpdated();
}