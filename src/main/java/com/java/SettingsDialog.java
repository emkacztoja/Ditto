package com.java;

import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class SettingsDialog extends Stage {

    private final SettingsManager settingsManager;
    private final CheckBox alwaysOnTopBox;
    private final Spinner<Integer> historySizeSpinner;
    private final ComboBox<String> themeComboBox;

    public SettingsDialog(Stage owner, SettingsManager manager) {
        this.settingsManager = manager;

        initOwner(owner);
        initModality(Modality.APPLICATION_MODAL);
        setTitle("Settings");

        GridPane grid = new GridPane();
        grid.setPadding(new Insets(20));
        grid.setHgap(10);
        grid.setVgap(10);

        // --- Theme ---
        Label themeLabel = new Label("Theme:");
        themeComboBox = new ComboBox<>(FXCollections.observableArrayList("Light", "Dark"));
        themeComboBox.setValue(settingsManager.getTheme());
        grid.add(themeLabel, 0, 0);
        grid.add(themeComboBox, 1, 0);

        // --- Always on Top ---
        Label alwaysOnTopLabel = new Label("Always on Top:");
        alwaysOnTopBox = new CheckBox();
        alwaysOnTopBox.setSelected(settingsManager.isAlwaysOnTop());
        grid.add(alwaysOnTopLabel, 0, 1);
        grid.add(alwaysOnTopBox, 1, 1);

        // --- Max History Size ---
        Label historySizeLabel = new Label("Max History Size:");
        historySizeSpinner = new Spinner<>(10, 200, settingsManager.getMaxHistorySize(), 10);
        grid.add(historySizeLabel, 0, 2);
        grid.add(historySizeSpinner, 1, 2);

        // --- Save/Cancel ---
        Button saveButton = new Button("Save");
        saveButton.setOnAction(e -> saveAndClose());

        Button cancelButton = new Button("Cancel");
        cancelButton.setOnAction(e -> close());

        HBox buttonBox = new HBox(10, saveButton, cancelButton);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);
        grid.add(buttonBox, 1, 3);

        Scene scene = new Scene(grid);
        setScene(scene);
    }

    private void saveAndClose() {
        settingsManager.setTheme(themeComboBox.getValue());
        settingsManager.setAlwaysOnTop(alwaysOnTopBox.isSelected());
        settingsManager.setMaxHistorySize(historySizeSpinner.getValue());
        settingsManager.saveSettings();
        close();
    }
}
