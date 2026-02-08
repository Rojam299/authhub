package ai.mailhub.authhub.application.service;

import ai.mailhub.authhub.crypto.PrincipalIdUtil;
import ai.mailhub.authhub.domain.identity.ExternalIdPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;


/**
 *
 * Boundary between domain and infra
 * Later replaced by Vault implementation
 */
@Service
@RequiredArgsConstructor
public class PrincipalIdService {

    private final SecretKey principalIdSecret;

    public String derive(ExternalIdPrincipal principal){
        return PrincipalIdUtil.hmacPrincipalId(principal,principalIdSecret) ;
    }


}
