package com.java;

import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.scene.input.Clipboard;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ClipboardManager {

    private int maxHistorySize;
    private final ObservableList<ClipboardItem> clipboardHistory;
    private Connection connection;
    private String lastCopied = null;
    
    private final ExecutorService dbExecutor = Executors.newSingleThreadExecutor();

    public ClipboardManager(ObservableList<ClipboardItem> clipboardHistory, int maxHistorySize) {
        this.clipboardHistory = clipboardHistory;
        this.maxHistorySize = maxHistorySize;
        initializeDatabase();
    }

    private void initializeDatabase() {
        dbExecutor.submit(() -> {
            try {
                connection = DriverManager.getConnection("jdbc:sqlite:clipboard.db");
                Statement statement = connection.createStatement();
                
                statement.executeUpdate("CREATE TABLE IF NOT EXISTS clipboard (id INTEGER PRIMARY KEY AUTOINCREMENT, content TEXT NOT NULL, timestamp TEXT NOT NULL)");
                
                try {
                    statement.executeQuery("SELECT timestamp FROM clipboard LIMIT 1");
                } catch (SQLException e) {
                    System.out.println("Migrating database: Adding timestamp column...");
                    statement.executeUpdate("ALTER TABLE clipboard ADD COLUMN timestamp TEXT");
                    String now = LocalDateTime.now().toString();
                    statement.executeUpdate("UPDATE clipboard SET timestamp = '" + now + "' WHERE timestamp IS NULL");
                }
                
                loadHistory();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
    }

    public void startPolling() {
        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.scheduleAtFixedRate(() -> Platform.runLater(this::pollClipboard), 0, 1000, TimeUnit.MILLISECONDS);
    }
    
    public void setLastCopied(String content) {
        this.lastCopied = content;
    }

    private void pollClipboard() {
        try {
            Clipboard clipboard = Clipboard.getSystemClipboard();
            
            if (clipboard.hasString()) {
                String content = clipboard.getString();
                
                if (content != null && !content.equals(lastCopied)) {
                    if (clipboardHistory.isEmpty() || !content.equals(clipboardHistory.get(0).getContent())) {
                        lastCopied = content;
                        
                        saveToDatabase(content, newItem -> {
                            clipboardHistory.add(0, newItem);
                            if (clipboardHistory.size() > maxHistorySize) {
                                clipboardHistory.remove(maxHistorySize);
                            }
                        });
                    } else {
                        lastCopied = content;
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Failed to access clipboard: " + e.getMessage());
        }
    }

    private void loadHistory() {
        try {
            Statement statement = connection.createStatement();
            ResultSet rs = statement.executeQuery("SELECT id, content, timestamp FROM clipboard ORDER BY id DESC LIMIT " + maxHistorySize);
            
            List<ClipboardItem> loadedItems = new ArrayList<>();
            while (rs.next()) {
                String timestampStr = rs.getString("timestamp");
                LocalDateTime timestamp = (timestampStr != null) ? LocalDateTime.parse(timestampStr) : LocalDateTime.now();
                
                loadedItems.add(new ClipboardItem(
                    rs.getInt("id"),
                    rs.getString("content"),
                    timestamp
                ));
            }
            
            Platform.runLater(() -> {
                clipboardHistory.addAll(loadedItems);
                if (!clipboardHistory.isEmpty()) {
                    lastCopied = clipboardHistory.get(0).getContent();
                }
            });
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void saveToDatabase(String content, java.util.function.Consumer<ClipboardItem> callback) {
        dbExecutor.submit(() -> {
            try {
                LocalDateTime timestamp = LocalDateTime.now();
                PreparedStatement pstmt = connection.prepareStatement("INSERT INTO clipboard (content, timestamp) VALUES (?, ?)", Statement.RETURN_GENERATED_KEYS);
                pstmt.setString(1, content);
                pstmt.setString(2, timestamp.toString());
                pstmt.executeUpdate();
                
                ResultSet generatedKeys = pstmt.getGeneratedKeys();
                if (generatedKeys.next()) {
                    int id = generatedKeys.getInt(1);
                    ClipboardItem newItem = new ClipboardItem(id, content, timestamp);
                    Platform.runLater(() -> callback.accept(newItem));
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
    }

    public void deleteItem(ClipboardItem item) {
        dbExecutor.submit(() -> {
            try {
                PreparedStatement pstmt = connection.prepareStatement("DELETE FROM clipboard WHERE id = ?");
                pstmt.setInt(1, item.getId());
                pstmt.executeUpdate();
                Platform.runLater(() -> clipboardHistory.remove(item));
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
    }

    public void updateItem(ClipboardItem item) {
        dbExecutor.submit(() -> {
            try {
                PreparedStatement pstmt = connection.prepareStatement("UPDATE clipboard SET content = ? WHERE id = ?");
                pstmt.setString(1, item.getContent());
                pstmt.setInt(2, item.getId());
                pstmt.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
    }
    
    public void clearAll() {
        dbExecutor.submit(() -> {
            try {
                Statement stmt = connection.createStatement();
                stmt.executeUpdate("DELETE FROM clipboard");
                Platform.runLater(clipboardHistory::clear);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
    }
}
