package com.z4r1tsu.codereader.statusbar;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.CustomStatusBarWidget;
import com.intellij.openapi.wm.StatusBar;
import com.intellij.ui.JBColor;
import com.intellij.util.ui.UIUtil;
import com.z4r1tsu.codereader.listeners.CodeReaderListener;
import com.z4r1tsu.codereader.services.CodeReaderService;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class CodeReaderStatusBarWidget implements CustomStatusBarWidget {

    private final JLabel label;
    private final Project project;

    public CodeReaderStatusBarWidget(Project project) {
        this.project = project;
        this.label = new JLabel(getDisplayText());
        updateLabelAppearance();
        
        project.getMessageBus().connect(this).subscribe(CodeReaderListener.TOPIC, () -> {
            label.setText(getDisplayText());
            label.setVisible(CodeReaderService.getInstance(project).isVisible());
            updateLabelAppearance();
        });

        label.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON1) {
                    CodeReaderService.getInstance(project).toggleVisibility();
                }
            }
        });
    }

    private void updateLabelAppearance() {
        CodeReaderService.State state = CodeReaderService.getInstance(project).getState();
        
        label.setFont(UIUtil.getLabelFont());
        
        // 使用标准文本颜色作为 100% 可见度的基准，以确保最亮时不低于正常清晰度
        // 用户可以通过将可见度滑块调至 70%-90% 来完美匹配状态栏其他文字的“灰调”感觉
        Color fg = UIUtil.getLabelForeground();
                   
        // Use standard status bar background
        Color bg = JBColor.namedColor("StatusBar.background", UIUtil.getPanelBackground());
        
        int visibility = state.visibility; // 0 to 100
        float factor = visibility / 100.0f;
        
        int r = (int) (bg.getRed() + (fg.getRed() - bg.getRed()) * factor);
        int g = (int) (bg.getGreen() + (fg.getGreen() - bg.getGreen()) * factor);
        int bl = (int) (bg.getBlue() + (fg.getBlue() - bg.getBlue()) * factor);
        
        r = Math.max(0, Math.min(255, r));
        g = Math.max(0, Math.min(255, g));
        bl = Math.max(0, Math.min(255, bl));
        
        label.setForeground(new Color(r, g, bl, fg.getAlpha()));
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