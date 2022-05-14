/*
 * Copyright 2021 Thibault Seisel
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

plugins {
    id("odeon-convention")
    id("com.android.library")
    kotlin("android")
    kotlin("kapt")
}

dependencies {
    implementation(project(":core"))
    implementation(project(":core-database"))
    implementation(project(":media"))

    // Dispatcher to Android main thread
    api(libs.kotlinx.coroutines.android)

    api(libs.androidx.media)
    api(libs.androidx.appcompat)
    api(libs.androidx.fragment)
    api(libs.androidx.constraintlayout)
    api(libs.androidx.palette)

    // Android Arch Components
    api(libs.bundles.androidx.lifecycle)

    // Navigation Components
    api(libs.androidx.navigation.fragment)
    api(libs.androidx.navigation.ui)

    // Image loading
    api(libs.glide)
    kapt(libs.glide.compiler)

    // Material Components
    api(libs.material)

    implementation(libs.identikon)

    // Hilt
    kapt(libs.hilt.compiler)

    testImplementation(project(":core-test"))
}
