package com.example.william.my.basic.basic_testing

import com.example.william.my.core.base.timezone.TimeZoneMonitor
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.TimeZone

/**
 * 系统时区监听测试替身（Test Double）
 *
 * 通过 [setTimeZone] 确定性驱动时区切换场景；截图测试可固定 UTC，避免机器默认时区导致断言失败。
 */
class TestTimeZoneMonitor(
    initialTimeZone: TimeZone = TimeZone.getTimeZone("UTC"),
) : TimeZoneMonitor {

    private val timeZoneFlow = MutableStateFlow(initialTimeZone)

    override val currentTimeZone: Flow<TimeZone> = timeZoneFlow.asStateFlow()

    /**
     * 手动更新时区（供单测与截图测试显式触发）
     */
    fun setTimeZone(timeZone: TimeZone) {
        timeZoneFlow.value = timeZone
    }
}
