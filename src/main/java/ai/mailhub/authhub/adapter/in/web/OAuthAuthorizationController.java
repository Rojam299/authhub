package ai.mailhub.authhub.adapter.in.web;


import ai.mailhub.authhub.adapter.out.oauth.OAuthProvider;
import ai.mailhub.authhub.adapter.out.oauth.OAuthProviderRegistry;
import ai.mailhub.authhub.application.service.JwtPrincipalExtractor;
import ai.mailhub.authhub.application.service.PrincipalIdService;
import ai.mailhub.authhub.utils.OAuthStateUtil;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.net.URI;


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
    public Mono<ResponseEntity<Void>> authorize(@PathVariable String providerId){

        //TODO: Add request id to state, type logic to extract Req id
        return JwtPrincipalExtractor.extract()
                .map(principalIdService::derive)
                .map(principal -> {
                    String state = OAuthStateUtil.build(principal, providerId) ;

                    OAuthProvider provider = providerRegistry.get(providerId) ;
                    //TODO : Check and remove or logger.debug
                    logger.info("Provider : {} , auth url : {}", provider.id(), provider.buildAuthorizeUrl(state));

                    String authUrl = providerRegistry.get(providerId).buildAuthorizeUrl(state) ;

                    return ResponseEntity
                            .status(HttpStatus.FOUND)
                            .location(URI.create(authUrl))
                            .build();
                        }
                ) ;
    }


}
