package ai.mailhub.authhub.application.service;

import ai.mailhub.authhub.domain.identity.ExternalIdPrincipal;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import reactor.core.publisher.Mono;

/**
 * Helper class that reads from context and has no state.
 *
 * This assumes authentication exists. In case of no JWT
 * You never reach your controller
 * 401 Unauthorized is returned
 *
 * Who puts JWT into the SecurityContext? -> Answer: Spring Security filter chain
 * More info on Who puts JWT into the SecurityContext? in DesignQnA Doc
 */
public final class JwtPrincipalExtractor {

    private JwtPrincipalExtractor() {
    }

    public static Mono<ExternalIdPrincipal> extract() {

        return ReactiveSecurityContextHolder.getContext()
                .map(ctx -> ctx.getAuthentication().getPrincipal())//Gets the authenticated principal On OAuth2 resource server → this is a Jwt
                .cast(Jwt.class)
                .map(jwt -> new ExternalIdPrincipal(
                        jwt.getIssuer().toString(), //http://localhost:8080/realms/master
                        jwt.getSubject() //UUID - ad954b1f-b865-44b4-9333-cf242ad53d79
                ));
    }
}
