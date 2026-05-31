package com.ai.ide.utils

import android.content.Context
import java.io.File
import java.io.FileOutputStream
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.KeyStore
import java.security.PrivateKey
import java.security.cert.X509Certificate
import java.util.Date
import javax.security.auth.x500.X500Principal

class ApkSigner(private val context: Context) {

    /**
     * 💡 [Sign APK Task]
     * ထွက်လာသည့် Unsigned APK ဖိုင်ကို သီးသန့် Key သုံးပြီး လက်မှတ်ထိုးပေးမည့် စနစ်
     */
    fun signApk(unsignedApk: File, signedApk: File): Boolean {
        return try {
            if (!unsignedApk.exists()) return false

            // ၁။ အလိုအလျောက် ဒစ်ဂျစ်တယ်လက်မှတ် (Key & Certificate) ထုတ်ယူခြင်း
            val keyPair = generateKeyPair()
            val cert = generateCertificate(keyPair)

            // ၂။ APK အား လက်မှတ်ထိုးပြီး အတည်ထုတ်လုပ်ခြင်း Logic
            val command = "apksigner sign --key-content ${keyPair.private.encoded} --cert-content ${cert.encoded} --out ${signedApk.absolutePath} ${unsignedApk.absolutePath}"
            
            executeSignCommand(command)
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    // လက်မှတ်ထိုးရန် သီးသန့် KeyPair ထုတ်ပေးသည့် စနစ်
    private fun generateKeyPair(): KeyPair {
        val keyGen = KeyPairGenerator.getInstance("RSA")
        keyGen.initialize(2048)
        return keyGen.generateKeyPair()
    }

    // JDK 17 နှင့် ကိုက်ညီသော စံနှုန်းမှီ Self-Signed Certificate ဖန်တီးခြင်း
    private fun generateCertificate(keyPair: KeyPair): X509Certificate {
        val keyStore = KeyStore.getInstance(KeyStore.getDefaultType())
        keyStore.load(null, null)
        
        // Android က လက်ခံနိုင်စေရန် Android KeyStore သို့မဟုတ် standard သတ်မှတ်ချက်အတိုင်း ယာယီ Cert Object တစ်ခု ထုတ်ပေးခြင်း
        // ⚠️ သတိပြုရန် - Pure Java တွင် BouncyCastle မပါဘဲ X509 V3 Certificate ကို အစမှ ဆောက်လျှင် sun.* လိုအပ်သဖြင့်
        // Build Error ကင်းစေရန် လက်ရှိတွင် Mock သဘောမျိုး Standard X509 အဖြစ် ပြောင်းလဲပေးထားသည်။
        
        val dn = "CN=AI-IDE, O=ZyntraAI, C=MM"
        val principal = X500Principal(dn)
        
        // ဤနေရာတွင် လက်တွေ့ အလုပ်လုပ်မည့် Cert ထွက်လာစေရန် Android KeyStore API ကို အသုံးပြုခြင်းက ပိုမိုကောင်းမွန်ပါသည်
        throw UnsupportedOperationException("Android custom signing requires Android KeyStore provider for V2/V3 signing.")
    }

    private fun executeSignCommand(command: String): Boolean {
        return try {
            val process = Runtime.getRuntime().exec(command)
            process.waitFor() == 0
        } catch (e: Exception) {
            false
        }
    }
}
