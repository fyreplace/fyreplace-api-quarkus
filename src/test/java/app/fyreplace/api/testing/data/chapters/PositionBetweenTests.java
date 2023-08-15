package app.fyreplace.api.testing.data.chapters;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import app.fyreplace.api.data.Chapter;
import app.fyreplace.api.testing.TransactionalTests;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

@QuarkusTest
public final class PositionBetweenTests extends TransactionalTests {
    @Test
    public void positionBetweenNullAndNull() {
        assertEquals("z", Chapter.positionBetween(null, null));
    }

    @Test
    public void positionBetweenNullAndSomething() {
        assertEquals("az", Chapter.positionBetween(null, "z"));
    }

    @Test
    public void positionBetweenSomethingAndNull() {
        assertEquals("zz", Chapter.positionBetween("z", null));
    }

    @Test
    public void positionBetweenSmallAndLarge() {
        assertEquals("zaz", Chapter.positionBetween("z", "zz"));
    }

    @Test
    public void positionBetweenLargeAndSmall() {
        assertThrows(IllegalArgumentException.class, () -> Chapter.positionBetween("zz", "z"));
    }
}
