package nerd.amara;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class UpdateCheckerTest {

    @Test
    public void testIsNewerMajor() {
        assertTrue(UpdateChecker.isNewer("3.0.0", "2.5.3"));
        assertFalse(UpdateChecker.isNewer("2.5.3", "3.0.0"));
    }

    @Test
    public void testIsNewerMinor() {
        assertTrue(UpdateChecker.isNewer("2.6.0", "2.5.3"));
        assertFalse(UpdateChecker.isNewer("2.5.3", "2.6.0"));
    }

    @Test
    public void testIsNewerPatch() {
        assertTrue(UpdateChecker.isNewer("2.5.4", "2.5.3"));
        assertFalse(UpdateChecker.isNewer("2.5.3", "2.5.4"));
    }

    @Test
    public void testIsNewerSameVersion() {
        assertFalse(UpdateChecker.isNewer("2.5.3", "2.5.3"));
    }

    @Test
    public void testIsNewerWithPrefixOrSuffix() {
        assertTrue(UpdateChecker.isNewer("v3.0.1", "2.5.3"));
        assertTrue(UpdateChecker.isNewer("3.0.1-beta", "3.0.0"));
        assertFalse(UpdateChecker.isNewer("2.5.3", "v2.5.3"));
    }

    @Test
    public void testIsNewerNull() {
        assertFalse(UpdateChecker.isNewer(null, "2.5.3"));
        assertFalse(UpdateChecker.isNewer("2.5.3", null));
        assertFalse(UpdateChecker.isNewer(null, null));
    }
}
