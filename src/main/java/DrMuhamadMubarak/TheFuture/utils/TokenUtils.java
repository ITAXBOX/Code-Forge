package DrMuhamadMubarak.TheFuture.utils;

import java.security.SecureRandom;
import java.util.Base64;

public class TokenUtils {

    public static String generateSecureToken() {
        SecureRandom secureRandom = new SecureRandom();
        byte[] tokenBytes = new byte[32]; // 32 bytes = 256 bits
        secureRandom.nextBytes(tokenBytes);
        return Base64.getEncoder().encodeToString(tokenBytes);
    }
}
