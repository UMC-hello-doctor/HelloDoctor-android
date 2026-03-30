import java.io.FileInputStream
import java.util.Properties
import org.gradle.testing.jacoco.tasks.JacocoCoverageVerification
import org.gradle.testing.jacoco.tasks.JacocoReport

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id("androidx.navigation.safeargs.kotlin")
    id("com.google.devtools.ksp")
    id("com.google.dagger.hilt.android")
    kotlin("plugin.parcelize")

    id("org.jlleitschuh.gradle.ktlint") version "12.1.1"
    id("io.gitlab.arturbosch.detekt") version "1.23.6"
    jacoco
}

val propertiesFile = rootProject.file("gradle.properties")
val properties = Properties()
if (propertiesFile.exists()) {
    properties.load(FileInputStream(propertiesFile))
}

android {
    namespace = "com.umc.hellodoctor"
    compileSdk = 36

    buildFeatures {
        buildConfig = true
        viewBinding = true
    }

    defaultConfig {
        applicationId = "com.umc.hellodoctor"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        buildConfigField(
            "String",
            "AI_API_KEY",
            "\"${properties.getProperty("AI_API_KEY", "")}\""
        )
        buildConfigField(
            "String",
            "SERVER_BASE_URL",
            "\"${properties.getProperty("SERVER_BASE_URL", "https://api.hellodoctor.dev")}\""
        )
        buildConfigField(
            "String",
            "NAVER_MAP_CLIENT_ID",
            "\"${properties.getProperty("NAVER_MAP_CLIENT_ID", "")}\""
        )
        buildConfigField(
            "String",
            "NAVER_MAP_CLIENT_SECRET",
            "\"${properties.getProperty("NAVER_MAP_CLIENT_SECRET", "")}\""
        )
        buildConfigField(
            "String",
            "GOOGLE_OAUTH_CLIENT_ID",
            "\"${properties.getProperty("GOOGLE_OAUTH_CLIENT_ID", "")}\""
        )

        manifestPlaceholders["NAVER_MAP_CLIENT_ID"] =
            project.properties["NAVER_MAP_CLIENT_ID"] ?: ""
    }

    signingConfigs {
        create("release") {
            storeFile = file(project.properties["storeFile"] as String)
            storePassword = project.properties["storePassword"] as String
            keyAlias = project.properties["keyAlias"] as String
            keyPassword = project.properties["keyPassword"] as String
        }
    }

    buildTypes {
        getByName("debug") {
            signingConfig = signingConfigs.getByName("release")
            isMinifyEnabled = false
            enableUnitTestCoverage = true
        }

        getByName("release") {
            signingConfig = signingConfigs.getByName("release")
            isMinifyEnabled = false
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    testOptions {
        unitTests.all {
            it.extensions.configure(org.gradle.testing.jacoco.plugins.JacocoTaskExtension::class.java) {
                isIncludeNoLocationClasses = true
                excludes = listOf("jdk.internal.*")
            }
        }
    }
}

jacoco {
    toolVersion = "0.8.11"
}

val jacocoExcludedFiles = listOf(
    "**/R.class",
    "**/R$*.class",
    "**/BuildConfig.*",
    "**/Manifest*.*",
    "**/*Test*.*",
    "android/**/*.*",

    // Android / Kotlin generated
    "**/*\$ViewInjector*.*",
    "**/*\$ViewBinder*.*",
    "**/BR.*",
    "**/DataBinderMapperImpl.*",
    "**/*Binding*.*",
    "**/*MapperImpl*.*",
    "**/*Companion*.*",
    "**/*Module*.*",
    "**/*Dagger*.*",
    "**/*Hilt*.*",
    "**/*_Hilt*.*",
    "**/*MembersInjector*.*",
    "**/*_Factory*.*",
    "**/*_Provide*Factory*.*",
    "**/*Extensions*.*",
    "**/*\$Result.*",
    "**/*\$Result$*.*",
    "**/*Directions*.*",
    "**/*Args*.*",

    // Compose / lambda / synthetic
    "**/*ComposableSingletons*.*",
    "**/*\$Lambda$*.*",
    "**/*\$inlined$*.*"
)

val debugTree = fileTree("${layout.buildDirectory.get().asFile}/tmp/kotlin-classes/debug") {
    exclude(jacocoExcludedFiles)
}

val mainJavaTree = fileTree("${layout.buildDirectory.get().asFile}/intermediates/javac/debug/classes") {
    exclude(jacocoExcludedFiles)
}

val jacocoExecutionData = fileTree(layout.buildDirectory.get().asFile) {
    include(
        "jacoco/testDebugUnitTest.exec",
        "outputs/unit_test_code_coverage/debugUnitTest/testDebugUnitTest.exec"
    )
}

tasks.register<JacocoReport>("jacocoDebugReport") {
    group = "verification"
    description = "Generate JaCoCo coverage report for debug unit tests"

    dependsOn("testDebugUnitTest")

    reports {
        xml.required.set(true)
        html.required.set(true)
        csv.required.set(false)
    }

    classDirectories.setFrom(files(debugTree, mainJavaTree))
    sourceDirectories.setFrom(files("src/main/java", "src/main/kotlin"))
    executionData.setFrom(jacocoExecutionData)
}

tasks.register<JacocoCoverageVerification>("jacocoCoverageVerification") {
    group = "verification"
    description = "Verify JaCoCo coverage threshold for debug unit tests"

    dependsOn("testDebugUnitTest")

    classDirectories.setFrom(files(debugTree, mainJavaTree))
    sourceDirectories.setFrom(files("src/main/java", "src/main/kotlin"))
    executionData.setFrom(jacocoExecutionData)

    violationRules {
        rule {
            limit {
                minimum = "0.80".toBigDecimal()
            }
        }
    }
}

tasks.register("testCoverage") {
    group = "verification"
    description = "Run unit tests, generate coverage report, and verify coverage"

    dependsOn(
        "testDebugUnitTest",
        "jacocoDebugReport",
        "jacocoCoverageVerification"
    )
}

ktlint {
    android.set(false)
    ignoreFailures.set(false)
    reporters {
        reporter(org.jlleitschuh.gradle.ktlint.reporter.ReporterType.PLAIN)
        reporter(org.jlleitschuh.gradle.ktlint.reporter.ReporterType.CHECKSTYLE)
    }
}

detekt {
    toolVersion = "1.23.6"
    config.setFrom(files("$rootDir/config/detekt/detekt.yml"))
    buildUponDefaultConfig = true
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.fragment)
    implementation(libs.androidx.cardview)
    implementation(libs.play.services.location)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    implementation("androidx.navigation:navigation-fragment-ktx:2.8.4")
    implementation("androidx.navigation:navigation-ui-ktx:2.8.4")

    implementation("androidx.fragment:fragment-ktx:1.8.5")
    implementation("androidx.viewpager2:viewpager2:1.1.0")
    implementation("androidx.gridlayout:gridlayout:1.0.0")
    implementation("androidx.recyclerview:recyclerview:1.3.2")

    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.8.7")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.1")

    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")

    implementation("com.github.bumptech.glide:glide:4.16.0")
    ksp("com.github.bumptech.glide:ksp:4.16.0")

    implementation("androidx.credentials:credentials:1.3.0")
    implementation("androidx.credentials:credentials-play-services-auth:1.3.0")
    implementation("com.google.android.libraries.identity.googleid:googleid:1.1.1")
    implementation("com.google.android.gms:play-services-auth:21.4.0")

    implementation("com.naver.maps:map-sdk:3.23.0")

    implementation("com.google.dagger:hilt-android:2.57.2")
    ksp("com.google.dagger:hilt-compiler:2.57.2")

    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    ksp("androidx.room:room-compiler:2.6.1")
}