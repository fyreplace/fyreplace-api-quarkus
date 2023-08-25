package app.fyreplace.api.services;

import jakarta.annotation.Nullable;
import jakarta.enterprise.context.Dependent;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.Objects;
import java.util.ResourceBundle;

@Dependent
public final class LocaleService {
    @Context
    HttpHeaders headers;

    public ResourceBundle getResourceBundle(final String name) {
        final var path = "i18n." + name;
        return headers.getAcceptableLanguages().stream()
                .map(locale -> getResourceBundleOrNull(path, locale))
                .filter(Objects::nonNull)
                .findFirst()
                .orElse(ResourceBundle.getBundle(path));
    }

    private @Nullable ResourceBundle getResourceBundleOrNull(final String name, final Locale locale) {
        try {
            return ResourceBundle.getBundle(name, locale);
        } catch (final MissingResourceException e) {
            return null;
        }
    }
}
