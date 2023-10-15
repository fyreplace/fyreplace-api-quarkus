package app.fyreplace.api.services.storage.s3;

import java.util.List;

public record Policy(String Version, List<Statement> Statement) {
    public static final String currentVersion = "2012-10-17";

    public record Statement(String Effect, String Principal, String Action, String Resource) {}
}
