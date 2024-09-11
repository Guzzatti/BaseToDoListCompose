package br.edu.satc.todolistcompose.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.edu.satc.todolistcompose.data.AppDatabase
import br.edu.satc.todolistcompose.data.Task
import kotlinx.coroutines.launch

class TaskViewModel(private val database: AppDatabase) : ViewModel() {
    val tasks = database.taskDao().getAllTasks()

    fun addTask(task: Task) = viewModelScope.launch {
        database.taskDao().insertTask(task)
    }

    fun updateTask(task: Task) = viewModelScope.launch {
        database.taskDao().updateTask(task)
    }
}
