package ai.mailhub.authhub.domain.oauth;

//JUST value object so in domain
public record OAuthStateContext( String principalId,
                                  String providerId) {
}
