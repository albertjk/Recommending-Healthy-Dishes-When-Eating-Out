package com.albertjk.dishrecommender

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ViewModel(application: Application): AndroidViewModel(application) {

    private val repository: Repository

    // These LiveData objects will cache the list of dishes, the user's preferences, and the user's logs.
    val allDishes: LiveData<List<Dish>>
    val favouriteDishes: LiveData<List<Dish>>
    val dailyLogs: LiveData<List<DailyLog>>
    val savedPreferences: LiveData<List<SavedRecommendationPreference>>
    val dishLogs: LiveData<List<DishLog>>
    val breakfastLogs: LiveData<List<BreakfastLog>>
    val lunchLogs: LiveData<List<LunchLog>>
    val dinnerLogs: LiveData<List<DinnerLog>>

    init {
        val dishDao = AppDatabase.getDatabase(application, viewModelScope).dishDao()
        val recommendationPreferenceDao = AppDatabase.getDatabase(application, viewModelScope).recommendationPreferenceDao()
        val dishLogDao = AppDatabase.getDatabase(application, viewModelScope).dishLogDao()
        val dailyLogDao = AppDatabase.getDatabase(application, viewModelScope).dailyLogDao()
        val breakfastLogDao = AppDatabase.getDatabase(application, viewModelScope).breakfastLogDao()
        val lunchLogDao = AppDatabase.getDatabase(application, viewModelScope).lunchLogDao()
        val dinnerLogDao = AppDatabase.getDatabase(application, viewModelScope).dinnerLogDao()

        repository = Repository(dishDao, recommendationPreferenceDao, dishLogDao, dailyLogDao, breakfastLogDao, lunchLogDao, dinnerLogDao)

        // Initialise the LiveData objects using the repository.
        allDishes = repository.allDishes
        favouriteDishes = repository.favouriteDishes
        dailyLogs = repository.dailyLogs
        savedPreferences = repository.savedRecommendationPreferences
        dishLogs = repository.dishLogs
        breakfastLogs = repository.breakfastLogs
        lunchLogs = repository.lunchLogs
        dinnerLogs = repository.dinnerLogs
    }

    /**
     * Launch a new coroutine to update the data about a dish in a non-blocking way.
     */
    fun updateDish(dish: Dish) = viewModelScope.launch(Dispatchers.IO) {
        repository.updateDish(dish)
    }

    /**
     * Launch a new coroutine to insert a new DailyLog instance into the DailyLog table in a non-blocking way.
     */
    fun insertDailyLog(dailyLog: DailyLog)= viewModelScope.launch(Dispatchers.IO) {
        repository.insertDailyLog(dailyLog)
    }

    /**
     * Launch a new coroutine to update the data about a DailyLog in a non-blocking way.
     */
    fun updateDailyLog(dailyLog: DailyLog) = viewModelScope.launch(Dispatchers.IO) {
        repository.updateDailyLog(dailyLog)
    }

    /**
     * Launch a new coroutine to delete a DailyLog instance in a non-blocking way.
     */
    fun deleteDailyLog(dailyLog: DailyLog) = viewModelScope.launch(Dispatchers.IO) {
        repository.deleteDailyLog(dailyLog)
    }

    /**
     * Launch a new coroutine to insert a new BreakfastLog instance into the BreakfastLog table in a non-blocking way.
     */
    fun insertBreakfastLog(breakfastLog: BreakfastLog)= viewModelScope.launch(Dispatchers.IO) {
        repository.insertBreakfastLog(breakfastLog)
    }

    /**
     * Launch a new coroutine to update the data about a BreakfastLog in a non-blocking way.
     */
    fun updateBreakfastLog(breakfastLog: BreakfastLog) = viewModelScope.launch(Dispatchers.IO) {
        repository.updateBreakfastLog(breakfastLog)
    }

    /**
     * Launch a new coroutine to delete a BreakfastLog instance in a non-blocking way.
     */
    fun deleteBreakfastLog(breakfastLog: BreakfastLog) = viewModelScope.launch(Dispatchers.IO) {
        repository.deleteBreakfastLog(breakfastLog)
    }

    /**
     * Launch a new coroutine to insert a new LunchLog instance into the LunchLog table in a non-blocking way.
     */
    fun insertLunchLog(lunchLog: LunchLog)= viewModelScope.launch(Dispatchers.IO) {
        repository.insertLunchLog(lunchLog)
    }

    /**
     * Launch a new coroutine to update the data about a LunchLog in a non-blocking way.
     */
    fun updateLunchLog(lunchLog: LunchLog) = viewModelScope.launch(Dispatchers.IO) {
        repository.updateLunchLog(lunchLog)
    }

    /**
     * Launch a new coroutine to delete a LunchLog instance in a non-blocking way.
     */
    fun deleteLunchLog(lunchLog: LunchLog) = viewModelScope.launch(Dispatchers.IO) {
        repository.deleteLunchLog(lunchLog)
    }

    /**
     * Launch a new coroutine to insert a new DinnerLog instance into the DinnerLog table in a non-blocking way.
     */
    fun insertDinnerLog(dinnerLog: DinnerLog)= viewModelScope.launch(Dispatchers.IO) {
        repository.insertDinnerLog(dinnerLog)
    }

    /**
     * Launch a new coroutine to update the data about a DinnerLog in a non-blocking way.
     */
    fun updateDinnerLog(dinnerLog: DinnerLog) = viewModelScope.launch(Dispatchers.IO) {
        repository.updateDinnerLog(dinnerLog)
    }

    /**
     * Launch a new coroutine to delete a DinnerLog instance in a non-blocking way.
     */
    fun deleteDinnerLog(dinnerLog: DinnerLog) = viewModelScope.launch(Dispatchers.IO) {
        repository.deleteDinnerLog(dinnerLog)
    }

    /**
     * Launch a new coroutine to insert a list of SavedRecommendationPreference instances into the
     * SavedRecommendationPreference table in a non-blocking way.
     */
    fun insertAllRecommendationPreferences(savedRecommendationPreferences: List<SavedRecommendationPreference>)= viewModelScope.launch(Dispatchers.IO) {
        repository.insertAllRecommendationPreferences(savedRecommendationPreferences)
    }

    /**
     * Launch a new coroutine to delete all SavedRecommendationPreference instances in a non-blocking way.
     */
    fun deleteAllRecommendationPreferences() = viewModelScope.launch(Dispatchers.IO) {
        repository.deleteAllRecommendationPreferences()
    }

    /**
     * Launch a new coroutine to insert a new DishLog instance into the DishLog table in a non-blocking way.
     */
    fun insertDishLog(dishLog: DishLog) = viewModelScope.launch(Dispatchers.IO)  {
        repository.insertDishLog(dishLog)
    }

    /**
     * Launch a new coroutine to delete a DishLog instance in a non-blocking way.
     */
    fun deleteDishLog(dishLog: DishLog) = viewModelScope.launch(Dispatchers.IO)  {
        repository.deleteDishLog(dishLog)
    }
}