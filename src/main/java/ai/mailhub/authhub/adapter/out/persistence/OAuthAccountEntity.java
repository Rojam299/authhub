package ai.mailhub.authhub.adapter.out.persistence ;

import org.springframework.data.annotation.Id ;
import org.springframework.data.relational.core.mapping.Table ;

import java.time.Instant ;


@Table("oauth_account")
public record OAuthAccountEntity(@Id String id,
                                 String providerUserId,//external user Id
                                 String principalId,
                                 String providerId,
                                 String accessTokenEnc,
                                 Instant tokenExpiry,
                                 String refreshTokenEnc,
                                 String scopesJson,
                                 String status,
                                 Instant createdAt,
                                 Instant updatedAt
                                 ) {

}
