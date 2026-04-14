package com.example.taskmanager.presentation.task

import android.app.DatePickerDialog
import android.util.Log
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.taskmanager.domain.model.Task
import com.example.taskmanager.domain.model.TaskCategory
import com.example.taskmanager.domain.model.TaskPriority
import com.example.taskmanager.domain.model.TaskStatus
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateEditTaskScreen(
    initialTask: Task?,
    onBack: () -> Unit,
    onSave: (Task) -> Unit,
    onDelete: (() -> Unit)? = null
) {
    val normalizedInitialDueDate = initialTask?.dueDate?.let(::normalizeToLocalNoon)
    var title by rememberSaveable { mutableStateOf(initialTask?.title.orEmpty()) }
    var description by rememberSaveable { mutableStateOf(initialTask?.description.orEmpty()) }
    var category by rememberSaveable { mutableStateOf(initialTask?.category ?: TaskCategory.WORK) }
    var priority by rememberSaveable { mutableStateOf(initialTask?.priority ?: TaskPriority.MED) }
    var dueDate by rememberSaveable { mutableStateOf(normalizedInitialDueDate) }
    var status by rememberSaveable { mutableStateOf(initialTask?.status ?: TaskStatus.PENDING) }
    var hasAttemptedSave by rememberSaveable { mutableStateOf(false) }
    val titleError = if (hasAttemptedSave && title.isBlank()) "Title is required" else null
    val dueDateError = if (hasAttemptedSave && dueDate == null) "Due date is required" else null

    val isEditMode = initialTask != null

    Scaffold(
        topBar = {
            CreateEditTaskTopBar(
                isEditMode = isEditMode,
                onBack = onBack,
                onSaveClick = {
                    hasAttemptedSave = true
                    if (title.isNotBlank() && dueDate != null) {
                        Log.d(
                            "CreateEditTaskScreen",
                            "save taskId=${initialTask?.id ?: 0} dueDate=$dueDate mode=${if (isEditMode) "edit" else "create"}"
                        )
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
                }
            )
        }
    ) { innerPadding ->
        val scrollState = rememberScrollState()
        CreateEditTaskContent(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .imePadding()
                .verticalScroll(scrollState)
                .padding(16.dp),
            title = title,
            description = description,
            category = category,
            priority = priority,
            dueDate = dueDate,
            status = status,
            titleError = titleError,
            dueDateError = dueDateError,
            showDelete = isEditMode && onDelete != null,
            onTitleChanged = { title = it },
            onDescriptionChanged = { description = it },
            onCategoryChanged = { category = it },
            onPriorityChanged = { priority = it },
            onDueDateChanged = { dueDate = it },
            onStatusChanged = { status = it },
            onDelete = onDelete
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CreateEditTaskTopBar(
    isEditMode: Boolean,
    onBack: () -> Unit,
    onSaveClick: () -> Unit
) {
    CenterAlignedTopAppBar(
        title = { Text(if (isEditMode) "Edit Task" else "New Task") },
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }
        },
        actions = {
            TextButton(onClick = onSaveClick) { Text("Save") }
        }
    )
}

@Composable
private fun CreateEditTaskContent(
    modifier: Modifier = Modifier,
    title: String,
    description: String,
    category: TaskCategory,
    priority: TaskPriority,
    dueDate: Long?,
    status: TaskStatus,
    titleError: String?,
    dueDateError: String?,
    showDelete: Boolean,
    onTitleChanged: (String) -> Unit,
    onDescriptionChanged: (String) -> Unit,
    onCategoryChanged: (TaskCategory) -> Unit,
    onPriorityChanged: (TaskPriority) -> Unit,
    onDueDateChanged: (Long?) -> Unit,
    onStatusChanged: (TaskStatus) -> Unit,
    onDelete: (() -> Unit)?
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        CreateEditTextFields(
            title = title,
            description = description,
            titleError = titleError,
            onTitleChanged = onTitleChanged,
            onDescriptionChanged = onDescriptionChanged
        )
        CategorySelector(category = category, onCategoryChanged = onCategoryChanged)
        PrioritySelector(priority = priority, onPriorityChanged = onPriorityChanged)
        DueDateField(
            dueDate = dueDate,
            dueDateError = dueDateError,
            onDueDateChanged = onDueDateChanged
        )
        StatusSelector(status = status, onStatusChanged = onStatusChanged)
        if (showDelete && onDelete != null) {
            DeleteTaskButton(onDelete = onDelete)
        }
        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun CreateEditTextFields(
    title: String,
    description: String,
    titleError: String?,
    onTitleChanged: (String) -> Unit,
    onDescriptionChanged: (String) -> Unit
) {
    OutlinedTextField(
        value = title,
        onValueChange = onTitleChanged,
        label = { Text("Title") },
        isError = titleError != null,
        supportingText = { if (titleError != null) Text(titleError) },
        modifier = Modifier.fillMaxWidth()
    )
    OutlinedTextField(
        value = description,
        onValueChange = onDescriptionChanged,
        label = { Text("Description") },
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 50.dp),
        minLines = 3,
        maxLines = 16,
        singleLine = false
    )
}

@Composable
private fun CategorySelector(
    category: TaskCategory,
    onCategoryChanged: (TaskCategory) -> Unit
) {
    Text("Category", style = MaterialTheme.typography.labelLarge)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        TaskCategory.entries.forEach { item ->
            SelectableChip(
                text = item.name.lowercase().replaceFirstChar { c -> c.titlecase() },
                isSelected = category == item,
                onClick = { onCategoryChanged(item) }
            )
        }
    }
}

@Composable
private fun PrioritySelector(
    priority: TaskPriority,
    onPriorityChanged: (TaskPriority) -> Unit
) {
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
                onClick = { onPriorityChanged(item) }
            )
        }
    }
}

@Composable
private fun StatusSelector(
    status: TaskStatus,
    onStatusChanged: (TaskStatus) -> Unit
) {
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
                onClick = { onStatusChanged(item) }
            )
        }
    }
}

@Composable
private fun DueDateField(
    dueDate: Long?,
    dueDateError: String?,
    onDueDateChanged: (Long?) -> Unit
) {
    val context = LocalContext.current
    OutlinedTextField(
        value = dueDate?.let(::formatDate) ?: "",
        onValueChange = {},
        enabled = false,
        label = { Text("Due Date") },
        isError = dueDateError != null,
        supportingText = {
            if (dueDateError != null) {
                Text(
                    text = dueDateError,
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
                        val selectedMillis = normalizeToLocalNoon(year, month, dayOfMonth)
                        onDueDateChanged(selectedMillis)
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
}

@Composable
private fun DeleteTaskButton(onDelete: () -> Unit) {
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

private fun formatDate(millis: Long): String =
    SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(millis))

private fun normalizeToLocalNoon(millis: Long): Long {
    val source = Calendar.getInstance().apply { timeInMillis = millis }
    return normalizeToLocalNoon(
        year = source.get(Calendar.YEAR),
        month = source.get(Calendar.MONTH),
        dayOfMonth = source.get(Calendar.DAY_OF_MONTH)
    )
}

private fun normalizeToLocalNoon(year: Int, month: Int, dayOfMonth: Int): Long {
    val normalized = Calendar.getInstance().apply {
        clear()
        set(year, month, dayOfMonth, 12, 0, 0)
    }.timeInMillis
    return normalized
}
