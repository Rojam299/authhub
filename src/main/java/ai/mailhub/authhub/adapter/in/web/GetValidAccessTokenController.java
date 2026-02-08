package ai.mailhub.authhub.adapter.in.web;


import ai.mailhub.authhub.application.usecase.GetValidAccessTokenService;
import ai.mailhub.authhub.domain.oauth.OAuthToken;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequiredArgsConstructor
@RequestMapping("/internal/oauth")
public class GetValidAccessTokenController {

    private final GetValidAccessTokenService useCase ;

    @GetMapping("/{providerId}/{externalAccountId}/token")
    public Mono<OAuthToken> getToken(
            @PathVariable String providerId,
            @PathVariable String externalAccountId
    ){

        return useCase.execute(providerId, externalAccountId) ;
    }

}
