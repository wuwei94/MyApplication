
@file:Suppress("ktlint:standard:value-parameter-comment")

package com.example.william.my.module.database.objectbox

import io.objectbox.annotation.Entity
import io.objectbox.annotation.Id
import java.util.Date

@Entity
data class ObjectBoxNote(

    // @Id(assignable = true) // 可分配
    @Id
    var id: Long = 0,

    var text: String? = null,

    var date: Date = Date(),
)
