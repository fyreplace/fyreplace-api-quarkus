package app.fyreplace.api.services.storage.s3;

import java.util.List;

public class Policy {
    public String Version = "2012-10-17";

    public List<Statement> Statement;

    public Policy(final List<Statement> statement) {
        Statement = statement;
    }

    public static class Statement {
        public String Effect;

        public String Principal;

        public String Action;

        public String Resource;
    }
}
