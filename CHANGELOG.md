<!-- Keep a Changelog guide -> https://keepachangelog.com -->

# CodeReader

## [Unreleased]

## [1.3.0]
### Added
- **Word Count Slider**: Replaced the input dialog for word count adjustment with a slider, featuring a range of 1-100 and real-time preview functionality.

### Fixed
- **Performance Lag**: Implemented a debounced refresh mechanism for the word count slider, ensuring smooth dragging without UI stuttering.
- **Text Visibility in GoLand**: Resolved a critical issue in newer IDE versions (like GoLand 2025.3) where text could become invisible due to incorrect color or height calculation.
- **Chapter Info Jitter**: Eliminated the status bar jitter caused by changing digit widths in chapter progress by implementing a fixed-width info label.
- **Startup Visibility**: Fixed a bug where the plugin would incorrectly show on IDE startup if it was left open in the previous session.
- **File Chooser Compatibility**: Improved the file filter for importing books to work correctly on newer JetBrains platforms.

### Changed
- **Optimized Color Logic**: Improved the visibility adjustment algorithm to use native alpha blending for better compatibility with various IDE themes.
- **UI Refinement**: Updated slider labels to use a more natural 0-100 range with regular tick marks.

## [1.2.0]
### Fixed
- **Content Truncation**: Resolved the issue where the end of a page could be truncated with `...` when the combined length of chapter info and text exceeded the display area, ensuring a continuous reading experience without missing words.
- **Layout Jumping**: Fixed the frequent expansion and contraction of the status bar when turning pages by implementing a fixed-width display area for the reading text.

### Changed
- **Optimized Layout Architecture**: Refactored the status bar widget to use a multi-label layout. The chapter/progress info is now independent from the reading text, ensuring the main text position remains stable when toggling info visibility.
- **Improved Performance**: Introduced a layout calculation cache to avoid redundant UI measurements during page turns, making reading transitions even smoother.
- **Compact UI**: Fine-tuned the spacing and alignment in the status bar to achieve a more professional and compact appearance.

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