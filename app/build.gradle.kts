import java.util.Properties

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

    signingConfigs {
        create("release") {
            val properties = Properties()
            val localPropertiesFile = project.rootProject.file("local.properties")
            if (localPropertiesFile.exists()) {
                val inputStream = localPropertiesFile.inputStream()
                properties.load(inputStream)
                inputStream.close()
            }

            storeFile = file("${project.rootDir}/keystore.jks")
            storePassword = properties.getProperty("keystore.password")
            keyAlias = properties.getProperty("key.alias")
            keyPassword = properties.getProperty("key.password")
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            signingConfig = signingConfigs.getByName("release")
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
