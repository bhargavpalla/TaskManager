package com.example.taskmanager.presentation.task

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.taskmanager.data.local.db.DatabaseProvider
import com.example.taskmanager.data.repository.TaskRepositoryImpl
import com.example.taskmanager.domain.model.Task
import com.example.taskmanager.domain.model.TaskCategory
import com.example.taskmanager.domain.model.TaskPriority
import com.example.taskmanager.domain.model.TaskStatus
import com.example.taskmanager.domain.usecase.CreateTaskUseCase
import com.example.taskmanager.domain.usecase.DeleteTaskPermanentlyUseCase
import com.example.taskmanager.domain.usecase.DeleteTaskUseCase
import com.example.taskmanager.domain.usecase.FilterTasksUseCase
import com.example.taskmanager.domain.usecase.GetDeletedTasksUseCase
import com.example.taskmanager.domain.usecase.GetTasksUseCase
import com.example.taskmanager.domain.usecase.GroupTasksUseCase
import com.example.taskmanager.domain.usecase.RestoreTaskUseCase
import com.example.taskmanager.domain.usecase.SearchTasksUseCase
import com.example.taskmanager.domain.usecase.UpdateTaskUseCase

@Composable
fun TaskNavGraph() {
    val navController = rememberNavController()
    val context = LocalContext.current
    val taskViewModel: TaskViewModel = viewModel(factory = rememberTaskViewModelFactory(context))
    val tasksUi by taskViewModel.tasksUi.collectAsState()
    val trashUi by taskViewModel.trashUi.collectAsState()

    NavHost(navController = navController, startDestination = "tasks") {
        composable("tasks") {
            TaskListScreen(
                uiModel = tasksUi,
                onSearchChanged = taskViewModel::onSearchQueryChanged,
                onFilterSelected = taskViewModel::onFilterSelected,
                onTaskClick = { id -> navController.navigate("edit/$id") },
                onCreateClick = { navController.navigate("create") },
                onTrashClick = { navController.navigate("trash") }
            )
        }
        composable("trash") {
            TrashScreen(
                state = trashUi,
                onRestore = taskViewModel::restoreTask,
                onDeleteForever = taskViewModel::deleteTaskPermanently,
                onTasksClick = { navController.navigate("tasks") }
            )
        }
        composable("create") {
            CreateEditTaskScreen(
                initialTask = null,
                onBack = { navController.popBackStack() },
                onSave = {
                    taskViewModel.createTask(it)
                    navController.popBackStack()
                },
                onDelete = null
            )
        }
        composable(
            route = "edit/{taskId}",
            arguments = listOf(navArgument("taskId") { type = NavType.IntType })
        ) { backStackEntry ->
            val taskId = backStackEntry.arguments?.getInt("taskId") ?: 0
            val initialTask = (tasksUi.state as? UiState.Success)?.data?.firstOrNull { it.id == taskId }
            CreateEditTaskScreen(
                initialTask = initialTask,
                onBack = { navController.popBackStack() },
                onSave = {
                    taskViewModel.updateTask(it)
                    navController.popBackStack()
                },
                onDelete = {
                    taskViewModel.deleteTask(taskId)
                    navController.popBackStack()
                }
            )
        }
    }

}

@Composable
private fun rememberTaskViewModelFactory(context: android.content.Context): ViewModelProvider.Factory {
    return object : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            val repository = TaskRepositoryImpl(DatabaseProvider.getDatabase(context).taskDao())
            val getTasksUseCase = GetTasksUseCase(repository)
            return TaskViewModel(
                createTaskUseCase = CreateTaskUseCase(repository),
                updateTaskUseCase = UpdateTaskUseCase(repository),
                deleteTaskUseCase = DeleteTaskUseCase(repository),
                restoreTaskUseCase = RestoreTaskUseCase(repository),
                deleteTaskPermanentlyUseCase = DeleteTaskPermanentlyUseCase(repository),
                searchTasksUseCase = SearchTasksUseCase(repository, getTasksUseCase),
                getDeletedTasksUseCase = GetDeletedTasksUseCase(repository),
                filterTasksUseCase = FilterTasksUseCase(),
                groupTasksUseCase = GroupTasksUseCase()
            ) as T
        }
    }
}
