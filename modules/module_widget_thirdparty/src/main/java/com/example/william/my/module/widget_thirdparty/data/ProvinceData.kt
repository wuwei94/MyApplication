package com.example.william.my.module.widget_thirdparty.data

import androidx.annotation.Keep
import com.contrarywind.interfaces.IPickerViewData

/**
 * 省份数据
 *
 * 城市选择器的省份数据模型。
 */
@Keep
class ProvinceData(val id: String, val name: String, val cityList: List<CityData>) : IPickerViewData {

    /**
     * 实现IPickerViewData接口，显示在PickerView上面的字符串
     */
    override fun getPickerViewText(): String = name

    /**
     * 城市数据
     *
     * 城市选择器的城市数据模型。
     */
    @Keep
    class CityData(val id: String, val name: String, val areaList: List<AreaData>?) : IPickerViewData {

        override fun getPickerViewText(): String = name

        @Keep
        data class AreaData(val id: String, val name: String) : IPickerViewData {

            override fun getPickerViewText(): String = name
        }
    }
}
