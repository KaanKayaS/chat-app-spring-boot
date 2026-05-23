package com.app.chat_app.core.security.context;

import java.util.List;

import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Component;

/**
 * Request-scoped. Her HTTP isteği için yeni bir instance.
 * JwtAuthFilter token'ı doğruladıktan sonra burayı doldurur,
 * downstream kod (AuthorizationBehavior, handler'lar) buradan okur.
 */
@Component
@Scope(value = "request", proxyMode = ScopedProxyMode.TARGET_CLASS)
public class UserContext {

    private String userId;
    private String email;
    private List<String> roles = List.of();
    private boolean isAuthenticated = false;

    public void setUser(String userId, String email, List<String> roles) {
        this.isAuthenticated = true;
        this.userId = userId;
        this.email = email;
        this.roles = roles == null ? List.of() : List.copyOf(roles);
    }

    public void clear() {
        this.isAuthenticated = false;
        this.userId = null;
        this.email = null;
        this.roles = List.of();
    }

    public String getUserId() {
        return userId;
    }

    public String getEmail() {
        return email;
    }

    public List<String> getRoles() {
        return roles;
    }

    public boolean isAuthenticated() {
        return isAuthenticated;
    }

    public boolean hasRole(String role) {
        return roles.contains(role);
    }
}
