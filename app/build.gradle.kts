plugins {
    id("com.android.application")
}

android {
    namespace = "com.example.intentbrowserrouter"
    compileSdk = 37

    defaultConfig {
        applicationId = "com.example.intentbrowserrouter"
        minSdk = 34
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
        }
    }
    
    lint {
        checkReleaseBuilds = false
        abortOnError = false
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        viewBinding = true
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.19.0")
    implementation("androidx.browser:browser:1.10.0")
    implementation("androidx.appcompat:appcompat:1.7.1")
    implementation("com.google.android.material:material:1.14.0")
    implementation("androidx.constraintlayout:constraintlayout:2.2.1")
    implementation("com.google.code.gson:gson:2.14.0")
    implementation("androidx.recyclerview:recyclerview:1.4.0")
}
