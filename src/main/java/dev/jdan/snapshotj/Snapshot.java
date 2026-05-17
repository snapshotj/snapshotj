package dev.jdan.snapshotj;

import dev.jdan.snapshotj.internal.CsvRenderer;
import dev.jdan.snapshotj.internal.DiffFormatter;
import dev.jdan.snapshotj.internal.JsonRenderer;
import dev.jdan.snapshotj.internal.Normalizer;
import dev.jdan.snapshotj.internal.PendingEdits;
import dev.jdan.snapshotj.internal.SourceLocator;
import dev.jdan.snapshotj.internal.SourceLocator.CallerFrame;
import dev.jdan.snapshotj.internal.TextBlockFinder;

import java.nio.file.Path;
import java.util.Objects;
import java.util.function.Function;

/**
 * Fluent comparison handle for an inline snapshot.
 *
 * <p>{@link #matches(String, Function)} is the single comparison primitive;
 * {@link #matchesJson(String)} and {@link #matchesCsv(String)} bind the built-in
 * renderers and delegate. Both sides are folded to canonical form via
 * {@link Normalizer} before comparison, so trailing whitespace, trailing newlines,
 * and line-ending differences do not cause spurious mismatches.
 *
 * <p>{@link #update()} opts into in-place rewriting: on mismatch the inline literal
 * is queued for rewriting and the test fails with a "snapshot updated" message so
 * CI never silently passes a stale {@code .update()} call.
 */
public final class Snapshot<T> {

    private final T value;
    private boolean updateRequested;

    Snapshot(T value) {
        this.value = value;
    }

    /**
     * Opt into rewriting the inline expected literal on mismatch.
     *
     * <p>When set, a mismatching {@code matches*} call queues an in-place rewrite of
     * the inline text block at the caller site and then fails the test with a
     * {@code "snapshot updated at <file>:<line>; rerun without .update() to verify"}
     * message. The test never silently passes while {@code .update()} is present, so a
     * forgotten call in committed code always turns CI red.
     *
     * @return this handle for chaining
     */
    public Snapshot<T> update() {
        this.updateRequested = true;
        return this;
    }

    /**
     * Compare the wrapped value against an inline expected text block using the supplied renderer.
     *
     * <p>This is the single comparison primitive; {@link #matchesJson(String)} and
     * {@link #matchesCsv(String)} are sugar that bind the built-in renderers and delegate
     * here. Both {@code expected} and the renderer output are folded to canonical form
     * (trailing whitespace stripped, trailing newlines collapsed) before equality check,
     * so insignificant whitespace differences never produce false mismatches.
     *
     * <p>Behavior on mismatch:
     * <ul>
     *   <li>If {@link #update()} was called on this handle or {@link SnapshotConfig#globalUpdate()}
     *       is true, the inline literal is queued for rewriting and the test fails with a
     *       {@code "snapshot updated at <file>:<line>"} message.</li>
     *   <li>Otherwise the test fails with a unified diff against the canonical expected
     *       and actual strings.</li>
     * </ul>
     *
     * @param expected the inline expected snapshot (typically a Java text block); must not be {@code null}
     * @param renderer function that converts the wrapped value to its snapshot string; must not be {@code null}
     *                 and must not return {@code null}
     * @throws NullPointerException  if {@code expected} or {@code renderer} is {@code null}
     * @throws IllegalStateException if {@code renderer} returns {@code null}
     * @throws AssertionError        on mismatch, or after queuing an update-mode rewrite
     */
    public void matches(String expected, Function<T, String> renderer) {
        Objects.requireNonNull(expected, "expected");
        Objects.requireNonNull(renderer, "renderer");

        CallerFrame caller = SourceLocator.callerFrame();

        String actual = renderer.apply(value);
        if (actual == null) {
            throw new IllegalStateException("renderer returned null");
        }

        String normalizedActual = Normalizer.normalize(actual);
        String normalizedExpected = Normalizer.normalize(expected);
        if (normalizedActual.equals(normalizedExpected)) {
            return;
        }

        if (updateRequested || SnapshotConfig.globalUpdate()) {
            Path file = SourceLocator.locate(caller.className(), caller.fileName());
            TextBlockFinder.Range range = TextBlockFinder.find(file, caller.lineNumber());
            PendingEdits.enqueue(file, range, normalizedActual);
            throw new AssertionError(
                    "snapshot updated at " + caller.fileName() + ":" + caller.lineNumber()
                            + "; rerun without .update() to verify");
        }

        throw new AssertionError(
                DiffFormatter.format(
                        caller.fileName() + ":" + caller.lineNumber(),
                        normalizedExpected,
                        normalizedActual));
    }

    /**
     * Compare the wrapped value against an inline expected JSON text block.
     *
     * <p>Delegates to {@link #matches(String, Function)} with the built-in JSON renderer
     * (Jackson, alphabetical property order, ISO-8601 dates, {@code \n} line endings).
     *
     * @param expected the inline expected JSON snapshot; must not be {@code null}
     * @throws NullPointerException if {@code expected} is {@code null}
     * @throws AssertionError       on mismatch, or after queuing an update-mode rewrite
     */
    public void matchesJson(String expected) {
        matches(expected, JsonRenderer::render);
    }

    /**
     * Compare the wrapped value against an inline expected CSV text block.
     *
     * <p>Delegates to {@link #matches(String, Function)} with the built-in CSV renderer
     * (Commons CSV, header derived from the bean's properties in alphabetical order).
     * The wrapped value is typically a {@code Collection} of beans.
     *
     * @param expected the inline expected CSV snapshot; must not be {@code null}
     * @throws NullPointerException if {@code expected} is {@code null}
     * @throws AssertionError       on mismatch, or after queuing an update-mode rewrite
     */
    public void matchesCsv(String expected) {
        matches(expected, CsvRenderer::render);
    }
}
