package com.example.william.my.module.demo.activity2

import android.os.Bundle
import com.alibaba.android.arouter.facade.annotation.Route
import com.example.william.my.basic.basic_module.router.path.RouterPath
import com.example.william.my.lib.activity.BaseVBActivity
import com.example.william.my.module.demo.databinding.DemoActivitySecureKeyBinding
import com.example.william.my.module.demo.utils.SecureKeyDemoUtils
import com.example.william.my.module.demo.utils.SecureKeyInfo
import com.example.william.my.module.demo.utils.SecureSignatureResult

@Route(path = RouterPath.Demo.SecureKey)
class SecureKeyDemoActivity : BaseVBActivity<DemoActivitySecureKeyBinding>() {

    companion object {
        private const val EMPTY_LOGS_TEXT = "暂无日志"
    }

    override fun getViewBinding(): DemoActivitySecureKeyBinding {
        return DemoActivitySecureKeyBinding.inflate(layoutInflater)
    }

    private var currentKeyInfo: SecureKeyInfo? = null
    private var currentSignResult: SecureSignatureResult? = null

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)

        initButtons()
        renderInfo()
    }

    private fun initButtons() {
        mBinding.btnCreateKey.setOnClickListener {
            runCatching {
                currentKeyInfo = SecureKeyDemoUtils.createKeyIfNeeded()
                appendLog("创建成功，安全级别：${currentKeyInfo?.secureLevel}，信任等级：${currentKeyInfo?.trustLevel}")
            }.onFailure {
                appendLog("创建密钥失败：${it.message}")
            }
            renderInfo()
        }

        mBinding.btnGetKeyInfo.setOnClickListener {
            runCatching {
                currentKeyInfo = SecureKeyDemoUtils.getKeyInfo()
                if (currentKeyInfo == null) {
                    appendLog("当前还没有系统安全密钥")
                } else {
                    appendLog("读取成功，安全级别：${currentKeyInfo?.secureLevel}，信任等级：${currentKeyInfo?.trustLevel}")
                }
            }.onFailure {
                appendLog("读取密钥信息失败：${it.message}")
            }
            renderInfo()
        }

        mBinding.btnSignChallenge.setOnClickListener {
            runCatching {
                val challenge = "jijin_nonce_${System.currentTimeMillis()}"
                currentSignResult = SecureKeyDemoUtils.signChallenge(challenge)
                appendLog("签名成功，可将 signature + publicKey 发给服务端验签")
            }.onFailure {
                appendLog("签名失败：${it.message}")
            }
            renderInfo()
        }

        mBinding.btnDeleteKey.setOnClickListener {
            runCatching {
                SecureKeyDemoUtils.deleteKey()
                currentKeyInfo = null
                currentSignResult = null
                appendLog("删除成功")
            }.onFailure {
                appendLog("删除密钥失败：${it.message}")
            }
            renderInfo()
        }
    }

    private fun renderInfo() {
        mBinding.tvSecureLevel.text = "安全级别：${currentKeyInfo?.secureLevel ?: "-"}"
        mBinding.tvTrustLevel.text = "信任等级：${currentKeyInfo?.trustLevel ?: "-"}"
        mBinding.tvKeyInfo.text = formatKeyInfo(currentKeyInfo)
        mBinding.tvSignResult.text = formatSignResult(currentSignResult)
    }

    private fun appendLog(message: String) {
        val current = mBinding.tvLogs.text?.toString().orEmpty()
        val next = buildString {
            append(message)
            if (current.isNotBlank() && current != EMPTY_LOGS_TEXT) {
                append("\n\n")
                append(current)
            }
        }
        mBinding.tvLogs.text = next
    }

    private fun formatKeyInfo(keyInfo: SecureKeyInfo?): String {
        if (keyInfo == null) return "暂无密钥信息"

        return buildString {
            appendLine("{")
            appendLine("  \"keyId\": \"${keyInfo.keyId}\",")
            appendLine("  \"publicKey\": \"${keyInfo.publicKey}\",")
            appendLine("  \"algorithm\": \"${keyInfo.algorithm}\",")
            appendLine("  \"secureLevel\": \"${keyInfo.secureLevel}\",")
            appendLine("  \"trustLevel\": \"${keyInfo.trustLevel}\",")
            appendLine("  \"hardwareBacked\": ${keyInfo.hardwareBacked}")
            append("}")
        }
    }

    private fun formatSignResult(signResult: SecureSignatureResult?): String {
        if (signResult == null) return "暂无签名结果"

        return buildString {
            appendLine("{")
            appendLine("  \"keyId\": \"${signResult.keyId}\",")
            appendLine("  \"signature\": \"${signResult.signature}\",")
            appendLine("  \"algorithm\": \"${signResult.algorithm}\",")
            appendLine("  \"challenge\": \"${signResult.challenge}\"")
            append("}")
        }
    }
}
