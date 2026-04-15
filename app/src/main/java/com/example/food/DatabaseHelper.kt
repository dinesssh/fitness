package com.example.food

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import java.text.SimpleDateFormat
import java.util.*

class DatabaseHelper(context: Context) :
    SQLiteOpenHelper(context, "FoodTracker.db", null, 7) {

    override fun onCreate(db: SQLiteDatabase) {

        // User Profile Table
        db.execSQL("""
            CREATE TABLE user_profile (
                id INTEGER PRIMARY KEY,
                age INTEGER,
                weight REAL,
                height INTEGER,
                gender TEXT,
                goal TEXT
            )
        """)

        // Meals Table
        db.execSQL("""
            CREATE TABLE meals (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                meal_name TEXT,
                calories INTEGER,
                protein REAL,
                carbs REAL,
                fat REAL,
                fiber REAL,
                meal_type TEXT,
                date TEXT,
                image BLOB
            )
        """)

        // Water Table
        db.execSQL("""
            CREATE TABLE water (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                ml INTEGER,
                date TEXT
            )
        """)

        // Activity Table
        db.execSQL("""
            CREATE TABLE activity (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                steps INTEGER,
                workout_minutes INTEGER,
                date TEXT UNIQUE
            )
        """)

        // Workout History
        db.execSQL("""
            CREATE TABLE workout_history (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                workout_type TEXT,
                duration_min INTEGER,
                calories_burned INTEGER,
                date TEXT
            )
        """)
    }

    override fun onUpgrade(db: SQLiteDatabase, old: Int, new: Int) {
        db.execSQL("DROP TABLE IF EXISTS user_profile")
        db.execSQL("DROP TABLE IF EXISTS meals")
        db.execSQL("DROP TABLE IF EXISTS water")
        db.execSQL("DROP TABLE IF EXISTS activity")
        db.execSQL("DROP TABLE IF EXISTS workout_history")
        onCreate(db)
    }

    // ---------------- USER PROFILE ----------------

    fun saveProfile(
        age: Int,
        weight: Double,
        height: Int,
        gender: String,
        goal: String
    ): Long {

        val db = writableDatabase

        val values = ContentValues().apply {
            put("id", 1)
            put("age", age)
            put("weight", weight)
            put("height", height)
            put("gender", gender)
            put("goal", goal)
        }

        val result = db.insertWithOnConflict(
            "user_profile",
            null,
            values,
            SQLiteDatabase.CONFLICT_REPLACE
        )

        db.close()
        return result
    }

    fun getProfile(): Cursor? {
        return readableDatabase.rawQuery(
            "SELECT * FROM user_profile WHERE id=1",
            null
        )
    }

    // ---------------- MEALS ----------------

    fun addMeal(
        name: String,
        cal: Int,
        pro: Double,
        carb: Double,
        fat: Double,
        fib: Double,
        type: String,
        image: ByteArray? = null
    ) {
        val date = getTodayDate()

        val values = ContentValues().apply {
            put("meal_name", name)
            put("calories", cal)
            put("protein", pro)
            put("carbs", carb)
            put("fat", fat)
            put("fiber", fib)
            put("meal_type", type)
            put("date", date)
            put("image", image)
        }

        writableDatabase.insert("meals", null, values)
    }

    fun getTodayMeals(): Cursor? {
        return readableDatabase.rawQuery(
            "SELECT * FROM meals WHERE date=?",
            arrayOf(getTodayDate())
        )
    }

    fun getWeeklyCalories(): Cursor? {
        return readableDatabase.rawQuery(
            "SELECT date, SUM(calories) FROM meals GROUP BY date ORDER BY date DESC LIMIT 7",
            null
        )
    }

    fun deleteMeal(id: Int) {
        writableDatabase.delete("meals", "id=?", arrayOf(id.toString()))
    }

    // ---------------- WATER ----------------

    fun addWater(ml: Int) {
        val values = ContentValues().apply {
            put("ml", ml)
            put("date", getTodayDate())
        }

        writableDatabase.insert("water", null, values)
    }

    fun getTodayWater(): Int {
        val cursor = readableDatabase.rawQuery(
            "SELECT SUM(ml) FROM water WHERE date=?",
            arrayOf(getTodayDate())
        )

        var total = 0
        if (cursor.moveToFirst()) total = cursor.getInt(0)

        cursor.close()
        return total
    }

    // ---------------- ACTIVITY ----------------

    fun updateActivity(steps: Int, workoutMin: Int) {
        val values = ContentValues().apply {
            put("date", getTodayDate())
            put("steps", steps)
            put("workout_minutes", workoutMin)
        }

        writableDatabase.insertWithOnConflict(
            "activity",
            null,
            values,
            SQLiteDatabase.CONFLICT_REPLACE
        )
    }

    fun getTodayActivity(): Cursor? {
        return readableDatabase.rawQuery(
            "SELECT * FROM activity WHERE date=?",
            arrayOf(getTodayDate())
        )
    }

    // ---------------- WORKOUT ----------------

    fun logWorkout(type: String, duration: Int, calories: Int) {
        val values = ContentValues().apply {
            put("workout_type", type)
            put("duration_min", duration)
            put("calories_burned", calories)
            put("date", getTodayDate())
        }

        writableDatabase.insert("workout_history", null, values)

        updateActivity(0, duration)
    }

    fun getWorkoutHistory(): Cursor? {
        return readableDatabase.rawQuery(
            "SELECT * FROM workout_history ORDER BY date DESC",
            null
        )
    }

    // ---------------- UTIL ----------------

    fun getStreak(): Int {
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT DISTINCT date FROM meals ORDER BY date DESC", null)

        var streak = 0
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val cal = Calendar.getInstance()

        val today = getTodayDate()
        cal.add(Calendar.DAY_OF_YEAR, -1)
        val yesterday = sdf.format(cal.time)

        if (cursor.moveToFirst()) {
            val lastDate = cursor.getString(0)

            if (lastDate == today || lastDate == yesterday) {
                streak = 1
                val currentCal = Calendar.getInstance()
                currentCal.time = sdf.parse(lastDate) ?: Date()

                while (cursor.moveToNext()) {
                    val nextDateStr = cursor.getString(0)
                    currentCal.add(Calendar.DAY_OF_YEAR, -1)
                    val expectedDate = sdf.format(currentCal.time)

                    if (nextDateStr == expectedDate) {
                        streak++
                    } else {
                        break
                    }
                }
            }
        }

        cursor.close()
        return streak
    }

    private fun getTodayDate(): String {
        return SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    }
}