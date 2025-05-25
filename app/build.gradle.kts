plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.example.cloova"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.cloova"
        minSdk = 24
        targetSdk = 35
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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.play.services.maps)

    // Удалите эту строку, если не используете Google Maps
    // implementation(libs.play.services.maps)

    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)

    // Основные зависимости
    implementation("org.mindrot:jbcrypt:0.4")
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.9.3")
    implementation("androidx.recyclerview:recyclerview:1.3.2")
    implementation("androidx.cardview:cardview:1.0.0")
    implementation("com.google.android.material:material:1.12.0")
    // Yandex Maps (обновленные версии)
    implementation("com.yandex.android:maps.mobile:4.3.1-lite")
    /*implementation("com.yandex.android:search.mobile:4.3.1-lite")*/

    // Добавьте эту зависимость для работы с местоположением
    implementation("com.google.android.gms:play-services-location:21.0.1")
}