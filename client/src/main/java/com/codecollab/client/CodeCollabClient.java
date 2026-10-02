package com.codecollab.client;

import com.codecollab.client.config.AppState;
import com.codecollab.client.service.TokenStorage;
import com.codecollab.client.ui.SceneRouter;
import javafx.application.Application;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * JavaFX entry point for the CodeCollab desktop client.
 * Run via: mvn javafx:run  (from the client/ directory)
 */
public class CodeCollabClient extends Application {

    private static final Logger log = LoggerFactory.getLogger(CodeCollabClient.class);

    @Override
    public void start(Stage primaryStage) {
        log.info("Starting CodeCollab Client");

        primaryStage.setTitle("CodeCollab");
        primaryStage.setWidth(1280);
        primaryStage.setHeight(800);
        primaryStage.setMinWidth(900);
        primaryStage.setMinHeight(600);

        SceneRouter router = new SceneRouter(primaryStage);
        AppState.getInstance().setRouter(router);

        // Try to restore persisted JWT
        String savedToken = TokenStorage.load();
        if (savedToken != null && !savedToken.isBlank()) {
            AppState.getInstance().setToken(savedToken);
            router.navigateTo(SceneRouter.View.MAIN);
        } else {
            router.navigateTo(SceneRouter.View.LOGIN);
        }

        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
