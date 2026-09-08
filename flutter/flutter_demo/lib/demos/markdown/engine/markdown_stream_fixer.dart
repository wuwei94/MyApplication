/// 工业级流式 Markdown 语法自动补全与容错工具（MarkdownStreamFixer）
///
/// 对标 Android module_markdown 的 `MarkdownStreamFixer`（Kotlin 实现），
/// 单遍扫描状态机，业务逻辑保持一致：
///
/// 1. 单遍扫描 O(N)：一次线性遍历即可完成转义字符跳过、代码块隔离、
///    行内标签与嵌套语法的精准追踪；
/// 2. 严格的上下文隔离：
///    - 在多行代码块（```）内部，自动忽略行内代码、粗体、斜体、删除线等符号；
///    - 在行内代码（`）内部，自动忽略粗体、斜体、删除线等嵌套符号；
///    - 严格跳过反斜杠 `\` 转义字符（如 `\*`、`` \` `` 不会被误判为未闭合语法）；
/// 3. LIFO 栈式逆序闭合：
///    - 支持 `***粗斜体***`、`**粗体**`、`*斜体*`、`~~删除线~~` 等
///      任意嵌套语法的先进后出严格合法闭合；
/// 4. 智能光标与表格自动补齐：
///    - 在真实输入末尾插入呼吸光标，并让虚拟闭合标记紧随光标之后；
///    - 光标字符不会插入 GFM 表格分隔行（| :--- |），避免 AST 树反复坍塌。
abstract final class MarkdownStreamFixer {
  static const String _cursorSymbol = '▍';

  /// 对流式 Markdown 文本进行未闭合语法修复并可选追加光标。
  ///
  /// [rawText] 当前打字机输出的原始文本；
  /// [appendCursor] 是否在末尾追加呼吸光标。
  static String fix(String rawText, {bool appendCursor = false}) {
    if (rawText.isEmpty) {
      return appendCursor ? _cursorSymbol : '';
    }

    int i = 0;
    bool isLineStart = true;
    bool inCodeBlock = false;
    bool inInlineCode = false;
    final List<String> syntaxStack = <String>[];

    while (i < rawText.length) {
      final String char = rawText[i];

      // 1. 处理转义字符
      if (char == r'\' && i + 1 < rawText.length) {
        i += 2;
        isLineStart = false;
        continue;
      }

      // 2. 检查多行围栏代码块（```）
      if (char == '`' &&
          isLineStart &&
          i + 2 < rawText.length &&
          rawText[i + 1] == '`' &&
          rawText[i + 2] == '`') {
        inCodeBlock = !inCodeBlock;
        i += 3;
        if (inCodeBlock) {
          syntaxStack.clear();
        }
        isLineStart = false;
        continue;
      }

      // 如果在多行代码块内部，跳过所有行内语法，仅追踪换行与行首状态
      if (inCodeBlock) {
        if (char == '\n') {
          isLineStart = true;
        } else if (char.trim().isNotEmpty) {
          isLineStart = false;
        }
        i++;
        continue;
      }

      // 3. 检查行内代码（`）
      if (char == '`') {
        inInlineCode = !inInlineCode;
        i++;
        isLineStart = false;
        continue;
      }

      // 如果在行内代码内部，跳过后续所有富文本语法标记
      if (inInlineCode) {
        if (char == '\n') {
          isLineStart = true;
          inInlineCode = false; // 行内代码跨行自动失效
        } else if (char.trim().isNotEmpty) {
          isLineStart = false;
        }
        i++;
        continue;
      }

      // 4. 行内富文本语法（粗斜体 ***、粗体 **、斜体 *、删除线 ~~）
      if (char == '*' || char == '_') {
        final String markChar = char;
        int count = 0;
        while (i < rawText.length && rawText[i] == markChar && count < 3) {
          count++;
          i++;
        }
        final String token = markChar * count;
        if (syntaxStack.isNotEmpty && syntaxStack.last == token) {
          syntaxStack.removeLast();
        } else {
          syntaxStack.add(token);
        }
        isLineStart = false;
        continue;
      } else if (char == '~' &&
          i + 1 < rawText.length &&
          rawText[i + 1] == '~') {
        const String token = '~~';
        if (syntaxStack.isNotEmpty && syntaxStack.last == token) {
          syntaxStack.removeLast();
        } else {
          syntaxStack.add(token);
        }
        i += 2;
        isLineStart = false;
        continue;
      }

      // 5. 维护行首状态
      if (char == '\n') {
        isLineStart = true;
      } else if (char.trim().isNotEmpty) {
        isLineStart = false;
      }
      i++;
    }

    final StringBuffer sb = StringBuffer(rawText);

    // 6. 如果在多行代码块中，虚拟闭合代码块
    if (inCodeBlock) {
      if (appendCursor) {
        sb.write(_cursorSymbol);
      }
      if (!sb.toString().endsWith('\n')) {
        sb.write('\n');
      }
      sb.write('```');
      return sb.toString();
    }

    final int lastNewLine = rawText.lastIndexOf('\n');
    final String lastLine =
        (lastNewLine != -1 ? rawText.substring(lastNewLine + 1) : rawText)
            .trim();
    final bool isTableLine = lastLine.startsWith('|');

    // 仅在非表格语法行时追加光标，避免光标字符破坏 GFM 表格分隔行
    if (appendCursor && !isTableLine) {
      sb.write(_cursorSymbol);
    }

    // 7. 如果在行内代码中，闭合反引号
    if (inInlineCode) {
      sb.write('`');
    }

    // 8. 按照栈的 LIFO 顺序，逆序闭合所有未闭合的行内样式标签
    while (syntaxStack.isNotEmpty) {
      sb.write(syntaxStack.removeLast());
    }

    return sb.toString();
  }
}
