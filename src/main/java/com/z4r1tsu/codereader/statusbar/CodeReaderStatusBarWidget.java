package com.z4r1tsu.codereader.statusbar;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.CustomStatusBarWidget;
import com.intellij.openapi.wm.StatusBar;
import com.z4r1tsu.codereader.listeners.CodeReaderListener;
import com.z4r1tsu.codereader.services.CodeReaderService;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public class CodeReaderStatusBarWidget implements CustomStatusBarWidget {

    private final JLabel label;

    public CodeReaderStatusBarWidget(Project project) {
        this.label = new JLabel(CodeReaderService.getInstance(project).getCurrentPageContent());
        project.getMessageBus().connect(this).subscribe(CodeReaderListener.TOPIC, () -> {
            label.setText(CodeReaderService.getInstance(project).getCurrentPageContent());
        });
    }

    @Override
    public JComponent getComponent() {
        return label;
    }

    @Override
    public @NotNull String ID() {
        return "CodeReaderStatusBar";
    }

    @Override
    public void install(@NotNull StatusBar statusBar) {
    }

    @Override
    public void dispose() {
    }
}