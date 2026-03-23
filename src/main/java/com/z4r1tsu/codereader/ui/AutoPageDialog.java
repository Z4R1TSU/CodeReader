package com.z4r1tsu.codereader.ui;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.z4r1tsu.codereader.listeners.CodeReaderListener;
import com.z4r1tsu.codereader.services.CodeReaderService;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.util.Hashtable;

public class AutoPageDialog extends DialogWrapper {

    private JCheckBox enableCheckBox;
    private JSlider intervalSlider;
    private JLabel intervalLabel;
    private final Project project;

    public AutoPageDialog(Project project) {
        super(project, true);
        this.project = project;

        setTitle("Auto Page Settings");
        init();
    }

    @Nullable
    @Override
    protected JComponent createCenterPanel() {
        JPanel panel = new JPanel(new GridLayout(2, 1, 10, 10));
        panel.setPreferredSize(new Dimension(350, 100));

        CodeReaderService service = CodeReaderService.getInstance(project);
        CodeReaderService.State state = service.getState();

        // Enable Panel
        JPanel enablePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        enableCheckBox = new JCheckBox("Enable Auto Page");
        enableCheckBox.setSelected(service.isAutoPageRunning());
        enableCheckBox.addActionListener(e -> updateSliderState());
        enablePanel.add(enableCheckBox);

        // Interval Panel
        JPanel intervalPanel = new JPanel(new BorderLayout());
        intervalLabel = new JLabel(String.format("Interval (%.1fs): ", state.autoPageInterval));
        intervalLabel.setPreferredSize(new Dimension(100, intervalLabel.getPreferredSize().height));
        intervalPanel.add(intervalLabel, BorderLayout.WEST);

        // Slider from 1 to 50 representing 0.1s to 5.0s
        int initialSliderValue = (int) (state.autoPageInterval * 10);
        intervalSlider = new JSlider(1, 50, initialSliderValue);
        
        // Add labels for 0.1s and 5.0s
        Hashtable<Integer, JLabel> labelTable = new Hashtable<>();
        labelTable.put(1, new JLabel("0.1s"));
        labelTable.put(50, new JLabel("5.0s"));
        intervalSlider.setLabelTable(labelTable);
        intervalSlider.setPaintLabels(true);
        
        intervalSlider.addChangeListener(e -> {
            float seconds = intervalSlider.getValue() / 10.0f;
            intervalLabel.setText(String.format("Interval (%.1fs): ", seconds));
        });
        intervalPanel.add(intervalSlider, BorderLayout.CENTER);

        panel.add(enablePanel);
        panel.add(intervalPanel);

        updateSliderState();

        return panel;
    }

    private void updateSliderState() {
        intervalSlider.setEnabled(enableCheckBox.isSelected());
        intervalLabel.setEnabled(enableCheckBox.isSelected());
    }

    @Override
    protected void doOKAction() {
        CodeReaderService service = CodeReaderService.getInstance(project);
        CodeReaderService.State state = service.getState();
        
        boolean wasEnabled = service.isAutoPageRunning();
        float oldInterval = state.autoPageInterval;
        
        boolean isEnabled = enableCheckBox.isSelected();
        state.autoPageInterval = intervalSlider.getValue() / 10.0f;
        
        if (isEnabled) {
            service.startAutoPage();
        } else {
            service.stopAutoPage();
        }
        
        // Always update the timer logic if interval changed while running
        if (isEnabled && oldInterval != state.autoPageInterval) {
            service.updateAutoPageTimer();
        }
        
        super.doOKAction();
    }
}