package com.example.william.my.module.widget_thirdparty.data

import androidx.annotation.Keep
import com.contrarywind.interfaces.IPickerViewData

@Keep
class ProvinceData(val id: String, val name: String, val cityList: List<CityData>) : IPickerViewData {

    /**
     * 实现IPickerViewData接口，显示在PickerView上面的字符串
     */
    override fun getPickerViewText(): String = name

    @Keep
    class CityData(val id: String, val name: String, val areaList: List<AreaData>?) : IPickerViewData {

        override fun getPickerViewText(): String = name

        @Keep
        data class AreaData(val id: String, val name: String) : IPickerViewData {

            override fun getPickerViewText(): String = name
        }
    }
}
