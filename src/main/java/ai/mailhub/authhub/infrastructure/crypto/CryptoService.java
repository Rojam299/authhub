package ai.mailhub.authhub.infrastructure.crypto;

public interface CryptoService {

    String encrypt(String plainText);
    String decrypt(String cipherText);
}
