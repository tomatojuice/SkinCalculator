import java.util.Properties

// local.propertiesを読み込む設定
val localProperties = Properties()
val localPropertiesFile = rootProject.file("local.properties")
if (localPropertiesFile.exists()) {
    localPropertiesFile.inputStream().use { localProperties.load(it) }
}

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.ratolab.skin.calculator"
    compileSdk = 36 // 2026年最新のSDK設定

    defaultConfig {
        applicationId = "com.ratolab.skin.calculator"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // ★ local.properties から ID を取得
        val appId = localProperties.getProperty("ADMOB_APP_ID") ?: ""
        val bannerId = localProperties.getProperty("ADMOB_BANNER_UNIT_ID") ?: ""

        // ① Kotlinコードから参照できるようにする (BuildConfig)
        buildConfigField("String", "ADMOB_APP_ID", "\"$appId\"")
        buildConfigField("String", "ADMOB_BANNER_UNIT_ID", "\"$bannerId\"")

        // ② AndroidManifest.xml から参照できるようにする (Placeholders)
        manifestPlaceholders["admobAppId"] = appId
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
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    buildFeatures {
        compose = true
        // ★ これを true にしないと BuildConfig が生成されません
        buildConfig = true
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation("androidx.activity:activity-ktx:1.12.4")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.10.0")
    implementation("androidx.datastore:datastore-preferences:1.2.0")
    implementation("androidx.navigation:navigation-compose:2.9.7")
    implementation("com.google.android.gms:play-services-ads:25.0.0")

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}