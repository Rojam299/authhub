package ai.mailhub.authhub.adapter.in.web;


import ai.mailhub.authhub.adapter.out.oauth.OAuthProviderClientRegistry;
import ai.mailhub.authhub.application.port.out.OAuthAccountRepository;
import ai.mailhub.authhub.application.port.out.OAuthProviderClient;
import ai.mailhub.authhub.domain.oauth.OAuthAccount;
import ai.mailhub.authhub.domain.oauth.OAuthAccountStatus;
import ai.mailhub.authhub.domain.oauth.OAuthStateContext;
import ai.mailhub.authhub.infrastructure.crypto.AesGcmCryptoService;
import ai.mailhub.authhub.utils.OAuthStateUtil;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.UUID;

@RestController
@AllArgsConstructor
@RequestMapping("/oauth")
public class OAuthCallbackController {

   // private final OAuthTokenExchangeService tokenExchangeService ;
    private final OAuthAccountRepository oAuthAccountRepository ;
    private final OAuthProviderClientRegistry clientRegistry ;
    private final AesGcmCryptoService cryptoService ;

    @GetMapping("/{providerId}/callback")
    public Mono<ResponseEntity<String>> callback(
            @PathVariable String providerId,
            @RequestParam String code,
            @RequestParam String state){

        //State validation
        OAuthStateContext ctx = OAuthStateUtil.parse(state) ;
        if (!ctx.providerId().equals(providerId)) {
            return Mono.error(new IllegalStateException("Provider mismatch"));
        }

        final OAuthProviderClient providerClient = clientRegistry.get(providerId ) ;

        return providerClient
                .exchangeCode(code)
                .flatMap(oAuthTokens ->
                        providerClient.fetchExternalAccountId(oAuthTokens)
                                .flatMap(extId ->
                                        oAuthAccountRepository
                                                .findActiveByPrincipalAndProviderAndExternalAccount(
                                                        ctx.principalId(),
                                                        providerId,extId
                                                ).flatMap(existing -> Mono.error(new IllegalStateException("Account already linked")))
                                                .switchIfEmpty(
                                                        oAuthAccountRepository.save(

                                                        /**
                                                         * OAuthTokens (runtime)
                                                         *         ↓ +
                                                         *  externalAccountId (from provider API)
                                                         *         ↓
                                                         * OAuthAccount (DB entity)
                                                         */
                                                        new OAuthAccount(
                                                                UUID.randomUUID().toString(),
                                                                extId,
                                                                ctx.principalId(),
                                                                ctx.providerId(),
                                                                cryptoService.encrypt(oAuthTokens.accessToken()), //encrypt(tokens.accessToken())
                                                                oAuthTokens.expiresAt(),
                                                                cryptoService.encrypt(oAuthTokens.refreshToken()),
                                                                oAuthTokens.scopes(),
                                                                OAuthAccountStatus.ACTIVE,
                                                                Instant.now(),
                                                                Instant.now()
                                                        )
                                                ))



                                )
                )
                .thenReturn(ResponseEntity.ok("Account Linked"));

    }

}
