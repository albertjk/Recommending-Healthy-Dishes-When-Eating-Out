package com.albertjk.dishrecommender

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/**
 * Annotates the class to be a Room Database with tables (entities) of the Dish, DishLog,
 * DailyLog, and SavedRecommendationPreference classes.
 */
@Database(
    entities = [Dish::class, DishLog::class, DailyLog::class, SavedRecommendationPreference::class,
        BreakfastLog::class, LunchLog::class, DinnerLog::class],
    version = 1
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    // These functions are used to access the DAOs.
    abstract fun dishDao(): DishDao
    abstract fun recommendationPreferenceDao(): SavedRecommendationPreferenceDao
    abstract fun dishLogDao(): DishLogDao
    abstract fun dailyLogDao(): DailyLogDao
    abstract fun breakfastLogDao(): BreakfastLogDao
    abstract fun lunchLogDao(): LunchLogDao
    abstract fun dinnerLogDao(): DinnerLogDao

    companion object {

        private val DATABASE_NAME = "app_database"

        /* This variable is created as this class must be turned into a Singleton.
        Singleton prevents multiple instances of the database opening at the same time. */
        @Volatile
        private var INSTANCE: AppDatabase? = null

        /**
         * Creates the database instance.
         * This method can be called to get a handle to this instance.
         * The CoroutineScope is needed to launch a coroutine.
         */
        fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
            val tempInstance = INSTANCE
            if (tempInstance != null) {
                return tempInstance
            }
            synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    DATABASE_NAME
                )
                    .addCallback(AppDatabaseCallback(context, scope))
                    .build()
                INSTANCE = instance
                return instance
            }
        }
    }

    /**
     * Populates the database when it is created for the first time.
     */
    private class AppDatabaseCallback(
        private val context: Context,
        private val scope: CoroutineScope
    ) : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                scope.launch {

                    val dishDao = database.dishDao()

                    // Delete all previously added dish data.
                    dishDao.deleteAllDishes()

                    // Read the dish data from a CSV file and add them to the database.
                    val dishes = CSVHelper().readCSVDatabase(context)

                    dishDao.insertAllDishes(dishes)
                }
            }
        }
    }
}