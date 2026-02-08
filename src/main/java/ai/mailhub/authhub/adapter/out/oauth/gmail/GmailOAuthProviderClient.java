package ai.mailhub.authhub.adapter.out.oauth.gmail;

import ai.mailhub.authhub.adapter.in.web.dto.GoogleTokenResponse;
import ai.mailhub.authhub.adapter.out.exception.ProviderTokenRefreshException;
import ai.mailhub.authhub.application.port.out.OAuthProviderClient;
import ai.mailhub.authhub.domain.oauth.OAuthAccount;
import ai.mailhub.authhub.domain.oauth.OAuthTokens;
import ai.mailhub.authhub.infrastructure.crypto.AesGcmCryptoService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.Set;

@Component("gmail") //TODO: Move to constants
@RequiredArgsConstructor
public class GmailOAuthProviderClient implements OAuthProviderClient {

    private final WebClient oauthWebClient;
    private final AesGcmCryptoService cryptoService ;
    private final static Logger logger = LoggerFactory.getLogger(GmailOAuthProviderClient.class) ;

    @Value("${authhub.oauth.providers.gmail.client-id}")
    private String clientId ;

    @Value("${authhub.oauth.providers.gmail.client-secret}")
    private String clientSecret ;

    @Value("${authhub.oauth.providers.gmail.redirect-uri}")
    private String redirectUri ;


//TODO: MAYBE Move provider id's to constants
    @Override
    public String providerId() {
        return "gmail";
    }

    @Override
    public Mono<OAuthTokens> exchangeCode(String code) {
       return oauthWebClient.post()
               .uri("https://oauth2.googleapis.com/token")
               .contentType(MediaType.APPLICATION_FORM_URLENCODED)
               .body(BodyInserters.fromFormData("grant_type", "authorization_code")
                       .with("code", code)
                       .with("client_id", clientId)
                       .with("client_secret", clientSecret)
                       .with("redirect_uri", redirectUri))
               .retrieve()
               .bodyToMono(GoogleTokenResponse.class)
               .map(this::toOAuthTokens) ;

    }

    @Override
    public Mono<OAuthTokens> refresh(OAuthAccount oAuthAccount) {

        String refreshToken = cryptoService.decrypt(oAuthAccount.encryptedRefreshToken());


        return oauthWebClient.post()
                .uri("https://oauth2.googleapis.com/token")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(BodyInserters.fromFormData("grant_type", "refresh_token")
                        .with("client_id", clientId)
                        .with("client_secret", clientSecret)
                        .with("refresh_token", refreshToken))
                .retrieve()
                .onStatus(HttpStatusCode::isError, resp ->
                        Mono.deferContextual(ctx ->
                                resp.bodyToMono(String.class)
                                        .map(body -> new ProviderTokenRefreshException(
                                                providerId(),
                                                resp.statusCode().value(),
                                                String.format("Token refresh failed | Request Id : %s | principal Id : %s | response : %s",
                                                ctx.getOrDefault("requestId","unknown"),
                                                ctx.getOrDefault("principalId", "anonymous"),
                                                        body)
                                        ))
                                ))
                .bodyToMono(GoogleTokenResponse.class)
                .map(r -> {
                    String newRefreshToken = r.getRefreshToken()!=null ? r.getRefreshToken() : refreshToken ;
                    return toOAuthTokens(r, newRefreshToken) ;
                })
                .doOnEach(signal ->{
                   if (!signal.isOnNext()) return ;
                   signal.getContextView().<String>getOrEmpty("requestId")
                           .ifPresent(reqId ->
                                   logger.info("Token Refreshed | request Id : {} | principal Id : {} | provider Id : {}",
                                           reqId, oAuthAccount.principalId(), providerId())
                                   );
                });

    }

    @Override
    public Mono<String> fetchExternalAccountId(OAuthTokens oAuthTokens) {

        return oauthWebClient.post()
                .uri("https://www.googleapis.com/oauth2/v2/userinfo")
                .headers(h -> h.setBearerAuth(oAuthTokens.accessToken()))
                .retrieve()
                .bodyToMono(String.class) ; // will return the email of the user
    }

    private OAuthTokens toOAuthTokens(GoogleTokenResponse r){

        return toOAuthTokens(r, r.getRefreshToken()) ;
    }

    private OAuthTokens toOAuthTokens(GoogleTokenResponse r, String refreshToken){
        return new OAuthTokens(r.getAccessToken(),
                refreshToken,
                Instant.now().plusSeconds(r.getExpiresIn()),
                Set.of(r.getScope().split(" ")));

    }
}
