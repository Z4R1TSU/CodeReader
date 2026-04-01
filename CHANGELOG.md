<!-- Keep a Changelog guide -> https://keepachangelog.com -->

# CodeReader

## [Unreleased]

## [1.8.0]
### Added
- **Centralized Status Messages**: System status messages (such as "Loading...", "Jump successful", "Cache cleared") and EPUB chapter titles are now absolutely centered in the reading area, providing a clearer distinction between system feedback and actual book content.
- **Enhanced Stealth Mode**: Paging actions (both manual shortcuts and mouse clicks) are now strictly disabled when the reading panel is hidden. This prevents accidental progress changes while in stealth mode.
- **Auto-Page State Restoration**: The auto-page timer is now smartly paused when hiding the reader and seamlessly resumed when unhidden, saving system resources and improving user experience.
- **Smart EPUB Title Deduplication**: Introduced a robust regex-based deduplication logic to strip out duplicate chapter titles that are hardcoded at the beginning of the EPUB HTML content.
- **Long Chapter Title Support**: Extremely long chapter titles are no longer truncated with `...`. They are now smoothly paginated just like regular content.

## [1.7.0]
### Fixed
- **EPUB Page Count Accuracy**: Fixed an issue where the current page could exceed the total chapter page count by aligning the page calculation logic with the actual rendering logic.
- **Improved File Chooser**: Enhanced the file selection dialog to correctly gray out unsupported file formats, making it clearer which files can be imported.
- **Marketplace Compliance**: Updated the plugin description to comply with JetBrains Marketplace requirements (Latin character start and minimum length).

## [1.6.0]
### Added
- **Zone-based Click Navigation**: Introduced a revolutionary way to navigate chapters directly from the status bar.
  - Click the **left 10%** area to jump to the previous chapter.
  - Click the **right 10%** area to jump to the next chapter.
  - Click the **middle 80%** area to toggle visibility.
- **Improved Persistence Architecture**: Completely refactored the persistence logic. Global settings (word count, visibility, intervals) are now stored at the Application level, while project-specific states are kept in memory to prevent `Failed to save settings` errors and unnecessary project file modifications.

### Optimized
- **Startup Privacy**: The plugin now strictly defaults to hidden on IDE startup, ensuring maximum privacy.
- **Refined Event Bus**: Optimized the internal listener system to use service-scoped updates, further reducing UI thread overhead.

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