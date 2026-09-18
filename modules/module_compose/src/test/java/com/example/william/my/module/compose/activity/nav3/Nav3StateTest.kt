package com.example.william.my.module.compose.activity.nav3

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * [Nav3State] 单元测试。
 *
 * 验证 Navigation 3 架构中基于纯 Compose 状态提升的返回栈行为：
 * - 栈顶导航推入 (push)
 * - 栈顶受控弹出 (pop)
 * - 栈底根节点重置 (popToRoot)
 * - 导航方向指示标记 (navDirection)
 */
class Nav3StateTest {

    @Test
    fun nav3State_initialState_containsHomeKey() {
        val state = Nav3State(DemoNavKey.Home)

        assertEquals(1, state.backStack.size)
        assertEquals(DemoNavKey.Home, state.currentKey)
        assertEquals(1, state.navDirection)
    }

    @Test
    fun nav3State_pushKey_updatesBackStackAndDirection() {
        val state = Nav3State(DemoNavKey.Home)
        val topic = DemoNavKey.TopicDetail("T01", "Kotlin", "Modern PL")

        state.push(topic)

        assertEquals(2, state.backStack.size)
        assertEquals(topic, state.currentKey)
        assertEquals(1, state.navDirection)
    }

    @Test
    fun nav3State_popKey_removesTopEntryAndDirectionIsBackward() {
        val state = Nav3State(DemoNavKey.Home)
        val topic = DemoNavKey.TopicDetail("T01", "Kotlin", "Modern PL")
        state.push(topic)

        val popped = state.pop()

        assertTrue(popped)
        assertEquals(1, state.backStack.size)
        assertEquals(DemoNavKey.Home, state.currentKey)
        assertEquals(-1, state.navDirection)

        // 仅剩 1 个根元素时无法继续 pop
        val secondPop = state.pop()
        assertFalse(secondPop)
        assertEquals(1, state.backStack.size)
    }

    @Test
    fun nav3State_popToRoot_clearsStackToSingleRoot() {
        val state = Nav3State(DemoNavKey.Home)
        state.push(DemoNavKey.TopicDetail("T01", "Kotlin", "Modern PL"))
        state.push(DemoNavKey.UserProfile("U01", "William", "Engineer"))
        state.push(DemoNavKey.Settings)

        assertEquals(4, state.backStack.size)

        state.popToRoot()

        assertEquals(1, state.backStack.size)
        assertEquals(DemoNavKey.Home, state.currentKey)
        assertEquals(-1, state.navDirection)
    }
}
