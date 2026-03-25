package com.z4r1tsu.codereader.ui;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.z4r1tsu.codereader.listeners.CodeReaderListener;
import com.z4r1tsu.codereader.services.CodeReaderService;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;

public class AdjustDisplayDialog extends DialogWrapper {

    private JSlider visibilitySlider;
    private JLabel visibilityLabel;
    private final Project project;
    private final int originalVisibility;

    public AdjustDisplayDialog(Project project) {
        super(project, true);
        this.project = project;

        CodeReaderService.State state = CodeReaderService.getInstance(project).getState();
        this.originalVisibility = state.visibility;

        setTitle("调节显示可见度");
        init();
    }

    @Nullable
    @Override
    protected JComponent createCenterPanel() {
        JPanel panel = new JPanel(new GridLayout(1, 1, 10, 10));
        panel.setPreferredSize(new Dimension(350, 50));

        CodeReaderService.State state = CodeReaderService.getInstance(project).getState();

        // Visibility Panel
        JPanel visibilityPanel = new JPanel(new BorderLayout());
        visibilityLabel = new JLabel("可见度 (" + state.visibility + "%): ");
        // 预设一个稍微宽一点的固定尺寸，防止数字从 9% 变成 100% 时宽度跳动，导致右侧的 Slider 跟着抖动
        visibilityLabel.setPreferredSize(new Dimension(120, visibilityLabel.getPreferredSize().height));
        visibilityPanel.add(visibilityLabel, BorderLayout.WEST);
        visibilitySlider = new JSlider(0, 100, state.visibility);
        visibilitySlider.setMajorTickSpacing(25);
        visibilitySlider.setPaintTicks(true);
        visibilitySlider.setPaintLabels(true);
        visibilitySlider.addChangeListener(e -> updatePreview());
        visibilityPanel.add(visibilitySlider, BorderLayout.CENTER);

        panel.add(visibilityPanel);

        return panel;
    }

    private void updatePreview() {
        CodeReaderService.State state = CodeReaderService.getInstance(project).getState();
        int currentValue = visibilitySlider.getValue();
        state.visibility = currentValue;
        visibilityLabel.setText("可见度 (" + currentValue + "%): ");
        project.getMessageBus().syncPublisher(CodeReaderListener.TOPIC).appearanceUpdated();
    }

    @Override
    protected void doOKAction() {
        // 确保在点击确定后，最后刷新一次状态栏，以消除由于模态窗口导致的“变浅”预览效果
        project.getMessageBus().syncPublisher(CodeReaderListener.TOPIC).appearanceUpdated();
        super.doOKAction();
    }

    @Override
    public void doCancelAction() {
        CodeReaderService.State state = CodeReaderService.getInstance(project).getState();
        state.visibility = originalVisibility;
        project.getMessageBus().syncPublisher(CodeReaderListener.TOPIC).appearanceUpdated();
        super.doCancelAction();
    }
}