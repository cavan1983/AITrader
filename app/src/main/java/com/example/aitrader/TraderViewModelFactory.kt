package com.example.aitrader

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class TraderViewModelFactory(
    private val repository: TraderRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TraderViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return TraderViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}