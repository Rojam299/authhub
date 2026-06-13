package ai.mailhub.authhub.adapter.out.oauth;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * This class would be populated from config
 * For now picking it up from YAML file
 *
 * Using @Component for registering this class else we will need to register by using
 * @EnableConfigurationProperties(OAuthProvidersConfig.class) on main class
 */
@Component
@ConfigurationProperties(prefix="authhub.oauth") //Bind all configuration properties starting with authhub.oauth into this object.
public class OAuthProvidersConfig {

    Map<String, OAuthProvider> providers ;

    public Map<String, OAuthProvider> getProviders() {
        return providers;
    }

    public void setProviders(Map<String, OAuthProvider> providers) {
        this.providers = providers;
    }
}

/**
 * Explanation:
 * ### OAuth Provider Configuration
 *
 * `OAuthProvidersConfig` is annotated with
 * `@ConfigurationProperties(prefix = "authhub.oauth")`,
 * which instructs Spring Boot to bind all properties under
 * `authhub.oauth` from `application.yml` into this bean.
 *
 * For example:
 *
 * ```yaml
 * authhub:
 *   oauth:
 *     providers:
 *       gmail:
 *         id: gmail
 *         client-id: ...
 *         client-secret: ...
 * ```
 *
 * is bound to:
 *
 * ```java
 * Map<String, OAuthProvider> providers;
 * ```
 *
 * where:
 *  `gmail` becomes the **map key** (`providers.get("gmail")`).
 *
 *  The nested properties are mapped to an `OAuthProvider` instance.
 *  Spring Boot's relaxed binding automatically maps
 * kebab-case properties (e.g., `client-id`) to camelCase record fields (e.g., `clientId`).
 *
 */

