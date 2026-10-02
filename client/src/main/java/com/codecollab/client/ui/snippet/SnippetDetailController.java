package com.codecollab.client.ui.snippet;

import com.codecollab.client.api.AstApi;
import com.codecollab.client.api.CommentApi;
import com.codecollab.client.api.SnippetApi;
import com.codecollab.client.api.VoteApi;
import com.codecollab.client.model.*;
import com.codecollab.client.ui.BaseController;
import com.codecollab.client.ui.components.AlertHelper;
import com.codecollab.client.util.TimeFormat;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class SnippetDetailController extends BaseController implements Initializable {

    @FXML private Label     titleLabel;
    @FXML private Label     authorLabel;
    @FXML private Label     languageLabel;
    @FXML private Label     viewsLabel;
    @FXML private Label     scoreLabel;
    @FXML private HBox      tagsBox;
    @FXML private TextArea  codeArea;
    @FXML private Button    upvoteBtn;
    @FXML private Button    downvoteBtn;
    @FXML private Button    editBtn;
    @FXML private Button    deleteBtn;
    @FXML private VBox      commentsBox;
    @FXML private TextArea  commentInput;
    @FXML private VBox      astBox;
    @FXML private Label     qualityLabel;
    @FXML private VBox      issuesBox;

    private final SnippetApi snippetApi = new SnippetApi();
    private final VoteApi    voteApi    = new VoteApi();
    private final CommentApi commentApi = new CommentApi();
    private final AstApi     astApi     = new AstApi();

    private Snippet current;

    @Override
    public void initialize(URL url, ResourceBundle rb) {}

    public void load(long snippetId) {
        runAsync(
            () -> snippetApi.getById(snippetId),
            this::populateSnippet,
            err -> AlertHelper.error("Load failed", err.getMessage())
        );
        loadAnalysis(snippetId);
        loadComments(snippetId, 0);
    }

    private void populateSnippet(Snippet s) {
        this.current = s;
        titleLabel.setText(s.getTitle());
        authorLabel.setText("by " + s.getAuthorUsername() + "  ·  " + TimeFormat.relative(s.getCreatedAt()));
        languageLabel.setText(s.getLanguage() != null ? s.getLanguage() : "");
        viewsLabel.setText(s.getViewCount() + " views");
        scoreLabel.setText("▲ " + s.getVoteScore());

        tagsBox.getChildren().clear();
        if (s.getTags() != null) {
            s.getTags().forEach(t -> {
                Label tl = new Label(t.getName());
                tl.getStyleClass().add("label-tag");
                tagsBox.getChildren().add(tl);
            });
        }

        codeArea.setText(s.getContent());

        // Show edit/delete only for owner
        Long myId = state().getCurrentUser() != null ? state().getCurrentUser().getId() : null;
        boolean isOwner = myId != null && myId.equals(s.getAuthorId());
        editBtn.setVisible(isOwner);
        deleteBtn.setVisible(isOwner || (state().getCurrentUser() != null && state().getCurrentUser().isAdmin()));

        // Highlight current vote
        String mv = s.getMyVote();
        upvoteBtn.setStyle("UP".equals(mv) ? "-fx-text-fill: #a6e3a1;" : "");
        downvoteBtn.setStyle("DOWN".equals(mv) ? "-fx-text-fill: #f38ba8;" : "");
    }

    @FXML private void handleUpvote() {
        if (current == null) return;
        String type = "UP".equals(current.getMyVote()) ? "NONE" : "UP";
        runAsync(() -> voteApi.vote(current.getId(), type), this::populateSnippet,
                 err -> AlertHelper.error("Vote failed", err.getMessage()));
    }

    @FXML private void handleDownvote() {
        if (current == null) return;
        String type = "DOWN".equals(current.getMyVote()) ? "NONE" : "DOWN";
        runAsync(() -> voteApi.vote(current.getId(), type), this::populateSnippet,
                 err -> AlertHelper.error("Vote failed", err.getMessage()));
    }

    @FXML private void handleEdit() {
        if (current != null) router().openSnippetEditor(current.getId());
    }

    @FXML private void handleDelete() {
        if (current == null) return;
        if (!AlertHelper.confirm("Delete Snippet", "Are you sure? This cannot be undone.")) return;
        runAsync(
            () -> { snippetApi.delete(current.getId()); return null; },
            ok -> router().navigateTo(com.codecollab.client.ui.SceneRouter.View.MY_SNIPPETS),
            err -> AlertHelper.error("Delete failed", err.getMessage())
        );
    }

    @FXML private void handleAddComment() {
        String text = commentInput.getText().trim();
        if (text.isBlank() || current == null) return;
        runAsync(
            () -> commentApi.post(current.getId(), text, null),
            c -> {
                commentInput.clear();
                loadComments(current.getId(), 0);
            },
            err -> AlertHelper.error("Comment failed", err.getMessage())
        );
    }

    private void loadComments(long snippetId, int page) {
        runAsync(
            () -> commentApi.list(snippetId, page, 20),
            resp -> renderComments(resp.getContent()),
            err -> {}
        );
    }

    private void renderComments(List<Comment> comments) {
        commentsBox.getChildren().clear();
        if (comments == null) return;
        for (Comment c : comments) {
            commentsBox.getChildren().add(buildCommentNode(c, false));
            if (c.getReplies() != null) {
                for (Comment r : c.getReplies()) {
                    VBox rNode = buildCommentNode(r, true);
                    commentsBox.getChildren().add(rNode);
                }
            }
        }
    }

    private VBox buildCommentNode(Comment c, boolean isReply) {
        VBox box = new VBox(4);
        box.getStyleClass().add("comment-item");
        if (isReply) box.setPadding(new Insets(0, 0, 0, 24));

        Label author = new Label(c.getAuthorUsername() + "  ·  " + TimeFormat.relative(c.getCreatedAt()));
        author.getStyleClass().add("label-muted");

        Label content = new Label(c.isDeleted() ? "[deleted]" : c.getContent());
        content.setWrapText(true);

        box.getChildren().addAll(author, content);
        return box;
    }

    private void loadAnalysis(long snippetId) {
        runAsync(
            () -> astApi.getAnalysis(snippetId),
            this::renderAnalysis,
            err -> astBox.setVisible(false)
        );
    }

    private void renderAnalysis(AstResult result) {
        if (result == null) { astBox.setVisible(false); return; }
        astBox.setVisible(true);
        int score = result.getQualityScore();
        String scoreColor = score >= 80 ? "#a6e3a1" : score >= 50 ? "#f9e2af" : "#f38ba8";
        qualityLabel.setText("Quality Score: " + score + "/100");
        qualityLabel.setStyle("-fx-text-fill: " + scoreColor + "; -fx-font-weight: bold;");

        issuesBox.getChildren().clear();
        if (result.getIssues() != null) {
            for (AstIssue issue : result.getIssues()) {
                Label l = new Label("[" + issue.getSeverity() + "] " + issue.getRuleName() + ": " + issue.getMessage());
                l.setWrapText(true);
                l.getStyleClass().add("label-muted");
                issuesBox.getChildren().add(l);
            }
        }
        if (result.getMetrics() != null) {
            AstMetrics m = result.getMetrics();
            Label ml = new Label(String.format("LOC: %d  Methods: %d  Complexity: %d  Max Nesting: %d",
                m.getLinesOfCode(), m.getMethodCount(), m.getCyclomaticComplexity(), m.getMaxNestingDepth()));
            ml.getStyleClass().add("label-muted");
            issuesBox.getChildren().add(ml);
        }
    }
}
