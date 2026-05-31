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
import sun.security.x509.* // 💡 သတိပြုရန် - လက်မှတ်ထုတ်ပေးရန် standard internal library သုံးထားပါသည်

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
            // (ဤနေရာတွင် ရိုးရှင်းသော ကူးယူမှုထက် လက်တွေ့တွင် zip-signer binary သို့မဟုတ် custom jar ကို ခေါ်သုံးရပါမည်)
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

    // Android မှ လက်ခံမည့် သက်တမ်းရှိ လက်မှတ် (Self-Signed Certificate) ဖန်တီးခြင်း
    private fun generateCertificate(keyPair: KeyPair): X509Certificate {
        val info = X509CertInfo()
        val interval = CertificateValidity(Date(), Date(System.currentTimeMillis() + 365L * 24 * 60 * 60 * 1000)) // သက်တမ်း ၁ နှစ်
        val sn = java.math.BigInteger(64, java.security.SecureRandom())
        val owner = X500Name("CN=AI-IDE, O=ZyntraAI, C=MM")

        info.set(X509CertInfo.VALIDITY, interval)
        info.set(X509CertInfo.SERIAL_NUMBER, CertificateSerialNumber(sn))
        info.set(X509CertInfo.SUBJECT, owner)
        info.set(X509CertInfo.ISSUER, owner)
        info.set(X509CertInfo.KEY, CertificateX509Key(keyPair.public))
        info.set(X509CertInfo.ALGORITHM_ID, CertificateAlgorithmId(AlgorithmId(AlgorithmId.sha256WithRSAEncryption_oid)))

        val cert = X509CertImpl(info)
        cert.sign(keyPair.private, "SHA256withRSA")
        return cert
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
