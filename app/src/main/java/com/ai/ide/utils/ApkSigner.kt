package com.ai.ide.utils

import android.content.Context
import java.io.File
import java.io.BufferedReader
import java.io.InputStreamReader

class ApkSigner(private val context: Context) {

    private val binToolsDir = File(context.filesDir, "bin_tools")
    private val keystoreFile = File(context.filesDir, "debug.keystore")

    fun signApk(unsignedApk: File, signedApk: File, onLogReceived: (String) -> Unit): Boolean {
        if (!unsignedApk.exists()) return false

        if (!keystoreFile.exists()) {
            val created = generateDebugKeystore()
            if (!created) {
                onLogReceived("Error: Keystore generation failed.")
                return false
            }
        }

        val apksignerBinary = File(binToolsDir, "apksigner")
        val command = listOf(
            apksignerBinary.absolutePath, "sign",
            "--ks", keystoreFile.absolutePath,
            "--ks-pass", "pass:android",
            "--ks-key-alias", "androiddebugkey",
            "--key-pass", "pass:android",
            "--out", signedApk.absolutePath,
            unsignedApk.absolutePath
        )
        return executeSignCommand(apksignerBinary, command, onLogReceived)
    }

    private fun generateDebugKeystore(): Boolean {
        val keytoolBinary = File(binToolsDir, "keytool")
        if (!keytoolBinary.exists()) return false
        keytoolBinary.setExecutable(true, false)
        val command = listOf(
            keytoolBinary.absolutePath, "-genkey", "-v",
            "-keystore", keystoreFile.absolutePath,
            "-storepass", "android", "-alias", "androiddebugkey",
            "-keypass", "android", "-keyalg", "RSA", "-keysize", "2048",
            "-validity", "10000", "-dname", "CN=Android Debug, O=Android, C=US"
        )
        return try {
            ProcessBuilder(command).start().waitFor() == 0
        } catch (e: Exception) { false }
    }

    private fun executeSignCommand(binaryFile: File, command: List<String>, onLogReceived: (String) -> Unit): Boolean {
        if (!binaryFile.exists()) return false
        return try {
            binaryFile.setExecutable(true, false)
            val process = ProcessBuilder(command).redirectErrorStream(true).start()
            val reader = BufferedReader(InputStreamReader(process.inputStream))
            var line: String?
            while (reader.readLine().also { line = it } != null) {
                onLogReceived("Signer: $line")
            }
            process.waitFor() == 0
        } catch (e: Exception) { false }
    }
}
