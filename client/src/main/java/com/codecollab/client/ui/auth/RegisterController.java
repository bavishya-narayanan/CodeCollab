package com.codecollab.client.ui.auth;

import com.codecollab.client.api.AuthApi;
import com.codecollab.client.service.SessionManager;
import com.codecollab.client.ui.BaseController;
import com.codecollab.client.ui.SceneRouter;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class RegisterController extends BaseController {

    @FXML private TextField     usernameField;
    @FXML private TextField     emailField;
    @FXML private PasswordField passwordField;
    @FXML private Button        registerButton;
    @FXML private Label         statusLabel;

    private final AuthApi        authApi = new AuthApi();
    private final SessionManager session = SessionManager.getInstance();

    @FXML
    private void handleRegister() {
        String username = usernameField.getText().trim();
        String email    = emailField.getText().trim();
        String password = passwordField.getText();

        if (username.isBlank() || email.isBlank() || password.isBlank()) {
            statusLabel.setText("All fields are required.");
            return;
        }

        registerButton.setDisable(true);
        statusLabel.setText("Registering…");

        runAsync(
            () -> {
                authApi.register(username, email, password);
                // Auto-login after registration
                return authApi.login(username, password);
            },
            token -> {
                session.onLoginSuccess(token);
                router().navigateTo(SceneRouter.View.MAIN);
            },
            err -> {
                registerButton.setDisable(false);
                statusLabel.setText("Registration failed: " + err.getMessage());
            }
        );
    }

    @FXML
    private void goToLogin() {
        router().navigateTo(SceneRouter.View.LOGIN);
    }
}
