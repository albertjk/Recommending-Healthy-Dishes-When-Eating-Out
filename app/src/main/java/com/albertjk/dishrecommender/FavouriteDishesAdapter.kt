package com.albertjk.dishrecommender

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.os.bundleOf
import androidx.navigation.Navigation
import androidx.recyclerview.widget.RecyclerView
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


class FavouriteDishesAdapter(var context: Context, var viewModel: ViewModel) :
    RecyclerView.Adapter<FavouriteDishesAdapter.FavouriteDishesViewHolder>() {

    private val TAG = FavouriteDishesAdapter::class.qualifiedName

    private var favouriteDishes: List<Dish> = ArrayList()

    /**
     * Creates a row displaying a favourite dish.
     */
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): FavouriteDishesAdapter.FavouriteDishesViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.favourite_dish_row, parent, false)
        return FavouriteDishesViewHolder(view)
    }

    /**
     * This function is called for each row and adds the data inside the rows.
     * The position parameter is the row number.
     */
    override fun onBindViewHolder(
        holder: FavouriteDishesAdapter.FavouriteDishesViewHolder,
        position: Int
    ) {
        holder.dishNameTextView.text = favouriteDishes[position].dishName
        holder.caloriesTextView.text = favouriteDishes[position].calories.toString()
        holder.totalFatTextView.text = favouriteDishes[position].totalFat.toString() + "g"
        holder.saturatedFatTextView.text =
            favouriteDishes[position].saturatedFat.toString() + "g"
        holder.carbsTextView.text = favouriteDishes[position].carbohydrates.toString() + "g"
        holder.sugarsTextView.text = favouriteDishes[position].sugars.toString() + "g"
        holder.saltTextView.text = favouriteDishes[position].salt.toString() + "g"
        holder.fibreTextView.text = favouriteDishes[position].fibre.toString() + "g"
        holder.proteinTextView.text = favouriteDishes[position].protein.toString() + "g"

        // Store whether the current row is expanded or not.
        val isExpanded: Boolean = favouriteDishes[position].isItemExpanded

        // Set the visibility based on whether the row is expanded or not.
        holder.expandableLayout.visibility = if (isExpanded) View.VISIBLE else View.GONE
    }

    /**
     * Returns the number of rows needed by the RecyclerView.
     */
    override fun getItemCount(): Int = favouriteDishes.size

    /**
     * If the list of favourite dishes has changed, update the recycler view.
     */
    internal fun setFavouriteDishes(favouriteDishes: List<Dish>) {
        this.favouriteDishes = favouriteDishes
        notifyDataSetChanged()
    }

    /**
     * Binds data to the text views and button in all rows.
     */
    inner class FavouriteDishesViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
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

        private var deleteFavouriteDishButton: ImageButton =
            itemView.findViewById(R.id.deleteFavouriteDishButton)

        var addToDiaryButton: ImageButton = itemView.findViewById(R.id.addToDiaryButton)

        // Initializer block
        init {

            // Click listener for each row.
            dishNameTextView.setOnClickListener {
                /* If a row is tapped, get the dish at this
                adapter position and expand it if it is not expanded
                and vice versa. */
                val dish = favouriteDishes[adapterPosition]
                dish.isItemExpanded = (!dish.isItemExpanded)
                notifyItemChanged(adapterPosition)
            }

            /* If the user clicks on the delete image button next to the dish name in the list,
            show a dialog, asking the user to confirm the deletion. */
            deleteFavouriteDishButton.setOnClickListener {
                val dialogBuilder = AlertDialog.Builder(itemView.context)

                dialogBuilder.setTitle("Are you sure you want to delete this item?")
                dialogBuilder.setMessage(favouriteDishes[adapterPosition].dishName + " will be removed from your favourites.")

                // Cancel button click listener
                dialogBuilder.setNeutralButton("Cancel") { dialog, _ ->
                    dialog.cancel()
                }
                // Positive button click listener
                dialogBuilder.setPositiveButton("Delete") { dialog, _ ->

                    // Find the identified dish based on its name in the database.
                    val dishList = viewModel.allDishes.value
                    var identifiedDish: Dish? = null

                    for (dish in dishList!!) {
                        if (dish.dishName == favouriteDishes[adapterPosition].dishName) {
                            identifiedDish = dish
                            break
                        }
                    }
                    // Set the isFavourite attribute of the dish to 0 in the database.
                    identifiedDish!!.isFavourite = 0
                    viewModel.updateDish(identifiedDish)

                    notifyItemRemoved(adapterPosition)
                    dialog.dismiss()
                }
                dialogBuilder.create().show()
            }

            /* If the user clicks on the add to diary button (diary icon) next to the dish name in the list,
            show a dialog, asking them to select the meal and when the dish was consumed. */
            addToDiaryButton.setOnClickListener {

                Log.d(TAG, "favouriteDishes[adapterPosition]: " + favouriteDishes[adapterPosition])

                // This variable will store the date, selected by the user, as a string.
                var selectedDateString: String? = null

                val calendar = Calendar.getInstance()

                // Show a date picker dialog.
                val datePicker = DatePickerDialog(
                    itemView.context,
                    { _, year, month, dayOfMonth ->

                        // Get the date the user selected.
                        val selectedDate = Calendar.getInstance()
                        selectedDate.set(Calendar.YEAR, year)
                        selectedDate.set(Calendar.MONTH, month)
                        selectedDate.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                        selectedDateString =
                            DateFormat.getDateInstance(DateFormat.FULL)
                                .format(selectedDate.time)

                        Log.d(TAG, "selectedDateString: $selectedDateString")

                        /* Show a dialog to let the user select the meal - breakfast, lunch, or dinner -
                        the dish is to be associated with. */

                        val mealNames: ArrayList<String> = ArrayList()
                        mealNames.add("breakfast")
                        mealNames.add("lunch")
                        mealNames.add("dinner")

                        // This string will store the selected meal, which can be either breakfast, lunch, or dinner.
                        var selectedMealName: String? = null

                        val dialogBuilder = AlertDialog.Builder(itemView.context)
                        dialogBuilder
                            .setTitle("Please select the meal the dish is to be associated with")
                            .setCancelable(false) // The dialog cannot be cancelled
                            .setItems(mealNames.toTypedArray()) { dialog, which ->

                                // The 'which' argument contains the index position of the selected item.
                                selectedMealName = mealNames[which]
                                Log.d(TAG, "in dialog selectedMealName: $selectedMealName")

                                /* Add an entry to the database to the DishLog and the DailyLog entities,
                                and the BreakfastLog, LunchLog, or DinnerLog entities, depending on the meal name. */

                                /* Record the user's daily nutrient intake in the DailyLog entity in the database. */

                                /* If the user already consumed dishes on the given date, get the user's already recorded
                                daily nutrient intake for this date from the database, and add the current dish's
                                nutrient values to these. This process updates the user's recorded daily nutrient intake. */
                                val dailyLogs = viewModel.dailyLogs.value

                                // This boolean is used to decide if the DailyLog table already contains an instance with the specified logDate.
                                var dailyLogInstanceExists = false

                                /* This variable stores the desired date format:
                                Weekday name, day number, month name, and year number. */
                                val dateFormat = SimpleDateFormat("EE, dd MMM yyyy", Locale.UK)

                                val logDate = dateFormat.parse(selectedDateString) as Date

                                Toast.makeText(
                                    itemView.context,
                                    "Dish added to diary",
                                    Toast.LENGTH_SHORT
                                ).show()

                                val selectedDish = favouriteDishes[adapterPosition]

                                if (dailyLogs != null && dailyLogs.isNotEmpty()) {
                                    for (dailyLog in dailyLogs) {
                                        if (dailyLog.logDate == logDate) {
                                            dailyLogInstanceExists = true
                                            Log.d(
                                                TAG,
                                                "dailyLogInstanceExists: $dailyLogInstanceExists"
                                            )
                                            viewModel.updateDailyLog(
                                                DailyLog(
                                                    logDate,
                                                    dailyLog.dailyCaloriesIntake + selectedDish.calories,
                                                    dailyLog.dailyTotalFatIntake + selectedDish.totalFat,
                                                    dailyLog.dailySaturatedFatIntake + selectedDish.saturatedFat,
                                                    dailyLog.dailyCarbohydratesIntake + selectedDish.carbohydrates,
                                                    dailyLog.dailySugarsIntake + selectedDish.sugars,
                                                    dailyLog.dailySaltIntake + selectedDish.salt,
                                                    dailyLog.dailyFibreIntake + selectedDish.fibre,
                                                    dailyLog.dailyProteinIntake + selectedDish.protein
                                                )
                                            ).invokeOnCompletion {
                                                // Add the log to either BreakfastLogs, LunchLogs, or DinnerLogs, depending on the meal name.
                                                storeMealLog(
                                                    selectedMealName,
                                                    selectedDish,
                                                    logDate
                                                )

                                                /* Add the dish, the received meal name, and the date to the DishLog entity in the database.
                                                logId is 0, as it will be auto-generated by Room when inserting the entry into the database. */
                                                viewModel.insertDishLog(
                                                    DishLog(
                                                        0,
                                                        favouriteDishes[adapterPosition].dishId,
                                                        selectedMealName!!,
                                                        logDate
                                                    )
                                                ).invokeOnCompletion {

                                                    Log.d(TAG, "Redirect to DiaryFragment")

                                                    Handler(Looper.getMainLooper()).post {
                                                        /* Redirect to DiaryFragment and pass the date as an argument
                                                        so that the logs for this date will be shown in the screen. */
                                                        val bundle = bundleOf(
                                                            "selectedDateString" to selectedDateString
                                                        )
                                                        Navigation.findNavController(itemView)
                                                            .navigate(
                                                                R.id.action_favouritesFragment_to_diaryFragment,
                                                                bundle
                                                            )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }

                                // If there is no entry for the given date in the DailyLog table yet, insert a new daily log.
                                if (!dailyLogInstanceExists) {

                                    Log.d(TAG, "dailyLogInstanceExists: $dailyLogInstanceExists")

                                    viewModel.insertDailyLog(
                                        DailyLog(
                                            logDate,
                                            selectedDish.calories,
                                            selectedDish.totalFat,
                                            selectedDish.saturatedFat,
                                            selectedDish.carbohydrates,
                                            selectedDish.sugars,
                                            selectedDish.salt,
                                            selectedDish.fibre,
                                            selectedDish.protein
                                        )
                                    ).invokeOnCompletion {
                                        // Add the log to either BreakfastLogs, LunchLogs, or DinnerLogs, depending on the meal name.
                                        storeMealLog(selectedMealName, selectedDish, logDate)

                                        /* Add the dish and the meal name and date to the DishLog entity in the database.
                                        logId is 0, as it will be auto-generated by Room when inserting the entry into the database. */
                                        viewModel.insertDishLog(
                                            DishLog(
                                                0,
                                                selectedDish.dishId,
                                                selectedMealName!!,
                                                logDate
                                            )
                                        ).invokeOnCompletion {

                                            Log.d(TAG, "Redirecting to Diary")

                                            Handler(Looper.getMainLooper()).post {
                                                /* Redirect to DiaryFragment and pass the date as an argument
                                                so that the logs for this date will be shown in the screen. */
                                                val bundle = bundleOf(
                                                    "selectedDateString" to selectedDateString
                                                )
                                                Navigation.findNavController(itemView).navigate(
                                                    R.id.action_favouritesFragment_to_diaryFragment,
                                                    bundle
                                                )
                                            }
                                        }
                                    }
                                }
                                dialog.dismiss()
                            }
                        dialogBuilder.create().show()
                    },
                    // When the dialog opens, the date is set to be the current date.
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH)
                )
                datePicker.show()
            }
        }
    }

    /**
     * Adds the nutrients of a dish logged on a specified date
     * to the BreakfastLog, LunchLog, or DinnerLog table, depending on the meal the dish is associated with.
     */
    private fun storeMealLog(mealName: String?, identifiedDish: Dish?, logDate: Date) {
        if (mealName == "breakfast") {

            /* If the user already consumed dishes on the given date for breakfast, get the user's already recorded
            breakfast nutrient intake for this date from the database, and add the current dish's
            nutrient values to these. This process updates the user's recorded breakfast nutrient intake. */
            val breakfastLogs = viewModel.breakfastLogs.value

            // This boolean is used to decide if the BreakfastLog table already contains an instance with the specified logDate.
            var breakfastLogInstanceExists = false

            if (breakfastLogs != null && breakfastLogs.isNotEmpty()) {
                for (breakfastLog in breakfastLogs) {
                    if (breakfastLog.logDate == logDate) {
                        breakfastLogInstanceExists = true
                        Log.d(TAG, "breakfastLogInstanceExists: $breakfastLogInstanceExists")
                        viewModel.updateBreakfastLog(
                            BreakfastLog(
                                logDate,
                                breakfastLog.breakfastCaloriesIntake + identifiedDish!!.calories,
                                breakfastLog.breakfastTotalFatIntake + identifiedDish.totalFat,
                                breakfastLog.breakfastSaturatedFatIntake + identifiedDish.saturatedFat,
                                breakfastLog.breakfastCarbohydratesIntake + identifiedDish.carbohydrates,
                                breakfastLog.breakfastSugarsIntake + identifiedDish.sugars,
                                breakfastLog.breakfastSaltIntake + identifiedDish.salt,
                                breakfastLog.breakfastFibreIntake + identifiedDish.fibre,
                                breakfastLog.breakfastProteinIntake + identifiedDish.protein
                            )
                        )
                    }
                }
            }

            // If there is no entry for the given date in the BreakfastLog table yet, insert a new entry.
            if (!breakfastLogInstanceExists) {

                Log.d(TAG, "breakfastLogInstanceExists: $breakfastLogInstanceExists")

                viewModel.insertBreakfastLog(
                    BreakfastLog(
                        logDate,
                        identifiedDish!!.calories,
                        identifiedDish.totalFat,
                        identifiedDish.saturatedFat,
                        identifiedDish.carbohydrates,
                        identifiedDish.sugars,
                        identifiedDish.salt,
                        identifiedDish.fibre,
                        identifiedDish.protein
                    )
                )
            }

        } else if (mealName == "lunch") {

            /* If the user already consumed dishes on the given date for lunch, get the user's already recorded
            lunch nutrient intake for this date from the database, and add the current dish's
            nutrient values to these. This process updates the user's recorded lunch nutrient intake. */
            val lunchLogs = viewModel.lunchLogs.value

            // This boolean is used to decide if the LunchLog table already contains an instance with the specified logDate.
            var lunchLogInstanceExists = false

            if (lunchLogs != null && lunchLogs.isNotEmpty()) {
                for (lunchLog in lunchLogs) {
                    if (lunchLog.logDate == logDate) {
                        lunchLogInstanceExists = true
                        Log.d(TAG, "lunchLogInstanceExists: $lunchLogInstanceExists")
                        viewModel.updateLunchLog(
                            LunchLog(
                                logDate,
                                lunchLog.lunchCaloriesIntake + identifiedDish!!.calories,
                                lunchLog.lunchTotalFatIntake + identifiedDish.totalFat,
                                lunchLog.lunchSaturatedFatIntake + identifiedDish.saturatedFat,
                                lunchLog.lunchCarbohydratesIntake + identifiedDish.carbohydrates,
                                lunchLog.lunchSugarsIntake + identifiedDish.sugars,
                                lunchLog.lunchSaltIntake + identifiedDish.salt,
                                lunchLog.lunchFibreIntake + identifiedDish.fibre,
                                lunchLog.lunchProteinIntake + identifiedDish.protein
                            )
                        )
                    }
                }
            }

            // If there is no entry for the given date in the LunchLog table yet, insert a new entry.
            if (!lunchLogInstanceExists) {

                Log.d(TAG, "lunchLogInstanceExists: $lunchLogInstanceExists")

                viewModel.insertLunchLog(
                    LunchLog(
                        logDate,
                        identifiedDish!!.calories,
                        identifiedDish.totalFat,
                        identifiedDish.saturatedFat,
                        identifiedDish.carbohydrates,
                        identifiedDish.sugars,
                        identifiedDish.salt,
                        identifiedDish.fibre,
                        identifiedDish.protein
                    )
                )
            }

        } else {

            /* If the user already consumed dishes on the given date for dinner, get the user's already recorded
            dinner nutrient intake for this date from the database, and add the current dish's
            nutrient values to these. This process updates the user's recorded dinner nutrient intake. */
            val dinnerLogs = viewModel.dinnerLogs.value

            // This boolean is used to decide if the DinnerLog table already contains an instance with the specified logDate.
            var dinnerLogInstanceExists = false

            if (dinnerLogs != null && dinnerLogs.isNotEmpty()) {
                for (dinnerLog in dinnerLogs) {
                    if (dinnerLog.logDate == logDate) {
                        dinnerLogInstanceExists = true
                        Log.d(TAG, "dinnerLogInstanceExists: $dinnerLogInstanceExists")
                        viewModel.updateDinnerLog(
                            DinnerLog(
                                logDate,
                                dinnerLog.dinnerCaloriesIntake + identifiedDish!!.calories,
                                dinnerLog.dinnerTotalFatIntake + identifiedDish.totalFat,
                                dinnerLog.dinnerSaturatedFatIntake + identifiedDish.saturatedFat,
                                dinnerLog.dinnerCarbohydratesIntake + identifiedDish.carbohydrates,
                                dinnerLog.dinnerSugarsIntake + identifiedDish.sugars,
                                dinnerLog.dinnerSaltIntake + identifiedDish.salt,
                                dinnerLog.dinnerFibreIntake + identifiedDish.fibre,
                                dinnerLog.dinnerProteinIntake + identifiedDish.protein
                            )
                        )
                    }
                }
            }

            // If there is no entry for the given date in the DinnerLog table yet, insert a new entry.
            if (!dinnerLogInstanceExists) {

                Log.d(TAG, "dinnerLogInstanceExists: $dinnerLogInstanceExists")

                viewModel.insertDinnerLog(
                    DinnerLog(
                        logDate,
                        identifiedDish!!.calories,
                        identifiedDish.totalFat,
                        identifiedDish.saturatedFat,
                        identifiedDish.carbohydrates,
                        identifiedDish.sugars,
                        identifiedDish.salt,
                        identifiedDish.fibre,
                        identifiedDish.protein
                    )
                )
            }
        }
    }
}
