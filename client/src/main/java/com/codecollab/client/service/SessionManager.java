package com.codecollab.client.service;

import com.codecollab.client.api.UserApi;
import com.codecollab.client.config.AppState;
import com.codecollab.client.model.User;
import com.codecollab.client.ui.SceneRouter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Manages the authenticated session lifecycle: login, logout, token refresh.
 */
public class SessionManager {

    private static final Logger log = LoggerFactory.getLogger(SessionManager.class);
    private static final SessionManager INSTANCE = new SessionManager();

    private final UserApi userApi = new UserApi();

    private SessionManager() {}

    public static SessionManager getInstance() { return INSTANCE; }

    /**
     * Called after obtaining a token from AuthApi.
     * Fetches the current user profile and persists the token.
     */
    public void onLoginSuccess(String token) {
        AppState state = AppState.getInstance();
        state.setToken(token);
        TokenStorage.save(token);
        try {
            User me = userApi.me();
            state.setCurrentUser(me);
            log.info("Logged in as {} ({})", me.getUsername(), me.getRole());
        } catch (Exception e) {
            log.warn("Could not fetch user profile after login: {}", e.getMessage());
        }
    }

    /** Clears session and navigates to login screen. */
    public void logout() {
        AppState state = AppState.getInstance();
        WebSocketService.getInstance().disconnect();
        TokenStorage.clear();
        state.logout();
        state.getRouter().navigateTo(SceneRouter.View.LOGIN);
    }

    /**
     * Called on startup when a saved token exists.
     * Validates by fetching /api/users/me. Returns false on 401.
     */
    public boolean restoreSession() {
        try {
            User me = userApi.me();
            AppState.getInstance().setCurrentUser(me);
            log.info("Session restored for {}", me.getUsername());
            return true;
        } catch (Exception e) {
            log.warn("Saved token is invalid, clearing");
            TokenStorage.clear();
            AppState.getInstance().logout();
            return false;
        }
    }
}
