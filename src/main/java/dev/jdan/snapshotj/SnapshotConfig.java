package dev.jdan.snapshotj;

import java.io.File;
import java.nio.file.Path;
import java.util.List;

/**
 * Resolves runtime configuration from environment variables and system properties.
 *
 * <p>Values are read once on first access and cached for the lifetime of the JVM, so
 * changing the environment after the first lookup has no effect.
 *
 * <p>Supported settings:
 * <ul>
 *   <li>{@code SNAPSHOTJ_UPDATE} env var or {@code -Dsnapshotj.update=true} — global
 *       opt-in to inline rewrites; equivalent to chaining {@link Snapshot#update()} on
 *       every comparison. See {@link #globalUpdate()}.</li>
 *   <li>{@code -Dsnapshotj.sourceRoots=path1:path2} — override the source roots searched
 *       when locating the calling test file. See {@link #sourceRoots()}.</li>
 * </ul>
 */
public final class SnapshotConfig {

    private SnapshotConfig() {}

    /**
     * Whether update mode is enabled globally.
     *
     * <p>Returns {@code true} when {@code SNAPSHOTJ_UPDATE} is set to {@code 1},
     * {@code true}, or {@code yes} (case-insensitive), or when the system property
     * {@code snapshotj.update} parses as {@code true}. Otherwise {@code false}.
     *
     * @return {@code true} if mismatches should rewrite the inline literal everywhere
     */
    public static boolean globalUpdate() {
        return GlobalUpdateHolder.VALUE;
    }

    private static final class GlobalUpdateHolder {
        private static final boolean VALUE = resolve();

        private static boolean resolve() {
            String env = System.getenv("SNAPSHOTJ_UPDATE");
            if (env != null && (env.equals("1")
                    || env.equalsIgnoreCase("true")
                    || env.equalsIgnoreCase("yes"))) {
                return true;
            }
            return Boolean.parseBoolean(System.getProperty("snapshotj.update"));
        }
    }

    /**
     * Source roots searched when resolving the calling test file.
     *
     * <p>Defaults to {@code [src/test/java, src/main/java]}. Overridden by the system
     * property {@code snapshotj.sourceRoots}, which takes a list of paths separated by
     * the platform path separator ({@code :} on Unix, {@code ;} on Windows). Blank
     * entries are ignored.
     *
     * @return immutable list of source root paths in priority order
     */
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
