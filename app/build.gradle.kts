plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.example.myapplicationgps"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.myapplicationgps"
        minSdk = 25
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
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
    // Dependência para GPS (adicione esta linha no arquivo libs.versions.toml)
    implementation(libs.play.services.location)

    // Dependência para tratamento de ciclo de vida (adicione esta linha no libs.versions.toml)
    implementation(libs.lifecycle.process)


}

