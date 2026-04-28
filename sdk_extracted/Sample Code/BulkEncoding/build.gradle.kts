plugins {
    id("com.android.application")
}

android {
    namespace = "com.uk.tsl.rfid.samples.bulkencoding"
    compileSdk {
        version = release(36)
    }

    defaultConfig {
        applicationId = "com.uk.tsl.rfid.samples.bulkencoding"
        minSdk = 26
        targetSdk = 36
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
    buildFeatures {
        viewBinding = true
        buildConfig = true
    }
}

dependencies {
    implementation(project(":Rfid.AsciiProtocol-Library"))
    implementation(project(":DeviceList"))
    implementation ("com.github.weliem:blessed-android:2.5.0")

    implementation ("androidx.localbroadcastmanager:localbroadcastmanager:1.1.0")

    implementation("androidx.appcompat:appcompat:1.7.1")
    implementation("com.google.android.material:material:1.13.0")
    implementation("androidx.constraintlayout:constraintlayout:2.2.1")
    implementation("androidx.navigation:navigation-fragment:2.9.7")
    implementation("androidx.navigation:navigation-ui:2.9.7")
}