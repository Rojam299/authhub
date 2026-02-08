package ai.mailhub.authhub.adapter.out.persistence;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public interface R2dbcOauthAccountRepository extends ReactiveCrudRepository<OAuthAccountEntity, String> {


    @Query("""
        SELECT * FROM oauth_account
        WHERE principal_id = :principalId
          AND provider_id = :providerId
          AND provider_user_id = :externalAccountId
          AND status = 'ACTIVE'
        """)
    Mono<OAuthAccountEntity> findActive(String principalId, String providerId, String externalAccountId);
}

  /*  @Override
    public Mono<OAuthAccount> findActiveByPrincipalAndProvider(String principalId, String providerId) {
        return Mono.empty();
    }

    @Override
    public Mono<OAuthAccount> findActiveByPrincipalAndProviderAndExternalAccount(String principalId, String providerId, String externalAccountId) {
        return Mono.empty();
    }

    @Override
    public Mono<OAuthAccount> save(OAuthAccount account) {
        //Save to DB - Override the existing value
        return Mono.just(account);
    }*/

