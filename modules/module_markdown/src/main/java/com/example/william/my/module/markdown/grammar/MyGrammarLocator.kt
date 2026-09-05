package com.example.william.my.module.markdown.grammar

import io.noties.prism4j.GrammarLocator
import io.noties.prism4j.Prism4j
import io.noties.prism4j.Prism4j.grammar
import io.noties.prism4j.Prism4j.pattern
import io.noties.prism4j.Prism4j.token
import java.util.regex.Pattern

/**
 * 自定义 Prism4j 语法规则定位器 (Grammar Locator)
 *
 * 为 Prism4j 词法分析器提供常用编程语言的语法分析规则（词法表与正则匹配），
 * 支持在 Android 端离线、极速解析代码高亮。
 *
 * 支持语言：
 * - C-Like (通用 C 语法族基类)
 * - Kotlin / Java
 * - Python
 * - JavaScript / TypeScript / JSON
 * - SQL
 * - Bash / Shell
 * - Markdown / HTML / XML / C++
 */
class MyGrammarLocator : GrammarLocator {

    private val mGrammars = HashMap<String, Prism4j.Grammar>()

    init {
        initGrammars()
    }

    override fun grammar(prism4j: Prism4j, language: String): Prism4j.Grammar? {
        val key = language.lowercase().trim()
        return mGrammars[key] ?: when (key) {
            "kt", "kts" -> mGrammars["kotlin"]
            "js", "ts", "typescript" -> mGrammars["javascript"]
            "py" -> mGrammars["python"]
            "sh", "zsh" -> mGrammars["bash"]
            "c++", "cc", "cxx", "h", "hpp" -> mGrammars["cpp"]
            "xml", "svg" -> mGrammars["html"]
            "md" -> mGrammars["markdown"]
            else -> mGrammars["clike"]
        }
    }

    override fun languages(): Set<String> = mGrammars.keys

    private fun initGrammars() {
        // 1. C-Like 基类
        val clike = grammar(
            "clike",
            token(
                "comment",
                pattern(Pattern.compile("(^|[^\\\\])/\\*[\\s\\S]*?(?:\\*/|$)"), true),
                pattern(Pattern.compile("(^|[^\\\\:])//.*"), true),
            ),
            token(
                "string",
                pattern(Pattern.compile("([\"'])(?:\\\\(?:\\r\\n|[\\s\\S])|(?!\\1)[^\\\\\\r\\n])*\\1"), false, true),
            ),
            token(
                "class-name",
                pattern(Pattern.compile("((?:\\b(?:class|interface|extends|implements|trait|instanceof|new)\\s+)|(?:catch\\s+\\())[a-z_]\\w*", Pattern.CASE_INSENSITIVE), true),
            ),
            token(
                "keyword",
                pattern(Pattern.compile("\\b(?:if|else|while|do|for|return|in|instanceof|function|new|try|throw|catch|finally|null|break|continue)\\b")),
            ),
            token(
                "boolean",
                pattern(Pattern.compile("\\b(?:true|false)\\b")),
            ),
            token(
                "function",
                pattern(Pattern.compile("[a-z0-9_]+(?=\\()", Pattern.CASE_INSENSITIVE)),
            ),
            token(
                "number",
                pattern(Pattern.compile("\\b0x[\\da-f]+\\b|(?:\\b\\d+\\.?\\d*|\\B\\.\\d+)(?:e[+-]?\\d+)?", Pattern.CASE_INSENSITIVE)),
            ),
            token(
                "operator",
                pattern(Pattern.compile("--?|\\+\\+?|!=?=?|<=?|>=?|==?=?|&&?|\\|\\|?|\\?|\\*|/|~|\\^|%")),
            ),
            token(
                "punctuation",
                pattern(Pattern.compile("[\\[\\]{}();,.:]")),
            ),
        )
        mGrammars["clike"] = clike

        // 2. Java
        val java = grammar(
            "java",
            token(
                "comment",
                pattern(Pattern.compile("(^|[^\\\\])/\\*[\\s\\S]*?(?:\\*/|$)"), true),
                pattern(Pattern.compile("(^|[^\\\\:])//.*"), true),
            ),
            token(
                "string",
                pattern(Pattern.compile("\"(?:\\\\.|[^\"\\\\\\r\\n])*\""), false, true),
            ),
            token(
                "annotation",
                pattern(Pattern.compile("@\\w+"), false, false, "punctuation"),
            ),
            token(
                "keyword",
                pattern(Pattern.compile("\\b(?:abstract|assert|boolean|break|byte|case|catch|char|class|const|continue|default|do|double|else|enum|extends|final|finally|float|for|goto|if|implements|import|instanceof|int|interface|long|native|new|package|private|protected|public|return|short|static|strictfp|super|switch|synchronized|this|throw|throws|transient|try|void|volatile|while)\\b")),
            ),
            token(
                "boolean",
                pattern(Pattern.compile("\\b(?:true|false)\\b")),
            ),
            token(
                "number",
                pattern(Pattern.compile("\\b0b[01]+\\b|\\b0x[\\da-f]*\\.?[\\da-f]+(?:p[+-]?\\d+)?[df]?\\b|(?:\\b\\d+\\.?\\d*|\\B\\.\\d+)(?:e[+-]?\\d+)?[df]?\\b", Pattern.CASE_INSENSITIVE)),
            ),
            token(
                "operator",
                pattern(Pattern.compile("(?:>>|<<|>>>|\\+\\+|--|&&|\\|\\||::|[-=!<>+/*%&|^~])=?|\\?|:")),
            ),
            token(
                "punctuation",
                pattern(Pattern.compile("[\\[\\]{}();,.:]")),
            ),
        )
        mGrammars["java"] = java

        // 3. Kotlin
        val kotlin = grammar(
            "kotlin",
            token(
                "comment",
                pattern(Pattern.compile("(^|[^\\\\])/\\*[\\s\\S]*?(?:\\*/|$)"), true),
                pattern(Pattern.compile("(^|[^\\\\:])//.*"), true),
            ),
            token(
                "string",
                pattern(Pattern.compile("\"\"\"[\\s\\S]*?\"\"\"|\"(?:\\\\.|[^\"\\\\\\r\\n])*\""), false, true),
            ),
            token(
                "annotation",
                pattern(Pattern.compile("@(?:file|property|field|get|set|receiver|param|setparam|delegate)?:?\\w+"), false, false, "punctuation"),
            ),
            token(
                "keyword",
                pattern(Pattern.compile("\\b(?:as|break|class|continue|do|else|for|fun|if|in|interface|is|null|object|package|return|super|this|throw|try|typealias|typeof|val|var|when|while|by|catch|companion|constructor|crossinline|data|dynamic|enum|external|final|finally|import|infix|inline|inner|internal|lateinit|noinline|open|operator|out|override|private|protected|public|reified|sealed|suspend|tailrec|vararg|value)\\b")),
            ),
            token(
                "boolean",
                pattern(Pattern.compile("\\b(?:true|false)\\b")),
            ),
            token(
                "function",
                pattern(Pattern.compile("[a-z0-9_]+(?=\\()", Pattern.CASE_INSENSITIVE)),
            ),
            token(
                "number",
                pattern(Pattern.compile("\\b0[xX][\\da-fA-F_]+[uUlL]*\\b|\\b0[bB][01_]+[uUlL]*\\b|(?:\\b\\d[\\d_]*\\.?[\\d_]*(?:[eE][+-]?\\d+)?|\\B\\.\\d[\\d_]*(?:[eE][+-]?\\d+)?)[fFLuU]*\\b")),
            ),
            token(
                "operator",
                pattern(Pattern.compile("\\?:|\\+\\+|--|&&|\\|\\||===|!==|==|!=|<=|>=|[-=!<>/+*%&|^~]")),
            ),
            token(
                "punctuation",
                pattern(Pattern.compile("[\\[\\]{}();,.:]")),
            ),
        )
        mGrammars["kotlin"] = kotlin

        // 4. Python
        val python = grammar(
            "python",
            token(
                "comment",
                pattern(Pattern.compile("#.*")),
            ),
            token(
                "string",
                pattern(Pattern.compile("(?:[rubf]|rb|rf|br)?(?:\"\"\"[\\s\\S]*?\"\"\"|'''[\\s\\S]*?'''|\"(?:\\\\.|[^\"\\\\\\r\\n])*\"|'(?:\\\\.|[^'\\\\\\r\\n])*')", Pattern.CASE_INSENSITIVE), false, true),
            ),
            token(
                "decorator",
                pattern(Pattern.compile("@[\\w.]+"), false, false, "function"),
            ),
            token(
                "keyword",
                pattern(Pattern.compile("\\b(?:and|as|assert|async|await|break|class|continue|def|del|elif|else|except|exec|finally|for|from|global|if|import|in|is|lambda|nonlocal|not|or|pass|print|raise|return|try|while|with|yield)\\b")),
            ),
            token(
                "boolean",
                pattern(Pattern.compile("\\b(?:True|False|None)\\b")),
            ),
            token(
                "function",
                pattern(Pattern.compile("[a-z0-9_]+(?=\\()", Pattern.CASE_INSENSITIVE)),
            ),
            token(
                "number",
                pattern(Pattern.compile("\\b0x[\\da-f]+\\b|(?:\\b\\d+\\.?\\d*|\\B\\.\\d+)(?:e[+-]?\\d+)?[jJ]?", Pattern.CASE_INSENSITIVE)),
            ),
            token(
                "operator",
                pattern(Pattern.compile("[-+%=]=?|!=|\\*\\*?=?|//?=?|<[<=]?|>[>=]?|[&|^~]|\\b(?:and|or|not|is|in)\\b")),
            ),
            token(
                "punctuation",
                pattern(Pattern.compile("[\\[\\]{}();,.:]")),
            ),
        )
        mGrammars["python"] = python

        // 5. JavaScript / TypeScript
        val javascript = grammar(
            "javascript",
            token(
                "comment",
                pattern(Pattern.compile("(^|[^\\\\])/\\*[\\s\\S]*?(?:\\*/|$)"), true),
                pattern(Pattern.compile("(^|[^\\\\:])//.*"), true),
            ),
            token(
                "string",
                pattern(Pattern.compile("`(?:\\\\.|[^`\\\\])*`|\"(?:\\\\.|[^\"\\\\\\r\\n])*\"|'(?:\\\\.|[^'\\\\\\r\\n])*'"), false, true),
            ),
            token(
                "keyword",
                pattern(Pattern.compile("\\b(?:as|async|await|break|case|catch|class|const|continue|debugger|default|delete|do|else|enum|export|extends|finally|for|from|function|get|if|implements|import|in|instanceof|interface|let|new|null|of|package|private|protected|public|return|set|static|super|switch|this|throw|try|typeof|undefined|var|void|while|with|yield)\\b")),
            ),
            token(
                "boolean",
                pattern(Pattern.compile("\\b(?:true|false)\\b")),
            ),
            token(
                "function",
                pattern(Pattern.compile("[a-z0-9_]+(?=\\()", Pattern.CASE_INSENSITIVE)),
            ),
            token(
                "number",
                pattern(Pattern.compile("\\b0[xX][\\da-fA-F]+\\b|\\b0[bB][01]+\\b|\\b0[oO][0-7]+\\b|(?:\\b\\d+\\.?\\d*|\\B\\.\\d+)(?:[eE][+-]?\\d+)?")),
            ),
            token(
                "operator",
                pattern(Pattern.compile("=>|\\+\\+|--|&&|\\|\\||\\?\\?|\\?\\.|===|!==|==|!=|<=|>=|[-=!<>/+*%&|^~]")),
            ),
            token(
                "punctuation",
                pattern(Pattern.compile("[\\[\\]{}();,.:]")),
            ),
        )
        mGrammars["javascript"] = javascript

        // 6. JSON
        val json = grammar(
            "json",
            token(
                "property",
                pattern(Pattern.compile("\"(?:\\\\.|[^\"\\\\\\r\\n])+\"(?=\\s*:)")),
            ),
            token(
                "string",
                pattern(Pattern.compile("\"(?:\\\\.|[^\"\\\\\\r\\n])*\""), false, true),
            ),
            token(
                "number",
                pattern(Pattern.compile("-?\\b\\d+(?:\\.\\d+)?(?:[eE][+-]?\\d+)?\\b")),
            ),
            token(
                "boolean",
                pattern(Pattern.compile("\\b(?:true|false|null)\\b")),
            ),
            token(
                "punctuation",
                pattern(Pattern.compile("[\\[\\]{}:,]")),
            ),
        )
        mGrammars["json"] = json

        // 7. SQL
        val sql = grammar(
            "sql",
            token(
                "comment",
                pattern(Pattern.compile("--.*|/\\*[\\s\\S]*?\\*/")),
            ),
            token(
                "string",
                pattern(Pattern.compile("'(?:''|[^'\\\\]|\\\\.)*'"), false, true),
            ),
            token(
                "keyword",
                pattern(Pattern.compile("\\b(?:SELECT|INSERT|UPDATE|DELETE|FROM|WHERE|AND|OR|NOT|JOIN|LEFT|RIGHT|INNER|OUTER|GROUP|BY|ORDER|HAVING|LIMIT|OFFSET|CREATE|TABLE|DROP|ALTER|INDEX|PRIMARY|KEY|FOREIGN|REFERENCES|DEFAULT|NULL|AS|DISTINCT|CASE|WHEN|THEN|ELSE|END|UNION|ALL|VIEW|INTO|VALUES|SET|COUNT|SUM|AVG|MAX|MIN)\\b", Pattern.CASE_INSENSITIVE)),
            ),
            token(
                "boolean",
                pattern(Pattern.compile("\\b(?:TRUE|FALSE|NULL)\\b", Pattern.CASE_INSENSITIVE)),
            ),
            token(
                "number",
                pattern(Pattern.compile("\\b\\d+(?:\\.\\d+)?(?:e[+-]?\\d+)?\\b", Pattern.CASE_INSENSITIVE)),
            ),
            token(
                "operator",
                pattern(Pattern.compile("[-+*/%^&|]=?|!=|<>|<=?|>=?|==?|\\|\\||\\b(?:LIKE|IN|BETWEEN|IS|EXISTS)\\b", Pattern.CASE_INSENSITIVE)),
            ),
            token(
                "punctuation",
                pattern(Pattern.compile("[\\[\\]{}();,.:]")),
            ),
        )
        mGrammars["sql"] = sql

        // 8. Bash / Shell
        val bash = grammar(
            "bash",
            token(
                "comment",
                pattern(Pattern.compile("(^|[^\\\\])#.*"), true),
            ),
            token(
                "string",
                pattern(Pattern.compile("\"(?:\\\\.|[^\"\\\\\\r\\n])*\"|'[^']*'"), false, true),
            ),
            token(
                "variable",
                pattern(Pattern.compile("\\$[a-zA-Z_]\\w*|\\$\\{[^}]+\\}")),
            ),
            token(
                "keyword",
                pattern(Pattern.compile("\\b(?:if|then|else|elif|fi|for|while|until|do|done|in|case|esac|function|select|time|return|exit)\\b")),
            ),
            token(
                "function",
                pattern(Pattern.compile("\\b[a-zA-Z_]\\w*(?=\\s*\\(\\))")),
            ),
            token(
                "operator",
                pattern(Pattern.compile("&&|\\|\\||;;|>&|>>|<<|>&|\\+|-|\\*|/|%|==|!=|<=|>=")),
            ),
            token(
                "punctuation",
                pattern(Pattern.compile("[\\[\\]{}();,.:]")),
            ),
        )
        mGrammars["bash"] = bash

        // 9. C / C++
        val cpp = grammar(
            "cpp",
            token(
                "comment",
                pattern(Pattern.compile("(^|[^\\\\])/\\*[\\s\\S]*?(?:\\*/|$)"), true),
                pattern(Pattern.compile("(^|[^\\\\:])//.*"), true),
            ),
            token(
                "string",
                pattern(Pattern.compile("(?:L|u|u8|U)?(?:\"(?:\\\\.|[^\"\\\\\\r\\n])*\"|'(?:\\\\.|[^'\\\\\\r\\n])*')"), false, true),
            ),
            token(
                "macro",
                pattern(Pattern.compile("#\\s*\\b(?:include|define|undef|if|ifdef|ifndef|elif|else|endif|pragma)\\b.*")),
            ),
            token(
                "keyword",
                pattern(Pattern.compile("\\b(?:alignas|alignof|and|and_eq|asm|auto|bitand|bitor|bool|break|case|catch|char|char16_t|char32_t|class|compl|const|constexpr|const_cast|continue|decltype|default|delete|do|double|dynamic_cast|else|enum|explicit|export|extern|false|float|for|friend|goto|if|inline|int|long|mutable|namespace|new|noexcept|not|not_eq|nullptr|operator|or|or_eq|private|protected|public|register|reinterpret_cast|return|short|signed|sizeof|static|static_assert|static_cast|struct|switch|template|this|thread_local|throw|true|try|typedef|typeid|typename|union|unsigned|using|virtual|void|volatile|wchar_t|while|xor|xor_eq)\\b")),
            ),
            token(
                "boolean",
                pattern(Pattern.compile("\\b(?:true|false)\\b")),
            ),
            token(
                "number",
                pattern(Pattern.compile("\\b0x[\\da-f]+\\b|(?:\\b\\d+\\.?\\d*|\\B\\.\\d+)(?:e[+-]?\\d+)?[ulf]*", Pattern.CASE_INSENSITIVE)),
            ),
            token(
                "operator",
                pattern(Pattern.compile("(?:>>|<<|\\+\\+|--|&&|\\|\\||::|[-=!<>+/*%&|^~])=?|\\?|:")),
            ),
            token(
                "punctuation",
                pattern(Pattern.compile("[\\[\\]{}();,.:]")),
            ),
        )
        mGrammars["cpp"] = cpp
        mGrammars["c"] = cpp
    }
}
