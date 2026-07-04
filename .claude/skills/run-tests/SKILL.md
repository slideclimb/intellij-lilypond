---
name: run-tests
description: Run this project's tests via Gradle and read per-test failure output. Use whenever you need to run tests and see which ones failed and why — the IDE run-configuration tool returns an empty output snapshot, so this runs Gradle in the shell and parses the JUnit XML for full detail. Works for any test class; run a narrow filter first, then the whole suite.
allowed-tools: Bash(./gradlew test:*), Bash(python3 .claude/skills/run-tests/summarize.py:*)
---

# Run tests

Run the project's tests through Gradle and get an actionable per-test summary. The IDE
`execute_run_configuration` tool often returns an empty output snapshot, and Gradle's own
stdout truncates failure detail — so this skill runs Gradle in the shell and parses the
JUnit XML in `build/test-results/test/`, which is always written in full.

Works for **any** test class in the project. Prefer a narrow `--tests` filter while
iterating (faster, focused output); run the whole suite once it's green.

## How to run

Always run from the repo root.

Whole suite:

```bash
./gradlew test 2>&1 | tail -30
python3 .claude/skills/run-tests/summarize.py
```

A single class (or any name substring):

```bash
./gradlew test --tests "*SnippetsTest*" 2>&1 | tail -30
python3 .claude/skills/run-tests/summarize.py SnippetsTest
```

`summarize.py`'s first argument is a substring matched against the JUnit result file
names (`build/test-results/test/TEST-<classname>.xml`); pass the same class name you used
in `--tests`, or omit it to summarize the whole suite. A second argument caps how many
failing tests are listed (default 40).

## Reading results

- `BUILD SUCCESSFUL` + `summarize.py` reporting `[PASS] N tests | N passed | 0 failures`
  means everything passed. Sanity-check that `N` matches the count you expect — a `--tests`
  filter typo can match **zero** tests and still report `BUILD SUCCESSFUL`.
- On failure, Gradle exits non-zero and truncates per-test detail. `summarize.py` lists
  each failing test (`ClassName.testName`) with the first line of its failure message.
- If `summarize.py` prints `No result XML matched`, the test task did not run — usually a
  compile or code-generation error earlier in the Gradle output. Read the output above it.

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
