package com.example.william.my.module.markdown.engine

import android.text.Layout
import android.widget.TextView
import io.noties.markwon.AbstractMarkwonPlugin
import io.noties.markwon.MarkwonVisitor
import io.noties.markwon.ext.tables.TableSpan
import io.noties.markwon.ext.tables.TableTheme
import org.commonmark.ext.gfm.tables.TableBlock
import org.commonmark.ext.gfm.tables.TableBody
import org.commonmark.ext.gfm.tables.TableCell
import org.commonmark.ext.gfm.tables.TableHead
import org.commonmark.ext.gfm.tables.TableRow
import org.commonmark.ext.gfm.tables.TablesExtension
import org.commonmark.node.Node
import org.commonmark.parser.Parser
import java.lang.ref.WeakReference
import java.util.Collections

/**
 * 流式 Markdown 零抖动表格插件 (StreamTablePlugin)
 *
 * 核心设计：
 * 1. 废除 TableRowsScheduler 的异步二次 setText 刷新死循环；
 * 2. 结合 TextView 实际可用宽度在第一轮 layout pass 中即时构建 StaticLayout；
 * 3. 测量期与绘制期高度严格一致，打字流式过程中 0 闪烁、0 抖动、文字永不压底线。
 */
class StreamTablePlugin(
    private val theme: TableTheme,
    private val textViewProvider: () -> TextView?
) : AbstractMarkwonPlugin() {

    private val visitor = TableVisitor(theme, textViewProvider)

    override fun configureParser(builder: Parser.Builder) {
        builder.extensions(Collections.singleton(TablesExtension.create()))
    }

    override fun configureVisitor(builder: MarkwonVisitor.Builder) {
        visitor.configure(builder)
    }

    override fun beforeRender(node: Node) {
        visitor.clear()
    }

    companion object {
        fun create(theme: TableTheme, textViewProvider: () -> TextView?): StreamTablePlugin {
            return StreamTablePlugin(theme, textViewProvider)
        }
    }

    private class TableVisitor(
        private val tableTheme: TableTheme,
        private val textViewProvider: () -> TextView?
    ) {
        private var pendingTableRow: ArrayList<StreamTableRowSpan.Cell>? = null
        private var tableRowIsHeader = false
        private var tableRows = 0

        fun clear() {
            pendingTableRow = null
            tableRowIsHeader = false
            tableRows = 0
        }

        fun configure(builder: MarkwonVisitor.Builder) {
            builder
                .on(TableBlock::class.java) { visitor, tableBlock ->
                    visitor.blockStart(tableBlock)
                    val length = visitor.length()
                    visitor.visitChildren(tableBlock)
                    visitor.setSpans(length, TableSpan())
                    visitor.blockEnd(tableBlock)
                }
                .on(TableBody::class.java) { visitor, tableBody ->
                    visitor.visitChildren(tableBody)
                    tableRows = 0
                }
                .on(TableRow::class.java) { visitor, tableRow ->
                    visitRow(visitor, tableRow)
                }
                .on(TableHead::class.java) { visitor, tableHead ->
                    visitRow(visitor, tableHead)
                }
                .on(TableCell::class.java) { visitor, tableCell ->
                    val length = visitor.length()
                    visitor.visitChildren(tableCell)

                    if (pendingTableRow == null) {
                        pendingTableRow = ArrayList(2)
                    }

                    pendingTableRow?.add(
                        StreamTableRowSpan.Cell(
                            alignment = tableCellAlignment(tableCell.alignment),
                            text = visitor.builder().removeFromEnd(length)
                        )
                    )
                    tableRowIsHeader = tableCell.isHeader
                }
        }

        private fun visitRow(visitor: MarkwonVisitor, node: Node) {
            val length = visitor.length()
            visitor.visitChildren(node)

            val pending = pendingTableRow
            if (pending != null) {
                val builder = visitor.builder()
                val builderLength = builder.length
                val addNewLine = builderLength > 0 && builder[builderLength - 1] != '\n'

                if (addNewLine) {
                    visitor.forceNewLine()
                }
                builder.append('\u00a0')

                val span = StreamTableRowSpan(
                    theme = tableTheme,
                    cells = pending,
                    header = tableRowIsHeader,
                    odd = tableRows % 2 == 1,
                    textViewRef = textViewProvider()?.let { WeakReference(it) }
                )

                tableRows = if (tableRowIsHeader) 0 else tableRows + 1
                visitor.setSpans(if (addNewLine) length + 1 else length, span)
                pendingTableRow = null
            }
        }

        private fun tableCellAlignment(alignment: TableCell.Alignment?): Layout.Alignment {
            return when (alignment) {
                TableCell.Alignment.CENTER -> Layout.Alignment.ALIGN_CENTER
                TableCell.Alignment.RIGHT -> Layout.Alignment.ALIGN_OPPOSITE
                else -> Layout.Alignment.ALIGN_NORMAL
            }
        }
    }
}
