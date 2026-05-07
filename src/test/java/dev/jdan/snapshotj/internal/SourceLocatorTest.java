package dev.jdan.snapshotj.internal;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SourceLocatorTest {

    @Test
    void resolvesUnderFirstRoot(@TempDir Path tempA, @TempDir Path tempB) throws IOException {
        Path expected = writeFixture(tempA, "dev/jdan/Foo.java");

        Path actual = SourceLocator.locate("dev.jdan.Foo", "Foo.java", List.of(tempA, tempB));

        assertEquals(expected, actual);
    }

    @Test
    void resolvesUnderSecondRoot(@TempDir Path tempA, @TempDir Path tempB) throws IOException {
        Path expected = writeFixture(tempB, "dev/jdan/Foo.java");

        Path actual = SourceLocator.locate("dev.jdan.Foo", "Foo.java", List.of(tempA, tempB));

        assertEquals(expected, actual);
    }

    @Test
    void firstRootWinsOverSecond(@TempDir Path tempA, @TempDir Path tempB) throws IOException {
        Path winner = writeFixture(tempA, "dev/jdan/Foo.java");
        writeFixture(tempB, "dev/jdan/Foo.java");

        Path actual = SourceLocator.locate("dev.jdan.Foo", "Foo.java", List.of(tempA, tempB));

        assertEquals(winner, actual);
    }

    @Test
    void defaultPackage(@TempDir Path root) throws IOException {
        Path expected = writeFixture(root, "Foo.java");

        Path actual = SourceLocator.locate("Foo", "Foo.java", List.of(root));

        assertEquals(expected, actual);
    }

    @Test
    void innerClassUsesEnclosingFile(@TempDir Path root) throws IOException {
        Path expected = writeFixture(root, "dev/jdan/Outer.java");

        Path actual = SourceLocator.locate("dev.jdan.Outer$Inner", "Outer.java", List.of(root));

        assertEquals(expected, actual);
    }

    @Test
    void missingFileThrowsWithCandidates(@TempDir Path tempA, @TempDir Path tempB) {
        IllegalStateException ex = assertThrows(
                IllegalStateException.class,
                () -> SourceLocator.locate("dev.jdan.Missing", "Missing.java", List.of(tempA, tempB)));

        String msg = ex.getMessage();
        assertTrue(msg.contains(tempA.resolve("dev/jdan/Missing.java").toString()), msg);
        assertTrue(msg.contains(tempB.resolve("dev/jdan/Missing.java").toString()), msg);
        assertTrue(msg.contains("snapshotj.sourceRoots"), msg);
    }

    @Test
    void nullArgumentsThrow(@TempDir Path root) {
        assertThrows(
                NullPointerException.class,
                () -> SourceLocator.locate(null, "Foo.java", List.of(root)));
        assertThrows(
                NullPointerException.class,
                () -> SourceLocator.locate("dev.jdan.Foo", null, List.of(root)));
        assertThrows(
                NullPointerException.class,
                () -> SourceLocator.locate("dev.jdan.Foo", "Foo.java", null));
    }

    private static Path writeFixture(Path root, String relative) throws IOException {
        Path target = root.resolve(relative);
        Files.createDirectories(target.getParent() == null ? root : target.getParent());
        Files.writeString(target, "// fixture\n");
        return target;
    }
}
