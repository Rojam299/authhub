package ai.mailhub.authhub.adapter.out.oauth;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * This class would be populated from config
 * Contains key value pairs of providers
 *
 * For now picking it up from YAML file
 *
 * Using @Component for registering this class else we will need to register by using
 * @EnableConfigurationProperties(OAuthProvidersConfig.class) on main class
 */
@Component
@ConfigurationProperties(prefix="authhub.oauth")
public class OAuthProvidersConfig {

    Map<String, OAuthProvider> providers ;

    public Map<String, OAuthProvider> getProviders() {
        return providers;
    }

    public void setProviders(Map<String, OAuthProvider> providers) {
        this.providers = providers;
    }
}
