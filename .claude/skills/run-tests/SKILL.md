---
name: run-tests
description: Run this project's tests via Gradle and read per-test failure output. Use whenever you need to run tests and see which ones failed and why — the IDE run-configuration tool returns an empty output snapshot, so this runs Gradle in the shell and reads the JUnit XML (with the Read tool) for full detail. Works for any test class; run a narrow filter first, then the whole suite.
allowed-tools: Bash(./gradlew test:*), Read
---

# Run tests

Run the project's tests through Gradle, then read failure detail from the JUnit XML that
`build/test-results/test/` always writes in full. The IDE `execute_run_configuration` tool
often returns an empty output snapshot, and Gradle's own stdout truncates per-test failure
detail — so run Gradle in the shell for the headline result, and open the XML with the
**Read tool** for the full message.

Works for **any** test class in the project. Prefer a narrow `--tests` filter while
iterating (faster, focused output); run the whole suite once it's green.

## How to run

Always run from the repo root.

Whole suite:

```bash
./gradlew test 2>&1 | tail -30
```

A single class (or any name substring):

```bash
./gradlew test --tests "*SnippetsTest*" 2>&1 | tail -30
```

Gradle's own output gives the headline: `BUILD SUCCESSFUL` / `BUILD FAILED`, the count
(`N tests completed, M failed`), and one `... FAILED` line naming each failing test.
Sanity-check that the count matches what you expect — a `--tests` filter typo can match
**zero** tests and still print `BUILD SUCCESSFUL`.

If the test task never ran (no `N tests completed` line, no XML written), it's usually a
compile or code-generation error — read the Gradle output above the summary.

## Reading failure detail — use the Read tool

Gradle truncates the per-test failure message, but the full JUnit XML is always written to:

```
build/test-results/test/TEST-<fully-qualified-class>.xml
```

e.g. `build/test-results/test/TEST-nl.abbyberkers.lilypond.language.parser.SnippetsTest.xml`.

Each test is a `<testcase name="..." classname="...">`; a failing one contains a
`<failure message="...">` whose `message` attribute holds the full assertion text (for the
parser tests, the `PsiError...` line naming the offending file/token).

These XML files are **large** — one `<testcase>` per corpus file (RegressionTest is ~2090,
well over the Read tool's default line limit) — so do **not** read the whole file:

1. The gradle `... FAILED` lines already tell you *which* tests failed. The XML is only for
   the full message.
2. **Locate each failure, then Read just around it.** Use a search tool (e.g.
   `mcp__idea__search_regex`) for `PsiError` (or `<failure` for non-parser tests) to get the
   line number, then Read that line with `offset`/`limit`. The `<failure message="...">`
   attribute on that line is the full message.

To see the whole failing PSI tree (not just the first error line), temporarily have the test
write `DebugUtil.psiToString(psiFile, true)` to a scratch file on failure, run once, then
Read that file — but revert the instrumentation before finishing.

## Project-specific test notes

- The parser tests assert **zero `PsiError`** over real LilyPond `.ly` files, one
  parameterized test per file, so a failure names the exact file and its `PsiError` line:
  - **`SnippetsTest`** — 389 files. Fast; iterate here first when changing the grammar
    (`Lilypond.bnf`) or lexer (`LilypondLexer.flex`).
  - **`RegressionTest`** — ~2090 files. The full corpus; run once `SnippetsTest` is green.
- Editing `Lilypond.bnf` or `LilypondLexer.flex` triggers `generateParser` /
  `generateLexer` automatically as Gradle task dependencies — no manual regeneration step.
- Gradle uses the configuration cache; the first run recalculates the task graph, later
  runs are faster. `2>&1 | tail -30` keeps the transcript small while preserving
  `BUILD SUCCESSFUL/FAILED` and any stack-trace tail — drop the `tail` if an error is cut off.
