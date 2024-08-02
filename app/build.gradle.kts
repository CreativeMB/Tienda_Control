plugins {
    alias(libs.plugins.android.application)

    id("com.google.gms.google-services") version "4.4.2" apply true

}

android {
    namespace = "com.example.tiendacontrol"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.tiendacontrol"
        minSdk = 27
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}

dependencies {

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)

    // Import the Firebase BoM
    implementation(platform("com.google.firebase:firebase-bom:33.1.0"))

    // Firebase Core and Analytics
    implementation("com.google.firebase:firebase-core:21.1.1")
    implementation("com.google.firebase:firebase-analytics")

    // Firebase Authentication and Firestore
    implementation("com.google.firebase:firebase-auth")
    implementation("com.google.android.gms:play-services-auth:21.0.0")
    implementation("com.google.firebase:firebase-firestore")
    implementation("com.google.firebase:firebase-database:20.3.1")
    implementation("com.google.firebase:firebase-database-ktx:20.2.2")
    implementation("com.google.android.material:material:1.10.0")
    implementation("androidx.navigation:navigation-ui-ktx:2.7.7")
    implementation("androidx.navigation:navigation-fragment-ktx:2.7.7")
    implementation("com.google.android.material:material:1.12.0")
    implementation ("com.google.firebase:firebase-storage:20.0.0")
    implementation ("com.squareup.picasso:picasso:2.8")
    implementation ("com.google.android.gms:play-services-drive:17.0.0")
    implementation ("com.google.android.gms:play-services-auth:21.2.0")
    implementation ("androidx.recyclerview:recyclerview:1.2.1")
    implementation ("androidx.recyclerview:recyclerview-selection:1.1.0")

    // Dropbox Core API v2 para Android
    implementation ("com.dropbox.core:dropbox-core-sdk:7.0.0")
    implementation("com.dropbox.core:dropbox-android-sdk:7.0.0")
    //exel
    implementation ("org.apache.poi:poi:5.2.4")
    implementation ("org.apache.poi:poi-ooxml:5.2.4")

    implementation ("androidx.appcompat:appcompat:1.4.0")

    implementation ("com.google.code.gson:gson:2.8.9")


    // Material Components for Android
    implementation ("com.google.android.material:material:1.8.0")

    // Material3 library (for Material You)
    implementation ("androidx.compose.material3:material3:1.1.0")

    implementation ("androidx.fragment:fragment:1.4.0")
}




