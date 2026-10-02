package com.codecollab.client.config;

import com.codecollab.client.model.User;
import com.codecollab.client.ui.SceneRouter;

/**
 * Application-wide singleton that holds the authenticated session state.
 */
public final class AppState {

    private static final AppState INSTANCE = new AppState();

    private String  token;
    private User    currentUser;
    private SceneRouter router;

    private AppState() {}

    public static AppState getInstance() { return INSTANCE; }

    // ── token ──────────────────────────────────────────────────────────────

    public String getToken()             { return token; }
    public void   setToken(String token) { this.token = token; }
    public boolean isLoggedIn()          { return token != null && !token.isBlank(); }

    // ── current user ───────────────────────────────────────────────────────

    public User getCurrentUser()           { return currentUser; }
    public void setCurrentUser(User user)  { this.currentUser = user; }

    // ── router ─────────────────────────────────────────────────────────────

    public SceneRouter getRouter()              { return router; }
    public void        setRouter(SceneRouter r) { this.router = r; }

    // ── lifecycle ──────────────────────────────────────────────────────────

    public void logout() {
        token       = null;
        currentUser = null;
    }
}
