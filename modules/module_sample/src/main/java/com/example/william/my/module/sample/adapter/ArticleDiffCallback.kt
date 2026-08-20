package com.example.william.my.module.sample.adapter

import android.os.Bundle
import androidx.recyclerview.widget.DiffUtil
import com.example.william.my.module.sample.bean.ArticleItem

/**
 * DiffUtil.Callback 标准写法示例
 * 负责计算新老列表的差量并生成更新 Payload
 */
class ArticleDiffCallback(
    private val oldList: List<ArticleItem>,
    private val newList: List<ArticleItem>
) : DiffUtil.Callback() {

    override fun getOldListSize(): Int = oldList.size
    override fun getNewListSize(): Int = newList.size

    // 步骤 1：比较是否为同一个数据项（通常比较唯一主键）
    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition].id == newList[newItemPosition].id
    }

    // 步骤 2：比较内容是否完全一致（若一致则无需任何重绘）
    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition] == newList[newItemPosition]
    }

    // 步骤 3：可选，针对局部字段变化提供 Payload 标记
    override fun getChangePayload(oldItemPosition: Int, newItemPosition: Int): Any? {
        val oldItem = oldList[oldItemPosition]
        val newItem = newList[newItemPosition]
        val payload = Bundle()
        if (oldItem.likes != newItem.likes) {
            payload.putInt(PAYLOAD_KEY_LIKES, newItem.likes)
        }
        return if (payload.isEmpty) null else payload
    }

    companion object {
        const val PAYLOAD_KEY_LIKES = "payload_key_likes"
    }
}
