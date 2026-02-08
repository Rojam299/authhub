package ai.mailhub.authhub.adapter.out.oauth;

import ai.mailhub.authhub.application.port.out.OAuthProviderClient;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class OAuthProviderClientRegistry {

    private final Map<String, OAuthProviderClient> clients;

    public OAuthProviderClientRegistry(Map<String, OAuthProviderClient> clients){
        this.clients=clients ;
    }

    public OAuthProviderClient get(String providerId){
        OAuthProviderClient client = clients.get(providerId) ;
        if (client == null) {
            throw new IllegalArgumentException("Unsupported OAuth provider: " + providerId);
        }
        return client;
    }
}
