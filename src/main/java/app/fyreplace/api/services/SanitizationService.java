package app.fyreplace.api.services;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public final class SanitizationService {
    public String sanitize(final String input) {
        return input.replace('\r', '\n').replaceAll("\n\n+", "\n\n").trim();
    }
}
