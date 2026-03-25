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

    private final JPanel panel;
    private final JLabel infoLabel;
    private final JLabel textLabel;
    private final Project project;

    public CodeReaderStatusBarWidget(Project project) {
        this.project = project;
        
        this.panel = new JPanel();
        this.panel.setLayout(new BoxLayout(this.panel, BoxLayout.X_AXIS));
        this.panel.setOpaque(false);
        
        this.infoLabel = new JLabel("");
        // 将间距调整为最小合理值(2px)，保持紧凑而不重叠
        this.infoLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 2));
        
        this.textLabel = new JLabel("");
        this.textLabel.setHorizontalAlignment(SwingConstants.CENTER);
        
        this.panel.add(infoLabel);
        this.panel.add(textLabel);
        
        updateContent();
        updateAppearance();
        
        project.getMessageBus().connect(this).subscribe(CodeReaderListener.TOPIC, () -> {
            updateContent();
            panel.setVisible(CodeReaderService.getInstance(project).isVisible());
            updateAppearance();
        });

        MouseAdapter clickListener = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON1) {
                    CodeReaderService.getInstance(project).toggleVisibility();
                }
            }
        };
        
        panel.addMouseListener(clickListener);
        infoLabel.addMouseListener(clickListener);
        textLabel.addMouseListener(clickListener);
    }

    private void updateContent() {
        CodeReaderService service = CodeReaderService.getInstance(project);
        if (!service.isVisible()) {
            infoLabel.setText("");
            textLabel.setText("");
            return;
        }

        if (service.getShowChapterInfo()) {
            String infoText = String.format("%s %s %s",
                    service.getCurrentChapterTitle(),
                    service.getCurrentChapterProgress(),
                    service.getBookProgress());
            infoLabel.setText(infoText);
            infoLabel.setVisible(true);
        } else {
            infoLabel.setText("");
            infoLabel.setVisible(false);
        }
        
        textLabel.setText(service.getCurrentPageContent());
    }

    private int cachedWordCount = -1;
    private int cachedTargetWidth = -1;

    private void updateAppearance() {
        CodeReaderService service = CodeReaderService.getInstance(project);
        CodeReaderService.State state = service.getState();
        
        Font font = UIUtil.getLabelFont();
        infoLabel.setFont(font);
        textLabel.setFont(font);
        
        Color fg = UIUtil.getLabelForeground();
        Color bg = JBColor.namedColor("StatusBar.background", UIUtil.getPanelBackground());
        
        int visibility = state.visibility;
        float factor = visibility / 100.0f;
        
        int r = (int) (bg.getRed() + (fg.getRed() - bg.getRed()) * factor);
        int g = (int) (bg.getGreen() + (fg.getGreen() - bg.getGreen()) * factor);
        int bl = (int) (bg.getBlue() + (fg.getBlue() - bg.getBlue()) * factor);
        
        r = Math.max(0, Math.min(255, r));
        g = Math.max(0, Math.min(255, g));
        bl = Math.max(0, Math.min(255, bl));
        
        Color textColor = new Color(r, g, bl, fg.getAlpha());
        infoLabel.setForeground(textColor);
        textLabel.setForeground(textColor);

        // 性能优化：仅在 wordCount 发生变化或首次加载时重新计算宽度
        if (cachedWordCount != state.wordCount) {
            FontMetrics metrics = textLabel.getFontMetrics(textLabel.getFont());
            int baseCharWidth = metrics.charWidth('中'); 
            cachedTargetWidth = (state.wordCount * baseCharWidth) + 2;
            cachedWordCount = state.wordCount;
            
            Dimension fixedSize = new Dimension(cachedTargetWidth, textLabel.getPreferredSize().height);
            textLabel.setPreferredSize(fixedSize);
            textLabel.setMinimumSize(fixedSize);
            textLabel.setMaximumSize(fixedSize);
            panel.revalidate();
        }
    }

    @Override
    public JComponent getComponent() {
        return panel;
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