package com.example.william.my.module.sse.utils

import android.content.Context
import android.text.InputType
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import com.example.william.my.basic.basic_shared.R
import com.example.william.my.basic.basic_shared.constant.Constants
import com.example.william.my.basic.basic_shared.databinding.SharedLayoutRecyclerResponseBinding

/**
 * DeepSeek API Key / 对话 Prompt 常驻输入栏
 *
 * 插入在控制台与操作列表之间，每行左侧为说明 TextView：
 * - API Key 预填编译期注入值，可在输入框修改；未注入时可手填；输入框空白则回退注入值
 * - Prompt 承载对话文本，空白回退默认提问
 */
object PromptInputHelper {

    class DeepSeekInputs(
        val apiKey: EditText,
        val prompt: EditText,
    )

    /**
     * 在控制台与操作列表之间安装 API Key + Prompt 输入栏。
     *
     * 发送仍由下方列表项触发，通过 [readApiKey] / [readPrompt] 读取。
     */
    fun install(binding: SharedLayoutRecyclerResponseBinding): DeepSeekInputs {
        val context = binding.root.context
        val margin = context.resources.getDimensionPixelSize(R.dimen.shared_dp_8)
        val padding = context.resources.getDimensionPixelSize(R.dimen.shared_dp_12)
        val labelColor = ContextCompat.getColor(context, R.color.shared_color_console_desc)

        val labelSizePx = context.resources.getDimension(R.dimen.shared_sp_12)

        val apiKey = EditText(context).apply {
            hint = "空则用注入值"
            setText(Constants.DeepSeek_ApiKey)
            inputType = InputType.TYPE_CLASS_TEXT
            maxLines = 1
            setSingleLine(true)
            setPadding(padding / 2, padding / 2, padding / 2, padding / 2)
        }
        val prompt = EditText(context).apply {
            hint = "空则用默认"
            setText(LlmStreamParser.DEFAULT_PROMPT)
            inputType = InputType.TYPE_CLASS_TEXT
            maxLines = 1
            setSingleLine(true)
            setPadding(padding / 2, padding / 2, padding / 2, padding / 2)
        }

        val container = LinearLayout(context).apply {
            id = View.generateViewId()
            orientation = LinearLayout.VERTICAL
            addView(createLabelRow(context, "API Key", apiKey, labelColor, padding, labelSizePx))
            addView(createLabelRow(context, "Prompt", prompt, labelColor, padding, labelSizePx))
        }

        val containerParams = ConstraintLayout.LayoutParams(
            ConstraintLayout.LayoutParams.MATCH_CONSTRAINT,
            ConstraintLayout.LayoutParams.WRAP_CONTENT,
        ).apply {
            marginStart = margin
            marginEnd = margin
            topToBottom = R.id.basics_response_container
            bottomToTop = R.id.basics_recycler
            startToStart = ConstraintLayout.LayoutParams.PARENT_ID
            endToEnd = ConstraintLayout.LayoutParams.PARENT_ID
        }
        binding.root.addView(container, containerParams)

        // 控制台底部与操作列表顶部改接到输入栏，避免与输入控件重叠
        (binding.basicsResponseContainer.layoutParams as ConstraintLayout.LayoutParams).apply {
            bottomToTop = container.id
        }.also { binding.basicsResponseContainer.layoutParams = it }

        (binding.basicsRecycler.layoutParams as ConstraintLayout.LayoutParams).apply {
            topToBottom = container.id
        }.also { binding.basicsRecycler.layoutParams = it }

        return DeepSeekInputs(apiKey = apiKey, prompt = prompt)
    }

    /**
     * 左侧说明 + 右侧输入框的一行布局。
     */
    private fun createLabelRow(
        context: Context,
        labelText: String,
        input: EditText,
        labelColor: Int,
        padding: Int,
        textSizePx: Float,
    ): LinearLayout {
        val label = TextView(context).apply {
            text = labelText
            setTextColor(labelColor)
            setTextSize(TypedValue.COMPLEX_UNIT_PX, textSizePx)
            gravity = Gravity.CENTER_VERTICAL
            minWidth = padding * 5
            maxLines = 1
        }
        return LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            addView(
                label,
                LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                ),
            )
            addView(
                input,
                LinearLayout.LayoutParams(
                    0,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    1f,
                ),
            )
        }
    }

    /**
     * 读取 API Key；输入框空白时回退编译期注入的 [Constants.DeepSeek_ApiKey]。
     */
    fun readApiKey(input: EditText): String = input.text?.toString()?.trim().orEmpty()
        .ifEmpty { Constants.DeepSeek_ApiKey }

    /**
     * 读取用户 Prompt；空白文本回退 [LlmStreamParser.DEFAULT_PROMPT]。
     */
    fun readPrompt(input: EditText): String = input.text?.toString()?.trim().orEmpty()
        .ifEmpty { LlmStreamParser.DEFAULT_PROMPT }
}
