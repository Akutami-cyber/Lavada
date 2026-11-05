package mainpackage;

import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

final class CredentialHash {
    private static final int ITERATIONS = 120_000;
    private static final int KEY_LENGTH = 256;

    private CredentialHash() {
    }

    static byte[] hash(char[] value, byte[] salt) {
        PBEKeySpec specification = new PBEKeySpec(value, salt, ITERATIONS, KEY_LENGTH);
        try {
            return SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
                .generateSecret(specification).getEncoded();
        } catch (GeneralSecurityException exception) {
            throw new IllegalStateException("Could not verify account credentials.", exception);
        } finally {
            specification.clearPassword();
        }
    }

    static boolean matches(char[] value, byte[] salt, byte[] expectedHash) {
        byte[] candidateHash = hash(value, salt);
        try {
            return MessageDigest.isEqual(expectedHash, candidateHash);
        } finally {
            java.util.Arrays.fill(candidateHash, (byte) 0);
        }
    }
}
