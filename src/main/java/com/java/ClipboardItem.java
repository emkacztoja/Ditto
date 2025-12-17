package com.java;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ClipboardItem {
    private int id;
    private String content;
    private LocalDateTime timestamp;

    public ClipboardItem(int id, String content, LocalDateTime timestamp) {
        this.id = id;
        this.content = content;
        this.timestamp = timestamp;
    }

    public int getId() {
        return id;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public String getFormattedTime() {
        return timestamp.format(DateTimeFormatter.ofPattern("MMM dd, HH:mm:ss"));
    }

    @Override
    public String toString() {
        return content;
    }
}
