package com.example.taskmanager.presentation.task

import android.app.DatePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.example.taskmanager.domain.model.Task
import com.example.taskmanager.domain.model.TaskCategory
import com.example.taskmanager.domain.model.TaskPriority
import com.example.taskmanager.domain.model.TaskStatus
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import androidx.compose.runtime.getValue
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskListScreen(
    uiModel: TaskListUiModel,
    onSearchChanged: (String) -> Unit,
    onFilterSelected: (TaskFilter) -> Unit,
    onTaskClick: (Int) -> Unit,
    onCreateClick: () -> Unit,
    onTrashClick: () -> Unit
) {
    Scaffold(
        topBar = { TaskListTopBar() },
        floatingActionButton = { TaskListFab(onCreateClick = onCreateClick) },
        bottomBar = { TaskListBottomBar(onTrashClick = onTrashClick) }
    ) { innerPadding ->
        TaskListMainColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            query = uiModel.query,
            selectedFilter = uiModel.selectedFilter,
            state = uiModel.state,
            todayTasks = uiModel.todayTasks,
            thisWeekTasks = uiModel.thisWeekTasks,
            laterTasks = uiModel.laterTasks,
            overdueTasks = uiModel.overdueTasks,
            onSearchChanged = onSearchChanged,
            onFilterSelected = onFilterSelected,
            onTaskClick = onTaskClick
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TaskListTopBar() {
    CenterAlignedTopAppBar(title = { Text("My Tasks") })
}

@Composable
private fun TaskListFab(onCreateClick: () -> Unit) {
    FloatingActionButton(onClick = onCreateClick) {
        Icon(Icons.Default.Add, contentDescription = "Create task")
    }
}

@Composable
private fun TaskListBottomBar(onTrashClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        TextButton(onClick = {}) { Text("Tasks") }
        TextButton(onClick = onTrashClick) { Text("Trash") }
    }
}

@Composable
private fun TaskListMainColumn(
    modifier: Modifier = Modifier,
    query: String,
    selectedFilter: TaskFilter,
    state: UiState<List<Task>>,
    todayTasks: List<Task>,
    thisWeekTasks: List<Task>,
    laterTasks: List<Task>,
    overdueTasks: List<Task>,
    onSearchChanged: (String) -> Unit,
    onFilterSelected: (TaskFilter) -> Unit,
    onTaskClick: (Int) -> Unit
) {
    Column(modifier = modifier) {
        TaskSearchBar(query = query, onSearchChanged = onSearchChanged)
        Spacer(modifier = Modifier.height(12.dp))
        TaskListFilterChips(
            selectedFilter = selectedFilter,
            onFilterSelected = onFilterSelected
        )
        Spacer(modifier = Modifier.height(12.dp))
        TaskListStateBody(
            state = state,
            todayTasks = todayTasks,
            thisWeekTasks = thisWeekTasks,
            laterTasks = laterTasks,
            overdueTasks = overdueTasks,
            onTaskClick = onTaskClick
        )
    }
}

@Composable
private fun TaskSearchBar(
    query: String,
    onSearchChanged: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = query,
        onValueChange = onSearchChanged,
        modifier = modifier.fillMaxWidth(),
        placeholder = { Text("Search tasks...") },
        singleLine = true
    )
}

@Composable
private fun TaskListStateBody(
    state: UiState<List<Task>>,
    todayTasks: List<Task>,
    thisWeekTasks: List<Task>,
    laterTasks: List<Task>,
    overdueTasks: List<Task>,
    onTaskClick: (Int) -> Unit
) {
    when (state) {
        UiState.Loading -> Text("Loading...", modifier = Modifier.alpha(0.7f))
        is UiState.Error -> Text(state.message, color = MaterialTheme.colorScheme.error)
        UiState.Empty -> Text("No tasks found.")
        is UiState.Success -> {
            TaskListLazySections(
                todayTasks = todayTasks,
                thisWeekTasks = thisWeekTasks,
                laterTasks = laterTasks,
                overdueTasks = overdueTasks,
                onTaskClick = onTaskClick
            )
        }
    }
}

@Composable
private fun TaskListLazySections(
    todayTasks: List<Task>,
    thisWeekTasks: List<Task>,
    laterTasks: List<Task>,
    overdueTasks: List<Task>,
    onTaskClick: (Int) -> Unit
) {
    LazyColumn(
        contentPadding = PaddingValues(bottom = 88.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        if (todayTasks.isNotEmpty()) {
            item(key = "header_today") {
                Text("Today", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
            items(todayTasks, key = { "today_${it.id}" }) { task ->
                TaskItem(task = task, onClick = { onTaskClick(task.id) })
            }
        }
        if (thisWeekTasks.isNotEmpty()) {
            item(key = "header_this_week") {
                Text("This Week", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
            items(thisWeekTasks, key = { "thisweek_${it.id}" }) { task ->
                TaskItem(task = task, onClick = { onTaskClick(task.id) })
            }
        }
        if (laterTasks.isNotEmpty()) {
            item(key = "header_later") {
                Text("Later", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
            items(laterTasks, key = { "later_${it.id}" }) { task ->
                TaskItem(task = task, onClick = { onTaskClick(task.id) })
            }
        }
        if (overdueTasks.isNotEmpty()) {
            item(key = "header_overdue") {
                Text(
                    "Overdue",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.error
                )
            }
            items(overdueTasks, key = { "overdue_${it.id}" }) { task ->
                TaskItem(task = task, isOverdue = true, onClick = { onTaskClick(task.id) })
            }
        }
    }
}

@Composable
private fun TaskListFilterChips(
    selectedFilter: TaskFilter,
    onFilterSelected: (TaskFilter) -> Unit
) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        TaskFilter.entries.forEach { filter ->
            val isSelected = selectedFilter == filter
            AssistChip(
                onClick = { onFilterSelected(filter) },
                label = { Text(filter.name.lowercase().replaceFirstChar { it.titlecase() }) },
                colors = if (isSelected) {
                    AssistChipDefaults.assistChipColors(
                        containerColor = Color.Black,
                        labelColor = Color.White
                    )
                } else {
                    AssistChipDefaults.assistChipColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                        labelColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f)
                    )
                },
                modifier = if (isSelected) Modifier else Modifier.alpha(0.95f)
            )
        }
    }
}

@Composable
fun TaskItem(task: Task, isOverdue: Boolean = false, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(task.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    if (isOverdue) {
                        Text(
                            text = "OVERDUE",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onError,
                            modifier = Modifier
                                .background(MaterialTheme.colorScheme.error, shape = MaterialTheme.shapes.extraSmall)
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "${task.category.name.lowercase().replaceFirstChar { it.titlecase() }} • Due ${formatDate(task.dueDate)} • ${task.status.name}",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isOverdue) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            PriorityBadge(task.priority)
        }
    }
}

@Composable
private fun PriorityBadge(priority: TaskPriority) {
    val color = when (priority) {
        TaskPriority.HIGH -> Color(0xFFE53935)
        TaskPriority.MED -> Color(0xFFFFA000)
        TaskPriority.LOW -> Color(0xFF43A047)
    }
    Text(
        text = priority.name,
        color = Color.White,
        modifier = Modifier
            .background(color, shape = MaterialTheme.shapes.small)
            .padding(horizontal = 8.dp, vertical = 4.dp),
        style = MaterialTheme.typography.labelSmall
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateEditTaskScreen(
    initialTask: Task?,
    onBack: () -> Unit,
    onSave: (Task) -> Unit,
    onDelete: (() -> Unit)? = null
) {
    val context = LocalContext.current
    var title by rememberSaveable { mutableStateOf(initialTask?.title.orEmpty()) }
    var description by rememberSaveable { mutableStateOf(initialTask?.description.orEmpty()) }
    var category by rememberSaveable { mutableStateOf(initialTask?.category ?: TaskCategory.WORK) }
    var priority by rememberSaveable { mutableStateOf(initialTask?.priority ?: TaskPriority.MED) }
    var dueDate by rememberSaveable { mutableStateOf(initialTask?.dueDate) }
    var status by rememberSaveable { mutableStateOf(initialTask?.status ?: TaskStatus.PENDING) }
    var hasAttemptedSave by rememberSaveable { mutableStateOf(false) }
    val titleError = if (hasAttemptedSave && title.isBlank()) "Title is required" else null
    val dueDateError = if (hasAttemptedSave && dueDate == null) "Due date is required" else null

    val isEditMode = initialTask != null

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(if (isEditMode) "Edit Task" else "New Task") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    TextButton(onClick = {
                        hasAttemptedSave = true
                        if (title.isNotBlank() && dueDate != null) {
                            onSave(
                                Task(
                                    id = initialTask?.id ?: 0,
                                    title = title.trim(),
                                    description = description.trim(),
                                    category = category,
                                    status = status,
                                    dueDate = dueDate!!,
                                    priority = priority,
                                    createdAt = initialTask?.createdAt ?: System.currentTimeMillis(),
                                    updatedAt = System.currentTimeMillis(),
                                    isDeleted = initialTask?.isDeleted ?: false,
                                    deletedAt = initialTask?.deletedAt
                                )
                            )
                        }
                    }) { Text("Save") }
                }
            )
        }
    ) { innerPadding ->
        val scrollState = rememberScrollState()
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .imePadding()
                .verticalScroll(scrollState)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Title") },
                isError = titleError != null,
                supportingText = { if (titleError != null) Text(titleError!!) },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Description") },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 50.dp),
                minLines = 3,
                maxLines = 16,
                singleLine = false
            )

            Text("Category", style = MaterialTheme.typography.labelLarge)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TaskCategory.entries.forEach {
                    SelectableChip(
                        text = it.name.lowercase().replaceFirstChar { c -> c.titlecase() },
                        isSelected = category == it,
                        onClick = { category = it }
                    )
                }
            }

            Text("Priority", style = MaterialTheme.typography.labelLarge)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TaskPriority.entries.forEach { item ->
                    SelectableChip(
                        text = item.name,
                        isSelected = priority == item,
                        onClick = { priority = item }
                    )
                }
            }

            OutlinedTextField(
                value = dueDate?.let(::formatDate) ?: "",
                onValueChange = {},
                enabled = false,
                label = { Text("Due Date") },
                isError = dueDateError != null,
                supportingText = {
                    if (dueDateError != null) {
                        Text(
                            text = dueDateError!!,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                },
                trailingIcon = {
                    TextButton(onClick = {
                        val now = Calendar.getInstance()
                        val initial = Calendar.getInstance().apply {
                            timeInMillis = dueDate ?: now.timeInMillis
                        }
                        DatePickerDialog(
                            context,
                            { _, year, month, dayOfMonth ->
                                  val selected = Calendar.getInstance().apply {
                                clear()
                                set(year, month, dayOfMonth, 12, 0, 0) // safer than midnight
                            }
                                dueDate = selected.timeInMillis
                            },
                            initial.get(Calendar.YEAR),
                            initial.get(Calendar.MONTH),
                            initial.get(Calendar.DAY_OF_MONTH)
                        ).apply {
                            datePicker.minDate = now.apply {
                                set(Calendar.HOUR_OF_DAY, 0)
                                set(Calendar.MINUTE, 0)
                                set(Calendar.SECOND, 0)
                                set(Calendar.MILLISECOND, 0)
                            }.timeInMillis
                        }.show()
                    }) { Text("Pick") }
                },
                modifier = Modifier.fillMaxWidth()
            )

            Text("Status", style = MaterialTheme.typography.labelLarge)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TaskStatus.entries.forEach { item ->
                    SelectableChip(
                        text = item.name.lowercase().replaceFirstChar { it.titlecase() },
                        isSelected = status == item,
                        onClick = { status = item }
                    )
                }
            }

            if (isEditMode && onDelete != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = onDelete,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = MaterialTheme.colorScheme.onError
                    )
                ) {
                    Text("Delete Task")
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun SelectableChip(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    AssistChip(
        onClick = onClick,
        label = { Text(text) },
        colors = if (isSelected) {
            AssistChipDefaults.assistChipColors(
                containerColor = Color.Black,
                labelColor = Color.White
            )
        } else {
            AssistChipDefaults.assistChipColors(
                containerColor = MaterialTheme.colorScheme.surface,
                labelColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f)
            )
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrashScreen(
    state: UiState<List<Task>>,
    onRestore: (Int) -> Unit,
    onDeleteForever: (Int) -> Unit,
    onTasksClick: () -> Unit
) {
    Scaffold(
        topBar = { CenterAlignedTopAppBar(title = { Text("Trash") }) },
        bottomBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                TextButton(onClick = onTasksClick) { Text("Tasks") }
                TextButton(onClick = {}) { Text("Trash") }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            Text(
                "Auto-deleted after 7 days",
                color = MaterialTheme.colorScheme.tertiary,
                style = MaterialTheme.typography.labelLarge
            )
            Spacer(modifier = Modifier.height(12.dp))
            when (state) {
                UiState.Loading -> Text("Loading...")
                UiState.Empty -> Text("Trash is empty.")
                is UiState.Error -> Text(state.message, color = MaterialTheme.colorScheme.error)
                is UiState.Success -> LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(state.data, key = { it.id }) { task ->
                        TrashItem(task = task, onRestore = { onRestore(task.id) }, onDeleteForever = { onDeleteForever(task.id) })
                    }
                }
            }
        }
    }
}

@Composable
private fun TrashItem(task: Task, onRestore: () -> Unit, onDeleteForever: () -> Unit) {
    val deletedAt = task.deletedAt ?: System.currentTimeMillis()
    val ageDays = ((System.currentTimeMillis() - deletedAt) / (24 * 60 * 60 * 1000)).toInt().coerceAtLeast(0)
    val daysLeft = (7 - ageDays).coerceAtLeast(0)

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(task.title, textDecoration = TextDecoration.LineThrough, fontWeight = FontWeight.SemiBold)
            Text("Deleted $ageDays day(s) ago")
            AssistChip(onClick = {}, label = { Text("$daysLeft day(s) left") })
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = onRestore) { Text("Restore") }
                Button(onClick = onDeleteForever) {
                    Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.size(4.dp))
                    Text("Delete")
                }
            }
        }
    }
}

private fun formatDate(millis: Long): String =
    SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(millis))
