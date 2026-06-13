package ai.mailhub.authhub.adapter.in.web;


import ai.mailhub.authhub.application.usecase.GetValidAccessTokenService;
import ai.mailhub.authhub.domain.oauth.OAuthToken;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import static reactor.netty.http.HttpConnectionLiveness.log;

@RestController
@RequiredArgsConstructor
@RequestMapping("/internal/oauth")
public class GetValidAccessTokenController {

    private final GetValidAccessTokenService useCase ;

    @GetMapping("/{providerId}/{externalAccountId}/token")
    public Mono<OAuthToken> getToken(
            Authentication authentication,
            @PathVariable String providerId,
            @PathVariable String externalAccountId
    ){
        log.info("AUTH TYPE = {}", authentication.getClass());
        log.info("AUTH = {}", authentication);
        //TODO: Add request id to state, type logic to extract Req id
        String principal = authentication.getName();
        return useCase.execute(principal, providerId, externalAccountId ) ;
    }

}
