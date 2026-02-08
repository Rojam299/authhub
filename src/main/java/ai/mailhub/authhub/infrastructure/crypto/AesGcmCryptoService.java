package ai.mailhub.authhub.infrastructure.crypto;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;

@Component
public class AesGcmCryptoService implements CryptoService{

    private static final String ALGO = "AES/GCM/NoPadding";
    private static final int IV_SIZE = 12 ;
    private static final int TAG_SIZE = 128;

    private final SecretKey key ;

    public AesGcmCryptoService(@Value("${crypto.master-key}") String base64Key) {
        this.key = new SecretKeySpec(Base64.getDecoder().decode(base64Key), "AES");
    }

    @Override
    public String encrypt(String plainText) {

        try{
            byte[] iv = SecureRandom.getInstanceStrong().generateSeed(IV_SIZE) ;
            Cipher cipher = Cipher.getInstance(ALGO);
            cipher.init(Cipher.ENCRYPT_MODE, key, new GCMParameterSpec(TAG_SIZE,iv));

            byte[] encrypted = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));
            byte[] combined = new byte[iv.length + encrypted.length] ;
            System.arraycopy(iv, 0 , combined, 0, iv.length);
            System.arraycopy(encrypted,0,combined, iv.length, encrypted.length);

            return Base64.getEncoder().encodeToString(combined) ;

        }catch(Exception ex){

            throw new IllegalStateException("Token encryption failed", ex) ;
        }

    }

    @Override
    public String decrypt(String cipherText) {
        try{
            byte[] decoded = Base64.getDecoder().decode(cipherText) ;
            byte[] iv = Arrays.copyOfRange(decoded, 0, IV_SIZE) ;
            byte[] data = Arrays.copyOfRange(decoded, IV_SIZE, decoded.length);

            Cipher cipher = Cipher.getInstance(ALGO);
            cipher.init(Cipher.DECRYPT_MODE, key, new GCMParameterSpec(TAG_SIZE, iv));

            return new String(cipher.doFinal(data), StandardCharsets.UTF_8);

        } catch (Exception ex) {
            throw new IllegalStateException("Token decryption failed", ex);
        }
    }
}
