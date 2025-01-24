plugins {
    alias(libs.plugins.android.application)
    id("com.google.gms.google-services")

}

android {
    namespace = "com.creativem.tiendacontrol"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.creativem.tiendacontrol"
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
    implementation(libs.gridlayout)
    implementation(libs.firebase.auth)
    implementation(libs.firebase.database)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)

    // Import the Firebase BoM
    implementation(platform("com.google.firebase:firebase-bom:33.8.0"))

    // Firebase Core and Analytics
    implementation("com.google.firebase:firebase-core:21.1.1")
    implementation("com.google.firebase:firebase-analytics")


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
    implementation ("androidx.coordinatorlayout:coordinatorlayout:1.2.0")
    implementation ("androidx.core:core-ktx:1.12.0")

    implementation ("com.google.android.material:material:1.9.0")
    implementation ("androidx.drawerlayout:drawerlayout:1.2.0")
    implementation ("androidx.appcompat:appcompat:1.6.1")
    implementation ("androidx.core:core-ktx:1.10.0")

    implementation ("com.google.android.material:material:1.8.0")

    implementation ("com.google.android.gms:play-services-auth:19.2.0")
    implementation ("com.google.firebase:firebase-auth:21.1.0")
}




