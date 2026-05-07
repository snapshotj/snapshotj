package dev.jdan.snapshotj.internal;

import dev.jdan.snapshotj.SnapshotConfig;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Resolves a {@code .java} source file from a fully-qualified class name and a
 * source file name (as reported by {@link StackWalker.StackFrame#getFileName()}).
 *
 * <p>For each configured source root, tries
 * {@code <root>/<packagePath>/<fileName>}. First match wins. On miss, throws an
 * {@link IllegalStateException} whose message lists every candidate path that
 * was tried plus the {@code -Dsnapshotj.sourceRoots} override hint.
 */
public final class SourceLocator {

    private SourceLocator() {}

    public static Path locate(String className, String fileName) {
        return locate(className, fileName, SnapshotConfig.sourceRoots());
    }

    public static Path locate(String className, String fileName, List<Path> roots) {
        Objects.requireNonNull(className, "className");
        Objects.requireNonNull(fileName, "fileName");
        Objects.requireNonNull(roots, "roots");

        Path packagePath = packagePath(className);
        List<Path> tried = new ArrayList<>(roots.size());
        for (Path root : roots) {
            Path candidate = root.resolve(packagePath).resolve(fileName);
            if (Files.isRegularFile(candidate)) {
                return candidate;
            }
            tried.add(candidate);
        }
        throw new IllegalStateException(buildMissingMessage(className, fileName, tried));
    }

    private static Path packagePath(String className) {
        int lastDot = className.lastIndexOf('.');
        if (lastDot < 0) {
            return Path.of("");
        }
        String pkg = className.substring(0, lastDot);
        return Path.of(pkg.replace('.', '/'));
    }

    private static String buildMissingMessage(String className, String fileName, List<Path> tried) {
        StringBuilder sb = new StringBuilder();
        sb.append("could not locate source file ").append(fileName)
                .append(" for class ").append(className).append("; tried:");
        for (Path p : tried) {
            sb.append("\n  - ").append(p);
        }
        sb.append("\noverride with -Dsnapshotj.sourceRoots=path1")
                .append(java.io.File.pathSeparator).append("path2");
        return sb.toString();
    }
}
