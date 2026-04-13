package com.example.taskmanager.data.local.entity

enum class TaskCategory {
    WORK,
    PERSONAL,
    SHOPPING
}

enum class TaskStatus {
    PENDING,
    INPROGRESS,
    COMPLETED
}

enum class TaskPriority(val rank: Int) {
    HIGH(3),
    MED(2),
    LOW(1)
}
