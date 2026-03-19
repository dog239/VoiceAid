import java.util.Properties
import org.gradle.api.tasks.compile.JavaCompile

plugins {
    id("com.android.application")
}

val localProperties = Properties()
val localPropertiesFile = rootProject.file("local.properties")
if (localPropertiesFile.exists()) {
    localPropertiesFile.inputStream().use { stream ->
        localProperties.load(stream)
    }
}
//val deepSeekApiKey = localProperties.getProperty("DEEPSEEK_API_KEY") ?: ""
val deepSeekApiKey = "sk-5301a5886d6d43029f3df4bc7a34f1e7"
val deepSeekApiKeyEscaped = deepSeekApiKey
    .replace("\\", "\\\\")
    .replace("\"", "\\\"")

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
        buildConfigField("String", "DEEPSEEK_API_KEY", "\"${deepSeekApiKeyEscaped}\"")
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
    buildFeatures {
        buildConfig = true
    }
    packagingOptions {
        resources {
            excludes += setOf(
                "META-INF/DEPENDENCIES",
                "META-INF/LICENSE",
                "META-INF/LICENSE.txt",
                "META-INF/license.txt",
                "META-INF/NOTICE",
                "META-INF/NOTICE.txt",
                "META-INF/notice.txt",
                "META-INF/ASL2.0",
                "com/itextpdf/text/pdf/fonts/cmap_info.txt"
            )
        }
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
    implementation("com.squareup.okhttp3:okhttp:4.9.3")
    implementation("uk.co.chrisjenx:calligraphy:2.2.0")
    implementation("androidx.multidex:multidex:2.0.1")
    implementation("com.itextpdf:itextpdf:5.5.13.2")
    implementation("com.itextpdf:itext-asian:5.2.0")
}

tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
}
