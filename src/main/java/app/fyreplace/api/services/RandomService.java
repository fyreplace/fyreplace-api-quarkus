package app.fyreplace.api.services;

import jakarta.enterprise.context.ApplicationScoped;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Random;

@ApplicationScoped
public final class RandomService {
    private final Random random;

    public RandomService() throws NoSuchAlgorithmException {
        random = SecureRandom.getInstanceStrong();
    }

    public String generateCode() {
        final var code = random.nextInt(1000000);
        return String.format("%06d", code);
    }
}
