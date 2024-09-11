// TaskViewModelFactory.kt
package br.edu.satc.todolistcompose.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import br.edu.satc.todolistcompose.data.AppDatabase

class TaskViewModelFactory(private val database: AppDatabase) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TaskViewModel::class.java)) {
            return TaskViewModel(database) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
