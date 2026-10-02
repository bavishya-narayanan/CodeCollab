package com.codecollab.client.ui.profile;

import com.codecollab.client.model.User;
import com.codecollab.client.ui.BaseController;
import com.codecollab.client.util.TimeFormat;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;

import java.net.URL;
import java.util.ResourceBundle;

public class ProfileController extends BaseController implements Initializable {

    @FXML private Label usernameLabel;
    @FXML private Label emailLabel;
    @FXML private Label roleLabel;
    @FXML private Label memberSinceLabel;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        User u = state().getCurrentUser();
        if (u == null) return;
        usernameLabel.setText(u.getUsername());
        emailLabel.setText(u.getEmail() != null ? u.getEmail() : "—");
        roleLabel.setText(u.getRole() != null ? u.getRole() : "USER");
        memberSinceLabel.setText(TimeFormat.full(u.getCreatedAt()));
    }
}
