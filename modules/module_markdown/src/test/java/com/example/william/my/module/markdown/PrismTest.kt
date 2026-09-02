package com.example.william.my.module.markdown

import com.example.william.my.module.markdown.grammar.MyGrammarLocator
import io.noties.markwon.syntax.Prism4jThemeDarkula
import io.noties.markwon.syntax.Prism4jThemeDefault
import io.noties.prism4j.Prism4j
import org.junit.Test

class PrismTest {
    @Test
    fun testPrismInit() {
        val locator = MyGrammarLocator()
        val prism4j = Prism4j(locator)

        val languages = listOf("kotlin", "java", "python", "javascript", "json", "sql", "bash", "cpp", "clike")
        for (lang in languages) {
            val grammar = prism4j.grammar(lang)
            assert(grammar != null) { "Grammar for $lang should not be null" }
            val tokens = prism4j.tokenize("val x = 123; // comment\nfun test() {}", grammar!!)
            println("Language $lang tokens count: ${tokens.size}")
        }

        val darkula = Prism4jThemeDarkula.create()
        val light = Prism4jThemeDefault.create()
        println("Darkula background: ${darkula.background()}")
        println("Light background: ${light.background()}")
    }
}
