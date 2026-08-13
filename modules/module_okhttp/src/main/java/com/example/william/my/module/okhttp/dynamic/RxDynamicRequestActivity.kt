package com.example.william.my.module.okhttp.dynamic

import com.alibaba.android.arouter.facade.annotation.Route
import com.example.william.my.basic.basic_shared.activity.BasicRecyclerActivity
import com.example.william.my.basic.basic_shared.base.Constants
import com.example.william.my.basic.basic_shared.router.path.RouterPath
import com.example.william.my.core.retrofit.rx.dynamic.RxDynamicRequest
import com.example.william.my.core.retrofit.rx.callback.RxResponseCallback
import com.example.william.my.core.retrofit.exception.ApiException
import com.google.gson.JsonElement
import org.json.JSONObject

/**
 * https://square.github.io/retrofit
 * https://github.com/square/retrofit
 */
@Route(path = RouterPath.OkHttp.Dynamic.RxDynamicRequest)
class RxDynamicRequestActivity : BasicRecyclerActivity() {

    override fun buildList(): ArrayList<String> {
        return arrayListOf(
            "RxDynamicRequest Post postForm",
            "RxDynamicRequest Post postJson",
        )
    }

    override fun onRecyclerClick(position: Int, string: String) {
        super.onRecyclerClick(position, string)
        when (position) {
            0 -> {
                postForm(Constants.Value_Username, Constants.Value_Password)
            }

            1 -> {
                postJson(Constants.Value_Username, Constants.Value_Password)
            }
        }
    }

    private fun postForm(username: String, password: String) {
        val params = mutableMapOf(
            Constants.Key_Username to username,
            Constants.Key_Password to password
        )

        RxDynamicRequest.builder<JsonElement>()
            .api(Constants.Url_Login)
            .addParams(params)
            .post()
            .setProvider(this)
            .buildSingle()
            .subscribe(object : RxResponseCallback<JsonElement>() {
                override fun onResponse(response: JsonElement?) {
                    super.onResponse(response)
                    showResponse(response?.toString())
                }

                override fun onFailure(e: ApiException) {
                    super.onFailure(e)
                    showFailure(e.message)
                }
            })
    }

    private fun postJson(username: String, password: String) {
        val jsonObject = JSONObject()
            .put(Constants.Key_Username, username)
            .put(Constants.Key_Password, password)

        RxDynamicRequest.builder<JsonElement>()
            .api(Constants.Url_Login)
            .addJsonObject(jsonObject)
            .post()
            .setProvider(this)
            .buildSingle()
            .subscribe(object : RxResponseCallback<JsonElement>() {
                override fun onResponse(response: JsonElement?) {
                    super.onResponse(response)
                    showResponse(response?.toString())
                }

                override fun onFailure(e: ApiException) {
                    super.onFailure(e)
                    showFailure(e.message)
                }
            })
    }
}
