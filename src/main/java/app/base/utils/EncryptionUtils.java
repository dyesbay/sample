package app.base.utils;


import app.base.exceptions.GBadRequest;
import app.base.objects.GPair;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class EncryptionUtils {

    private EncryptionUtils() {
    }

    private static final String ALGORITHM = "AES";
    private static final String TRANSFORM_FORMAT = "AES/CBC/PKCS5PADDING";

    private static final IvParameterSpec IV_PARAMETER_SPEC =
            new IvParameterSpec(INIT_VECTOR_16_BYTES.getBytes(StandardCharsets.UTF_8));

    private static final SecretKeySpec SECRET_KEY_SPEC =
            new SecretKeySpec(KEY_16_BYTES.getBytes(StandardCharsets.UTF_8), ALGORITHM);


    public static String encrypt(String value) throws GBadRequest {
        try {
            Cipher cipher = Cipher.getInstance(TRANSFORM_FORMAT);
            cipher.init(Cipher.ENCRYPT_MODE, SECRET_KEY_SPEC, IV_PARAMETER_SPEC);
            byte[] encrypted = cipher.doFinal(value.getBytes());
            return Base64.getUrlEncoder().encodeToString(encrypted);
        } catch (Exception ex) {
            throw new GBadRequest(ex, new GPair("value", value));
        }
    }

    public static String decrypt(String encrypted) throws GBadRequest {
        try {
            Cipher cipher = Cipher.getInstance(TRANSFORM_FORMAT);
            cipher.init(Cipher.DECRYPT_MODE, SECRET_KEY_SPEC, IV_PARAMETER_SPEC);
            byte[] original = cipher.doFinal(Base64.getUrlDecoder().decode(encrypted));
            return new String(original);
        } catch (Exception ex) {
            throw new GBadRequest(ex, new GPair("encrypted", encrypted));
        }
    }

    public static String clear(String keyString) {
        if (ObjectUtils.isBlank(keyString)) return keyString;
        return keyString.replaceAll("(-{2,5})(BEGIN|END)([A-Z ]*)(-{2,5})", "").replaceAll("\\n", "");
    }

}
