package com.example.food

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class WorkoutViewModel : ViewModel() {

    private val _plans = MutableLiveData<List<WorkoutPlan>>()
    val plans: LiveData<List<WorkoutPlan>> = _plans

    private val _exercises = MutableLiveData<List<Exercise>>()
    val exercises: LiveData<List<Exercise>> = _exercises

    private val _history = MutableLiveData<List<WorkoutHistory>>()
    val history: LiveData<List<WorkoutHistory>> = _history

    init {
        loadDummyData()
    }

    private fun loadDummyData() {
        _plans.value = listOf(
            WorkoutPlan("Push / Pull / Legs", 12, "60 min"),
            WorkoutPlan("Full Body", 8, "45 min"),
            WorkoutPlan("Beginner Plan", 6, "30 min")
        )

        _exercises.value = listOf(
            Exercise("Bench Press", "Chest", "Standard chest press", "Strength"),
            Exercise("Squats", "Legs", "Barbell back squats", "Strength"),
            Exercise("Deadlift", "Back/Legs", "Traditional deadlift", "Strength"),
            Exercise("Pull Ups", "Back", "Wide grip pull ups", "Bodyweight")
        )
        
        _history.value = emptyList()
    }

    fun setHistory(list: List<WorkoutHistory>) {
        _history.value = list
    }
}