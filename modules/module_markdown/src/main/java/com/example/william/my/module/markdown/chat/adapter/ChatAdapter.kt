package com.example.william.my.module.markdown.chat.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.william.my.module.markdown.chat.model.ChatMessage
import com.example.william.my.module.markdown.databinding.MarkdownItemChatAssistantBinding
import com.example.william.my.module.markdown.databinding.MarkdownItemChatUserBinding
import com.example.william.my.module.markdown.engine.MarkdownStreamFixer
import io.noties.markwon.Markwon

/**
 * AI 聊天消息列表适配器 (ChatAdapter)
 *
 * 核心优化：
 * 1. Multi-ViewType 分离用户提问气泡与 AI 助手回复气泡；
 * 2. Payload 细粒度局部增量刷新（Partial Update）：
 *    在流式推流过程中，仅通过 PAYLOAD_STREAM_CONTENT 触发 TextView 内容更新与 Spannable 重绘，
 *    完全避免整个 ViewHolder 重建、头像闪烁或布局重排；
 * 3. 结合 MarkdownStreamFixer 实现流式未闭合语法容错与呼吸光标。
 */
class ChatAdapter(
    private val mMarkwon: Markwon,
    private val mOnCopyClickListener: (content: String) -> Unit,
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val TYPE_USER = 1
        private const val TYPE_ASSISTANT = 2
    }

    private val mMessages = mutableListOf<ChatMessage>()

    fun setMessages(list: List<ChatMessage>) {
        mMessages.clear()
        mMessages.addAll(list)
        notifyDataSetChanged()
    }

    fun addMessage(message: ChatMessage) {
        mMessages.add(message)
        notifyItemInserted(mMessages.size - 1)
    }

    fun addMessages(vararg messages: ChatMessage) {
        if (messages.isEmpty()) return
        val startPos = mMessages.size
        mMessages.addAll(messages)
        notifyItemRangeInserted(startPos, messages.size)
    }

    fun updateMessage(index: Int, message: ChatMessage, payload: String? = null) {
        if (index in 0 until mMessages.size) {
            mMessages[index] = message
            if (payload != null) {
                notifyItemChanged(index, payload)
            } else {
                notifyItemChanged(index)
            }
        }
    }

    fun getMessage(index: Int): ChatMessage? = mMessages.getOrNull(index)

    override fun getItemCount(): Int = mMessages.size

    override fun getItemViewType(position: Int): Int = when (mMessages[position].role) {
        ChatMessage.Role.USER -> TYPE_USER
        else -> TYPE_ASSISTANT
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return if (viewType == TYPE_USER) {
            val binding = MarkdownItemChatUserBinding.inflate(inflater, parent, false)
            UserViewHolder(binding)
        } else {
            val binding = MarkdownItemChatAssistantBinding.inflate(inflater, parent, false)
            AssistantViewHolder(binding)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        onBindViewHolder(holder, position, emptyList())
    }

    override fun onBindViewHolder(
        holder: RecyclerView.ViewHolder,
        position: Int,
        payloads: List<Any>,
    ) {
        val message = mMessages[position]

        if (holder is UserViewHolder) {
            holder.bind(message)
            return
        }

        if (holder is AssistantViewHolder) {
            if (payloads.isNotEmpty()) {
                for (payload in payloads) {
                    when (payload) {
                        ChatMessage.PAYLOAD_STREAM_CONTENT -> {
                            holder.updateStreamContent(mMarkwon, message)
                        }
                        ChatMessage.PAYLOAD_STATUS -> {
                            holder.updateStatus(message)
                        }
                    }
                }
            } else {
                holder.bindFull(mMarkwon, message, mOnCopyClickListener)
            }
        }
    }

    /**
     * 用户消息 ViewHolder
     *
     * 聊天列表中用户消息的列表项。
     */
    class UserViewHolder(
        private val binding: MarkdownItemChatUserBinding,
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(message: ChatMessage) {
            binding.tvUserContent.text = message.content
        }
    }

    /**
     * 助手消息 ViewHolder
     *
     * 聊天列表中助手消息的列表项。
     */
    class AssistantViewHolder(
        private val binding: MarkdownItemChatAssistantBinding,
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bindFull(
            markwon: Markwon,
            message: ChatMessage,
            onCopyClick: (content: String) -> Unit,
        ) {
            updateStreamContent(markwon, message)
            updateStatus(message, onCopyClick)
        }

        fun updateStreamContent(markwon: Markwon, message: ChatMessage) {
            val isStreaming = message.status == ChatMessage.Status.STREAMING
            val fixedMarkdown = MarkdownStreamFixer.fix(message.content, appendCursor = isStreaming)

            if (fixedMarkdown.isEmpty() && message.status == ChatMessage.Status.SENDING) {
                binding.tvAssistantContent.text = ""
                binding.tvAssistantContent.visibility = View.GONE
                binding.pbThinking.visibility = View.VISIBLE
                binding.tvStatusTag.text = "正在思考中..."
                binding.btnCopyContent.visibility = View.GONE
            } else {
                if (binding.tvAssistantContent.visibility != View.VISIBLE) {
                    binding.tvAssistantContent.visibility = View.VISIBLE
                }
                if (binding.pbThinking.visibility != View.GONE) {
                    binding.pbThinking.visibility = View.GONE
                }
                if (isStreaming) {
                    binding.tvStatusTag.text = "正在生成回答..."
                    binding.btnCopyContent.visibility = View.GONE
                }
                markwon.setMarkdown(binding.tvAssistantContent, fixedMarkdown)
            }
        }

        fun updateStatus(message: ChatMessage, onCopyClick: ((content: String) -> Unit)? = null) {
            if (onCopyClick != null) {
                binding.btnCopyContent.setOnClickListener {
                    onCopyClick(message.content)
                }
            }
            when (message.status) {
                ChatMessage.Status.SENDING -> {
                    binding.pbThinking.visibility = View.VISIBLE
                    binding.tvStatusTag.text = "正在思考中..."
                    binding.btnCopyContent.visibility = View.GONE
                }
                ChatMessage.Status.STREAMING -> {
                    binding.pbThinking.visibility = View.GONE
                    binding.tvStatusTag.text = "正在生成回答..."
                    binding.btnCopyContent.visibility = View.GONE
                }
                ChatMessage.Status.COMPLETED -> {
                    binding.pbThinking.visibility = View.GONE
                    binding.tvStatusTag.text = "生成完成"
                    binding.btnCopyContent.visibility = View.VISIBLE
                }
                ChatMessage.Status.FAILED -> {
                    binding.pbThinking.visibility = View.GONE
                    binding.tvStatusTag.text = "生成已中断"
                    binding.btnCopyContent.visibility = View.VISIBLE
                }
            }
        }
    }
}
