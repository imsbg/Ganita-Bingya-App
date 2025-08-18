plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
}

android {
    namespace = "com.sandeep.ganitabigyan"
    compileSdk = 34

    //====================================================================
    //== THIS BLOCK IS UPDATED FOR YOUR NEW KEY ==
    //====================================================================
    signingConfigs {
        create("release") {
            // This is the path to the NEW key you just created.
            storeFile = file("E:/SBG Ganita APP FINAL/ganitabg.jks")

            // <-- IMPORTANT: Replace this with your NEW password.
            storePassword = "ganitabgodia"

            // The alias for your new key.
            keyAlias = "key0"

            // <-- IMPORTANT: Replace this with your NEW password again.
            keyPassword = "ganitabgodia"
        }
    }
    //====================================================================

    defaultConfig {
        applicationId = "com.sandeep.ganitabigyan"
        minSdk = 26
        targetSdk = 34
        versionCode = 2
        versionName = "2.0.2"

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
            // This links the release build to your new signing configuration.
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
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.3")
    implementation("androidx.navigation:navigation-compose:2.7.7")
    implementation("androidx.datastore:datastore-preferences:1.1.1")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.3")
    implementation("androidx.work:work-runtime-ktx:2.9.0")

    // <-- THIS IS THE ONLY LINE YOU NEED TO ADD FOR THE CALCULATOR
    implementation("org.mariuszgromada.math:MathParser.org-mXparser:5.2.1")

    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")
    androidTestImplementation(platform("androidx.compose:compose-bom:2024.06.00"))
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")

}