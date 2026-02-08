package ai.mailhub.authhub.adapter.in.web.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * specifically models Google OAuth response
 * @Data is not used since we have sensitive fields and there's risk of toString() exposing sensitive data
 * We can use @Data with @ToString(exclude = "refreshToken")
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class GoogleTokenResponse {

    @JsonProperty("access_token")
    private String accessToken;

    @JsonProperty("refresh_token")
    private String refreshToken ;

    @JsonProperty("expires_in")
    private int expiresIn;

    @JsonProperty("scope")
    private String scope;

    @JsonProperty("token_type")
    private String tokenType;

    @JsonProperty("id_token")
    private String idToken;

    public boolean hasRefreshToken(){
        return !refreshToken.isEmpty() ;
    }

}


