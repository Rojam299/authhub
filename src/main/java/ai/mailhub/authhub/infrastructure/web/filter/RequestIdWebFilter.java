package ai.mailhub.authhub.infrastructure.web.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 *
 * Inside the filter
 * If X-Request-Id exists → reuse it
 * Else → generate a new one
 *
 * Extract userId from Keycloak JWT (sub)
 * Propagation
 * Response headers
 * Adds X-Request-Id
 * Adds X-User-Id
 *
 *
 */

@Component
public class RequestIdWebFilter implements WebFilter {

    private final static Logger logger = LoggerFactory.getLogger(RequestIdWebFilter.class) ;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {

          final String REQUEST_ID = "X-Request-Id";
          final String USER_ID = "X-User-Id";

        String requestId = exchange.getRequest().getHeaders().getFirst(REQUEST_ID) ;

        if(requestId == null){
            requestId = UUID.randomUUID().toString() ;
            logger.info("Received empty request id. Setting request id to :{}", requestId);
        }


        String finalRequestId = requestId;
        return ReactiveSecurityContextHolder.getContext()
                .map(ctx -> ctx.getAuthentication())
                .filter(auth -> auth.getPrincipal() instanceof Jwt)
                .map(auth -> ((Jwt) auth.getPrincipal()).getSubject()) // KC user Id
                .defaultIfEmpty("anonymous")
                .flatMap(userId ->{

                            exchange.getResponse().getHeaders().add(REQUEST_ID, finalRequestId) ;
                            exchange.getResponse().getHeaders().add(USER_ID, userId) ;

                            return chain.filter(exchange)
                                    .contextWrite(context ->
                                            context.put(REQUEST_ID, finalRequestId)
                                                    .put(USER_ID,userId)
                                            ) ;
                        });
    }
}
