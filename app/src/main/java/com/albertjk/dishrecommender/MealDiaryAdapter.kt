package com.albertjk.dishrecommender

import android.app.AlertDialog
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import java.util.*

/**
 * mealName is a string which identifies the meal (breakfast, lunch, or dinner)
 * the recycler view is associated with.
 */
class MealDiaryAdapter(
    val context: Context,
    val viewModel: ViewModel,
    val mealName: String,
    val logDate: Date
) :
    RecyclerView.Adapter<MealDiaryAdapter.MealDiaryViewHolder>() {

    private val TAG = MealDiaryAdapter::class.qualifiedName

    // This list will store the dishes which were part of the user's meal (breakfast, lunch, or dinner) on the given day and were logged.
    private var mealDiaryDishes: List<Dish> = ArrayList()

    // These lists will store the logged dish and daily log, breakfast log, lunch log, and dinner log entries retrieved from the database.
    private lateinit var dishLogs: List<DishLog>
    private lateinit var dailyLogs: List<DailyLog>
    private lateinit var breakfastLogs: List<BreakfastLog>
    private lateinit var lunchLogs: List<LunchLog>
    private lateinit var dinnerLogs: List<DinnerLog>

    /**
     * Creates a row displaying a dish of the given meal.
     */
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): MealDiaryAdapter.MealDiaryViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.logged_dish_row, parent, false)
        return MealDiaryViewHolder(view)
    }

    /**
     * This function is called for each row and adds the data inside the rows.
     * The position parameter is the row number.
     */
    override fun onBindViewHolder(holder: MealDiaryAdapter.MealDiaryViewHolder, position: Int) {

        holder.dishNameTextView.text = mealDiaryDishes[position].dishName
        holder.caloriesTextView.text = mealDiaryDishes[position].calories.toString()
        holder.totalFatTextView.text = mealDiaryDishes[position].totalFat.toString() + "g"
        holder.saturatedFatTextView.text =
            mealDiaryDishes[position].saturatedFat.toString() + "g"
        holder.carbsTextView.text = mealDiaryDishes[position].carbohydrates.toString() + "g"
        holder.sugarsTextView.text = mealDiaryDishes[position].sugars.toString() + "g"
        holder.saltTextView.text = mealDiaryDishes[position].salt.toString() + "g"
        holder.fibreTextView.text = mealDiaryDishes[position].fibre.toString() + "g"
        holder.proteinTextView.text = mealDiaryDishes[position].protein.toString() + "g"

        /* Only show the add to favourites button (blue heart icon) next to the dish name in the row,
        if the dish is not in the user's favourites yet. */
        if (mealDiaryDishes[position].isFavourite == 0) {
            holder.addToFavouritesButton.visibility = View.VISIBLE
        }

        // Store whether the current row is expanded or not.
        val isExpanded: Boolean = mealDiaryDishes[position].isItemExpanded

        // Set the visibility based on whether the row is expanded or not.
        holder.expandableLayout.visibility = if (isExpanded) View.VISIBLE else View.GONE
    }

    /**
     * Returns the number of rows needed by the RecyclerView.
     */
    override fun getItemCount(): Int = mealDiaryDishes.size

    /**
     * If the lists of mealDiaryDishes, logged dishes, and daily logs have changed, update the recycler view.
     */
    internal fun setData(
        mealDiaryDishes: List<Dish>,
        dishLogs: List<DishLog>,
        dailyLogs: List<DailyLog>,
        breakfastLogs: List<BreakfastLog>,
        lunchLogs: List<LunchLog>,
        dinnerLogs: List<DinnerLog>
    ) {
        this.mealDiaryDishes = mealDiaryDishes
        this.dishLogs = dishLogs
        this.dailyLogs = dailyLogs
        this.breakfastLogs = breakfastLogs
        this.lunchLogs = lunchLogs
        this.dinnerLogs = dinnerLogs
        notifyDataSetChanged()
    }

    /**
     * Binds data to the text views and button in all rows.
     */
    inner class MealDiaryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var dishNameTextView: TextView = itemView.findViewById(R.id.dishNameTextView)
        var caloriesTextView: TextView = itemView.findViewById(R.id.caloriesTextView)
        var totalFatTextView: TextView = itemView.findViewById(R.id.totalFatTextView)
        var saturatedFatTextView: TextView = itemView.findViewById(R.id.saturatedFatTextView)
        var carbsTextView: TextView = itemView.findViewById(R.id.carbsTextView)
        var sugarsTextView: TextView = itemView.findViewById(R.id.sugarsTextView)
        var saltTextView: TextView = itemView.findViewById(R.id.saltTextView)
        var fibreTextView: TextView = itemView.findViewById(R.id.fibreTextView)
        var proteinTextView: TextView = itemView.findViewById(R.id.proteinTextView)

        var expandableLayout: ConstraintLayout = itemView.findViewById(R.id.expandableLayout)

        private var deleteDishLogButton: ImageButton =
            itemView.findViewById(R.id.deleteDishLogButton)

        var addToFavouritesButton: ImageButton = itemView.findViewById(R.id.addToFavouritesButton)

        // Initializer block
        init {

            // Click listener for each row.
            dishNameTextView.setOnClickListener {
                /* If a row is tapped, get the dish at this
                adapter position and expand it if it is not expanded
                and vice versa. */
                val dish = mealDiaryDishes[adapterPosition]
                dish.isItemExpanded = (!dish.isItemExpanded)
                notifyItemChanged(adapterPosition)
            }

            /* If the user clicks on the delete image button (red bin icon) next to the dish name in the list,
            show a dialog, asking the user to confirm the deletion. */
            deleteDishLogButton.setOnClickListener {
                val dialogBuilder = AlertDialog.Builder(itemView.context)

                dialogBuilder.setTitle("Are you sure you want to delete this item?")
                dialogBuilder.setMessage(mealDiaryDishes[adapterPosition].dishName + " will be removed from this list.")

                // Cancel button click listener
                dialogBuilder.setNeutralButton("Cancel") { dialog, _ ->
                    dialog.cancel()
                }
                // Positive button click listener
                dialogBuilder.setPositiveButton("Delete") { dialog, _ ->

                    // Store the dish which had been logged, but the logging will be removed.
                    val dish = mealDiaryDishes[adapterPosition]

                    Log.d(TAG, "dish: $dish")

                    /* Find the entry in the DishLog table based on the given dish's ID, the specified date, and the meal name.
                    Then remove the entry from the DishLog table. */

                    Log.d(TAG, "dishesLog: $dishLogs")
                    for (dishLog in dishLogs) {
                        if (dishLog.dishId == dish.dishId && dishLog.logDate == logDate && dishLog.mealName == mealName) {
                            viewModel.deleteDishLog(dishLog)
                            break
                        }
                    }
                    Log.d(TAG, "dishesLog: $dishLogs")
                    // Remove the nutrients of the dish from the user's daily log at the specified date recorded in the DailyLog table.
                    Log.d(TAG, "dailyLogs: $dailyLogs")
                    for (dailyLog in dailyLogs) {
                        if (dailyLog.logDate == logDate) {

                            /* If all nutrients have 0 values in the daily log entry,
                            then delete the whole entry from the DailyLog table. */
                            if (dailyLog.dailyCaloriesIntake - dish.calories == 0
                                && dailyLog.dailyTotalFatIntake - dish.totalFat == 0f
                                && dailyLog.dailySaturatedFatIntake - dish.saturatedFat == 0f
                                && dailyLog.dailyCarbohydratesIntake - dish.carbohydrates == 0f
                                && dailyLog.dailySugarsIntake - dish.sugars == 0f
                                && dailyLog.dailySaltIntake - dish.salt == 0f
                                && dailyLog.dailyFibreIntake - dish.fibre == 0f
                                && dailyLog.dailyProteinIntake - dish.protein == 0f
                            ) {
                                viewModel.deleteDailyLog(dailyLog)
                            }
                            /* Otherwise, remove the dish's nutrient values from the
                            daily totals and update the DailyLog record. */
                            else {
                                viewModel.updateDailyLog(
                                    DailyLog(
                                        logDate,
                                        dailyLog.dailyCaloriesIntake - dish.calories,
                                        dailyLog.dailyTotalFatIntake - dish.totalFat,
                                        dailyLog.dailySaturatedFatIntake - dish.saturatedFat,
                                        dailyLog.dailyCarbohydratesIntake - dish.carbohydrates,
                                        dailyLog.dailySugarsIntake - dish.sugars,
                                        dailyLog.dailySaltIntake - dish.salt,
                                        dailyLog.dailyFibreIntake - dish.fibre,
                                        dailyLog.dailyProteinIntake - dish.protein
                                    )
                                )
                            }
                            break
                        }
                    }
                    Log.d(TAG, "dailyLogs: $dailyLogs")

                    // Remove the nutrients of the dish from the user's recorded BreakfastLog, LunchLog, or DinnerLog at the specified date.
                    when (mealName) {
                        "breakfast" -> {
                            Log.d(TAG, "breakfastLogs: $breakfastLogs")
                            for (breakfastLog in breakfastLogs) {
                                if (breakfastLog.logDate == logDate) {

                                    /* If all nutrients have 0 values in the breakfast log entry,
                                    then delete the whole entry from the BreakfastLog table. */
                                    if (breakfastLog.breakfastCaloriesIntake - dish.calories == 0
                                        && breakfastLog.breakfastTotalFatIntake - dish.totalFat == 0f
                                        && breakfastLog.breakfastSaturatedFatIntake - dish.saturatedFat == 0f
                                        && breakfastLog.breakfastCarbohydratesIntake - dish.carbohydrates == 0f
                                        && breakfastLog.breakfastSugarsIntake - dish.sugars == 0f
                                        && breakfastLog.breakfastSaltIntake - dish.salt == 0f
                                        && breakfastLog.breakfastFibreIntake - dish.fibre == 0f
                                        && breakfastLog.breakfastProteinIntake - dish.protein == 0f
                                    ) {
                                        viewModel.deleteBreakfastLog(breakfastLog)
                                    }
                                    /* Otherwise, remove the dish's nutrient values from the
                                    breakfast totals and update the BreakfastLog record. */
                                    else {
                                        viewModel.updateBreakfastLog(
                                            BreakfastLog(
                                                logDate,
                                                breakfastLog.breakfastCaloriesIntake - dish.calories,
                                                breakfastLog.breakfastTotalFatIntake - dish.totalFat,
                                                breakfastLog.breakfastSaturatedFatIntake - dish.saturatedFat,
                                                breakfastLog.breakfastCarbohydratesIntake - dish.carbohydrates,
                                                breakfastLog.breakfastSugarsIntake - dish.sugars,
                                                breakfastLog.breakfastSaltIntake - dish.salt,
                                                breakfastLog.breakfastFibreIntake - dish.fibre,
                                                breakfastLog.breakfastProteinIntake - dish.protein
                                            )
                                        )
                                    }
                                    break
                                }
                            }
                            Log.d(TAG, "breakfastLogs: $breakfastLogs")
                        }
                        "lunch" -> {
                            Log.d(TAG, "lunchLogs: $lunchLogs")
                            for (lunchLog in lunchLogs) {
                                if (lunchLog.logDate == logDate) {

                                    /* If all nutrients have 0 values in the lunch log entry,
                                    then delete the whole entry from the LunchLog table. */
                                    if (lunchLog.lunchCaloriesIntake - dish.calories == 0
                                        && lunchLog.lunchTotalFatIntake - dish.totalFat == 0f
                                        && lunchLog.lunchSaturatedFatIntake - dish.saturatedFat == 0f
                                        && lunchLog.lunchCarbohydratesIntake - dish.carbohydrates == 0f
                                        && lunchLog.lunchSugarsIntake - dish.sugars == 0f
                                        && lunchLog.lunchSaltIntake - dish.salt == 0f
                                        && lunchLog.lunchFibreIntake - dish.fibre == 0f
                                        && lunchLog.lunchProteinIntake - dish.protein == 0f
                                    ) {
                                        viewModel.deleteLunchLog(lunchLog)
                                    }
                                    /* Otherwise, remove the dish's nutrient values from the
                                    lunch totals and update the LunchLog record. */
                                    else {
                                        viewModel.updateLunchLog(
                                            LunchLog(
                                                logDate,
                                                lunchLog.lunchCaloriesIntake - dish.calories,
                                                lunchLog.lunchTotalFatIntake - dish.totalFat,
                                                lunchLog.lunchSaturatedFatIntake - dish.saturatedFat,
                                                lunchLog.lunchCarbohydratesIntake - dish.carbohydrates,
                                                lunchLog.lunchSugarsIntake - dish.sugars,
                                                lunchLog.lunchSaltIntake - dish.salt,
                                                lunchLog.lunchFibreIntake - dish.fibre,
                                                lunchLog.lunchProteinIntake - dish.protein
                                            )
                                        )
                                    }
                                    break
                                }
                            }
                            Log.d(TAG, "lunchLogs: $lunchLogs")
                        }
                        "dinner" -> {
                            Log.d(TAG, "dinnerLogs: $dinnerLogs")
                            for (dinnerLog in dinnerLogs) {
                                if (dinnerLog.logDate == logDate) {

                                    /* If all nutrients have 0 values in the dinner log entry,
                                    then delete the whole entry from the DinnerLog table. */
                                    if (dinnerLog.dinnerCaloriesIntake - dish.calories == 0
                                        && dinnerLog.dinnerTotalFatIntake - dish.totalFat == 0f
                                        && dinnerLog.dinnerSaturatedFatIntake - dish.saturatedFat == 0f
                                        && dinnerLog.dinnerCarbohydratesIntake - dish.carbohydrates == 0f
                                        && dinnerLog.dinnerSugarsIntake - dish.sugars == 0f
                                        && dinnerLog.dinnerSaltIntake - dish.salt == 0f
                                        && dinnerLog.dinnerFibreIntake - dish.fibre == 0f
                                        && dinnerLog.dinnerProteinIntake - dish.protein == 0f
                                    ) {
                                        viewModel.deleteDinnerLog(dinnerLog)
                                    }
                                    /* Otherwise, remove the dish's nutrient values from the
                                    dinner totals and update the DinnerLog record. */
                                    else {
                                        viewModel.updateDinnerLog(
                                            DinnerLog(
                                                logDate,
                                                dinnerLog.dinnerCaloriesIntake - dish.calories,
                                                dinnerLog.dinnerTotalFatIntake - dish.totalFat,
                                                dinnerLog.dinnerSaturatedFatIntake - dish.saturatedFat,
                                                dinnerLog.dinnerCarbohydratesIntake - dish.carbohydrates,
                                                dinnerLog.dinnerSugarsIntake - dish.sugars,
                                                dinnerLog.dinnerSaltIntake - dish.salt,
                                                dinnerLog.dinnerFibreIntake - dish.fibre,
                                                dinnerLog.dinnerProteinIntake - dish.protein
                                            )
                                        )
                                    }
                                    break
                                }
                            }
                            Log.d(TAG, "dinnerLogs: $dinnerLogs")
                        }
                    }
                    notifyItemRemoved(adapterPosition)
                    dialog.dismiss()
                }
                dialogBuilder.create().show()
            }

            /* If the user clicks on the add to favourites button (blue heart icon) next to a dish name,
            add the dish to their favourites. */
            addToFavouritesButton.setOnClickListener {

                // Show a dialog asking the user to confirm if they want to add the dish to their favourites.
                val dialogBuilder = AlertDialog.Builder(itemView.context)

                dialogBuilder.setTitle("Are you sure you want to add this item to your favourites?")

                // Cancel button click listener
                dialogBuilder.setNeutralButton("Cancel") { dialog, _ ->
                    dialog.cancel()
                }
                // Positive button click listener
                dialogBuilder.setPositiveButton("Yes") { _, _ ->

                    // Store the dish which is to be added to the favourites.
                    val dish = mealDiaryDishes[adapterPosition]

                    Log.d(TAG, "dish: $dish")

                    /* Since the heart icon button is only shown if the dish is not in the user's
                    favourites, no need to check for this again. Set the dish as a favourite in the database. */
                    dish.isFavourite = 1
                    viewModel.updateDish(dish)

                    Toast.makeText(context, "Dish added to favourites", Toast.LENGTH_SHORT).show()

                    // Redirect to FavouritesFragment.
                    it.findNavController().navigate(
                        R.id.action_diaryFragment_to_favouritesFragment
                    )
                }
                dialogBuilder.create().show()
            }
        }
    }
}