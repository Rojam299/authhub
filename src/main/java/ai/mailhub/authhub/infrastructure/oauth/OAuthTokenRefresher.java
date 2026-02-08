package ai.mailhub.authhub.infrastructure.oauth;

import ai.mailhub.authhub.domain.oauth.OAuthAccount;
import ai.mailhub.authhub.domain.oauth.OAuthAccountStatus;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.time.Instant;

@Component
public class OAuthTokenRefresher {

    public Mono<OAuthAccount> refresh(OAuthAccount oAuthAccount){

        Instant newExpiry = Instant.now().plusSeconds(3600);
        String newAccessToken = "new-access-token";

        //TODO: Call provider token  endpoint
        return Mono.just(
            new OAuthAccount(
                    oAuthAccount.accountId(),
                    oAuthAccount.externalAccountId(),
                    oAuthAccount.principalId(),
                    oAuthAccount.providerId(),
                    newAccessToken,
                    newExpiry,
                    oAuthAccount.encryptedRefreshToken(),
                    oAuthAccount.scopesGranted(),
                    OAuthAccountStatus.ACTIVE,
                    oAuthAccount.createdAt(),
                    Instant.now()
            )
        ) ;
    }
}
