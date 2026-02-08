package ai.mailhub.authhub.utils;

import ai.mailhub.authhub.domain.oauth.OAuthStateContext;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;

public final class OAuthStateUtil {

    // TODO : move to env / Vault later
    private static final String HMAC_SECRET = "REPLACE_WITH_STRONG_KEY"; // Ideally from env
    private static final String HMAC_ALGO = "HmacSHA256" ;
    private static final long TTL_SECONDS = 300; // 5 min - so that state is not reused

    private OAuthStateUtil(){}

    public static String build(String principalId, String providerId){
        long exp = Instant.now().plusSeconds(TTL_SECONDS).getEpochSecond() ;
        String payload = "uid:" + principalId + "|pid:" + providerId +"|exp:"+exp ;
        String signature = sign(payload) ;
        String state = payload+"."+signature ;
        return Base64.getUrlEncoder().withoutPadding().encodeToString(state.getBytes(StandardCharsets.UTF_8)) ;
    }

    public static OAuthStateContext parse(String encodedState){
        byte[] decoded = Base64.getUrlDecoder().decode(encodedState) ;
        String state = new String(decoded, StandardCharsets.UTF_8) ;

        String[] parts = state.split("\\.") ;
        if(parts.length != 2){
            throw new IllegalArgumentException("Invalid OAuth state format");
        }

        String payload = parts[0] ;
        String signature = parts[1] ;

        if(!signature.equals(sign(payload))){
            throw new SecurityException("Invalid Oauth State signature") ;
        }

        String[] fields = payload.split("\\|");
        String principalId = null ;
        String providerId = null ;
        long exp = 0 ;

        for (String f : fields){
            if(f.startsWith("uid:")) principalId=f.substring(4) ;
            if(f.startsWith("pid:")) providerId=f.substring(4) ;
            if(f.startsWith("exp:")) exp=Long.parseLong(f.substring(4)) ;
        }

        if(Instant.now().getEpochSecond() > exp){
            throw new SecurityException("OAuth state expired");
        }

        return new OAuthStateContext(principalId, providerId);

    }

    private static String sign(String payload){
        try{
            Mac mac = Mac.getInstance(HMAC_ALGO) ;
            mac.init(new SecretKeySpec(HMAC_SECRET.getBytes(StandardCharsets.UTF_8), HMAC_ALGO));
            byte[] sig = mac.doFinal(payload.getBytes(StandardCharsets.UTF_8)) ;
            return Base64.getUrlEncoder().withoutPadding().encodeToString(sig) ;

        } catch (Exception e) {
            throw new RuntimeException("Failed to sign state: "+e);
        }
    }
}
