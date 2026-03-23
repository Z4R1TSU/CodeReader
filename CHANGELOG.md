<!-- Keep a Changelog guide -> https://keepachangelog.com -->

# CodeReader

## [Unreleased]

## [1.1.0]
### Added
- **Display Visibility Adjustment**: Added a slider to adjust the visibility/opacity of the reading text to blend it seamlessly into the status bar background for better stealth.
- **Auto Page Turning**: Added auto page turning feature with customizable interval (0.1s to 5.0s) and a shortcut (`alt + meta + K`) to easily toggle it on/off.
- **Full Chinese UI**: Translated all actions, menus, and dialogs to Chinese for a better localized user experience.

### Changed
- Improved the status bar text color logic to match the default IDE status bar "grayish" foreground color dynamically.
- Optimized auto page transition between chapters to ensure smooth reading without being interrupted by chapter jump notifications.

## [1.0.3]

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