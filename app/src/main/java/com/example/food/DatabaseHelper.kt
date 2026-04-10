package com.example.food

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import java.text.SimpleDateFormat
import java.util.*

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, "FoodTracker.db", null, 6) {

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("CREATE TABLE user_profile (id INTEGER PRIMARY KEY, age INTEGER, weight REAL, height INTEGER, gender TEXT, goal TEXT)")
        db.execSQL("CREATE TABLE meals (id INTEGER PRIMARY KEY AUTOINCREMENT, meal_name TEXT, calories INTEGER, protein REAL, carbs REAL, fat REAL, fiber REAL, meal_type TEXT, date TEXT)")
        db.execSQL("CREATE TABLE water (id INTEGER PRIMARY KEY AUTOINCREMENT, ml INTEGER, date TEXT)")
        db.execSQL("CREATE TABLE activity (id INTEGER PRIMARY KEY AUTOINCREMENT, steps INTEGER, workout_minutes INTEGER, date TEXT UNIQUE)")
        db.execSQL("CREATE TABLE workout_history (id INTEGER PRIMARY KEY AUTOINCREMENT, workout_type TEXT, duration_min INTEGER, calories_burned INTEGER, date TEXT)")
    }

    override fun onUpgrade(db: SQLiteDatabase, old: Int, new: Int) {
        if (old < 4) {
            db.execSQL("CREATE TABLE IF NOT EXISTS activity (id INTEGER PRIMARY KEY AUTOINCREMENT, steps INTEGER, workout_minutes INTEGER, date TEXT UNIQUE)")
        }
        if (old < 5) {
            db.execSQL("CREATE TABLE IF NOT EXISTS workout_history (id INTEGER PRIMARY KEY AUTOINCREMENT, workout_type TEXT, duration_min INTEGER, calories_burned INTEGER, date TEXT)")
        }
        if (old < 6) {
            // Upgrade meals table to include fiber and meal_type
            db.execSQL("DROP TABLE IF EXISTS meals")
            db.execSQL("CREATE TABLE meals (id INTEGER PRIMARY KEY AUTOINCREMENT, meal_name TEXT, calories INTEGER, protein REAL, carbs REAL, fat REAL, fiber REAL, meal_type TEXT, date TEXT)")
        }
    }

    // --- User Profile ---
    fun saveProfile(age: Int, weight: Double, height: Int, gender: String, goal: String) {
        val db = writableDatabase
        val values = ContentValues().apply {
            put("id", 1); put("age", age); put("weight", weight); put("height", height); put("gender", gender); put("goal", goal)
        }
        db.insertWithOnConflict("user_profile", null, values, SQLiteDatabase.CONFLICT_REPLACE)
    }

    fun getProfile(): Cursor? = readableDatabase.rawQuery("SELECT * FROM user_profile WHERE id=1", null)

    // --- Meals ---
    fun addMeal(name: String, cal: Int, pro: Double, carb: Double, fat: Double, fib: Double, type: String) {
        val date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val values = ContentValues().apply {
            put("meal_name", name)
            put("calories", cal)
            put("protein", pro)
            put("carbs", carb)
            put("fat", fat)
            put("fiber", fib)
            put("meal_type", type)
            put("date", date)
        }
        writableDatabase.insert("meals", null, values)
    }

    fun getTodayMeals(): Cursor? {
        val date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        return readableDatabase.rawQuery("SELECT * FROM meals WHERE date=?", arrayOf(date))
    }

    fun getWeeklyCalories(): Cursor? {
        return readableDatabase.rawQuery("SELECT date, SUM(calories) FROM meals GROUP BY date ORDER BY date DESC LIMIT 7", null)
    }

    fun deleteMeal(id: Int) = writableDatabase.delete("meals", "id=?", arrayOf(id.toString()))

    // --- Water ---
    fun addWater(ml: Int) {
        val date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val values = ContentValues().apply { put("ml", ml); put("date", date) }
        writableDatabase.insert("water", null, values)
    }

    fun getTodayWater(): Int {
        val date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val cursor = readableDatabase.rawQuery("SELECT SUM(ml) FROM water WHERE date=?", arrayOf(date))
        var total = 0
        if (cursor.moveToFirst()) total = cursor.getInt(0)
        cursor.close()
        return total
    }

    // --- Activity & Streak ---
    fun updateActivity(steps: Int, workoutMin: Int) {
        val date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val db = writableDatabase
        val values = ContentValues().apply {
            put("date", date)
            put("steps", steps)
            put("workout_minutes", workoutMin)
        }
        db.insertWithOnConflict("activity", null, values, SQLiteDatabase.CONFLICT_REPLACE)
    }

    fun getTodayActivity(): Cursor? {
        val date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        return readableDatabase.rawQuery("SELECT * FROM activity WHERE date=?", arrayOf(date))
    }

    fun getStreak(): Int {
        val cursor = readableDatabase.rawQuery("SELECT date FROM (SELECT date FROM meals UNION SELECT date FROM activity UNION SELECT date FROM workout_history) ORDER BY date DESC", null)
        var streak = 0
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        
        if (cursor.moveToFirst()) {
            var lastDateStr = cursor.getString(0)
            var lastDate = try { sdf.parse(lastDateStr) } catch(e: Exception) { null }
            
            if (lastDate != null) {
                val todayStr = sdf.format(Date())
                val today = sdf.parse(todayStr)
                val diff = (today.time - lastDate.time) / (1000 * 60 * 60 * 24)
                
                if (diff <= 1) {
                    streak = 1
                    while (cursor.moveToNext()) {
                        val currentStr = cursor.getString(0)
                        val current = try { sdf.parse(currentStr) } catch(e: Exception) { null } ?: break
                        val dayDiff = (lastDate!!.time - current.time) / (1000 * 60 * 60 * 24)
                        if (dayDiff == 1L) {
                            streak++
                            lastDate = current
                        } else if (dayDiff > 1L) {
                            break
                        }
                    }
                }
            }
        }
        cursor.close()
        return streak
    }

    // --- Workouts ---
    fun logWorkout(type: String, duration: Int, calories: Int) {
        val date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val values = ContentValues().apply {
            put("workout_type", type)
            put("duration_min", duration)
            put("calories_burned", calories)
            put("date", date)
        }
        writableDatabase.insert("workout_history", null, values)
        updateActivity(0, duration) 
    }

    fun getWorkoutHistory(): Cursor? {
        return readableDatabase.rawQuery("SELECT * FROM workout_history ORDER BY date DESC", null)
    }
}