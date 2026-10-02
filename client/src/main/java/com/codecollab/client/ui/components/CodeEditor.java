package com.codecollab.client.ui.components;

import javafx.scene.layout.StackPane;

/**
 * Simple code editor built on a styled TextArea.
 * (RichTextFX CodeArea can be swapped in here without changing the callers.)
 */
public class CodeEditor extends StackPane {

    private final javafx.scene.control.TextArea area;

    public CodeEditor() {
        area = new javafx.scene.control.TextArea();
        area.setStyle("-fx-font-family: 'Consolas', 'Courier New', monospace; -fx-font-size: 13;");
        area.setWrapText(false);
        area.getStyleClass().add("code-editor");
        getChildren().add(area);
        // fill available space
        javafx.scene.layout.VBox.setVgrow(this, javafx.scene.layout.Priority.ALWAYS);
    }

    public String getCode()             { return area.getText(); }
    public void   setCode(String code)  { area.setText(code); }
    public void   clear()               { area.clear(); }

    public javafx.scene.control.TextArea getTextArea() { return area; }
}
