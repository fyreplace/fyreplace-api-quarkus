package app.fyreplace.api.services.mimetype;

import java.util.Set;

public enum KnownMimeTypes {
    IMAGE(Set.of("image/jpeg", "image/png", "image/webp"));

    public final Set<String> types;

    KnownMimeTypes(final Set<String> types) {
        this.types = types;
    }
}
