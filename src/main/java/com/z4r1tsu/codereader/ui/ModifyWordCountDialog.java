package com.z4r1tsu.codereader.ui;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.z4r1tsu.codereader.listeners.CodeReaderListener;
import com.z4r1tsu.codereader.services.CodeReaderService;
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
        this.originalWordCount = CodeReaderService.getInstance(project).getState().getWordCount();

        setTitle("修改单页字数");
        init();
    }

    @Nullable
    @Override
    protected JComponent createCenterPanel() {
        JPanel panel = new JPanel(new GridLayout(1, 1, 10, 10));
        panel.setPreferredSize(new Dimension(350, 50));

        CodeReaderService.State state = CodeReaderService.getInstance(project).getState();

        // Word Count Panel
        JPanel wordCountPanel = new JPanel(new BorderLayout());
        wordCountLabel = new JLabel("字数 (" + state.wordCount + "): ");
        wordCountLabel.setPreferredSize(new Dimension(80, wordCountLabel.getPreferredSize().height));
        wordCountPanel.add(wordCountLabel, BorderLayout.WEST);
        
        wordCountSlider = new JSlider(0, 100, state.wordCount);
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
                CodeReaderService service = CodeReaderService.getInstance(project);
                service.getState().setWordCount(currentValue);
                service.refreshContent();
                // 刷新完内容后，显式触发外观更新以重新计算宽度
                project.getMessageBus().syncPublisher(CodeReaderListener.TOPIC).appearanceUpdated();
            }
        });
        wordCountPanel.add(wordCountSlider, BorderLayout.CENTER);

        panel.add(wordCountPanel);

        return panel;
    }

    private void updatePreview() {
        // 此方法已弃用，逻辑已移动至 ChangeListener 中
    }

    @Override
    protected void doOKAction() {
        super.doOKAction();
    }

    @Override
    public void doCancelAction() {
        CodeReaderService service = CodeReaderService.getInstance(project);
        service.getState().setWordCount(originalWordCount);
        service.refreshContent();
        super.doCancelAction();
    }
}
