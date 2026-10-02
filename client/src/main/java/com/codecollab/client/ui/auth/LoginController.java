package com.codecollab.client.ui.auth;

import com.codecollab.client.api.AuthApi;
import com.codecollab.client.service.SessionManager;
import com.codecollab.client.ui.BaseController;
import com.codecollab.client.ui.SceneRouter;
import com.codecollab.client.ui.components.AlertHelper;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class LoginController extends BaseController {

    @FXML private TextField     usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Button        loginButton;
    @FXML private Label         statusLabel;

    private final AuthApi       authApi = new AuthApi();
    private final SessionManager session = SessionManager.getInstance();

    @FXML
    private void handleLogin() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText();

        if (username.isBlank() || password.isBlank()) {
            statusLabel.setText("Username and password are required.");
            return;
        }

        loginButton.setDisable(true);
        statusLabel.setText("Logging in…");

        runAsync(
            () -> authApi.login(username, password),
            token -> {
                session.onLoginSuccess(token);
                router().navigateTo(SceneRouter.View.MAIN);
            },
            err -> {
                loginButton.setDisable(false);
                statusLabel.setText("Login failed: " + err.getMessage());
            }
        );
    }

    @FXML
    private void goToRegister() {
        router().navigateTo(SceneRouter.View.REGISTER);
    }
}
