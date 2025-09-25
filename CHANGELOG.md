<!-- Keep a Changelog guide -> https://keepachangelog.com -->

# CodeReader

## [Unreleased]

### Added
- In-memory reading history storage (cleared when IDE exits)
- Empty history message when no reading history exists
- Lazy loading for EPUB chapter page counts to improve initial load performance

### Changed
- Refactored state management using `ReaderState` enum for better maintainability
- Updated reading history display format to show progress and chapter details
- Direct content display after history navigation (removed "jump successful" messages)
- Clear cache action now also clears reading history

### Fixed
- Compilation errors from unused variables and state handling
- "No file loaded" message appearing on IDE startup