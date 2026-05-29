package ai.mailhub.authhub.adapter.in.web;


import ai.mailhub.authhub.adapter.out.oauth.OAuthProvider;
import ai.mailhub.authhub.adapter.out.oauth.OAuthProviderRegistry;
import ai.mailhub.authhub.application.service.PrincipalIdService;
import ai.mailhub.authhub.utils.OAuthStateUtil;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.net.URI;

import static reactor.netty.http.HttpConnectionLiveness.log;


/**
 * Responsibility
 * Build provider authorization URL
 * Redirect user to consent screen
 *
 * GET /oauth/{provider}/authorize
 *
 *
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/oauth")
public class OAuthAuthorizationController {

    private static final Logger logger = LoggerFactory.getLogger(OAuthAuthorizationController.class) ;
    private final OAuthProviderRegistry providerRegistry ;
    private final PrincipalIdService principalIdService;

    @GetMapping("{providerId}/authorize")
    public Mono<ResponseEntity<Void>> authorize(@PathVariable String providerId, Authentication authentication){

        log.info("AUTH TYPE = {}", authentication.getClass());
        log.info("AUTH = {}", authentication);
        //TODO: Add request id to state, type logic to extract Req id
        String principal = authentication.getName();
        String state = OAuthStateUtil.build(principal, providerId) ;
        OAuthProvider provider = providerRegistry.get(providerId) ;
        logger.info("Provider : {} , auth url : {}", provider.id(), provider.buildAuthorizeUrl(state));

        String authUrl = provider.buildAuthorizeUrl(state);

        return Mono.just(
                ResponseEntity
                        .status(HttpStatus.FOUND)
                        .location(URI.create(authUrl))
                        .build()
        );
    }
}
