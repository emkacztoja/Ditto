# Ditto - Smart Clipboard Manager

**Ditto** is a lightweight, privacy-focused, cross-platform clipboard manager built with **Java 17** and **JavaFX**. It runs entirely locally, persisting your clipboard history to a secure SQLite database, ensuring your data never leaves your machine.

---

## 🚀 Features

*   **Clipboard History**: Automatically captures text copied to the system clipboard.
*   **Persistence**: Saves the last 50 (configurable) items to a local SQLite database (`clipboard.db`), so history survives restarts.
*   **Smart "Self-Copy" Detection**: Prevents duplicate entries when you copy an item *from* Ditto back to the system clipboard.
*   **Search & Filter**: Real-time search bar to quickly find past snippets.
*   **Edit & Delete**: Right-click any item to edit its content or remove it from history.
*   **System Tray Integration**: Minimizes to the system tray to run unobtrusively in the background.
*   **Theming**: Built-in **Dark Mode** and Light Mode, configurable via settings.
*   **Always on Top**: Option to keep the window floating above other applications.
*   **Privacy First**: No internet connection required. No cloud syncing. Your data stays on your disk.

---

## 🛠 Prerequisites

*   **Java Development Kit (JDK) 17** or higher.
*   **Maven** (for dependency management and building).

---

## 📦 Installation & Running

1.  **Clone the repository**
    ```bash
    git clone https://github.com/emkacztoja/Ditto.git
    ```
2.  **Navigate to the project directory**
    ```bash
    cd Ditto
    ```
2.  **Run with Maven**:
    The project uses the `javafx-maven-plugin` for easy execution.
    ```bash
    mvn clean javafx:run
    ```

3.  **Building a Jar**:
    To package the application:
    ```bash
    mvn clean package
    ```

---

## 🖥 Usage Guide

### Main Interface
*   **Copying**: Just use `Ctrl+C` (or `Cmd+C`) in any application. Ditto will automatically add the text to the top of the list.
*   **Pasting**: Click any item in the Ditto list. It will be copied back to your system clipboard, ready to be pasted (`Ctrl+V`) anywhere.
*   **Searching**: Type in the top search bar to filter items by content.
*   **Context Menu**: Right-click an item to:
    *   **Edit**: Modify the text of a saved snippet.
    *   **Delete**: Permanently remove the item from the database.

### Settings
Click the **Settings** button to configure:
*   **Theme**: Toggle between Light and Dark visual themes.
*   **Always on Top**: Keep the Ditto window visible over other apps.
*   **Max History Size**: Limit how many items are stored (default: 50).

### System Tray
*   Closing the main window **minimizes** Ditto to the system tray (it does not exit).
*   Click the Tray Icon to restore the window.
*   Right-click the Tray Icon to **Exit** the application completely.

---

## 🏗 Technical Architecture

### Tech Stack
*   **Language**: Java 17
*   **UI Framework**: JavaFX 17.0.2
*   **Database**: SQLite (via `sqlite-jdbc`)
*   **Build Tool**: Maven

### Key Components

1.  **`Main.java`**:
    *   The entry point extending `javafx.application.Application`.
    *   Handles the UI setup, CSS loading, and Scene graph.
    *   Manages **System Tray** integration using AWT (`java.awt.SystemTray`) wrapped in `SwingUtilities.invokeLater` to ensure thread safety on Linux/macOS.
    *   Implements the "Always on Top" logic using a listener on the `showing` property to ensure cross-platform compatibility.

2.  **`ClipboardManager.java`**:
    *   **Polling Engine**: Uses a `ScheduledExecutorService` to check the system clipboard every 1000ms.
    *   **Thread Safety**: Polling happens on a background thread, but UI updates are dispatched to the JavaFX Application Thread using `Platform.runLater()`.
    *   **Database**: Handles all JDBC connections to `clipboard.db`. Includes automatic schema migration (e.g., adding the `timestamp` column if missing).
    *   **Loop Prevention**: Uses a `lastCopied` flag to distinguish between user copies and app-initiated copies.

3.  **`ClipboardItem.java`**:
    *   The data model representing a history entry. Stores `id`, `content`, and `timestamp`.

4.  **`ClipboardHistoryCell.java`**:
    *   A custom `ListCell` implementation.
    *   Handles the rendering of content + timestamp.
    *   Manages the Right-Click Context Menu (Edit/Delete).

5.  **`SettingsManager.java`**:
    *   Persists user preferences to a `config.properties` file.

### Linux Specifics
*   **GTK Version**: The app forces `jdk.gtk.version=2` to prevent `Gdk-WARNING` and conflicts between JavaFX and AWT on some Linux distributions.
*   **Threading**: AWT Tray initialization is strictly confined to the Event Dispatch Thread (EDT).

---

## 📂 Project Structure

```
ditto/
├── pom.xml                 # Maven dependencies and build config
├── config.properties       # User settings (auto-generated)
├── clipboard.db            # SQLite database (auto-generated)
└── src/
    └── main/
        ├── java/
        │   └── com/java/
        │       ├── Main.java                 # Entry point & UI
        │       ├── ClipboardManager.java     # Logic & DB
        │       ├── ClipboardItem.java        # Model
        │       ├── ClipboardHistoryCell.java # Custom List View
        │       ├── SettingsManager.java      # Config handling
        │       └── SettingsDialog.java       # Settings UI
        └── resources/
            ├── styles.css                    # CSS for Light/Dark themes
            └── icon.png                      # App Icon
```

---

## 🤝 Contributing

1.  Fork the repository.
2.  Create a feature branch (`git checkout -b feature/AmazingFeature`).
3.  Commit your changes (`git commit -m 'Add some AmazingFeature'`).
4.  Push to the branch (`git push origin feature/AmazingFeature`).
5.  Open a Pull Request.

---

## 📄 License

Distributed under the MIT License. See `LICENSE` for more information.
