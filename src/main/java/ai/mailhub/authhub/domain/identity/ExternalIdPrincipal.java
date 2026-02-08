package ai.mailhub.authhub.domain.identity;


/**
 * What it is
 * Represents who the user is, independent of MailHub, Keycloak, or any IdP.
 *
 * Why it exists
 * AuthHub must work with any OIDC IdP
 *
 * sub alone is not globally unique
 * (issuer + subject) is globally unique
 */

//A record represents a value, but it can have behavior related to that value.
public record ExternalIdPrincipal(String issuer, String subject) {

    /**
     * Stable, globally unique principal id
     * Example: sha256(issuer + "|" + subject)
     *
     * principalId = internal user identifier derived from JWT
     * extracted from the incoming JWT on every request
     *
     * EXAMPLE FOR KC
     * jwt.getIssuer().toString() → http://localhost:8080/realms/mailhub
     * jwt.getSubject()          → f3b2c1d0-8a9e-4e3a-9c6b-123456789abc
     */

    public String principalId() {
        return issuer + "|" + subject;
    }
}
