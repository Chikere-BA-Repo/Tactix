package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.data.TactixRepository
import com.example.ui.util.HapticsController

class TactixViewModelFactory(
    private val repository: TactixRepository,
    private val haptics: HapticsController,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(GameViewModel::class.java) -> GameViewModel(repository, haptics) as T
            modelClass.isAssignableFrom(DailyViewModel::class.java) -> DailyViewModel(repository, haptics) as T
            modelClass.isAssignableFrom(ArchiveViewModel::class.java) -> ArchiveViewModel(repository) as T
            else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}
