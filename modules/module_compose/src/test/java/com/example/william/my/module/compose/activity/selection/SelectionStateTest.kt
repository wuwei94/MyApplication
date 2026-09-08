package com.example.william.my.module.compose.activity.selection

import androidx.compose.ui.state.ToggleableState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * [TriStateSelectionState] 三态复选框联动状态机单元测试。
 *
 * 遵循测试命名规范：`被测对象_场景_预期结果`。
 */
class SelectionStateTest {

    @Test
    fun selectionState_initiallyEmpty_returnsOff() {
        val state = TriStateSelectionState(3)
        assertEquals(ToggleableState.Off, state.parentState)
        assertTrue(state.items.all { !it })
    }

    @Test
    fun selectionState_toggleParentFromOff_turnsAllOn() {
        val state = TriStateSelectionState(3)
        state.toggleParent()

        assertEquals(ToggleableState.On, state.parentState)
        assertTrue(state.items.all { it })
    }

    @Test
    fun selectionState_toggleOneChild_returnsIndeterminate() {
        val state = TriStateSelectionState(3)
        state.toggleChild(0)

        assertEquals(ToggleableState.Indeterminate, state.parentState)
        assertTrue(state.items[0])
        assertFalse(state.items[1])
        assertFalse(state.items[2])
    }

    @Test
    fun selectionState_toggleParentFromIndeterminate_turnsAllOn() {
        val state = TriStateSelectionState(3)
        state.toggleChild(1)
        assertEquals(ToggleableState.Indeterminate, state.parentState)

        state.toggleParent()
        assertEquals(ToggleableState.On, state.parentState)
        assertTrue(state.items.all { it })
    }

    @Test
    fun selectionState_toggleAllChildrenToOn_returnsOn() {
        val state = TriStateSelectionState(2)
        state.toggleChild(0)
        state.toggleChild(1)

        assertEquals(ToggleableState.On, state.parentState)
    }
}
