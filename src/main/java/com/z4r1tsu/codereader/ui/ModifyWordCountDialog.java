package com.z4r1tsu.codereader.ui;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.z4r1tsu.codereader.services.CodeReaderService;
import com.z4r1tsu.codereader.settings.CodeReaderSettingsService;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;

public class ModifyWordCountDialog extends DialogWrapper {

    private JSlider wordCountSlider;
    private JLabel wordCountLabel;
    private final Project project;
    private final int originalWordCount;

    public ModifyWordCountDialog(Project project) {
        super(project, true);
        this.project = project;
        this.originalWordCount = CodeReaderSettingsService.getInstance().getWordCount();

        setTitle("修改单页字数");
        init();
    }

    @Nullable
    @Override
    protected JComponent createCenterPanel() {
        JPanel panel = new JPanel(new GridLayout(1, 1, 10, 10));
        panel.setPreferredSize(new Dimension(350, 50));

        CodeReaderSettingsService settings = CodeReaderSettingsService.getInstance();

        // Word Count Panel
        JPanel wordCountPanel = new JPanel(new BorderLayout());
        wordCountLabel = new JLabel("字数 (" + settings.getWordCount() + "): ");
        wordCountLabel.setPreferredSize(new Dimension(80, wordCountLabel.getPreferredSize().height));
        wordCountPanel.add(wordCountLabel, BorderLayout.WEST);
        
        wordCountSlider = new JSlider(0, 100, settings.getWordCount());
        wordCountSlider.setMajorTickSpacing(20);
        wordCountSlider.setMinorTickSpacing(5);
        wordCountSlider.setPaintTicks(true);
        wordCountSlider.setPaintLabels(true);
        wordCountSlider.addChangeListener(e -> {
            int currentValue = wordCountSlider.getValue();
            // 确保字数至少为1，避免逻辑错误
            if (currentValue < 1) currentValue = 1;
            
            wordCountLabel.setText("字数 (" + currentValue + "): ");
            
            // 解决卡顿：只有在用户停止拖拽时才执行昂贵的刷新操作
            if (!wordCountSlider.getValueIsAdjusting()) {
                CodeReaderSettingsService.getInstance().setWordCount(currentValue);
                CodeReaderService service = CodeReaderService.getInstance(project);
                service.refreshContent();
                // 刷新完内容后，显式触发外观更新以重新计算宽度
                service.updateAppearance();
            }
        });
        wordCountPanel.add(wordCountSlider, BorderLayout.CENTER);

        panel.add(wordCountPanel);

        return panel;
    }

    @Override
    protected void doOKAction() {
        super.doOKAction();
    }

    @Override
    public void doCancelAction() {
        CodeReaderSettingsService.getInstance().setWordCount(originalWordCount);
        CodeReaderService service = CodeReaderService.getInstance(project);
        service.refreshContent();
        super.doCancelAction();
    }
}
