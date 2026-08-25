package com.example.william.my.module.systemservice.activity

import android.os.Bundle
import com.alibaba.android.arouter.facade.annotation.Route
import com.example.william.my.basic.basic_shared.activity.BasicResponseActivity
import com.example.william.my.basic.basic_shared.router.path.RouterPath
import com.example.william.my.module.systemservice.utils.SecureKeyDemoUtils

/**
 * Android Keystore — 安全密钥管理
 *
 * Android Keystore 提供硬件级安全密钥管理，私钥始终保存在系统安全边界内。
 *
 * 核心特性：
 * 1. 硬件保护：私钥存储在 TEE 或 StrongBox 中，无法导出
 * 2. 设备绑定：密钥与设备绑定，无法迁移到其他设备
 * 3. 安全签名：支持 ECDSA、RSA 等签名算法
 * 4. 防重放：支持 challenge-response 机制，防止重放攻击
 *
 * 密钥类型：
 * 1. EC P-256：椭圆曲线密钥，安全性高，性能好
 * 2. RSA：RSA 密钥，兼容性好
 * 3. AES：对称密钥，用于数据加密
 *
 * 基本用法：
 * ```kotlin
 * // 生成密钥对
 * val keyPairGenerator = KeyPairGenerator.getInstance(
 *     KeyProperties.KEY_ALGORITHM_EC, "AndroidKeyStore"
 * )
 * keyPairGenerator.initialize(
 *     KeyGenParameterSpec.Builder(
 *         "key_alias",
 *         KeyProperties.PURPOSE_SIGN or KeyProperties.PURPOSE_VERIFY
 *     )
 *     .setAlgorithmParameterSpec(ECGenParameterSpec("secp256r1"))
 *     .build()
 * )
 * val keyPair = keyPairGenerator.generateKeyPair()
 *
 * // 使用私钥签名
 * val signature = Signature.getInstance("SHA256withECDSA")
 * signature.initSign(keyPair.private)
 * signature.update(data)
 * val signedData = signature.sign()
 * ```
 *
 * 适用场景：
 * - 设备绑定、身份验证
 * - 安全签名、防篡改
 * - challenge-response 防重放
 * - 生物识别认证
 */
@Route(path = RouterPath.SystemService.SecureKey)
class SecureKeyActivity : BasicResponseActivity() {

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        showDescription("Android Keystore 安全密钥\n\n点击下方按钮操作密钥")
    }

    override fun buildList(): ArrayList<String> {
        return arrayListOf(
            "创建密钥（EC P-256）",
            "查看密钥信息",
            "ECDSA 签名",
            "删除密钥"
        )
    }

    override fun onRecyclerClick(position: Int, string: String) {
        when (position) {
            0 -> createKey()
            1 -> getKeyInfo()
            2 -> signChallenge()
            3 -> deleteKey()
        }
    }

    private fun createKey() {
        runCatching {
            val keyInfo = SecureKeyDemoUtils.createKeyIfNeeded()
            appendLog("创建密钥成功（安全级别：${keyInfo.secureLevel}，信任等级：${keyInfo.trustLevel}）")
        }.onFailure {
            appendLog("创建密钥失败：${it.message}")
        }
    }

    private fun getKeyInfo() {
        runCatching {
            val keyInfo = SecureKeyDemoUtils.getKeyInfo()
            if (keyInfo == null) {
                appendLog("当前还没有密钥，请先创建")
            } else {
                appendLog("密钥信息（安全级别：${keyInfo.secureLevel}，硬件保护：${keyInfo.hardwareBacked}）")
            }
        }.onFailure {
            appendLog("读取密钥失败：${it.message}")
        }
    }

    private fun signChallenge() {
        runCatching {
            val challenge = "nonce_${System.currentTimeMillis()}"
            val result = SecureKeyDemoUtils.signChallenge(challenge)
            appendLog("签名成功（算法：${result.algorithm}）")
        }.onFailure {
            appendLog("签名失败：${it.message}")
        }
    }

    private fun deleteKey() {
        runCatching {
            SecureKeyDemoUtils.deleteKey()
            appendLog("密钥已删除")
        }.onFailure {
            appendLog("删除失败：${it.message}")
        }
    }
}
