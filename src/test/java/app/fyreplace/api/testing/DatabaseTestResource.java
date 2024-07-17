package app.fyreplace.api.testing;

import io.quarkus.test.h2.H2DatabaseTestResource;
import java.util.Map;

public final class DatabaseTestResource extends H2DatabaseTestResource {
    @Override
    public Map<String, String> start() {
        super.start();
        return Map.of(
                "quarkus.datasource.jdbc.username",
                "h2",
                "quarkus.datasource.jdbc.password",
                "h2",
                "quarkus.datasource.jdbc.url",
                "jdbc:h2:mem:fyreplace");
    }
}
