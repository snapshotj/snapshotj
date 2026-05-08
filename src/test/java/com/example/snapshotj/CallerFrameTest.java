package com.example.snapshotj;

import dev.jdan.snapshotj.internal.SourceLocator;
import dev.jdan.snapshotj.internal.SourceLocator.CallerFrame;
import org.junit.jupiter.api.Test;

import java.util.regex.Pattern;

import static dev.jdan.snapshotj.Snap.snap;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CallerFrameTest {

    @Test
    void callerFrameReturnsThisTestClass() {
        CallerFrame frame = SourceLocator.callerFrame();

        assertEquals("com.example.snapshotj.CallerFrameTest", frame.className());
        assertEquals("CallerFrameTest.java", frame.fileName());
        assertTrue(frame.lineNumber() > 0, "line number should be positive");
    }

    @Test
    void nestedHelperResolvesToHelperFrame() {
        CallerFrame frame = helper();

        assertEquals("com.example.snapshotj.CallerFrameTest", frame.className());
        assertEquals("CallerFrameTest.java", frame.fileName());
    }

    @Test
    void mismatchAssertionErrorIncludesFileAndLine() {
        AssertionError err = assertThrows(
                AssertionError.class,
                () -> snap(0).matches("a", n -> "b"));

        String msg = err.getMessage();
        assertTrue(
                Pattern.compile("at CallerFrameTest\\.java:\\d+").matcher(msg).find(),
                "expected 'at CallerFrameTest.java:<n>' in message, got: " + msg);
    }

    @Test
    void matchesJsonMismatchIncludesCallerLine() {
        AssertionError err = assertThrows(
                AssertionError.class,
                () -> snap(1).matchesJson("2"));

        String msg = err.getMessage();
        assertTrue(
                Pattern.compile("at CallerFrameTest\\.java:\\d+").matcher(msg).find(),
                "expected 'at CallerFrameTest.java:<n>' in message, got: " + msg);
    }

    private static CallerFrame helper() {
        return SourceLocator.callerFrame();
    }
}
