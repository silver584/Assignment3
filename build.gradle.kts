plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.example.assignmentthree"
    compileSdk {
        version = release(36)
    }

    defaultConfig {
        applicationId = "com.example.assignmentthree"
        minSdk = 24
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
}

dependencies {
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)


    // 高德地图核心依赖
    implementation("com.amap.api:map2d:6.0.0")
    implementation("com.amap.api:location:5.6.1")
    implementation("com.amap.api:search:8.1.0")
// 网络请求（Volley，与原代码一致）
    implementation("com.android.volley:volley:1.2.1")
// JSON 解析
    implementation("com.google.code.gson:gson:2.10.1")
// 图片加载（街景图）
    implementation("com.squareup.picasso:picasso:2.71828")
}