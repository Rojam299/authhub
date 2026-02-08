package ai.mailhub.authhub.application.port.out;

import ai.mailhub.authhub.domain.oauth.OAuthAccount;
import ai.mailhub.authhub.domain.oauth.OAuthTokens;
import reactor.core.publisher.Mono;

/**
 * Interface/Service to be used by individual Oauth providers like google etc
 *
 * Resolve provider later on using
 * OAuthProviderClient client =
 *         providerClientRegistry.get(providerId);
 */
public interface OAuthProviderClient {
    /** gmail / outlook */
    String providerId();
    Mono<OAuthTokens> exchangeCode(String code) ;
    Mono<OAuthTokens> refresh(OAuthAccount oAuthAccount);
    Mono<String> fetchExternalAccountId(OAuthTokens oAuthTokens) ;

}
