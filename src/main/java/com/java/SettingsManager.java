package com.java;

import java.io.*;
import java.util.Properties;

public class SettingsManager {

    private static final String CONFIG_FILE = "config.properties";
    private final Properties properties = new Properties();

    public SettingsManager() {
        loadSettings();
    }

    private void loadSettings() {
        File file = new File(CONFIG_FILE);
        if (file.exists()) {
            try (InputStream input = new FileInputStream(file)) {
                properties.load(input);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            // Default settings
            properties.setProperty("alwaysOnTop", "true");
            properties.setProperty("maxHistorySize", "50");
            properties.setProperty("theme", "Light");
            saveSettings();
        }
    }

    public void saveSettings() {
        try (OutputStream output = new FileOutputStream(CONFIG_FILE)) {
            properties.store(output, "Ditto Configuration");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean isAlwaysOnTop() {
        return Boolean.parseBoolean(properties.getProperty("alwaysOnTop", "true"));
    }

    public void setAlwaysOnTop(boolean alwaysOnTop) {
        properties.setProperty("alwaysOnTop", String.valueOf(alwaysOnTop));
    }

    public int getMaxHistorySize() {
        try {
            return Integer.parseInt(properties.getProperty("maxHistorySize", "50"));
        } catch (NumberFormatException e) {
            return 50;
        }
    }

    public void setMaxHistorySize(int size) {
        properties.setProperty("maxHistorySize", String.valueOf(size));
    }

    public String getTheme() {
        return properties.getProperty("theme", "Light");
    }

    public void setTheme(String theme) {
        properties.setProperty("theme", theme);
    }
}
