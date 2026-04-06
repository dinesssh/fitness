package com.example.food

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import java.text.SimpleDateFormat
import java.util.*

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, "FoodTracker.db", null, 2) {

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("CREATE TABLE user_profile (id INTEGER PRIMARY KEY, age INTEGER, weight REAL, gender TEXT, goal TEXT)")
        db.execSQL("CREATE TABLE meals (id INTEGER PRIMARY KEY AUTOINCREMENT, meal_name TEXT, calories INTEGER, protein REAL, carbs REAL, fat REAL, date TEXT)")
        db.execSQL("CREATE TABLE water (id INTEGER PRIMARY KEY AUTOINCREMENT, ml INTEGER, date TEXT)")
    }

    override fun onUpgrade(db: SQLiteDatabase, old: Int, new: Int) {
        db.execSQL("DROP TABLE IF EXISTS user_profile")
        db.execSQL("DROP TABLE IF EXISTS meals")
        db.execSQL("DROP TABLE IF EXISTS water")
        onCreate(db)
    }

    // --- User Profile ---
    fun saveProfile(age: Int, weight: Double, gender: String, goal: String) {
        val db = writableDatabase
        val values = ContentValues().apply {
            put("id", 1); put("age", age); put("weight", weight); put("gender", gender); put("goal", goal)
        }
        db.insertWithOnConflict("user_profile", null, values, SQLiteDatabase.CONFLICT_REPLACE)
    }

    fun getProfile(): Cursor? = readableDatabase.rawQuery("SELECT * FROM user_profile WHERE id=1", null)

    // --- Meals ---
    fun addMeal(name: String, cal: Int, pro: Double, carb: Double, fat: Double) {
        val date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val values = ContentValues().apply {
            put("meal_name", name); put("calories", cal); put("protein", pro); put("carbs", carb); put("fat", fat); put("date", date)
        }
        writableDatabase.insert("meals", null, values)
    }

    fun getTodayMeals(): Cursor? {
        val date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        return readableDatabase.rawQuery("SELECT * FROM meals WHERE date=?", arrayOf(date))
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
}