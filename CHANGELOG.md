<!-- Keep a Changelog guide -> https://keepachangelog.com -->

# CodeReader

## [Unreleased]

## [1.5.0]
### Added
- **Smart Paragraph Handling**: Enhanced EPUB reading experience with paragraph-aware spacing. Paragraphs are now visually separated by a 4-space gap for better readability in the status bar.
- **Edge Space Trimming**: Automatically trims leading and trailing spaces when word count splits happen at paragraph boundaries, ensuring no display space is wasted.

### Optimized
- **Ultra-Performance TXT Engine**: Refactored the TXT reader to use a file-pointer based lazy loading strategy. This allows for near-zero memory footprint and instantaneous loading of multi-gigabyte files.
- **Enhanced EPUB Content Cleaning**: Improved HTML entity decoding (e.g., &ldquo;, &mdash;) and whitespace normalization for a much cleaner reading interface.
- **Fine-grained UI Refresh**: Decoupled content updates from appearance updates in the status bar widget, significantly reducing CPU usage during auto-paging.

## [1.4.0]
### Fixed
- **Visual Jitter Fixes**: Completely eliminated status bar flickering and jumping by implementing fixed-width containers for both chapter info and main text.
- **Text Visibility**: Resolved rendering issues in high-version IDEs (like GoLand 2025.3) by adopting a more robust native color blending strategy.
- **Startup Logic**: Ensured the plugin always stays hidden upon IDE startup for maximum privacy.
- **Native File Chooser**: Optimized the file selection dialog to correctly grey out unsupported file formats on all JetBrains platforms.

## [1.3.0]
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