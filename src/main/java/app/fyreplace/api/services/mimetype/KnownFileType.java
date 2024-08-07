package app.fyreplace.api.services.mimetype;

public enum KnownFileType {
    JPEG("jpeg", "image/jpeg"),
    PNG("png", "image/png"),
    WEBP("webp", "image/webp");

    public final String name;
    public final String mime;

    KnownFileType(String name, String mime) {
        this.name = name;
        this.mime = mime;
    }
}
