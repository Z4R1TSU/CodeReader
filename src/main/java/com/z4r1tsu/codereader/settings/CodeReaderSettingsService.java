package com.z4r1tsu.codereader.settings;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * 全局设置服务，使用 Application 级别持久化，避免在每个项目的 .idea 文件夹中创建大量配置。
 */
@State(
        name = "com.z4r1tsu.codereader.settings.CodeReaderSettingsService",
        storages = @Storage("CodeReaderGlobalSettings.xml")
)
public final class CodeReaderSettingsService implements PersistentStateComponent<CodeReaderSettingsService.State> {

    public static class State {
        public int wordCount = 30;
        public int visibility = 100;
        public float autoPageInterval = 2.0f;
    }

    private State myState = new State();

    public static CodeReaderSettingsService getInstance() {
        return ApplicationManager.getApplication().getService(CodeReaderSettingsService.class);
    }

    @Override
    public @Nullable State getState() {
        return myState;
    }

    @Override
    public void loadState(@NotNull State state) {
        myState = state;
    }

    public int getWordCount() {
        return myState.wordCount;
    }

    public void setWordCount(int wordCount) {
        myState.wordCount = wordCount;
    }

    public int getVisibility() {
        return myState.visibility;
    }

    public void setVisibility(int visibility) {
        myState.visibility = visibility;
    }

    public float getAutoPageInterval() {
        return myState.autoPageInterval;
    }

    public void setAutoPageInterval(float autoPageInterval) {
        myState.autoPageInterval = autoPageInterval;
    }
}
