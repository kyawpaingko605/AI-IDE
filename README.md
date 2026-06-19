# AI-IDE
# 🚀 AI-IDE (Mobile Android Compiler Engine)

AI-IDE သည် Android ဖုန်းပေါ်တွင်တင် Kotlin/Java ကုဒ်များကို တိုက်ရိုက်ရေးသားပြီး နောက်ကွယ်တွင် Native Compilers များ အသုံးပြုကာ အမှန်တကယ် ထည့်သွင်းအသုံးပြုနိုင်သည့် APK Package အဖြစ် ပြောင်းလဲပေးနိုင်သော အစွမ်းထက် Mobile IDE အက်ပ်တစ်ခု ဖြစ်သည်။

---

## ✨ Core Features (အဓိက လုပ်ဆောင်ချက်များ)

* **Advanced Code Editor:** Real-time Syntax Highlighting (Keywords, Strings, Comments, Numbers) နှင့် Line Number Bar ပါဝင်သော Editor စနစ်။
* **File Explorer Tree:** ပရောဂျက်အတွင်းရှိ `src`၊ `res`၊ `bin` ဖိုင်တွဲများနှင့် ကုဒ်ဖိုင်များကို စနစ်တကျ စီမံခန့်ခွဲနိုင်ခြင်း။
* **Native Build Pipeline:** နောက်ကွယ်တွင် AAPT2 (Resource Compiler)၊ D8 (Dexer Engine) နှင့် ApkSigner တို့ကို အသုံးပြု၍ APK ထုတ်ပေးခြင်း။
* **Real-time Terminal View:** ကုဒ်များကို Compile ပတ်နေစဉ် ဖြစ်ပျက်နေသည့် အခြေအနေများနှင့် Error Logs များကို တိုက်ရိုက်ဖတ်ရှုနိုင်ခြင်း။
* **Modern Jetpack Compose UI:** အဆင့်မြင့်ပြီး သေသပ်လှပသော Dark Theme Design ဗိသုကာဖြင့် တည်ဆောက်ထားခြင်း။

---

## 🏗️ Project Architecture (ပရောဂျက် ဗိသုကာ ဖွဲ့စည်းပုံ)

```text
AI-IDE/
│
├── gradle/wrapper/          # Gradle Wrapper Configuration (v8.4)
├── app/
│   ├── src/main/
│   │   ├── java/com/ai/ide/
│   │   │   ├── ui/
│   │   │   │   ├── components/  # MainIdeScreen, AdvancedCodeEditor, FileExplorer, TerminalView
│   │   │   │   └── theme/       # Color, Theme, Typography, Shape
│   │   │   │   └── viewmodel/   # MainViewModel (Build State & Terminal Log Control)
│   │   │   └── utils/           # AssetManager, ProjectBuilder, ApkSigner (Core Engines)
│   │   └── AndroidManifest.xml  # Permissions & Activity Declarations
│   └── build.gradle             # App Module Dependencies & Compose Config
│
├── settings.gradle.kts          # Root Project Modules Inclusion
└── README.md                    # Project Documentation
