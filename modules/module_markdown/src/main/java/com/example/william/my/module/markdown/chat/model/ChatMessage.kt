package com.example.william.my.module.markdown.chat.model

/**
 * 聊天消息数据实体
 *
 * @param id 消息唯一标识 UUID
 * @param role 角色（USER / ASSISTANT / SYSTEM）
 * @param content 消息文本内容（支持 Markdown）
 * @param status 发送与生成状态（SENDING / STREAMING / COMPLETED / FAILED）
 * @param timestamp 发送时间戳
 */
data class ChatMessage(
    val id: String,
    val role: Role,
    var content: String,
    var status: Status = Status.COMPLETED,
    val timestamp: Long = System.currentTimeMillis(),
) {
    /**
     * 消息角色
     *
     * 区分用户与助手消息。
     */
    enum class Role {
        USER,
        ASSISTANT,
        SYSTEM,
    }

    /**
     * 消息状态
     *
     * 消息的加载状态。
     */
    enum class Status {
        SENDING,
        STREAMING,
        COMPLETED,
        FAILED,
    }

    companion object {
        const val PAYLOAD_STREAM_CONTENT = "payload_stream_content"
        const val PAYLOAD_STATUS = "payload_status"
    }
}
