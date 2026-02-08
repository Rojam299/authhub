package ai.mailhub.authhub.crypto;

import ai.mailhub.authhub.domain.identity.ExternalIdPrincipal;

import javax.crypto.Mac;
import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.HexFormat;

/**
 * Pure crypto utility
 * No Spring, no config
 * Easy to replace later
 */

/**
 *
 * MAC (Message Authentication Code) verifies that
 * “This data hasn’t been tampered with”
 * Used for:
 * API request validation
 * Token integrity
 * Example: HmacSHA256
 */

public class PrincipalIdUtil {

    private PrincipalIdUtil(){
    }

    public static String hmacPrincipalId(ExternalIdPrincipal principal,
                                         SecretKey key){

        try {
            Mac mac = Mac.getInstance("HmacSHA256") ;
            mac.init(key);
            byte[] result = mac.doFinal(principal.principalId().getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(result) ;

        }catch(Exception ex ){
            throw new IllegalStateException("Failed to derive principalId", ex);
        }
    }
}
