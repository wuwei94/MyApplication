package com.example.william.my.module.system.utils

import android.os.Build
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyInfo
import android.security.keystore.KeyProperties
import android.util.Base64
import java.nio.charset.StandardCharsets
import java.security.KeyFactory
import java.security.KeyPairGenerator
import java.security.KeyStore
import java.security.Signature
import java.security.spec.ECGenParameterSpec

data class SecureKeyInfo(
    val keyId: String,
    val publicKey: String,
    val algorithm: String,
    val secureLevel: String,
    val trustLevel: String,
    val hardwareBacked: Boolean
)

data class SecureSignatureResult(
    val keyId: String,
    val signature: String,
    val algorithm: String,
    val challenge: String
)

object SecureKeyDemoUtils {
    private const val ANDROID_KEYSTORE = "AndroidKeyStore"
    private const val KEY_ALIAS = "demo_secure_key"

    /**
     * 创建系统安全密钥；如果已存在则直接复用。
     *
     * 当前 demo 使用 Android Keystore 生成 EC P-256 密钥对，
     * 私钥保持在系统安全边界内，不会导出到业务层。
     */
    fun createKeyIfNeeded(): SecureKeyInfo {
        val keyStore = loadKeyStore()
        if (!keyStore.containsAlias(KEY_ALIAS)) {
            val generator = KeyPairGenerator.getInstance(
                KeyProperties.KEY_ALGORITHM_EC,
                ANDROID_KEYSTORE
            )

            val spec = KeyGenParameterSpec.Builder(
                KEY_ALIAS,
                KeyProperties.PURPOSE_SIGN or KeyProperties.PURPOSE_VERIFY
            )
                .setAlgorithmParameterSpec(ECGenParameterSpec("secp256r1"))
                .setDigests(KeyProperties.DIGEST_SHA256)
                .build()

            generator.initialize(spec)
            generator.generateKeyPair()
        }

        return getKeyInfo() ?: throw IllegalStateException("Secure key creation failed")
    }

    /**
     * 获取当前密钥信息。
     *
     * 返回 keyId、公钥、算法、安全级别和信任等级，便于上层展示和后续注册到服务端。
     */
    fun getKeyInfo(): SecureKeyInfo? {
        val keyStore = loadKeyStore()
        val entry = keyStore.getEntry(KEY_ALIAS, null) as? KeyStore.PrivateKeyEntry ?: return null
        val keyFactory = KeyFactory.getInstance(entry.privateKey.algorithm, ANDROID_KEYSTORE)
        val keyInfo = keyFactory.getKeySpec(entry.privateKey, KeyInfo::class.java)
        val secureLevel = resolveSecurityLevel(keyInfo)

        return SecureKeyInfo(
            keyId = KEY_ALIAS,
            publicKey = Base64.encodeToString(entry.certificate.publicKey.encoded, Base64.NO_WRAP),
            algorithm = "EC_P256",
            secureLevel = secureLevel,
            trustLevel = resolveTrustLevel(secureLevel),
            hardwareBacked = keyInfo.isInsideSecureHardware
        )
    }

    /**
     * 使用系统安全密钥对 challenge 做 ECDSA 签名。
     *
     * 适用于 challenge-response 场景；签名结果和原始 challenge 一起返回，
     * 方便上层调试或提交到服务端验签。
     */
    fun signChallenge(challenge: String): SecureSignatureResult {
        val keyStore = loadKeyStore()
        val privateKey = keyStore.getKey(KEY_ALIAS, null)
            ?: throw IllegalStateException("Secure key not found")

        val signature = Signature.getInstance("SHA256withECDSA")
        signature.initSign(privateKey as java.security.PrivateKey)
        signature.update(challenge.toByteArray(StandardCharsets.UTF_8))

        return SecureSignatureResult(
            keyId = KEY_ALIAS,
            signature = Base64.encodeToString(signature.sign(), Base64.NO_WRAP),
            algorithm = "ES256",
            challenge = challenge
        )
    }

    /**
     * 删除当前系统安全密钥。
     *
     * 适用于测试重置、设备解绑或账号切换场景。
     */
    fun deleteKey(): Boolean {
        val keyStore = loadKeyStore()
        if (!keyStore.containsAlias(KEY_ALIAS)) return true
        keyStore.deleteEntry(KEY_ALIAS)
        return true
    }

    private fun loadKeyStore(): KeyStore =
        KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }

    /**
     * 将 Android 平台返回的密钥属性转换成统一安全级别字符串。
     */
    private fun resolveSecurityLevel(keyInfo: KeyInfo): String {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            return when (keyInfo.securityLevel) {
                KeyProperties.SECURITY_LEVEL_STRONGBOX -> "strongbox"
                KeyProperties.SECURITY_LEVEL_TRUSTED_ENVIRONMENT -> "tee"
                KeyProperties.SECURITY_LEVEL_SOFTWARE -> "software"
                else -> if (keyInfo.isInsideSecureHardware) "tee" else "unknown"
            }
        }

        return if (keyInfo.isInsideSecureHardware) "tee_or_strongbox" else "software"
    }

    /**
     * 根据安全级别映射业务可读的信任等级。
     */
    fun resolveTrustLevel(secureLevel: String): String {
        return when (secureLevel) {
            "strongbox" -> "高信任"
            "tee", "tee_or_strongbox" -> "中高信任"
            "software" -> "低信任"
            else -> "未知信任"
        }
    }
}
