package com.codecollab.client.ui.snippet;

import com.codecollab.client.api.SnippetApi;
import com.codecollab.client.model.Snippet;
import com.codecollab.client.model.Tag;
import com.codecollab.client.ui.BaseController;
import com.codecollab.client.ui.SceneRouter;
import com.codecollab.client.ui.components.AlertHelper;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;

import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class SnippetEditorController extends BaseController implements Initializable {

    @FXML private Label        headingLabel;
    @FXML private TextField    titleField;
    @FXML private TextArea     descField;
    @FXML private ComboBox<String> languageBox;
    @FXML private TextArea     codeArea;
    @FXML private TextField    tagsField;
    @FXML private Button       saveBtn;
    @FXML private Button       cancelBtn;

    private final SnippetApi snippetApi = new SnippetApi();
    private long snippetId = -1; // -1 = create mode

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        languageBox.getItems().addAll("Java", "Python", "JavaScript", "TypeScript",
                "Kotlin", "Go", "Rust", "C++", "C#", "SQL", "Bash", "Other");
    }

    public void load(long id) {
        this.snippetId = id;
        if (id == -1) {
            headingLabel.setText("New Snippet");
        } else {
            headingLabel.setText("Edit Snippet");
            runAsync(
                () -> snippetApi.getById(id),
                s -> {
                    titleField.setText(s.getTitle());
                    descField.setText(s.getDescription() != null ? s.getDescription() : "");
                    languageBox.setValue(s.getLanguage());
                    codeArea.setText(s.getContent());
                    if (s.getTags() != null) {
                        tagsField.setText(s.getTags().stream()
                            .map(Tag::getName).collect(Collectors.joining(", ")));
                    }
                },
                err -> AlertHelper.error("Load failed", err.getMessage())
            );
        }
    }

    @FXML private void handleSave() {
        String title    = titleField.getText().trim();
        String desc     = descField.getText().trim();
        String language = languageBox.getValue();
        String code     = codeArea.getText();
        List<String> tags = parseTags(tagsField.getText());

        if (title.isBlank() || code.isBlank()) {
            AlertHelper.error("Validation", "Title and code are required.");
            return;
        }

        saveBtn.setDisable(true);

        if (snippetId == -1) {
            runAsync(
                () -> snippetApi.create(title, desc, language, code, tags),
                s -> { router().openSnippetDetail(s.getId()); },
                err -> { saveBtn.setDisable(false); AlertHelper.error("Save failed", err.getMessage()); }
            );
        } else {
            final long id = snippetId;
            runAsync(
                () -> snippetApi.update(id, title, desc, language, code, tags),
                s -> { router().openSnippetDetail(s.getId()); },
                err -> { saveBtn.setDisable(false); AlertHelper.error("Save failed", err.getMessage()); }
            );
        }
    }

    @FXML private void handleCancel() {
        if (snippetId == -1) {
            router().navigateTo(SceneRouter.View.SNIPPETS);
        } else {
            router().openSnippetDetail(snippetId);
        }
    }

    private List<String> parseTags(String raw) {
        if (raw == null || raw.isBlank()) return List.of();
        return Arrays.stream(raw.split("[,\\s]+"))
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .collect(Collectors.toList());
    }
}
