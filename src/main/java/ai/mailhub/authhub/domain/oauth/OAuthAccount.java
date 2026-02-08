package ai.mailhub.authhub.domain.oauth;


import ai.mailhub.authhub.infrastructure.crypto.AesGcmCryptoService;

import java.time.Instant;
import java.util.Set;

/**
 * This is where everything comes together
 * <p>
 * core ownership + security model
 * <p>
 * Conceptual responsibilities
 * Owned by one ExternalIdPrincipal
 * Linked to one OAuthProvider
 * Holds encrypted tokens
 * Controls lifecycle (revoked, expired, rotated)
 * <p>
 * AuthHub is the sole owner of tokens
 * Services never see refresh tokens
 * Provider unlink = status → REVOKED
 *
 * @param accountId   UUID
 * @param externalAccountId, // user@gmail.com - to supports multiple accounts per user
 * @param principalId derived from ExternalIdPrincipal
 * @param providerId  google, microsoft
 * @param status      ACTIVE, REVOKED
 *
 *
 */

public record OAuthAccount(String accountId, // UUID
                           String externalAccountId, // user@gmail.com - to supports multiple accounts per user
                           String principalId,
                           String providerId,
                           String encryptedAccessToken,
                           Instant accessTokenExpiry,
                           String encryptedRefreshToken,
                           Set<String> scopesGranted,
                           OAuthAccountStatus status,
                           Instant createdAt,
                           Instant updatedAt) {

    public OAuthAccount withRefreshedTokens(OAuthTokens tokens, AesGcmCryptoService cryptoService) {



        return new OAuthAccount(
                this.accountId(),
                this.externalAccountId(),
                this.principalId(),
                this.providerId(),
                cryptoService.encrypt(tokens.accessToken()),
                tokens.expiresAt(),
                this.encryptedRefreshToken(),
                tokens.scopes(),
                OAuthAccountStatus.ACTIVE,
                this.createdAt,
                Instant.now() ) ;
    }

}
