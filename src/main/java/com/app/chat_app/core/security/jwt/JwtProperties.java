package com.app.chat_app.core.security.jwt;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "security.jwt")
public class JwtProperties {
    private String secret;
    private String issuer = "chat-app";

    // .NET'teki appsettings.json'da iki ayrı alan tutmana karşılık geliyor.
    private long accessTokenExpirationInSeconds = 900;         // 15 dk
    private long refreshTokenExpirationInSeconds = 7 * 24 * 60 * 60; // 7 gün

    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    public String getIssuer() {
        return issuer;
    }

    public void setIssuer(String issuer) {
        this.issuer = issuer;
    }

    public long getAccessTokenExpirationInSeconds() {
        return accessTokenExpirationInSeconds;
    }

    public void setAccessTokenExpirationInSeconds(long accessTokenExpirationInSeconds) {
        this.accessTokenExpirationInSeconds = accessTokenExpirationInSeconds;
    }

    public long getRefreshTokenExpirationInSeconds() {
        return refreshTokenExpirationInSeconds;
    }

    public void setRefreshTokenExpirationInSeconds(long refreshTokenExpirationInSeconds) {
        this.refreshTokenExpirationInSeconds = refreshTokenExpirationInSeconds;
    }
}
