plugins {
    id("com.android.application")
    id("com.google.gms.google-services")
}

android {
    namespace = "com.cookandroid.tab2"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.cookandroid.tab2"
        minSdk = 30
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}

dependencies {
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.viewpager2:viewpager2:1.1.0")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    implementation("com.google.android.gms:play-services-maps:18.2.0")
    implementation("com.google.android.gms:play-services-location:21.3.0")
    implementation("com.akexorcist:bluetoothspp:1.0.0")
    implementation("io.github.ParkSangGwon:tedpermission-normal:3.3.0")
    implementation("com.github.PhilJay:MPAndroidChart:v3.1.0")

    implementation("com.github.bumptech.glide:glide:4.12.0")
    annotationProcessor("com.github.bumptech.glide:compiler:4.12.0")

    implementation("com.google.android.gms:play-services-fitness:21.2.0")
    implementation("com.google.android.gms:play-services-auth:21.2.0")
    implementation("com.google.gms:google-services:4.4.2")
    implementation(platform("com.google.firebase:firebase-bom:33.0.0"))

    implementation("com.google.android.material:material:1.12.0")

    /*implementation("com.dinuscxj:circleprogressbar:1.3.0")*/


}