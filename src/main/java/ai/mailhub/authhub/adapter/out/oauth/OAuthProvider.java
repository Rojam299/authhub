package ai.mailhub.authhub.adapter.out.oauth;

import java.util.List;

/**
 * This is configuration, not behavior or spring component
 * Likely load these via YAML / DB
 * <p>
 * Loaded via:
 *
 * @param id google
 * @ConfigurationProperties or DB-backed provider registry
 * <p>
 * Used by authorization + refresh flows
 */

public record OAuthProvider(String id,
                            String clientId,
                            String clientSecret,
                            String authorizeUri,
                            String tokenUri,
                            String redirectUri,
                            String params,
                            List<String> scopes) {

    public String buildAuthorizeUrl(String state) {

        /**
         * extra params will vary per individual OAuth. For example for google we will have
         * "&response_type=code&prompt=consent&access_type=offline"
         *
         * Make sure extra params starts with &
         */
        return authorizeUri +
                "?response_type=code" +
                "&client_id=" + clientId +
                "&redirect_uri=" + redirectUri +
                "&scope=" + String.join(" ", scopes) +
                 params +
                "&state=" + state
                ;


    }


}
