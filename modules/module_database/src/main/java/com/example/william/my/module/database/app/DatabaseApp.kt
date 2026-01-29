package com.example.william.my.module.database.app

import com.example.william.my.lib.app.BaseAppInit
import com.example.william.my.module.database.objectbox.ObjectBox

class DatabaseApp : BaseAppInit() {

    override fun init() {
        super.init()

        ObjectBox.init(app)
    }
}