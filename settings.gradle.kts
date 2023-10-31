pluginManagement {
    includeBuild("build-logic")
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven {
            url = uri("https://api.mapbox.com/downloads/v2/releases/maven")
            authentication {
                create<BasicAuthentication>("basic")
            }
            credentials {
                username = "mapbox"
                // Use the secret token you stored in gradle.properties as the password
                password =
                    "sk.eyJ1Ijoic2hlcmEwMSIsImEiOiJjbGV5YmQ5d3UwM2ZhM3FwdWxmd3Z3Z3pyIn0.Pucvab3nYo1jtfQTtjiZ_w"
            }
        }
    }
}

rootProject.name = "TaxiApp"
include(":app")
include(":core:common")
include(":core:data")
include(":core:database")
include(":core:domain")
include(":core:model")
include(":core:ui")
include(":feature:home")
