package dev.jdan.snapshotj;

/**
 * Entry point for inline snapshot testing.
 *
 * <p>Wrap a value with {@link #snap(Object)} to obtain a {@link Snapshot} handle, then
 * compare against an inline Java text block via {@link Snapshot#matches(String, java.util.function.Function)},
 * {@link Snapshot#matchesJson(String)}, or {@link Snapshot#matchesCsv(String)}.
 *
 * <p>Typical usage:
 * <pre>{@code
 * import static dev.jdan.snapshotj.Snap.snap;
 *
 * snap(user).matchesJson("""
 *         {
 *           "id": 1,
 *           "name": "Ada"
 *         }
 *         """);
 * }</pre>
 *
 * <p>On mismatch the test fails with a unified diff. To rewrite the expected literal
 * in place, chain {@link Snapshot#update()} or set the {@code SNAPSHOTJ_UPDATE=1}
 * environment variable (equivalently, {@code -Dsnapshotj.update=true}).
 */
public final class Snap {

    private Snap() {}

    /**
     * Wrap {@code value} in a {@link Snapshot} handle for inline comparison.
     *
     * @param value the actual value under test; may be {@code null} if the renderer accepts it
     * @param <T>   the value type
     * @return a fluent comparison handle
     */
    public static <T> Snapshot<T> snap(T value) {
        return new Snapshot<>(value);
    }
}
