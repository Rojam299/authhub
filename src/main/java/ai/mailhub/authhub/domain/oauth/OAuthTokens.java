package ai.mailhub.authhub.domain.oauth;


import java.time.Instant;
import java.util.Set;

/**
 * business concept, not an API DTO.
 */
public record OAuthTokens(
        String accessToken,
        String refreshToken,
        Instant expiresAt,
        Set<String> scopes
) {}