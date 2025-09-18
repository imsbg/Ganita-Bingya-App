plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
}

android {
    namespace = "com.sandeep.ganitabigyan"
    compileSdk = 34

    signingConfigs {
        create("release") {
            storeFile = file("E:/SBG Ganita APP FINAL/ganitabg.jks")
            storePassword = "ganitabgodia"
            keyAlias = "key0"
            keyPassword = "ganitabgodia"
        }
    }

    defaultConfig {
        applicationId = "com.sandeep.ganitabigyan"
        minSdk = 26
        targetSdk = 34
        versionCode = 12
        versionName = "5.2.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("release")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        compose = true
    }
    composeOptions {
        // The kotlinCompilerExtensionVersion is managed by the plugin
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {

    // All of your original dependencies are here
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.3")
    implementation("androidx.activity:activity-compose:1.9.0")
    implementation(platform("androidx.compose:compose-bom:2024.06.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.navigation:navigation-compose:2.7.7")
    implementation("androidx.datastore:datastore-preferences:1.1.1")
    implementation("androidx.work:work-runtime-ktx:2.9.0")
    implementation("org.mariuszgromada.math:MathParser.org-mXparser:5.2.1")
    implementation("io.coil-kt:coil-compose:2.6.0")
    // --- NEW DEPENDENCIES FOR LANGUAGE SWITCHING AND SPLASH SCREEN ---
    implementation("androidx.appcompat:appcompat:1.6.1") // For AppCompatDelegate (language switching)
    implementation("androidx.core:core-splashscreen:1.0.1") // For the splash screen API
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.3") // Ensure you have the latest ViewModel Compose
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.3") // Ensure you have the latest runtime Compose


    // Test dependencies (unchanged)
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")
    androidTestImplementation(platform("androidx.compose:compose-bom:2024.06.00"))
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
}