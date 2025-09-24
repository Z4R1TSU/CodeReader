# %NAME%

![Build](https://github.com/%REPOSITORY%/workflows/Build/badge.svg)
[![Version](https://img.shields.io/jetbrains/plugin/v/MARKETPLACE_ID.svg)](https://plugins.jetbrains.com/plugin/MARKETPLACE_ID)
[![Downloads](https://img.shields.io/jetbrains/plugin/d/MARKETPLACE_ID.svg)](https://plugins.jetbrains.com/plugin/MARKETPLACE_ID)

## Template ToDo list
- [x] Create a new [IntelliJ Platform Plugin Template][template] project.
- [ ] Get familiar with the [template documentation][template].
- [ ] Adjust the [pluginGroup](./gradle.properties) and [pluginName](./gradle.properties), as well as the [id](./src/main/resources/META-INF/plugin.xml) and [sources package](./src/main/kotlin).
- [ ] Adjust the plugin description in `README` (see [Tips][docs:plugin-description])
- [ ] Review the [Legal Agreements](https://plugins.jetbrains.com/docs/marketplace/legal-agreements.html?from=IJPluginTemplate).
- [ ] [Publish a plugin manually](https://plugins.jetbrains.com/docs/intellij/publishing-plugin.html?from=IJPluginTemplate) for the first time.
- [ ] Set the `MARKETPLACE_ID` in the above README badges. You can obtain it once the plugin is published to JetBrains Marketplace.
- [ ] Set the [Plugin Signing](https://plugins.jetbrains.com/docs/intellij/plugin-signing.html?from=IJPluginTemplate) related [secrets](https://github.com/JetBrains/intellij-platform-plugin-template#environment-variables).
- [ ] Set the [Deployment Token](https://plugins.jetbrains.com/docs/marketplace/plugin-upload.html?from=IJPluginTemplate).
- [ ] Click the <kbd>Watch</kbd> button on the top of the [IntelliJ Platform Plugin Template][template] to be notified about releases containing new features and fixes.
- [ ] Configure the [CODECOV_TOKEN](https://docs.codecov.com/docs/quick-start) secret for automated test coverage reports on PRs

<!-- Plugin description -->
CodeReader 是一款纯粹的、用于程序员上班摸鱼的、支持本地文件上传的、JetBrains IDE 小说阅读插件。

欢迎使用 CodeReader 插件！本插件可让您直接在 IDE 的状态栏中阅读 `.txt` 和 `.epub` 格式的书籍，提供一种在编码和阅读之间无缝切换的方式，而无需离开您的开发环境。

## 功能

- **在状态栏中阅读**: 在状态栏中逐页显示您的书籍内容。
- **支持 `.txt` 和 `.epub`**: 从纯文本和 EPUB 文件加载书籍。
- **高效的 `.txt` 文件处理**: 采用优化的内存管理策略，即使是大型 `.txt` 文件也能实现快速加载和流畅的阅读体验。
- **章节导航**: 对于 `.epub` 文件，您可以轻松地在章节之间导航。
- **翻页**: 使用简单的键盘快捷键在页面之间移动。
- **可自定义显示**: 调整一次显示的字数，并切换阅读器和章节信息的可见性。

## 如何使用

### 加载书籍

- **从菜单**: 前往 `工具 -> CodeReader` 并选择 `从本地 txt 文件导入` 或 `从本地 epub 文件导入`。
- **键盘快捷键**: 使用 `control + alt + O` 打开文件选择对话框。

### 阅读

- **下一页**: `alt + meta + RIGHT`
- **上一页**: `alt + meta + LEFT`

### 显示选项

- **切换阅读器可见性**: `alt + meta + H` 或 直接点击状态栏中的插件主体 以显示或隐藏状态栏中的 CodeReader。
- **切换章节信息**: `alt + meta + J` 以显示或隐藏当前章节标题、章节进度和书籍进度（仅适用于 `.epub` 文件）。
- **修改字数**: 前往 `工具 -> CodeReader -> 修改字数` 以更改一次显示的字数。

### 章节选择

- **选择章节**: 对于 `.epub` 文件，您可以通过前往 `工具 -> CodeReader -> 选择章节` 来跳转到特定章节。

### 调整快捷键

- **调整快捷键**: 前往 `设置 -> Keymap -> 搜索 "CodeReader"` 以调整插件的快捷键。

## Q&A

- **Q: 插件支持哪些文件格式？**
  - **A**: 插件支持 `.txt` 和 `.epub` 文件格式。
- **Q：还会支持其他文件格式吗？**
  - **A**: 常用的小说文件格式还有 `AZW3`、`MOBI` 等。前者目前只有一个商用的库可以解析，怕有一些问题；后者完全是下位替代，不太好用不想做。
- **Q: 插件是否支持从网络上下载文件？**
  - **A**: 不支持。作者从来都是本地下载小说再阅读的（因为能找到高质量的书），网站上太过良莠不齐不喜欢用。

感谢您使用 CodeReader！我们希望它能帮助您保持高效并享受阅读。
<!-- Plugin description end -->

## Installation

- Using the IDE built-in plugin system:
  
  <kbd>Settings/Preferences</kbd> > <kbd>Plugins</kbd> > <kbd>Marketplace</kbd> > <kbd>Search for "%NAME%"</kbd> >
  <kbd>Install</kbd>
  
- Using JetBrains Marketplace:

  Go to [JetBrains Marketplace](https://plugins.jetbrains.com/plugin/MARKETPLACE_ID) and install it by clicking the <kbd>Install to ...</kbd> button in case your IDE is running.

  You can also download the [latest release](https://plugins.jetbrains.com/plugin/MARKETPLACE_ID/versions) from JetBrains Marketplace and install it manually using
  <kbd>Settings/Preferences</kbd> > <kbd>Plugins</kbd> > <kbd>⚙️</kbd> > <kbd>Install plugin from disk...</kbd>

- Manually:

  Download the [latest release](https://github.com/%REPOSITORY%/releases/latest) and install it manually using
  <kbd>Settings/Preferences</kbd> > <kbd>Plugins</kbd> > <kbd>⚙️</kbd> > <kbd>Install plugin from disk...</kbd>


---
Plugin based on the [IntelliJ Platform Plugin Template][template].

[template]: https://github.com/JetBrains/intellij-platform-plugin-template
[docs:plugin-description]: https://plugins.jetbrains.com/docs/intellij/plugin-user-experience.html#plugin-description-and-presentation
