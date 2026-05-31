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

// 🚀 Kotlin DSL အတွက် မှန်ကန်သော သတ်မှတ်ချက် (Double Quotes ဖြင့် ကွင်းစကွင်းပိတ်သုံးရန်)
rootProject.name = "AI-IDE"
include(":app")
