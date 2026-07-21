# Markdown Todo List Support

An IntelliJ IDEA plugin that makes task-list checkboxes clickable in the built-in Markdown JCEF preview.

Clicking a preview checkbox changes the corresponding source marker:

- `- [ ]` becomes `- [x]`
- `- [x]` or `- [X]` becomes `- [ ]`

The change goes through IntelliJ IDEA's document and command systems, so the source editor and preview update normally and Undo/Redo remains available.

## Compatibility

- IntelliJ IDEA 2022.3 or later (build 223+)
- The bundled Markdown plugin must be enabled
- The interactive behavior applies to the built-in JCEF Markdown preview

The preview API used by the plugin was checked against IDEA 2022.3, 2023.3, 2024.1, and 2026.1.3. The release uses Java 17 bytecode and has no `until-build`.

## Build

Use JDK 21 (the output still targets Java 17) and Gradle 9.x:

```powershell
$env:JAVA_HOME = 'G:\ProjectsDemo\tools\jdk-21.0.2'
G:\ProjectsDemo\tools\gradle-9.6.0\bin\gradle.bat clean test buildPlugin
```

The installable ZIP is produced in `build/distributions/`.
