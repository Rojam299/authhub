package ai.mailhub.authhub.application.usecase;

import ai.mailhub.authhub.adapter.out.oauth.OAuthProviderClientRegistry;
import ai.mailhub.authhub.application.port.in.GetValidAccessTokenUseCase;
import ai.mailhub.authhub.application.port.out.OAuthAccountRepository;
import ai.mailhub.authhub.application.service.JwtPrincipalExtractor;
import ai.mailhub.authhub.application.service.PrincipalIdService;
import ai.mailhub.authhub.domain.oauth.OAuthAccount;
import ai.mailhub.authhub.domain.oauth.OAuthToken;
import ai.mailhub.authhub.infrastructure.crypto.AesGcmCryptoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.Clock;

/**
 * Goal:
 * Given (user, provider) → return a valid OAuth access token for a user + provider
 *
 * If token exists and not expired → return it
 * If expired → refresh using refresh token
 * If refresh fails → fail clearly (re-consent required)
 *
 * Input:
 * ExternalIdPrincipal (derived from JWT)
 * providerId (google, microsoft, etc)
 *
 * Output:
 * OAuthToken (runtime object)
 *
 * Requirement:
 * principalIdService → derive principalId
 * oauthAccountRepo   → load OAuthAccount from DB
 * cryptoService      → decrypt tokens (stub for now)
 * oauthClient        → call provider token endpoint
 *
 */

@Service
@RequiredArgsConstructor
public class GetValidAccessTokenService implements GetValidAccessTokenUseCase {

    private final PrincipalIdService principalIdService;
    private final OAuthAccountRepository oAuthAccountRepository;
    private final OAuthProviderClientRegistry providerClientRegistry ;
    private final AesGcmCryptoService cryptoService ;
    private final Clock clock;


    public Mono<OAuthToken> execute(
            String providerId, String externalAccountId) {

        return JwtPrincipalExtractor.extract()
                .map(principalIdService::derive) //extPrincipal-> principalIdService.derive(extPrincipal)
                .flatMap(principalId ->
                        oAuthAccountRepository
                                .findActiveByPrincipalAndProviderAndExternalAccount(principalId, providerId, externalAccountId)
                                .switchIfEmpty(Mono.error(new IllegalStateException("No Oauth Account active or linked"))) //TODO :  Mono.error(new OAuthAccountNotLinkedException())
                                .flatMap(oAuthAccount -> {
                                    OAuthToken token = toOauthToken(oAuthAccount);
                                    if (!token.isExpired(clock)) {
                                        return Mono.just(token);
                                    }

                                    return providerClientRegistry
                                            .get(providerId)
                                            .refresh(oAuthAccount)
                                            .flatMap(tokens ->
                                                    oAuthAccountRepository.save(
                                                            oAuthAccount.withRefreshedTokens(tokens, cryptoService))
                                            )
                                            .map(this::toOauthToken) ;
                                })
                );

    }

    private OAuthToken toOauthToken(OAuthAccount account) {

        /**
         * return plaintext access token not encrypted access tokens
         */
        return new OAuthToken(
                cryptoService.decrypt(account.encryptedAccessToken()),// plain text for now
                account.accessTokenExpiry()
        );
    }



}
