package com.codecollab.client.ui;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

import com.codecollab.client.ui.main.MainController;

/**
 * Central navigation hub.
 * Auth views replace the entire scene.
 * Content views (snippets, chat, profile) are injected into MainController's center pane.
 */
public class SceneRouter {

    private static final Logger log = LoggerFactory.getLogger(SceneRouter.class);

    public enum View {
        LOGIN, REGISTER,
        MAIN,
        SNIPPETS, MY_SNIPPETS, SEARCH,
        SNIPPET_DETAIL, SNIPPET_EDITOR,
        CHAT, PROFILE
    }

    private final Stage          stage;
    private MainController       mainController;
    private Snippet              pendingSnippet; // passed to detail/editor

    // public: used by MainController
    public long detailSnippetId = -1;
    public long editorSnippetId = -1; // -1 = create new

    public SceneRouter(Stage stage) {
        this.stage = stage;
    }

    public void navigateTo(View view) {
        switch (view) {
            case LOGIN    -> switchScene("/fxml/LoginView.fxml");
            case REGISTER -> switchScene("/fxml/RegisterView.fxml");
            case MAIN     -> switchScene("/fxml/MainView.fxml");
            default -> {
                if (mainController != null) mainController.navigate(view);
                else log.warn("MainController not set; cannot navigate to {}", view);
            }
        }
    }

    /** Called by MainController after it loads itself. */
    public void setMainController(MainController mc) {
        this.mainController = mc;
    }

    /** Convenience: navigate to snippet detail for a given ID. */
    public void openSnippetDetail(long snippetId) {
        this.detailSnippetId = snippetId;
        navigateTo(View.SNIPPET_DETAIL);
    }

    /** Convenience: open editor for existing snippet (-1 = new). */
    public void openSnippetEditor(long snippetId) {
        this.editorSnippetId = snippetId;
        navigateTo(View.SNIPPET_EDITOR);
    }

    // ── internals ──────────────────────────────────────────────────────────

    private void switchScene(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();

            // If it's the main view, register its controller
            Object ctrl = loader.getController();
            if (ctrl instanceof MainController mc) {
                mc.setRouter(this);
                mainController = mc;
            }

            Scene scene = new Scene(root);
            scene.getStylesheets().add(
                getClass().getResource("/styles/main.css").toExternalForm());
            stage.setScene(scene);
        } catch (IOException e) {
            log.error("Failed to load {}: {}", fxmlPath, e.getMessage(), e);
        }
    }

    // inner stub to avoid forward-reference issue
    private record Snippet(long id) {}
}
