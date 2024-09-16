package app.fyreplace.api.testing;

import static java.util.stream.IntStream.range;

import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;

public class UserTestsBase extends TransactionalTestsBase {
    @BeforeEach
    @Transactional
    @Override
    public void beforeEach() {
        super.beforeEach();
        range(0, getActiveUserCount()).forEach(i -> dataSeeder.createUser("user_" + i, true, false));
        range(0, getInactiveUserCount()).forEach(i -> dataSeeder.createUser("user_inactive_" + i, false, false));
    }

    public int getActiveUserCount() {
        return 3;
    }

    public int getInactiveUserCount() {
        return 3;
    }
}
