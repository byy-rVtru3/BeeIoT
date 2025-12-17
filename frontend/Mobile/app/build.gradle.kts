import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.google.devtools.ksp)
}

configurations.all {
    resolutionStrategy {
        eachDependency {
            if (requested.group == "com.intellij" && requested.name == "annotations") {
                useTarget("org.jetbrains:annotations:23.0.0")
            }
        }
    }
}

android {
    namespace = "com.app.mobile"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.app.mobile"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    flavorDimensions += "version"
    productFlavors {
        create("develop") {
            dimension = "version"
            versionNameSuffix = "-dev"
            applicationIdSuffix = ".dev"
            buildConfigField("Boolean", "IS_DEVELOP", "true")
        }
        create("live") {
            dimension = "version"
            buildConfigField("Boolean", "IS_DEVELOP", "false")
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
        buildConfig = true // Включаем генерацию BuildConfig
    }
    packaging {
        resources {
            excludes.add("META-INF/versions/9/OSGI-INF/MANIFEST.MF")
        }
    }

    ksp {
        arg("room.schemaLocation", "$projectDir/schemas")
    }
}

tasks.withType<KotlinCompile>().configureEach {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_11)
    }
}

dependencies {

    //DataStore
    implementation(libs.androidx.datastore.preferences)

    //Room: Библиотека для работы с базой данных SQLite.
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    implementation(libs.androidx.room.compiler)
    ksp(libs.androidx.room.compiler)

    // Koin: Библиотека для внедрения зависимостей (Dependency Injection).
    implementation(platform(libs.koin.bom))
    implementation(libs.koin.core)
    implementation(libs.koin.android)
    implementation(libs.koin.androidx.compose)

    // Network: Библиотеки для работы с сетью.
    implementation(libs.retrofit.core)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.retrofit.converter.kotlinxserialization)
    implementation(libs.okhttp.logginginterceptor)

    // AndroidX Core & Lifecycle: Основные компоненты и утилиты Android Jetpack.
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)

    // Jetpack Compose: Современный UI-инструментарий для создания нативного интерфейса Android.
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.runtime.livedata)
    implementation(libs.androidx.material.icons.extended.android)

    // Инструменты для отладки и тестирования (Debug & Test).
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
    implementation(libs.androidx.ui.tooling.preview)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)

}