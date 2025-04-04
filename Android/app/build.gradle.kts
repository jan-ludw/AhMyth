plugins {
    id("com.android.application")
}

android {
    namespace = "ahmyth.mine.king.ahmyth"
    compileSdk = 34

    defaultConfig {
        applicationId = "ahmyth.mine.king.ahmyth"
        minSdk = 21
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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    
    packaging {
        resources {
            excludes.add("META-INF/INDEX.LIST")
            excludes.add("META-INF/io.netty.versions.properties")
        }
    }
}

dependencies {
    // AndroidX Core Libraries
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.11.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.core:core:1.12.0")
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.legacy:legacy-support-v4:1.0.0")
    implementation("androidx.lifecycle:lifecycle-runtime:2.7.0")
    implementation("androidx.lifecycle:lifecycle-common-java8:2.7.0")
    
    // Socket.IO v0.9.x client
    implementation("com.github.nkzawa:socket.io-client:0.6.0") {
        exclude(group = "org.json", module = "json")
    }
    implementation("com.github.nkzawa:engine.io-client:0.6.0") {
        exclude(group = "org.json", module = "json")
    }
    
    // JSON library
    implementation("org.json:json:20210307")
    
    // Android SDK
    implementation("com.android.support:support-v4:28.0.0")
    implementation("com.android.support:appcompat-v7:28.0.0")
}