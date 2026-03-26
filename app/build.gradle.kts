import java.io.FileInputStream
import java.util.Properties
import org.gradle.testing.jacoco.tasks.JacocoCoverageVerification

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    //def version in project gradle
    id("androidx.navigation.safeargs.kotlin")
    id("com.google.devtools.ksp")
    id("com.google.dagger.hilt.android")
    kotlin("plugin.parcelize")

    // 🔧 CI/CD 플러그인 추가
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
        //AI API KEY
        buildConfigField(
            "String",
            "AI_API_KEY",
            "\"${properties.getProperty("AI_API_KEY", "")}\""
        )
        //프로젝트 서버
        buildConfigField(
            "String",
            "SERVER_BASE_URL",
            "\"${properties.getProperty("SERVER_BASE_URL", "https://api.hellodoctor.dev")}\""
        )
        //navermap
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
        //OAUTH
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
        //디버그시(run app시)에도 release key
        getByName("debug") {
            signingConfig = signingConfigs.getByName("release")
            isMinifyEnabled = false
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
}

// 🔧 Jacoco 커버리지 설정 (80% 기준)
jacoco {
    toolVersion = "0.8.11"
}

tasks.withType<JacocoCoverageVerification> {
    it.violationRules {
        rule {
            limit {
                minimum = "0.80".toBigDecimal()
            }
        }
    }
}
tasks.named<JacocoCoverageVerification>("jacocoTestCoverageVerification") {
    classDirectories.setFrom(files("$buildDir/tmp/kotlin-classes/debug"))
    sourceDirectories.setFrom(files("src/main/kotlin"))
    executionData.setFrom(files("$buildDir/jacoco/testDebugUnitTest.exec"))
}

// 🔧 ktlint 설정
ktlint {
    android.set(false)
    ignoreFailures.set(false)
    reporters {
        reporter(org.jlleitschuh.gradle.ktlint.reporter.ReporterType.PLAIN)
        reporter(org.jlleitschuh.gradle.ktlint.reporter.ReporterType.CHECKSTYLE)
    }
}

// 🔧 detekt 설정 (deprecation 수정)
detekt {
    toolVersion = "1.23.6"
    // config deprecated → from() 사용
    config.setFrom(files("$rootDir/config/detekt/detekt.yml"))
    buildUponDefaultConfig = true
}

dependencies {
    //basic lib
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

    // --- Navigation ---
    implementation("androidx.navigation:navigation-fragment-ktx:2.8.4")
    implementation("androidx.navigation:navigation-ui-ktx:2.8.4")

    // --- UI / 레이아웃 ---
    implementation("androidx.fragment:fragment-ktx:1.8.5")
    implementation("androidx.viewpager2:viewpager2:1.1.0")
    implementation("androidx.gridlayout:gridlayout:1.0.0")
    implementation("androidx.recyclerview:recyclerview:1.3.2")

    // --- 아키텍처 / 코루틴 ---
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.8.7")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.1")

    // --- 네트워크 (Retrofit) ---
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")

    // --- 이미지 로딩 (Glide + KSP) ---
    implementation("com.github.bumptech.glide:glide:4.16.0")
    ksp("com.github.bumptech.glide:ksp:4.16.0")

    // --- 인증 (Credential Manager / Google 로그인) ---
    implementation("androidx.credentials:credentials:1.3.0")
    implementation("androidx.credentials:credentials-play-services-auth:1.3.0")
    implementation("com.google.android.libraries.identity.googleid:googleid:1.1.1")
    implementation("com.google.android.gms:play-services-auth:21.4.0")

    // --- 지도 (Naver Maps) ---
    implementation("com.naver.maps:map-sdk:3.23.0")

    // --- Hilt (DI) ---
    implementation("com.google.dagger:hilt-android:2.57.2")
    ksp("com.google.dagger:hilt-compiler:2.57.2")

    // --- Room DB ---
    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    ksp("androidx.room:room-compiler:2.6.1")
}