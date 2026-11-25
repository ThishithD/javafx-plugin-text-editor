# Text Editor

A JavaFX text editor with configurable keyboard mappings, UTF-8/UTF-16/UTF-32 file support, localization, Java plugins, and Groovy scripts.

## Features

- Open and save text files
- Choose UTF-8, UTF-16, or UTF-32 encoding for file operations
- Insert and delete text from the keyboard using `keymap.txt`
- Load Java plugins through a fully-qualified class name
- Load Groovy scripts at runtime
- English and Sinhala interface translations
- Built-in Date and Find plugins

## Requirements

- Java 21 or a later compatible JDK
- Internet access on the first build if Gradle or dependencies are not already cached

The repository includes the Gradle Wrapper, so a separate Gradle installation is not required.

## Getting Started

Clone or download the project, open a terminal in the project root, and run:

### Windows

```powershell
.\gradlew.bat :app:run
```

### macOS and Linux

```bash
./gradlew :app:run
```

The application starts at a minimum window size of 800 x 600 pixels.

## Localization

The application selects the operating system locale by default.

To request a specific locale on Windows:

```powershell
.\gradlew.bat :app:run --args="--locale=en"
```

To request Sinhala:

```powershell
.\gradlew.bat :app:run --args="--locale=si"
```

Supported resource bundles are [bundle_en.properties](app/src/main/resources/bundle_en.properties) and [bundle_si.properties](app/src/main/resources/bundle_si.properties). The default bundle is [bundle.properties](app/src/main/resources/bundle.properties).

## Keyboard Mappings

The application loads `keymap.txt` from the working directory, or falls back to `app/keymap.txt` when it is launched from the project root.

Each binding uses this syntax:

```text
modifier+modifier+letter = insert "text" at start of line
modifier+letter = delete "text" at caret
```

Supported modifiers are `alt`, `ctrl`, and `shift`. The letter must be a lowercase letter in the grammar. Locations can be `start of line` or `caret`. Lines beginning with `#` are comments.

Example:

```text
ctrl+i = insert "    " at start of line
ctrl+shift+i = delete "    " at start of line
alt+t = insert "/*TODO*/" at caret
alt+s = delete "/*TODO*/" at caret
```

The default mappings are stored in [keymap.txt](app/keymap.txt).

## Plugins and Scripts

Open **Plugins / Scripts...** in the toolbar to add either a Java plugin or a Groovy script.

### Java Plugins

Enter the plugin's fully-qualified class name. The class must implement `EditorPlugin`, have a public no-argument constructor, and be available on the application's class path.

Built-in plugin class names:

| Plugin | Class name |
|---|---|
| Date | `edu.curtin.texteditor.DatePlugin` |
| Find | `edu.curtin.texteditor.FindPlugin` |

The Date plugin inserts the current localized date and time at the caret.

The Find plugin adds a **Find** button and an `F3` callback. It accepts a search term and selects the next matching text at or after the caret.

### Groovy Scripts

Groovy scripts receive an `api` variable that implements `EditorAPI`. For example, [emoji.groovy](emoji.groovy) replaces `:-)` with a smiling emoji after the text is modified:

```groovy
api.addTextModificationCallback({
    String text = api.getText()
    int caret = api.getCaretPosition()

    if(caret >= 3 && text.substring(caret - 3, caret) == ":-)")
    {
        String smiley = "\uD83D\uDE0A"
        String updated = text.substring(0, caret - 3) + smiley + text.substring(caret)
        api.setText(updated)
        api.setCaretPosition(caret - 3 + smiley.length())
    }
})
```

Use **Add Script...** in the Plugins and Scripts dialog to select and run a `.groovy` file.

## Editor API

Java plugins can use [EditorAPI](api/src/main/java/edu/curtin/texteditor/api/EditorAPI.java). It exposes editor text and caret operations, selection operations, input prompts, toolbar buttons, text-change callbacks, function-key callbacks, locale data, and localized strings.

Important methods include:

```java
String getText();
void setText(String text);

int getCaretPosition();
void setCaretPosition(int position);

int getSelectionStart();
int getSelectionEnd();
void selectText(int start, int end);

String promptForInput(String title, String promptText);
void addButton(String label, ButtonCallback callback);
void addTextModificationCallback(TextModificationCallback callback);
void addFunctionKeyCallback(String functionKey, FunctionKeyCallback callback);

Locale getLocale();
String getTranslation(String key);
```

Implement [EditorPlugin](api/src/main/java/edu/curtin/texteditor/api/EditorPlugin.java):

```java
public class ExamplePlugin implements EditorPlugin
{
    @Override
    public void start(EditorAPI api)
    {
        api.addButton("Hello", () ->
            api.setText(api.getText() + "Hello!")
        );
    }
}
```

## Project Structure

```text
texteditor/
├── api/             Shared EditorAPI and plugin interfaces
├── app/             JavaFX application, UI, localization, and ANTLR keymap grammar
├── dateplugin/      Built-in date insertion plugin
├── findplugin/      Built-in find plugin
├── buildSrc/        Shared Gradle Java/PMD convention
├── gradle/          Gradle Wrapper configuration
├── app/keymap.txt   Default keyboard mappings
└── emoji.groovy     Example runtime Groovy script
```

The root Gradle project contains four modules:

- `api`
- `app`
- `dateplugin`
- `findplugin`

## Build and Quality Checks

Build all modules and run the configured Gradle checks:

```powershell
.\gradlew.bat build
```

Run only the application module:

```powershell
.\gradlew.bat :app:run
```

Run all PMD checks:

```powershell
.\gradlew.bat pmdMain
```

Clean generated build output:

```powershell
.\gradlew.bat clean
```

The shared Gradle convention in [texteditor-java-convention.gradle.kts](buildSrc/src/main/kotlin/texteditor-java-convention.gradle.kts) configures Java 21, Maven Central dependencies, compiler warnings, and PMD 7.26.0 using [saed-pmd-rules.xml](saed-pmd-rules.xml).

## Development Notes

- JavaFX is configured for JavaFX 21.
- Keymap parsing uses ANTLR 4.13.2.
- Groovy is provided through Groovy 5.0.6.
- Keymap files are read as UTF-8.
- Open and save operations support UTF-8, UTF-16, and UTF-32.
- The Gradle toolchain resolves JDK 21 when it is not already available.
