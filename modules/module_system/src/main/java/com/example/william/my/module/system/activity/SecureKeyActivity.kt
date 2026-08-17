package com.example.william.my.module.system.activity

import android.os.Bundle
import com.alibaba.android.arouter.facade.annotation.Route
import com.example.william.my.basic.basic_shared.activity.BasicResponseActivity
import com.example.william.my.basic.basic_shared.router.path.RouterPath
import com.example.william.my.module.system.utils.SecureKeyDemoUtils

/**
 * Android Keystore 安全密钥演示
 *
 * 演示使用 Android Keystore 生成 EC P-256 密钥对，并通过私钥进行 ECDSA 签名。
 * 私钥始终保存在系统安全边界内（TEE 或 StrongBox），不会导出到业务层，
 * 适用于设备绑定、challenge-response 防重放等安全场景。
 */
@Route(path = RouterPath.System.SecureKey)
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
