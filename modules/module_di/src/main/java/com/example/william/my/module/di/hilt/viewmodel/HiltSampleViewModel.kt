package com.example.william.my.module.di.hilt.viewmodel

import androidx.lifecycle.ViewModel
import com.example.william.my.module.di.hilt.model.HiltUserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

/**
 * 演示 Hilt 与 Jetpack ViewModel 集成
 */
@HiltViewModel
class HiltSampleViewModel @Inject constructor(
    private val userRepository: HiltUserRepository,
) : ViewModel() {

    private var counter = 0

    fun incrementAndGet(): Int {
        counter++
        return counter
    }

    fun getViewModelInfo(): String = "HiltViewModel: ${userRepository.getUserInfo(888)}"
}
