# Project notes

## Tooling
Prefer the JetBrains MCP (`mcp__idea__*`) for all reading, writing, searching and investigating.
Shell equivalents (`cat`, `grep`, `find`, `ls`, `sed`) trigger a permission prompt for work the MCP
does without one, so reaching for them wastes the user's attention.

- Read/search: `read_file`, `search_file`, `search_text`, `search_regex`, `search_symbol`,
  `list_directory_tree`. Write: `create_new_file`, `apply_patch`, `rename_refactoring`.
- Diagnostics without a full build: `get_file_problems`, `lint_files`, `reformat_file`.
- The IntelliJ Platform **sources are attached**, javadoc included — use them instead of guessing at
  platform API. `search_symbol` with `include_external=true` returns paths into
  `ideaIU-<version>-sources.jar`, `read_file` reads those jar paths directly (it also decompiles
  `.class` files), and `get_symbol_info` gives Quick Documentation at a file/line/column.
- Not covered by the MCP: the IntelliJ Platform SDK *guide* (`plugins.jetbrains.com/docs/intellij/`),
  which needs WebFetch, and Gradle runs, which need the shell.

## Code style
Kotlin style is enforced by ktlint, configured through `.editorconfig` (the single source
of truth — the IDE reads it too, so "Reformat Code" agrees with the linter).

- Run `./gradlew ktlintFormat` before committing; `./gradlew check` runs `ktlintCheck`.
- `src/main/gen` is a generated build product and is excluded from linting.
