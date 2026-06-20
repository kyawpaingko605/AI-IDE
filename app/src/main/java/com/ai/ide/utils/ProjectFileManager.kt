    // 📁 အသုံးပြုသူ စိတ်ကြိုက်ပေးသော နာမည်များဖြင့် Project အသစ်ဆောက်မည့် လုပ်ဆောင်ချက်
    fun createNewProject(parentDir: File, projectName: String, packageName: String): Boolean {
        return try {
            // ၁။ အသုံးပြုသူ ပေးလိုက်သော နာမည်ဖြင့် ပရောဂျက် Folder ဆောက်မည်
            val projectDir = File(parentDir, projectName)
            if (!projectDir.exists()) projectDir.mkdirs()

            // ၂။ Folder ဖွဲ့စည်းပုံများကို တိတိကျကျ ဆောက်မည်
            val appDir = File(projectDir, "app")
            val srcDir = File(appDir, "src/main/java/${packageName.replace('.', '/')}")
            val resLayoutDir = File(appDir, "src/main/res/layout")
            val resValuesDir = File(appDir, "src/main/res/values")

            srcDir.mkdirs()
            resLayoutDir.mkdirs()
            resValuesDir.mkdirs()

            // ၃။ ရိုးရိုး ကူးရုံုံရမည့် ဖိုင်များကို ကူးမည်
            copyAssetFile("template/build.gradle", File(projectDir, "build.gradle"))
            copyAssetFile("template/settings.gradle", File(projectDir, "settings.gradle"))
            copyAssetFile("template/app/build.gradle", File(appDir, "build.gradle"))
            copyAssetFile("template/app/activity_main.xml", File(resLayoutDir, "activity_main.xml"))

            // ၄။ အသုံးပြုသူပေးသော Dynamic နာမည်များကို အစားထိုးပြီးမှ ဖိုင်ထဲ ထည့်မည်
            
            // (က) AndroidManifest.xml ထဲတွင် Package Name အစားထိုးခြင်း
            copyAssetWithReplace("template/app/AndroidManifest.xml", File(appDir, "src/main/AndroidManifest.xml")) { content ->
                content.replace("com.example.template", packageName)
            }

            // (ခ) strings.xml ထဲတွင် Project Name (App Name) အစားထိုးခြင်း
            copyAssetWithReplace("template/app/strings.xml", File(resValuesDir, "strings.xml")) { content ->
                content.replace("TemplateApp", projectName)
            }

            // (ဂ) MainActivity.kt ထဲတွင် Package Name အစားထိုးခြင်း
            copyAssetWithReplace("template/app/MainActivity.kt", File(srcDir, "MainActivity.kt")) { content ->
                content.replace("package com.example.template", "package $packageName")
            }

            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    // စာသားများကို အစားထိုးပြီးမှ ဖိုင်အဖြစ် သိမ်းဆည်းပေးမည့် အကူအညီပေးချက် (Helper)
    private fun copyAssetWithReplace(assetPath: String, targetFile: File, replaceLogic: (String) -> String) {
        context.assets.open(assetPath).bufferedReader().use { reader ->
            val originalContent = reader.readText()
            val updatedContent = replaceLogic(originalContent)
            
            FileOutputStream(targetFile).bufferedWriter().use { writer ->
                writer.write(updatedContent)
            }
        }
    }

    private fun copyAssetFile(assetPath: String, targetFile: File) {
        context.assets.open(assetPath).use { inputStream ->
            FileOutputStream(targetFile).use { outputStream ->
                inputStream.copyTo(outputStream)
            }
        }
    }
