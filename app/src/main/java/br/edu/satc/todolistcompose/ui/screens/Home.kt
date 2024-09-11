@file:OptIn(ExperimentalMaterial3Api::class)

package br.edu.satc.todolistcompose.ui.screens

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import br.edu.satc.todolistcompose.data.AppDatabase
import br.edu.satc.todolistcompose.data.Task
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

// Componente TaskCard
@Composable
fun TaskCard(
    title: String,
    description: String,
    complete: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    ElevatedCard(
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        modifier = Modifier
            .padding(top = 8.dp)
            .fillMaxWidth()
            .height(100.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(8.dp)
                .fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium
                )
                Checkbox(
                    checked = complete,
                    onCheckedChange = onCheckedChange
                )
            }
            Text(text = description, style = MaterialTheme.typography.bodySmall)
        }
    }
}

// Componente para adicionar nova tarefa
@Composable
fun NewTask(showBottomSheet: Boolean, onComplete: () -> Unit, viewModel: TaskViewModel) {
    val sheetState = rememberModalBottomSheetState()
    val scope = rememberCoroutineScope()
    var taskTitle by remember { mutableStateOf("") }
    var taskDescription by remember { mutableStateOf("") }

    if (showBottomSheet) {
        ModalBottomSheet(
            onDismissRequest = { onComplete() },
            sheetState = sheetState
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                OutlinedTextField(
                    value = taskTitle,
                    onValueChange = { taskTitle = it },
                    label = { Text(text = "Título da tarefa") }
                )
                OutlinedTextField(
                    value = taskDescription,
                    onValueChange = { taskDescription = it },
                    label = { Text(text = "Descrição da tarefa") }
                )
                Button(
                    modifier = Modifier.padding(top = 4.dp),
                    onClick = {
                        scope.launch {
                            viewModel.addTask(Task(title = taskTitle, description = taskDescription, complete = false))
                            sheetState.hide()
                        }.invokeOnCompletion {
                            if (!sheetState.isVisible) {
                                onComplete()
                            }
                        }
                    }
                ) {
                    Text("Salvar")
                }
            }
        }
    }
}

// Componente principal da tela
@Composable
fun HomeScreen(viewModel: TaskViewModel) {
    var showBottomSheet by remember { mutableStateOf(false) }
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(rememberTopAppBarState())
    val tasks by viewModel.tasks.collectAsState(initial = emptyList())

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.primary
                ),
                title = { Text(text = "ToDoList UniSATC") },
                actions = {
                    IconButton(onClick = { /* do something */ }) {
                        Icon(
                            Icons.Rounded.Settings,
                            contentDescription = ""
                        )
                    }
                },
                scrollBehavior = scrollBehavior
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                text = { Text("Nova tarefa") },
                icon = { Icon(Icons.Filled.Add, contentDescription = "") },
                onClick = {
                    showBottomSheet = true
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(horizontal = 4.dp)
                .padding(top = innerPadding.calculateTopPadding())
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.Top
        ) {
            tasks.forEach { task ->
                TaskCard(
                    title = task.title,
                    description = task.description,
                    complete = task.complete,
                    onCheckedChange = { newCheckedState ->
                        viewModel.updateTask(task.copy(complete = newCheckedState))
                    }
                )
            }
        }
        NewTask(showBottomSheet, { showBottomSheet = false }, viewModel)
    }
}

// ViewModel para tarefas
class TaskViewModel(private val database: AppDatabase) : ViewModel() {
    val tasks: Flow<List<Task>> = database.taskDao().getAllTasks()

    fun addTask(task: Task) {
        viewModelScope.launch {
            database.taskDao().insertTask(task)
        }
    }

    fun updateTask(task: Task) {
        viewModelScope.launch {
            database.taskDao().updateTask(task)
        }
    }
}

// Factory para ViewModel
class TaskViewModelFactory(private val database: AppDatabase) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TaskViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return TaskViewModel(database) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

// Função principal para definir o conteúdo da tela
@Composable
fun App() {
    val context = LocalContext.current
    val database = AppDatabase.getDatabase(context)
    val viewModel = ViewModelProvider(
        LocalContext.current as ComponentActivity,
        TaskViewModelFactory(database)
    ).get(TaskViewModel::class.java)

    HomeScreen(viewModel = viewModel)
}
