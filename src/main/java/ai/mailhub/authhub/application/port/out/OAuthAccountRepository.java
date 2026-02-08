package ai.mailhub.authhub.application.port.out;

import ai.mailhub.authhub.domain.oauth.OAuthAccount;
import reactor.core.publisher.Mono;

/**
 *
 * Defines what the app needs, not how
 * No Spring Data, no SQL, no R2DBC
 * Used by application layer
 *
 * Application asks → “give me an OAuthAccount”
 * Infrastructure answers → “from Postgres via R2DBC”
 *
 * Application never cares how.
 */

public interface OAuthAccountRepository {

    // Maybe fetch all active accounts for a user + provider.
    Mono<OAuthAccount> findActiveByPrincipalAndProvider(
            String principalId,
            String providerId) ;

    Mono<OAuthAccount> findActiveByPrincipalAndProviderAndExternalAccount(
            String principalId,
            String providerId,
            String externalAccountId);

    Mono<OAuthAccount> save(OAuthAccount account);

}
