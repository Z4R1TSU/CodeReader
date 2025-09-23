# CodeReader Plugin Instructions

## How to Use the Plugin

### Loading a Book

You can load a book in two ways:

1.  **From the Menu**: Navigate to `Tools -> CodeReader` and select either `Import from Local Txt File` or `Import from Local Epub File`.
2.  **Keyboard Shortcut**: Use `control + alt + O` to open the file chooser dialog.

### Reading

-   **Next Page**: `alt + meta + RIGHT`
-   **Previous Page**: `alt + meta + LEFT`

### Display Options

-   **Toggle Reader Visibility**: `alt + meta + H` to show or hide the CodeReader in the status bar.
-   **Toggle Chapter Info**: `alt + meta + J` to show or hide the current chapter title, chapter progress, and book progress (for `.epub` files only).
-   **Modify Word Count**: Go to `Tools -> CodeReader -> Modify Word Count` to change the number of words displayed at a time.

### Chapter Selection

-   **Select Chapter**: For `.epub` files, you can jump to a specific chapter by going to `Tools -> CodeReader -> Select Chapter`.

## How to Upload to JetBrains Marketplace

To upload your plugin to the JetBrains Marketplace, you will need to do the following:

1.  **Prepare for Deployment**:
    *   Ensure your plugin is properly configured in the `build.gradle.kts` and `plugin.xml` files. This includes setting the plugin version, description, and other metadata.
    *   Build the plugin by running the `./gradlew buildPlugin` command in your terminal. This will create a `.zip` file in the `build/distributions` directory.

2.  **Register on JetBrains Marketplace**:
    *   If you don't have one already, create an account on the [JetBrains Marketplace](https://plugins.jetbrains.com/).
    *   Go to your profile and select "Add new plugin".

3.  **Upload the Plugin**:
    *   Fill out the plugin submission form with all the required information, such as the plugin name, description, and logo.
    *   Upload the `.zip` file you created in the first step.
    *   Submit the plugin for review. The JetBrains team will review your plugin to ensure it meets their quality standards.

4.  **Release the Plugin**:
    *   Once your plugin is approved, you can publish it to the marketplace.
    *   You can manage your plugin and view its statistics from your JetBrains Marketplace account.

For more detailed instructions, please refer to the [official JetBrains documentation](https://plugins.jetbrains.com/docs/marketplace/publishing-plugin.html).