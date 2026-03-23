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

        setTitle("Adjust Display Settings");
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
        visibilityLabel = new JLabel("Visibility (" + state.visibility + "%): ");
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
        visibilityLabel.setText("Visibility (" + currentValue + "%): ");
        project.getMessageBus().syncPublisher(CodeReaderListener.TOPIC).contentUpdated();
    }

    @Override
    protected void doOKAction() {
        super.doOKAction();
    }

    @Override
    public void doCancelAction() {
        CodeReaderService.State state = CodeReaderService.getInstance(project).getState();
        state.visibility = originalVisibility;
        project.getMessageBus().syncPublisher(CodeReaderListener.TOPIC).contentUpdated();
        super.doCancelAction();
    }
}