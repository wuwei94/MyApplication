package com.example.william.my.module.markdown.plugin

import android.text.Layout
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
import java.util.Collections

/**
 * 流式专属高性能 GFM 表格插件 (GfmTablePlugin)
 */
class GfmTablePlugin(
    private val theme: TableTheme,
    private val widthProvider: () -> Int,
) : AbstractMarkwonPlugin() {

    private var pendingTableRow: ArrayList<StreamTableRowSpan.Cell>? = null
    private var tableRowIsHeader = false
    private var tableRows = 0

    override fun configureParser(builder: Parser.Builder) {
        builder.extensions(Collections.singleton(TablesExtension.create()))
    }

    override fun configureVisitor(builder: MarkwonVisitor.Builder) {
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
                        tableCellAlignment(tableCell.alignment),
                        visitor.builder().removeFromEnd(length),
                    ),
                )
                tableRowIsHeader = tableCell.isHeader
            }
    }

    override fun beforeRender(node: Node) {
        pendingTableRow = null
        tableRowIsHeader = false
        tableRows = 0
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
            val spanStart = visitor.length()
            builder.append('\u00a0')

            val span = StreamTableRowSpan(
                theme = theme,
                cells = pending,
                isHeader = tableRowIsHeader,
                isOdd = tableRows % 2 == 1,
                widthProvider = widthProvider,
            )

            tableRows = if (tableRowIsHeader) 0 else tableRows + 1
            visitor.setSpans(spanStart, span)
            pendingTableRow = null
        }
    }

    private fun tableCellAlignment(alignment: TableCell.Alignment?): Layout.Alignment = when (alignment) {
        TableCell.Alignment.CENTER -> Layout.Alignment.ALIGN_CENTER
        TableCell.Alignment.RIGHT -> Layout.Alignment.ALIGN_OPPOSITE
        else -> Layout.Alignment.ALIGN_NORMAL
    }

    companion object {
        fun create(theme: TableTheme, widthProvider: () -> Int): GfmTablePlugin = GfmTablePlugin(theme, widthProvider)
    }
}
