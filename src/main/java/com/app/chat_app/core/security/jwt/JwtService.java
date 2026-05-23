package com.app.chat_app.core.security.jwt;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;

import javax.crypto.SecretKey;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

@Service
@EnableConfigurationProperties(JwtProperties.class)
public class JwtService {

    private static final Logger log = LoggerFactory.getLogger(JwtService.class);

    // Claim'lerde tip ayırt etmek için. Access token zorunlu olarak bu claim ile imzalı,
    // refresh JWT değil zaten — bu claim sadece "biri elle JWT üretip access yerine kullanmasın"
    // diye defansif tedbir.
    public static final String TOKEN_TYPE_CLAIM = "tokenType";
    public static final String TOKEN_TYPE_ACCESS = "access";
    public static final String EMAIL_CLAIM = "email";

    private static final SecureRandom RANDOM = new SecureRandom();
    private static final int REFRESH_TOKEN_BYTES = 64;

    private final JwtProperties jwtProperties;
    private final SecretKey signingKey;

    public JwtService(JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;

        byte[] keyBytes = Decoders.BASE64.decode(jwtProperties.getSecret());
        this.signingKey = Keys.hmacShaKeyFor(keyBytes);
    }

    // ---- ACCESS TOKEN ----

    public String generateAccessToken(UUID userId, String email) {
        Instant now = Instant.now();
        Instant expiresAt = now.plusSeconds(jwtProperties.getAccessTokenExpirationInSeconds());

        return Jwts.builder()
                .issuer(jwtProperties.getIssuer())
                .subject(userId.toString())
                .claim(EMAIL_CLAIM, email)
                .claim(TOKEN_TYPE_CLAIM, TOKEN_TYPE_ACCESS)
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiresAt))
                .signWith(signingKey)
                .compact();
    }

    /**
     * Token'ı tek seferde parse + verify + tip kontrolü yapar.
     * - İmza geçersiz, expire olmuş veya tokenType=access değilse Optional.empty() döner.
     * - Geçerliyse claim'leri AccessTokenClaims içinde döner.
     *
     * Tüm "access token doğrula" ihtiyacı bu metoddan geçmeli — çıplak extract metodu yok
     * ki yanlışlıkla doğrulamamış token'dan claim okunmasın.
     */
    public Optional<AccessTokenClaims> parseAccessToken(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(signingKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            // parseSignedClaims() expiration'ı kendisi kontrol ediyor (ExpiredJwtException),
            // bu yüzden burada ekstra expire kontrolü gerekmiyor.

            String tokenType = claims.get(TOKEN_TYPE_CLAIM, String.class);
            if (!TOKEN_TYPE_ACCESS.equals(tokenType)) {
                log.debug("Token reddedildi: tokenType '{}' beklenmiyor", tokenType);
                return Optional.empty();
            }

            UUID userId = UUID.fromString(claims.getSubject());
            String email = claims.get(EMAIL_CLAIM, String.class);
            return Optional.of(new AccessTokenClaims(userId, email));
        } catch (Exception e) {
            // ExpiredJwtException, SignatureException, MalformedJwtException, IllegalArgumentException...
            // Hepsini "geçersiz" olarak ele alıyoruz; debug log yeterli.
            log.debug("Access token doğrulanamadı: {}", e.getMessage());
            return Optional.empty();
        }
    }

    // ---- REFRESH TOKEN ----
    // Refresh token JWT DEĞİL, kriptografik rastgele opaque string.
    // DB'de user.refreshToken alanında tutulur; expire bilgisi user.refreshTokenExpiresAt'te.
    // İptal etmek için DB'den null'lamak yeterli (anında etki).

    public String generateRefreshToken() {
        byte[] bytes = new byte[REFRESH_TOKEN_BYTES];
        RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    public Instant getRefreshTokenExpiry() {
        return Instant.now().plusSeconds(jwtProperties.getRefreshTokenExpirationInSeconds());
    }

    /** Kolaylık: tek çağrıda access + refresh ikilisi. */
    public TokenPair generateTokenPair(UUID userId, String email) {
        String access = generateAccessToken(userId, email);
        String refresh = generateRefreshToken();
        Instant refreshExpiresAt = getRefreshTokenExpiry();
        return new TokenPair(access, refresh, refreshExpiresAt);
    }

    public record AccessTokenClaims(UUID userId, String email) { }

    public record TokenPair(String accessToken, String refreshToken, Instant refreshTokenExpiresAt) { }
}
