package app.base.utils;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.Arrays;
import java.util.Base64;

public class PasswordUtils {

    private static final int ITERATIONS = 10000;
    private static final int KEY_LENGTH = 256;

    private static byte[] hash(char[] password, byte[] salt) {
        PBEKeySpec spec = new PBEKeySpec(password, salt, ITERATIONS, KEY_LENGTH);
        Arrays.fill(password, Character.MIN_VALUE);
        try {
            SecretKeyFactory skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
            return skf.generateSecret(spec).getEncoded();
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            String message = String
                    .format("Error while hashing a password: [%s], ex: ", new String(password))
                    .concat(e.getMessage());
            throw new AssertionError(message, e);
        } finally {
            spec.clearPassword();
        }
    }

    public static String generate(String password, String salt) {
        byte[] securePassword = hash(password.toCharArray(), salt.getBytes());
        return Base64.getEncoder().encodeToString(securePassword);
    }

    public static String getSalt(String uuid) {
        return "63c5c26a-e1d1-11ea-87d0-0242ac130003__".concat(uuid);
    }

    public static String generateUserPassword(String password, String userUuid) {
        return generate(password,getSalt(userUuid));
    }

}
