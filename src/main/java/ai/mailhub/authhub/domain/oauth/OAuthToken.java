package ai.mailhub.authhub.domain.oauth;

import java.time.Clock;
import java.time.Instant;

/**
 * Exists only in memory
 * Never persisted, logged
 *
 * runtime token, access-only
 */
public record OAuthToken(String value, Instant expiresAt) {

    public boolean isExpired(Clock clock) {
        return expiresAt.isBefore(Instant.now(clock).plusSeconds(30));
    }

}
