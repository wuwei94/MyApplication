package com.example.william.my.module.arch.mvvm.utils

import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.william.my.module.arch.mvvm.factory.ViewModelFactory

/**
 * Activity 和 Fragment 的 ViewModel 获取扩展函数
 */

fun <T : ViewModel> AppCompatActivity.obtainViewModel(viewModelClass: Class<T>) =
    ViewModelProvider(
        this, ViewModelFactory.getInstance(this.application)
    )[viewModelClass]

fun <T : ViewModel> Fragment.obtainViewModel(viewModelClass: Class<T>) =
    ViewModelProvider(
        this, ViewModelFactory.getInstance(this.requireActivity().application)
    )[viewModelClass]
