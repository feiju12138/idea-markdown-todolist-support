plugins {
    id("java")
    id("org.jetbrains.intellij.platform")
}

group = "cn.fj.loli"
version = "1.0.0"

dependencies {
    intellijPlatform {
        val localIdePath = providers.gradleProperty("localIdePath").orNull
        if (localIdePath != null) {
            local(localIdePath)
        }
        else {
            intellijIdea("2026.1")
        }
        bundledPlugin("org.intellij.plugins.markdown")
    }

    testImplementation("junit:junit:4.13.2")
}

intellijPlatform {
    pluginConfiguration {
        id = "cn.fj.loli.markdowntodolist"
        name = "Markdown Todo List Support"
        version = project.version.toString()
        description = """
            <p>Makes task-list checkboxes in IntelliJ IDEA's built-in Markdown JCEF preview interactive.</p>
            <p>Click a checkbox in the preview to update the source Markdown between <code>- [ ]</code> and <code>- [x]</code>. Changes use the IDE document and command systems, so preview refresh, undo/redo, and the normal save workflow continue to work.</p>
        """.trimIndent()
        changeNotes = """
            <ul>
                <li>1.0.0: Add clickable Markdown task-list checkboxes in the built-in JCEF preview, with source-position mapping and undo/redo support.</li>
            </ul>
        """.trimIndent()
        vendor {
            name = "feiju12138"
            url = "https://github.com/feiju12138/idea-markdown-todolist-support"
        }
        ideaVersion {
            sinceBuild = "223"
        }
    }
}

tasks {
    withType<JavaCompile> {
        sourceCompatibility = "17"
        targetCompatibility = "17"
        options.release = 17
        options.encoding = "UTF-8"
    }

    test {
        useJUnit()
    }

    named("buildSearchableOptions") {
        enabled = false
    }

    named("prepareJarSearchableOptions") {
        enabled = false
    }

    named("jarSearchableOptions") {
        enabled = false
    }

}
