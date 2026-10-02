package com.codecollab.client.ui.snippet;

import com.codecollab.client.api.SnippetApi;
import com.codecollab.client.model.PageResponse;
import com.codecollab.client.model.Snippet;
import com.codecollab.client.ui.BaseController;
import com.codecollab.client.ui.components.AlertHelper;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class SnippetListController extends BaseController implements Initializable {

    @FXML private Label        headingLabel;
    @FXML private TextField    searchField;
    @FXML private ComboBox<String> languageBox;
    @FXML private HBox         searchBar;
    @FXML private ListView<Snippet> snippetList;
    @FXML private Button       prevBtn;
    @FXML private Button       nextBtn;
    @FXML private Label        pageLabel;
    @FXML private Button       newSnippetBtn;

    private final SnippetApi snippetApi = new SnippetApi();

    private enum Mode { ALL, MY, SEARCH }
    private Mode mode = Mode.ALL;

    private int currentPage = 0;
    private int totalPages  = 1;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        snippetList.setCellFactory(lv -> new SnippetCard());
        snippetList.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2) {
                Snippet s = snippetList.getSelectionModel().getSelectedItem();
                if (s != null) router().openSnippetDetail(s.getId());
            }
        });
        languageBox.getItems().addAll("", "Java", "Python", "JavaScript", "TypeScript",
                "Kotlin", "Go", "Rust", "C++", "C#", "Other");
        languageBox.setValue("");
        loadPage(0);
    }

    public void showMySnippets() {
        mode = Mode.MY;
        headingLabel.setText("My Snippets");
        searchBar.setVisible(false);
        loadPage(0);
    }

    public void showSearch() {
        mode = Mode.SEARCH;
        headingLabel.setText("Search Snippets");
        searchBar.setVisible(true);
    }

    @FXML private void handleSearch() {
        mode = Mode.SEARCH;
        loadPage(0);
    }

    @FXML private void handleNewSnippet() {
        router().openSnippetEditor(-1);
    }

    @FXML private void prevPage() { if (currentPage > 0) loadPage(currentPage - 1); }
    @FXML private void nextPage() { if (currentPage < totalPages - 1) loadPage(currentPage + 1); }

    private void loadPage(int page) {
        runAsync(
            () -> fetchPage(page),
            pageResp -> {
                List<Snippet> items = pageResp.getContent();
                snippetList.setItems(FXCollections.observableArrayList(items));
                currentPage = pageResp.getPage();
                totalPages  = pageResp.getTotalPages();
                pageLabel.setText((currentPage + 1) + " / " + Math.max(1, totalPages));
                prevBtn.setDisable(currentPage == 0);
                nextBtn.setDisable(pageResp.isLast());
            },
            err -> AlertHelper.error("Load failed", err.getMessage())
        );
    }

    private PageResponse<Snippet> fetchPage(int page) throws Exception {
        return switch (mode) {
            case MY     -> snippetApi.mySnippets(page, 20);
            case SEARCH -> snippetApi.search(
                    searchField.getText(), languageBox.getValue(), null, page, 20);
            default     -> snippetApi.list(page, 20);
        };
    }
}
