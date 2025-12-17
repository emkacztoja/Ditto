package com.java;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.net.URL;

public class Main extends Application {

    private Stage stage;
    private Scene scene;
    private VBox root;
    private final ObservableList<ClipboardItem> clipboardHistory = FXCollections.observableArrayList();
    private ClipboardManager clipboardManager;
    private SettingsManager settingsManager;

    public static void main(String[] args) {
        // Fix for GDK warning on Linux
        System.setProperty("jdk.gtk.version", "2");
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        this.stage = primaryStage;
        Platform.setImplicitExit(false);

        // --- Settings ---
        settingsManager = new SettingsManager();
        primaryStage.setTitle("Ditto");
        
        // Defer setting alwaysOnTop until the stage is shown
        primaryStage.showingProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> obs, Boolean oldValue, Boolean newValue) {
                if (newValue) {
                    stage.setAlwaysOnTop(settingsManager.isAlwaysOnTop());
                    // Remove listener to not apply it again
                    obs.removeListener(this);
                }
            }
        });


        root = new VBox(10);
        root.setPadding(new Insets(10));

        // --- Top Bar ---
        TextField searchField = new TextField();
        searchField.setPromptText("Search...");
        HBox.setHgrow(searchField, Priority.ALWAYS);
        
        Button settingsButton = new Button("Settings");
        settingsButton.setOnAction(e -> showSettingsDialog());
        
        Button clearButton = new Button("Clear All");
        clearButton.setOnAction(e -> clipboardManager.clearAll());
        
        HBox topBar = new HBox(10, searchField, settingsButton, clearButton);
        topBar.setAlignment(Pos.CENTER_LEFT);

        // --- Data and Filtering ---
        FilteredList<ClipboardItem> filteredData = new FilteredList<>(clipboardHistory, s -> true);
        searchField.textProperty().addListener(obs -> {
            String filter = searchField.getText();
            if (filter == null || filter.isEmpty()) {
                filteredData.setPredicate(s -> true);
            } else {
                String lowerCaseFilter = filter.toLowerCase();
                filteredData.setPredicate(item -> item.getContent().toLowerCase().contains(lowerCaseFilter));
            }
        });

        // --- Manager and ListView ---
        clipboardManager = new ClipboardManager(clipboardHistory, settingsManager.getMaxHistorySize());

        ListView<ClipboardItem> listView = new ListView<>(filteredData);
        listView.setCellFactory(param -> new ClipboardHistoryCell(clipboardManager));
        VBox.setVgrow(listView, Priority.ALWAYS); // Make ListView fill vertical space

        root.getChildren().addAll(topBar, listView);

        scene = new Scene(root, 400, 600);
        
        // Load CSS and Apply Theme
        URL cssUrl = getClass().getResource("/styles.css");
        if (cssUrl != null) {
            scene.getStylesheets().add(cssUrl.toExternalForm());
        }
        applyTheme();
        
        primaryStage.setScene(scene);

        SwingUtilities.invokeLater(() -> createTrayIcon(stage));

        clipboardManager.startPolling();

        primaryStage.setOnCloseRequest(event -> stage.hide());
        
        // Show the stage, which will trigger the showingProperty listener
        primaryStage.show();
    }
    
    private void applyTheme() {
        if ("Dark".equals(settingsManager.getTheme())) {
            if (!root.getStyleClass().contains("dark-theme")) {
                root.getStyleClass().add("dark-theme");
            }
        } else {
            root.getStyleClass().remove("dark-theme");
        }
    }

    private void showSettingsDialog() {
        SettingsDialog dialog = new SettingsDialog(stage, settingsManager);
        dialog.showAndWait();
        stage.setAlwaysOnTop(settingsManager.isAlwaysOnTop());
        applyTheme(); // Re-apply theme in case it changed
    }

    private void createTrayIcon(final Stage stage) {
        if (SystemTray.isSupported()) {
            SystemTray tray = SystemTray.getSystemTray();

            URL imageUrl = Main.class.getResource("/icon.png");
            Image image = null;
            if (imageUrl != null) {
                try {
                    image = ImageIO.read(imageUrl);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            
            if (image == null) {
                image = new java.awt.image.BufferedImage(16, 16, java.awt.image.BufferedImage.TYPE_INT_ARGB);
            }

            TrayIcon trayIcon = new TrayIcon(image, "Ditto");
            trayIcon.setImageAutoSize(true);

            trayIcon.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    if (e.getButton() == MouseEvent.BUTTON1) {
                        Platform.runLater(() -> {
                            stage.show();
                            stage.toFront();
                        });
                    }
                }
            });

            PopupMenu popupMenu = new PopupMenu();
            MenuItem showItem = new MenuItem("Show");
            showItem.addActionListener(e -> Platform.runLater(() -> {
                stage.show();
                stage.toFront();
            }));
            popupMenu.add(showItem);

            MenuItem exitItem = new MenuItem("Exit");
            exitItem.addActionListener(e -> {
                Platform.exit();
                tray.remove(trayIcon);
                System.exit(0);
            });
            popupMenu.add(exitItem);

            trayIcon.setPopupMenu(popupMenu);

            try {
                tray.add(trayIcon);
            } catch (AWTException e) {
                e.printStackTrace();
            }
        }
    }
}
