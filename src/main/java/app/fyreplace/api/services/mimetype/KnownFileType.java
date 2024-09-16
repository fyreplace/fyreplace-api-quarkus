package app.fyreplace.api.services.mimetype;

public enum KnownFileType {
    JPEG("image/jpeg"),
    PNG("image/png"),
    WEBP("image/webp");

    public final String mime;

    KnownFileType(String mime) {
        this.mime = mime;
    }
}
