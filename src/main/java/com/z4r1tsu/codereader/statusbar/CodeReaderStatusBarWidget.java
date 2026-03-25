package com.z4r1tsu.codereader.statusbar;

import com.intellij.ide.ui.LafManagerListener;
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
        
        CodeReaderService service = CodeReaderService.getInstance(project);
        updateContent();
        updateAppearance();
        
        // 关键修复：构造时同步一次面板显隐状态，确保启动即生效
        this.panel.setVisible(service.isVisible());
        
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
        
        Color fg = JBColor.namedColor("StatusBar.foreground", UIUtil.getLabelForeground());
        
        if (state.visibility < 100) {
            int alpha = (int) (255 * (state.visibility / 100.0f));
            alpha = Math.max(1, Math.min(255, alpha));
            fg = new Color(fg.getRed(), fg.getGreen(), fg.getBlue(), alpha);
        }
        
        infoLabel.setForeground(fg);
        textLabel.setForeground(fg);

        // 性能优化：仅在 wordCount 发生变化或首次加载时重新计算宽度
        if (cachedWordCount != state.wordCount) {
            FontMetrics metrics = textLabel.getFontMetrics(textLabel.getFont());
            int baseCharWidth = metrics.charWidth('中'); 
            cachedTargetWidth = (state.wordCount * baseCharWidth) + 2;
            cachedWordCount = state.wordCount;
            
            // 确保高度不为 0，优先使用字体测量的高度
            int height = Math.max(metrics.getHeight(), textLabel.getPreferredSize().height);
            if (height <= 0) height = 20; // 最后的保底高度
            
            Dimension fixedSize = new Dimension(cachedTargetWidth, height);
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