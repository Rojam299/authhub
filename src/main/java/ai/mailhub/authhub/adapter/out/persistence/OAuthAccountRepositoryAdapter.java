package ai.mailhub.authhub.adapter.out.persistence;

import ai.mailhub.authhub.application.port.out.OAuthAccountRepository;
import ai.mailhub.authhub.domain.oauth.OAuthAccount;
import ai.mailhub.authhub.domain.oauth.OAuthAccountStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

import java.util.Set;

@Component
@RequiredArgsConstructor
public class OAuthAccountRepositoryAdapter implements OAuthAccountRepository {

    private final R2dbcOauthAccountRepository repo ;
    private final ObjectMapper mapper ;

    //TODO: IF required
    @Override
    public Mono<OAuthAccount> findActiveByPrincipalAndProvider(String principalId, String providerId) {
        return null;
    }

    @Override
    public Mono<OAuthAccount> findActiveByPrincipalAndProviderAndExternalAccount(String principalId, String providerId, String externalAccountId) {
        return repo.findActive(principalId,providerId,externalAccountId)
                .map(this::toDomain);
    }

    @Override
    public Mono<OAuthAccount> save(OAuthAccount account) {
        return repo.save(toEntity(account))
                .map(this::toDomain);
    }


    private OAuthAccount toDomain(OAuthAccountEntity e){

        return new OAuthAccount(
                e.id(),
                e.providerUserId(),
                e.principalId(),
                e.providerId(),
                e.accessTokenEnc(),
                e.tokenExpiry(),
                e.refreshTokenEnc(),
                readScopes(e.scopesJson()),
                OAuthAccountStatus.valueOf(e.status()),
                e.createdAt(),
                e.updatedAt()
        );
    }

    private OAuthAccountEntity toEntity(OAuthAccount domain){

        return new OAuthAccountEntity(
                domain.accountId(),
                domain.externalAccountId(),
                domain.principalId(),
                domain.providerId(),
                domain.encryptedAccessToken(),
                domain.accessTokenExpiry(),
                domain.encryptedRefreshToken(),
                writeScopes(domain.scopesGranted()),
                domain.status().name(),
                domain.createdAt(),
                domain.updatedAt()
        ) ;
    }


    //----helpers

    private String writeScopes(Set<String> scopes){

        try{

            return mapper.writeValueAsString(scopes);
        }catch (Exception ex ){
            throw new IllegalStateException("Failed to serialize scopes ", ex);
        }
    }

    private Set<String> readScopes(String json){

        try{

            return mapper.readValue(json,
                    new TypeReference<>() {
                    });
        }catch (Exception ex ){
            throw new IllegalStateException("Failed to deserialize scopes ", ex);
        }
    }


}
