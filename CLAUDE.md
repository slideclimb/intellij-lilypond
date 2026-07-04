# Project notes

## Code style
Kotlin style is enforced by ktlint, configured through `.editorconfig` (the single source
of truth — the IDE reads it too, so "Reformat Code" agrees with the linter).

- Run `./gradlew ktlintFormat` before committing; `./gradlew check` runs `ktlintCheck`.
- `src/main/gen` is a generated build product and is excluded from linting.
