package ai.mailhub.authhub.adapter.in.web.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.time.Instant;

/**
 * RFC 7807 compliant
 *   "type": "about:blank",
 *   "title": "Conflict",
 *   "status": 409,
 *   "detail": "Account already linked",
 *   "instance": "/oauth/gmail/callback",
 *   "requestId": "a9f3c2e1",
 *   "userId": "keycloak-sub-123",
 *   "timestamp": "2026-01-24T14:22:11Z"
 * }
 */

@ControllerAdvice
public class GlobalExcetionHandler {

    private static final String REQUEST_ID_HEADER = "X-Request-Id";
    private static final String USER_HEADER = "X-User-Id";
    private static final String NO_REQUEST_ID = "No-request-id";

    @ExceptionHandler(IllegalStateException.class)
    public Mono<ResponseEntity<ProblemDetail>> handleIllegalStateException(IllegalStateException exception,
                                                                           ServerWebExchange exchange){

        return buildProblem(HttpStatus.CONFLICT,
                "Conflict",
                exception.getMessage(),
                exchange);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public Mono<ResponseEntity<ProblemDetail>> handleIllegalArgumentException(IllegalArgumentException exception,
                                                                              ServerWebExchange exchange){

        return buildProblem(HttpStatus.BAD_REQUEST,
                "Bad Request",
                exception.getMessage(),
                exchange) ;
    }

    @ExceptionHandler(Exception.class)
    public Mono<ResponseEntity<ProblemDetail>> handleGeneric(Exception exception,
                                                             ServerWebExchange exchange){

        return buildProblem(HttpStatus.INTERNAL_SERVER_ERROR,
                "Internal Server Error",
                "Unexpected Error occurred",
                exchange) ;
    }

    private Mono<ResponseEntity<ProblemDetail>> buildProblem(HttpStatus status,
                                                             String title,
                                                             String detail,
                                                             ServerWebExchange exchange){

        return Mono.deferContextual(ctx->{

            String requestId = ctx.getOrDefault(REQUEST_ID_HEADER, NO_REQUEST_ID) ;
            String userId = ctx.getOrDefault(USER_HEADER, "anonymous") ;

            ProblemDetail problem = ProblemDetail.forStatusAndDetail(status, detail) ;
            problem.setTitle(title);
            problem.setInstance(exchange.getRequest().getURI());
            problem.setProperty("requestId", requestId);
            problem.setProperty("userId", userId);
            problem.setProperty("timestamp", Instant.now());

            return Mono.just(ResponseEntity.status(status).body(problem));
        }) ;
    }

}


