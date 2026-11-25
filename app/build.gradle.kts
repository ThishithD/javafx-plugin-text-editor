plugins {
    id("texteditor-java-convention")
    application
    antlr
    id("org.openjfx.javafxplugin") version "0.1.0"
}

javafx {
    version = "21"
    modules("javafx.controls", "javafx.graphics")
}

application {
    mainClass = "edu.curtin.texteditor.TextEditorApp"
}

dependencies {
    implementation(project(":api"))
    implementation("org.apache.groovy:groovy:5.0.6")
    antlr("org.antlr:antlr4:4.13.2")


    runtimeOnly(project(":dateplugin"))
    runtimeOnly(project(":findplugin"))
}

tasks.generateGrammarSource {
    packageName = "edu.curtin.texteditor.dsl"
}

tasks.named("pmdMain") {
    dependsOn("generateGrammarSource")
}