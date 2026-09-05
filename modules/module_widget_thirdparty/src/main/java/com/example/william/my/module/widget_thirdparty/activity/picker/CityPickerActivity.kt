package com.example.william.my.module.widget_thirdparty.activity.picker

import android.os.Bundle
import com.alibaba.android.arouter.facade.annotation.Route
import com.example.william.my.basic.basic_shared.activity.BasicResponseActivity
import com.example.william.my.basic.basic_shared.router.path.RouterPath
import com.zaaach.citypicker.CityPicker
import com.zaaach.citypicker.R
import com.zaaach.citypicker.adapter.OnPickListener
import com.zaaach.citypicker.model.City
import com.zaaach.citypicker.model.LocateState
import com.zaaach.citypicker.model.LocatedCity

/**
 * CityPicker — 城市选择器
 *
 * CityPicker 是一个漂亮的城市选择器，支持城市搜索和定位功能。
 *
 * 核心特性：
 * 1. 美观的 UI：Material Design 风格，动画流畅
 * 2. 城市搜索：支持城市名称搜索，快速定位
 * 3. 定位功能：支持 GPS 定位当前城市
 * 4. 热门城市：支持自定义热门城市列表
 *
 * 基本用法：
 * ```kotlin
 * CityPicker.from(context)
 *     .enableAnimation(true)
 *     .setLocatedCity(null)
 *     .setOnPickListener(object : OnPickListener {
 *         override fun onPick(position: Int, data: City) {
 *             // 处理选择结果
 *         }
 *         override fun onCancel() {}
 *         override fun onLocate() {
 *             // 开始定位
 *         }
 *     })
 *     .show()
 * ```
 *
 * 适用场景：
 * - 切换城市、选择收货地址
 * - 城市列表选择
 * - 需要定位功能的场景
 *
 * https://github.com/zaaach/CityPicker
 */
@Route(path = RouterPath.WidgetThirdparty.CityPicker)
class CityPickerActivity : BasicResponseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.DefaultCityPickerTheme)
        super.onCreate(savedInstanceState)
    }

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        showDescription("点击下方列表项打开城市选择器")
    }

    override fun buildList(): ArrayList<String> = arrayListOf("打开 CityPicker 城市选择器")

    override fun onRecyclerClick(position: Int, string: String) {
        super.onRecyclerClick(position, string)
        if (position == 0) {
            showCityPicker()
        }
    }

    private fun showCityPicker() {
        CityPicker.from(this@CityPickerActivity)
            .enableAnimation(true) // 启用动画
            // .setAnimationStyle(anim)//自定义动画
            .setLocatedCity(null) // 定位城市
            // .setHotCities(hotCities)//指定热门城市
            .setOnPickListener(object : OnPickListener {
                override fun onPick(position: Int, data: City) {
                    appendLog("选中城市: ${data.name}，城市代码: ${data.code}")
                }

                override fun onCancel() {
                    appendLog("取消选择城市")
                }

                override fun onLocate() {
                    // 开始定位，这里模拟一下定位
                    runOnUiThread(
                        Runnable {
                            // 定位完成之后更新数据
                            CityPicker.from(this@CityPickerActivity).locateComplete(
                                LocatedCity("深圳", "广东", "101280601"),
                                LocateState.SUCCESS,
                            )
                        },
                    )
                }
            })
            .show()
        appendLog("打开城市选择器")
    }
}
