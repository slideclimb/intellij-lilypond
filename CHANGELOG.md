<!-- Keep a Changelog guide -> https://keepachangelog.com -->

# intellij-lilypond Changelog

## [Unreleased]
### Added
- Initial scaffold created from [IntelliJ Platform Plugin Template](https://github.com/JetBrains/intellij-platform-plugin-template)
- LilyPond run configuration: compiles a `.ly` file and opens the resulting PDF. Right-click a score to
  create one. The `lilypond` executable, the output directory (`<project>/out` by default) and the PDF
  viewer are configurable, and the run configuration template holds the defaults.
