buildscript {
    dependencies {
        classpath("com.google.gms:google-services:4.4.0")
    }
}
// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    id("com.android.application") version "8.2.2" apply false
    id("org.jetbrains.kotlin.android") version "1.9.0" apply false
    // TODO: figure out how to set up gradle task to add addKtlintFormatGitPreCommitHook and other configs automatically
    id("org.jlleitschuh.gradle.ktlint") version "12.1.0"
    id("com.google.gms.google-services") version "4.4.0" apply false
}
