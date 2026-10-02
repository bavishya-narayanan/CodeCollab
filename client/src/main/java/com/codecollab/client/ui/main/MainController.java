package com.codecollab.client.ui.main;

import com.codecollab.client.service.SessionManager;
import com.codecollab.client.ui.BaseController;
import com.codecollab.client.ui.SceneRouter;
import com.codecollab.client.ui.chat.ChatController;
import com.codecollab.client.ui.profile.ProfileController;
import com.codecollab.client.ui.snippet.SnippetDetailController;
import com.codecollab.client.ui.snippet.SnippetEditorController;
import com.codecollab.client.ui.snippet.SnippetListController;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class MainController extends BaseController implements Initializable {

    private static final Logger log = LoggerFactory.getLogger(MainController.class);

    @FXML private BorderPane rootPane;
    @FXML private VBox       sidebar;
    @FXML private Label      usernameLabel;
    @FXML private Label      roleLabel;
    @FXML private Button     btnSnippets;
    @FXML private Button     btnMySnippets;
    @FXML private Button     btnSearch;
    @FXML private Button     btnChat;
    @FXML private Button     btnProfile;

    private SceneRouter myRouter;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        if (state().getCurrentUser() != null) {
            usernameLabel.setText(state().getCurrentUser().getUsername());
            roleLabel.setText(state().getCurrentUser().getRole());
        }
        // Default view
        navigate(SceneRouter.View.SNIPPETS);
    }

    /** Called by SceneRouter after loading this controller. */
    public void setRouter(SceneRouter r) {
        this.myRouter = r;
        r.setMainController(this);
    }

    public void navigate(SceneRouter.View view) {
        clearActiveButtons();
        switch (view) {
            case SNIPPETS    -> { setContent("/fxml/SnippetListView.fxml", null); btnSnippets.getStyleClass().add("active"); }
            case MY_SNIPPETS -> { setContent("/fxml/SnippetListView.fxml", ctrl -> ((SnippetListController)ctrl).showMySnippets()); btnMySnippets.getStyleClass().add("active"); }
            case SEARCH      -> { setContent("/fxml/SnippetListView.fxml", ctrl -> ((SnippetListController)ctrl).showSearch()); btnSearch.getStyleClass().add("active"); }
            case SNIPPET_DETAIL -> setContent("/fxml/SnippetDetailView.fxml",
                    ctrl -> ((SnippetDetailController)ctrl).load(router().detailSnippetId));
            case SNIPPET_EDITOR -> setContent("/fxml/SnippetEditorView.fxml",
                    ctrl -> ((SnippetEditorController)ctrl).load(router().editorSnippetId));
            case CHAT    -> { setContent("/fxml/ChatView.fxml", null); btnChat.getStyleClass().add("active"); }
            case PROFILE -> { setContent("/fxml/ProfileView.fxml", null); btnProfile.getStyleClass().add("active"); }
            default -> {}
        }
    }

    @FXML private void onSnippets()   { router().navigateTo(SceneRouter.View.SNIPPETS); }
    @FXML private void onMySnippets() { router().navigateTo(SceneRouter.View.MY_SNIPPETS); }
    @FXML private void onSearch()     { router().navigateTo(SceneRouter.View.SEARCH); }
    @FXML private void onChat()       { router().navigateTo(SceneRouter.View.CHAT); }
    @FXML private void onProfile()    { router().navigateTo(SceneRouter.View.PROFILE); }
    @FXML private void onLogout()     { SessionManager.getInstance().logout(); }

    // ── helpers ────────────────────────────────────────────────────────────

    private void clearActiveButtons() {
        for (Button b : new Button[]{btnSnippets, btnMySnippets, btnSearch, btnChat, btnProfile}) {
            b.getStyleClass().remove("active");
        }
    }

    private void setContent(String fxmlPath, java.util.function.Consumer<Object> postLoad) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Node node = loader.load();
            rootPane.setCenter(node);
            if (postLoad != null) postLoad.accept(loader.getController());
        } catch (IOException e) {
            log.error("Failed to load {}", fxmlPath, e);
        }
    }
}
