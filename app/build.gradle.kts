plugins {
    id("com.android.application")
}

android {
    namespace = "com.example.CCLEvaluation"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.CCLEvaluation"
        minSdk = 20
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
        multiDexEnabled = true
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
    packagingOptions {
        exclude ("META-INF/DEPENDENCIES")
        exclude ("META-INF/LICENSE")
        exclude ("META-INF/LICENSE.txt")
        exclude ("META-INF/license.txt")
        exclude ("META-INF/NOTICE")
        exclude ("META-INF/NOTICE.txt")
        exclude ("META-INF/notice.txt")
        exclude ("META-INF/ASL2.0")
        exclude ("com/itextpdf/text/pdf/fonts/cmap_info.txt")
    }


}

dependencies {
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.9.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    implementation("androidx.recyclerview:recyclerview:1.3.0")
    implementation("com.squareup.okhttp3:okhttp:4.8.1")
    implementation("uk.co.chrisjenx:calligraphy:2.2.0")
//    implementation("com.itextpdf:itext7-core:7.1.15")
    implementation("com.android.support:multidex:1.0.3")
    implementation ("com.itextpdf:itextpdf:5.5.13.2")
    implementation ("com.itextpdf:itext-asian:5.2.0")
}