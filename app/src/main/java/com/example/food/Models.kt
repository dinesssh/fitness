package com.example.food

data class Exercise(
    val name: String,
    val targetMuscle: String,
    val description: String,
    val category: String
)

data class WorkoutPlan(
    val title: String,
    val exerciseCount: Int,
    val estimatedDuration: String
)

data class WorkoutHistory(
    val type: String,
    val duration: Int,
    val calories: Int,
    val date: String
)