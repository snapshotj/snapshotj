package dev.jdan.snapshotj;

import java.io.File;
import java.nio.file.Path;
import java.util.List;

public final class SnapshotConfig {

    private SnapshotConfig() {}

    public static boolean globalUpdate() {
        throw new UnsupportedOperationException("not implemented");
    }

    public static List<Path> sourceRoots() {
        return SourceRootsHolder.VALUE;
    }

    private static final class SourceRootsHolder {
        private static final List<Path> VALUE = resolve();

        private static List<Path> resolve() {
            String raw = System.getProperty("snapshotj.sourceRoots");
            if (raw == null || raw.isBlank()) {
                return List.of(Path.of("src/test/java"), Path.of("src/main/java"));
            }
            String[] parts = raw.split(java.util.regex.Pattern.quote(File.pathSeparator));
            List<Path> roots = new java.util.ArrayList<>(parts.length);
            for (String p : parts) {
                if (!p.isBlank()) {
                    roots.add(Path.of(p));
                }
            }
            return List.copyOf(roots);
        }
    }
}
