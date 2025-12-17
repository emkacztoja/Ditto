package com.java;

import javafx.scene.control.*;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import java.util.Optional;

public class ClipboardHistoryCell extends ListCell<ClipboardItem> {

    private final ClipboardManager clipboardManager;
    private final VBox graphic = new VBox(4); // Spacing between lines
    private final Text contentText = new Text();
    private final Text timestampText = new Text();

    public ClipboardHistoryCell(ClipboardManager manager) {
        this.clipboardManager = manager;

        // Apply CSS
        contentText.getStyleClass().add("cell-content");
        timestampText.getStyleClass().add("cell-timestamp");

        graphic.getChildren().addAll(contentText, timestampText);

        // --- Context Menu for Edit/Delete ---
        ContextMenu contextMenu = new ContextMenu();
        MenuItem editItem = new MenuItem("Edit");
        MenuItem deleteItem = new MenuItem("Delete");
        
        editItem.setOnAction(event -> showEditDialog());
        deleteItem.setOnAction(event -> {
            if (getItem() != null) {
                clipboardManager.deleteItem(getItem());
            }
        });
        
        contextMenu.getItems().addAll(editItem, deleteItem);
        setContextMenu(contextMenu);

        // --- Mouse Click to Copy ---
        setOnMouseClicked(event -> {
            if (!isEmpty() && getItem() != null) {
                String itemContent = getItem().getContent();
                clipboardManager.setLastCopied(itemContent);
                
                Clipboard clipboard = Clipboard.getSystemClipboard();
                ClipboardContent content = new ClipboardContent();
                content.putString(itemContent);
                clipboard.setContent(content);
            }
        });
    }

    private void showEditDialog() {
        ClipboardItem currentItem = getItem();
        if (currentItem == null) return;

        TextInputDialog dialog = new TextInputDialog(currentItem.getContent());
        dialog.setTitle("Edit Clipboard Item");
        dialog.setHeaderText("Editing item from " + currentItem.getFormattedTime());
        dialog.setContentText("Content:");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(newContent -> {
            currentItem.setContent(newContent);
            clipboardManager.updateItem(currentItem);
            updateItem(currentItem, false); // Force the cell to refresh
        });
    }

    @Override
    protected void updateItem(ClipboardItem item, boolean empty) {
        super.updateItem(item, empty);
        if (empty || item == null) {
            setGraphic(null);
        } else {
            contentText.setText(item.getContent());
            timestampText.setText(item.getFormattedTime());
            setGraphic(graphic);
        }
    }
}
