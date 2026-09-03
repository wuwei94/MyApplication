package com.example.william.my.module.markdown.engine

import java.util.ArrayDeque

/**
 * 工业级流式 Markdown 语法自动补全与容错工具 (MarkdownStreamFixer)
 *
 * 核心设计（上下文感知 + 栈式单遍扫描状态机）：
 * 1. 单遍扫描 O(N)：一次线性遍历即可完成转义字符跳过、代码块隔离、行内标签与嵌套语法的精准追踪；
 * 2. 严格的上下文隔离：
 *    - 在多行代码块 (```) 内部，自动忽略行内代码、粗体、斜体、删除线等符号；
 *    - 在行内代码 (`) 内部，自动忽略粗体、斜体、删除线等嵌套符号；
 *    - 严格跳过反斜杠 `\` 转义字符（如 `\*`、`\`` 不会被误判为未闭合语法）；
 * 3. LIFO 栈式逆序闭合：
 *    - 支持 `***粗斜体***`、`**粗体**`、`*斜体*`、`~~删除线~~` 等任意嵌套语法的先进后出严格合法闭合；
 * 4. 智能光标与表格自动补齐：
 *    - 在真实输入末尾插入呼吸光标，并让虚拟闭合标记紧随光标之后，实现最逼真的打字机效果。
 */
object MarkdownStreamFixer {

    private const val CURSOR_SYMBOL = "▍"

    /**
     * 对流式 Markdown 文本进行未闭合语法修复并可选追加光标
     *
     * @param rawText 当前打字机输出的原始文本
     * @param appendCursor 是否在末尾追加呼吸光标
     * @return 修复并补全后的 Markdown 文本
     */
    fun fix(rawText: String, appendCursor: Boolean = false): String {
        if (rawText.isEmpty()) {
            return if (appendCursor) CURSOR_SYMBOL else ""
        }

        val len = rawText.length
        var i = 0
        var isLineStart = true
        var inCodeBlock = false
        var inInlineCode = false
        val syntaxStack = ArrayDeque<String>()

        while (i < len) {
            val c = rawText[i]

            // 1. 处理转义字符
            if (c == '\\' && i + 1 < len) {
                i += 2
                isLineStart = false
                continue
            }

            // 2. 检查多行围栏代码块 (```)
            if (c == '`' && isLineStart && i + 2 < len && rawText[i + 1] == '`' && rawText[i + 2] == '`') {
                inCodeBlock = !inCodeBlock
                i += 3
                if (inCodeBlock) {
                    syntaxStack.clear()
                }
                isLineStart = false
                continue
            }

            // 如果在多行代码块内部，跳过所有行内语法，仅追踪换行与行首状态
            if (inCodeBlock) {
                if (c == '\n') {
                    isLineStart = true
                } else if (!c.isWhitespace()) {
                    isLineStart = false
                }
                i++
                continue
            }

            // 3. 检查行内代码 (`)
            if (c == '`') {
                inInlineCode = !inInlineCode
                i++
                isLineStart = false
                continue
            }

            // 如果在行内代码内部，跳过后续所有富文本语法标记
            if (inInlineCode) {
                if (c == '\n') {
                    isLineStart = true
                    inInlineCode = false // 行内代码跨行自动失效
                } else if (!c.isWhitespace()) {
                    isLineStart = false
                }
                i++
                continue
            }

            // 4. 行内富文本语法（粗斜体 ***、粗体 **、斜体 *、删除线 ~~）
            if (c == '*' || c == '_') {
                val markChar = c
                var count = 0
                while (i < len && rawText[i] == markChar && count < 3) {
                    count++
                    i++
                }
                val token = markChar.toString().repeat(count)
                if (syntaxStack.isNotEmpty() && syntaxStack.peek() == token) {
                    syntaxStack.pop()
                } else {
                    syntaxStack.push(token)
                }
                isLineStart = false
                continue
            } else if (c == '~' && i + 1 < len && rawText[i + 1] == '~') {
                val token = "~~"
                if (syntaxStack.isNotEmpty() && syntaxStack.peek() == token) {
                    syntaxStack.pop()
                } else {
                    syntaxStack.push(token)
                }
                i += 2
                isLineStart = false
                continue
            }

            // 5. 维护行首状态
            if (c == '\n') {
                isLineStart = true
            } else if (!c.isWhitespace()) {
                isLineStart = false
            }
            i++
        }

        // 6. 如果在多行代码块中，闭合代码块
        if (inCodeBlock) {
            val sb = StringBuilder(rawText)
            if (appendCursor) {
                sb.append(CURSOR_SYMBOL)
            }
            if (!sb.endsWith("\n")) {
                sb.append("\n")
            }
            sb.append("```")
            return sb.toString()
        }

        val lastNewLine = rawText.lastIndexOf('\n')
        val lastLine = (if (lastNewLine != -1) rawText.substring(lastNewLine + 1) else rawText).trim()
        val isTableLine = lastLine.startsWith("|")

        val sb = StringBuilder(rawText)

        // 仅在非表格语法行时追加光标，避免光标字符破坏 GFM 表格分隔行（如 | :--- |）导致 AST 树反复坍塌
        if (appendCursor && !isTableLine) {
            sb.append(CURSOR_SYMBOL)
        }

        // 7. 如果在行内代码中，闭合反引号
        if (inInlineCode) {
            sb.append("`")
        }

        // 8. 按照栈的 LIFO 顺序，逆序闭合所有未闭合的行内样式标签
        while (syntaxStack.isNotEmpty()) {
            sb.append(syntaxStack.pop())
        }

        return sb.toString()
    }
}
