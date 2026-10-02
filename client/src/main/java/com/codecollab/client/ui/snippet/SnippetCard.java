package com.codecollab.client.ui.snippet;

import com.codecollab.client.model.Snippet;
import com.codecollab.client.util.TimeFormat;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

/** Custom list cell rendering a snippet card in the list view. */
public class SnippetCard extends ListCell<Snippet> {

    private final VBox  card      = new VBox(4);
    private final Label titleLbl  = new Label();
    private final Label authorLbl = new Label();
    private final Label langLbl   = new Label();
    private final Label metaLbl   = new Label();
    private final HBox  header    = new HBox(8);
    private final HBox  footer    = new HBox(8);

    public SnippetCard() {
        card.getStyleClass().add("snippet-card");
        card.setPadding(new Insets(10, 14, 10, 14));

        titleLbl.getStyleClass().add("snippet-title");
        titleLbl.setStyle("-fx-font-weight: bold; -fx-font-size: 14;");

        langLbl.getStyleClass().addAll("label-tag");
        authorLbl.getStyleClass().add("label-muted");
        metaLbl.getStyleClass().add("label-muted");

        HBox.setHgrow(titleLbl, Priority.ALWAYS);
        header.getChildren().addAll(titleLbl, langLbl);
        footer.getChildren().addAll(authorLbl, metaLbl);

        card.getChildren().addAll(header, footer);
        setGraphic(card);
        setPadding(new Insets(4, 8, 4, 8));
        setStyle("-fx-background-color: transparent;");
    }

    @Override
    protected void updateItem(Snippet s, boolean empty) {
        super.updateItem(s, empty);
        if (empty || s == null) {
            setGraphic(null);
            return;
        }
        titleLbl.setText(s.getTitle());
        langLbl.setText(s.getLanguage() != null ? s.getLanguage() : "");
        authorLbl.setText("by " + (s.getAuthorUsername() != null ? s.getAuthorUsername() : "?"));
        metaLbl.setText(
            "  ▲" + s.getVoteScore() +
            "  ◆" + s.getViewCount() +
            "  ◎" + s.getCommentCount() +
            "  " + TimeFormat.relative(s.getCreatedAt())
        );
        setGraphic(card);
    }
}
