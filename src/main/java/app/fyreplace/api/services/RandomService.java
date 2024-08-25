package app.fyreplace.api.services;

import jakarta.enterprise.context.ApplicationScoped;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Random;

@ApplicationScoped
public final class RandomService {
    private static final int NUMBER_COUNT = 10;
    private static final int LETTER_COUNT = 26;

    private final Random random;

    public RandomService() throws NoSuchAlgorithmException {
        random = SecureRandom.getInstanceStrong();
    }

    public String generateCode(final int length) {
        final var code = new StringBuilder(length);

        for (var i = 0; i < length; i++) {
            final var n = random.nextInt(NUMBER_COUNT + LETTER_COUNT * 2);
            final int c;

            if (n < NUMBER_COUNT) {
                c = '0' + n;
            } else if (n < NUMBER_COUNT + LETTER_COUNT) {
                c = 'A' + n - NUMBER_COUNT;
            } else {
                c = 'a' + n - NUMBER_COUNT - LETTER_COUNT;
            }

            code.append((char) c);
        }

        return code.toString();
    }
}
