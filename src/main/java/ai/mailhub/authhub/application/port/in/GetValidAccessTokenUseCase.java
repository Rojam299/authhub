package ai.mailhub.authhub.application.port.in;

import ai.mailhub.authhub.domain.oauth.OAuthToken;
import reactor.core.publisher.Mono;

public interface GetValidAccessTokenUseCase {
    Mono<OAuthToken> execute(String providerId, String externalAccountId);
}
