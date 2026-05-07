package dev.jdan.snapshotj;

import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

// note: snapshotj.sourceRoots is read once per JVM and cached, so the override path
// cannot be exercised reliably here. SourceLocatorTest covers that path via the
// three-arg locate(...) overload, which is the surface that actually matters.
class SnapshotConfigTest {

    @Test
    void defaultsWhenSyspropUnset() {
        if (System.getProperty("snapshotj.sourceRoots") != null) {
            return;
        }
        assertEquals(
                List.of(Path.of("src/test/java"), Path.of("src/main/java")),
                SnapshotConfig.sourceRoots());
    }
}
