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
    private final Project project;

    public CodeReaderStatusBarWidget(Project project) {
        this.project = project;
        this.label = new JLabel(getDisplayText());
        project.getMessageBus().connect(this).subscribe(CodeReaderListener.TOPIC, () -> {
            label.setText(getDisplayText());
            label.setVisible(CodeReaderService.getInstance(project).isVisible());
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

    private String getDisplayText() {
        CodeReaderService service = CodeReaderService.getInstance(project);
        if (!service.isVisible()) {
            return "";
        }
        if (service.getShowChapterInfo()) {
            return String.format("%s %s %s %s",
                    service.getCurrentChapterTitle(),
                    service.getCurrentChapterProgress(),
                    service.getBookProgress(),
                    service.getCurrentPageContent());
        }
        return service.getCurrentPageContent();
    }

    @Override
    public void install(@NotNull StatusBar statusBar) {
    }

    @Override
    public void dispose() {
    }
}