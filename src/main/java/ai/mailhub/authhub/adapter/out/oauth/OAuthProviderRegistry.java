package ai.mailhub.authhub.adapter.out.oauth;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class OAuthProviderRegistry {

    private final OAuthProvidersConfig config ;

    public OAuthProvider get(String id){

        return Optional.ofNullable(config.getProviders().get(id))
                .orElseThrow(() -> new IllegalArgumentException("Unknown provider : "+id));
    }


}
