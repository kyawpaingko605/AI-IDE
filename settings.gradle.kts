pluginManagement {
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
    }
}

// 🚀 ကျွန်ုပ်တို့ ဖန်တီးခဲ့သော အဓိက App Module အား Build System ထဲသို့ ထည့်သွင်းခြင်း
rootProject.name = "AI-IDE"
include ':app'
