plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.kotlinxSerialization)
}

kotlin {

    js(IR) {
        nodejs()
        browser()
        binaries.executable()
    }

    // wait for https://github.com/jillesvangurp/kt-search/issues/69
    //    @OptIn(ExperimentalWasmDsl::class)
    //    wasmJs {
    //        binaries.executable()
    //        nodejs()
    //        browser()
    //        binaries.executable()
    //    }

    jvm {
        withJava()
    }

    iosX64()
    iosArm64()
    iosSimulatorArm64()

    sourceSets {

        commonMain.dependencies {
//            api(projects.krosaiCore)
            api(libs.ktsearch)
            implementation(Ktor.client.core)
        }


        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.kotlinx.coroutines.test)

        }

    }
}
