# Contributing to snapshotj

Thanks for your interest in contributing. This document covers what you need to know to make a change land cleanly.

## Before you start

- Check the issue tracker before starting work on something non-trivial. Mention which issue you're picking up.
- For anything that changes the public API (`Snap`, `Snapshot`, `SnapshotConfig`) or violates one of the invariants listed below, open an issue first to discuss the change.

## Requirements

- JDK 17 (Gradle toolchain will resolve it if you don't have it locally).
- Git.

No IDE is required, but IntelliJ IDEA imports the Gradle project cleanly.

## Building and testing

```bash
./gradlew build                                                     # compile + test
./gradlew test                                                      # unit tests only
./gradlew test --tests dev.jdan.snapshotj.internal.NormalizerTest   # one class
./gradlew test --tests '*NormalizerTest.normalizesTrailingNewlines' # one method
./gradlew publishToMavenLocal                                       # smoke test the published artifact
```

`./gradlew build` must pass before you open a PR. The same command runs in CI.

## Code layout

```
dev.jdan.snapshotj
├── Snap                # static snap(value) entry
├── Snapshot            # update(), matches(), matchesJson(), matchesCsv()
├── SnapshotConfig      # env var / sysprop reads
└── internal/           # everything else; not part of the public surface
```

`internal/` is a convention, not a JPMS module. Treat anything under `internal/` as private — callers outside the package should not need to touch it. If you find yourself wanting to expose something, raise it in an issue.

## Invariants you must not break

1. **`.update()` always fails the test after rewriting.** A `.update()` left in committed code must turn CI red. Never make it silently green.
2. **Comparison normalizes trailing whitespace and trailing newlines on both sides.** Normalization happens in one place (`Normalizer`) and is documented as the canonical form.
3. **Edits are queued, not applied at mismatch.** A JVM shutdown hook flushes per file: reads, applies queued edits in reverse line order, writes via `Files.move` from a same-directory temp file.
4. **Source path discovery uses `src/test/java` and `src/main/java` defaults**, overridable via `-Dsnapshotj.sourceRoots=path1:path2`. If the file can't be located, throw a clear error listing the candidates.
5. **`<snap:ignore>` placeholders are out of scope for v1.** Don't add them.
6. **`matchesTable` is dropped from v1.** Don't add it.

## Testing strategy

Two layers, both required:

- **Layer 1 — plain `assertEquals`**: covers `Normalizer`, `JsonRenderer`, `CsvRenderer`, `TextBlockFinder`, `TextBlockWriter`, `SourceLocator`, `PendingEdits`, `DiffFormatter`. These tests must **not** use the snapshot machinery — they are the foundation it relies on.
- **Layer 2 — self-snapshotting**: dogfoods the library against itself for renderer outputs and diff messages. Catches accidental canonicalization drift. Layer 2 tests can only land after the underlying primitive is green at Layer 1.

`TextBlockFinder` fixtures live under `src/test/resources/fixtures/`.

New behaviour needs a test. Bug fixes need a regression test that fails without the fix.

## Style

- Match the surrounding code. The project uses standard Java 17 conventions — no checkstyle config, but consistency matters.
- Public types and methods get Javadoc. Internal types get Javadoc when the *why* isn't obvious from the name; skip it when it is.
- Don't add comments that restate what the code does. Add them when a non-obvious constraint, invariant, or workaround needs to be preserved.
- Keep imports tidy and avoid wildcard imports.

## Commits and pull requests

- Write commits that explain *why*, not just *what*. The diff already shows the what.
- Prefer small, focused commits over one large catch-all commit. Rebase before opening the PR.
- PR description should state the user-visible change, the motivation, and any decisions worth flagging for review. Link the relevant issue.
- Don't merge your own PR. Wait for review.

## Reporting bugs

File an issue with:
- snapshotj version (or commit SHA if building from source)
- JDK version (`java -version`)
- A minimal reproducer — ideally a failing test
- Expected vs. observed behaviour, with the full failure message

## License

By contributing, you agree that your contributions are licensed under the MIT License, the same license as the project (`LICENSE`).
