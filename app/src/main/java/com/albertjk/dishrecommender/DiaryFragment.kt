package com.albertjk.dishrecommender

import android.app.DatePickerDialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.android.synthetic.main.fragment_diary.*
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


class DiaryFragment : Fragment(), View.OnClickListener {

    private val TAG = DiaryFragment::class.qualifiedName

    private lateinit var navController: NavController

    private lateinit var viewModel: ViewModel

    /* This variable will store the selected date which is displayed in the selectedDateButton.
    The default is the current date, but the user can change this by clicking on the selectedDateButton and choosing from a date picker. */
    private var calendar = Calendar.getInstance()
    private var selectedDateString =
        DateFormat.getDateInstance(DateFormat.FULL).format(calendar.time)

    // This variable will store the selected date to be passed to WeeklyNutritionLineChartFragment.
    private var selectedDate = Calendar.getInstance()

    /* This variable stores the desired date format:
    Weekday name, day number, month name, and year number. */
    private val dateFormat = SimpleDateFormat("EE, dd MMM yyyy", Locale.UK)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_diary, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        /* Make the bottom navigation visible after the user has been navigated here from the
        Crop Image or the Daily Nutrition or Weekly Nutrition screens. */
        val bottomNavigationView =
            activity?.findViewById<BottomNavigationView>(R.id.bottomNavigationView)
        if (bottomNavigationView != null) {
            bottomNavigationView.visibility = View.VISIBLE
        }

        // Instantiate the navController which has a reference to the navigation graph.
        navController = Navigation.findNavController(view)

        Log.d(
            TAG,
            "arguments?.getString(\"selectedDateString\"): " + arguments?.getString("selectedDateString")
        )

        /* If the fragment was re-created after selecting a new date, get the selected date
        from the bundle. */
        if (savedInstanceState != null && savedInstanceState["selectedDateString"] != null) {
            selectedDateString = savedInstanceState.getString("selectedDateString")
        }
        /* Or, if a favourite dish was added to the diary, so the user was redirected from the
        Favourites screen to the Diary screen, set the date the dish was logged for and show all logs
        for that date. Otherwise, the selected date remains the current date set by default. */
        else if (arguments?.getString("selectedDateString") != null) {
            Log.d(TAG, "selectedDateString argument is not null")
            selectedDateString = arguments?.getString("selectedDateString")
        }

        selectedDateButton.text = selectedDateString

        Log.d(TAG, "selectedDateString: $selectedDateString")

        // Assign click listeners to the buttons on this screen.
        selectedDateButton.setOnClickListener(this)
        leftArrowButton.setOnClickListener(this)
        rightArrowButton.setOnClickListener(this)
        weeklyNutritionButton.setOnClickListener(this)
        dailyNutritionButton.setOnClickListener(this)
        addBreakfastDishInDiaryButton.setOnClickListener(this)
        addLunchDishInDiaryButton.setOnClickListener(this)
        addDinnerDishInDiaryButton.setOnClickListener(this)

        // Assign the ViewModel.
        viewModel = activity?.let { ViewModelProvider(it).get(ViewModel::class.java) }!!

        // Set the breakfast, lunch, and dinner dishes' recycler views' layout managers and adapters.
        val breakfastDishesRecyclerView: RecyclerView =
            view.findViewById(R.id.breakfastDishesRecyclerView)
        val breakfastDishesAdapter = activity?.let {
            MealDiaryAdapter(
                it,
                viewModel,
                "breakfast",
                dateFormat.parse(selectedDateString) as Date
            )
        }
        breakfastDishesRecyclerView.layoutManager = LinearLayoutManager(activity)
        breakfastDishesRecyclerView.adapter = breakfastDishesAdapter

        val lunchDishesRecyclerView: RecyclerView = view.findViewById(R.id.lunchDishesRecyclerView)
        val lunchDishesAdapter = activity?.let {
            MealDiaryAdapter(
                it,
                viewModel,
                "lunch",
                dateFormat.parse(selectedDateString) as Date
            )
        }
        lunchDishesRecyclerView.layoutManager = LinearLayoutManager(activity)
        lunchDishesRecyclerView.adapter = lunchDishesAdapter

        val dinnerDishesRecyclerView: RecyclerView =
            view.findViewById(R.id.dinnerDishesRecyclerView)
        val dinnerDishesAdapter = activity?.let {
            MealDiaryAdapter(
                it,
                viewModel,
                "dinner",
                dateFormat.parse(selectedDateString) as Date
            )
        }
        dinnerDishesRecyclerView.layoutManager = LinearLayoutManager(activity)
        dinnerDishesRecyclerView.adapter = dinnerDishesAdapter

        // Add divider item decorations between each recycler view's rows.
        breakfastDishesRecyclerView.addItemDecoration(
            DividerItemDecoration(
                breakfastDishesRecyclerView.context,
                (breakfastDishesRecyclerView.layoutManager as LinearLayoutManager).orientation
            )
        )
        lunchDishesRecyclerView.addItemDecoration(
            DividerItemDecoration(
                lunchDishesRecyclerView.context,
                (lunchDishesRecyclerView.layoutManager as LinearLayoutManager).orientation
            )
        )
        dinnerDishesRecyclerView.addItemDecoration(
            DividerItemDecoration(
                dinnerDishesRecyclerView.context,
                (dinnerDishesRecyclerView.layoutManager as LinearLayoutManager).orientation
            )
        )

        val allDishes = viewModel.allDishes.value

        /* Access the LiveData objects. They will be scoped to the lifecycle of the fragment's view.
        Get all logged dishes, daily logs, and breakfast, lunch, and dinner logs from the database. */
        viewModel.dishLogs.observe(viewLifecycleOwner, Observer { dishLogList ->
            viewModel.dailyLogs.observe(viewLifecycleOwner, Observer { dailyLogList ->
                viewModel.breakfastLogs.observe(viewLifecycleOwner, Observer { breakfastLogList ->
                    viewModel.lunchLogs.observe(viewLifecycleOwner, Observer { lunchLogList ->
                        viewModel.dinnerLogs.observe(viewLifecycleOwner, Observer { dinnerLogList ->

                            Log.d(TAG, "dishLogList: $dishLogList")
                            Log.d(TAG, "dailyLogList: $dailyLogList")
                            Log.d(TAG, "breakfastLogList: $breakfastLogList")
                            Log.d(TAG, "lunchLogList: $lunchLogList")
                            Log.d(TAG, "dinnerLogList: $dinnerLogList")

                            /* Populate the recycler view rows of logged breakfast, lunch, and dinner
                            dishes for the selected date. */

                            /* Based on the IDs of the logged breakfast, lunch, and dinner dishes,
                            get their nutrition data from the Dish table. */
                            val loggedBreakfastDishes: ArrayList<Dish> = ArrayList()
                            val loggedLunchDishes: ArrayList<Dish> = ArrayList()
                            val loggedDinnerDishes: ArrayList<Dish> = ArrayList()

                            for (dishLog in dishLogList) {
                                for (dish in allDishes!!) {
                                    if (dish.dishId == dishLog.dishId && dishLog.logDate == dateFormat.parse(
                                            selectedDateString
                                        ) as Date
                                    ) {
                                        when (dishLog.mealName) {
                                            "breakfast" -> loggedBreakfastDishes.add(dish)
                                            "lunch" -> loggedLunchDishes.add(dish)
                                            "dinner" -> loggedDinnerDishes.add(dish)
                                        }
                                    }
                                }
                            }

                            // Update the cached copies of the daily logged breakfast, lunch, and dinner dishes in the adapters.
                            breakfastDishesAdapter!!.setData(
                                loggedBreakfastDishes,
                                dishLogList,
                                dailyLogList,
                                breakfastLogList,
                                lunchLogList,
                                dinnerLogList
                            )
                            lunchDishesAdapter!!.setData(
                                loggedLunchDishes,
                                dishLogList,
                                dailyLogList,
                                breakfastLogList,
                                lunchLogList,
                                dinnerLogList
                            )
                            dinnerDishesAdapter!!.setData(
                                loggedDinnerDishes,
                                dishLogList,
                                dailyLogList,
                                breakfastLogList,
                                lunchLogList,
                                dinnerLogList
                            )
                        })
                    })
                })
            })
        })
    }

    override fun onClick(v: View?) {
        when (v) {
            selectedDateButton -> {
                // Show a date picker dialog.
                val datePicker = activity?.let {
                    DatePickerDialog(
                        it,
                        { _, year, month, dayOfMonth ->

                            // Get the selected date and display it in the selectedDateButton.
                            selectedDate.set(Calendar.YEAR, year)
                            selectedDate.set(Calendar.MONTH, month)
                            selectedDate.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                            selectedDateString =
                                DateFormat.getDateInstance(DateFormat.FULL)
                                    .format(selectedDate.time)

                            /* Re-create this fragment to display in the recycler views the dishes
                            logged for the selected date. */
                            this.onViewCreated(
                                this.requireView(),
                                bundleOf("selectedDateString" to selectedDateString)
                            )

                            Log.d(TAG, "selectedDateString: $selectedDateString")
                        },
                        // When the dialog opens, the date is set to be the current date.
                        calendar.get(Calendar.YEAR),
                        calendar.get(Calendar.MONTH),
                        calendar.get(Calendar.DAY_OF_MONTH)
                    )
                }
                datePicker!!.show()
            }
            leftArrowButton -> {
                // Move the date backward by one day. Update selectedDateString.

                // Set the current date.
                val dateOneDayBefore = Calendar.getInstance()
                dateOneDayBefore.time = dateFormat.parse(selectedDateString) as Date

                // Subtract one day from it.
                dateOneDayBefore.add(Calendar.DAY_OF_MONTH, -1)

                selectedDate = dateOneDayBefore

                selectedDateString =
                    DateFormat.getDateInstance(DateFormat.FULL)
                        .format(dateOneDayBefore.time)

                /* Re-create this fragment to display in the recycler views the dishes logged for
                one day before the currently selected date. */
                this.onViewCreated(
                    this.requireView(),
                    bundleOf("selectedDateString" to selectedDateString)
                )
            }
            rightArrowButton -> {
                // Move the date forward by one day. Update selectedDateString.

                // Set the current date.
                val dateOneDayAfter = Calendar.getInstance()
                dateOneDayAfter.time = dateFormat.parse(selectedDateString) as Date

                // Add one day to it.
                dateOneDayAfter.add(Calendar.DAY_OF_MONTH, +1)

                selectedDate = dateOneDayAfter

                selectedDateString =
                    DateFormat.getDateInstance(DateFormat.FULL)
                        .format(dateOneDayAfter.time)

                /* Re-create this fragment to display in the recycler views the dishes logged for
                one day after the currently selected date. */
                this.onViewCreated(
                    this.requireView(),
                    bundleOf("selectedDateString" to selectedDateString)
                )
            }
            weeklyNutritionButton -> {
                val bundle = bundleOf(
                    "selectedDate" to selectedDate
                )
                navController.navigate(
                    R.id.action_diaryFragment_to_weeklyNutritionLineChartFragment,
                    bundle
                )
            }
            dailyNutritionButton -> {
                val bundle = bundleOf(
                    "selectedDateString" to selectedDateString
                )
                navController.navigate(
                    R.id.action_diaryFragment_to_dailyNutritionPieChartFragment,
                    bundle
                )
            }
            addBreakfastDishInDiaryButton -> {
                val bundle = bundleOf(
                    "isBeingAddedToDiary" to true,
                    "mealName" to "breakfast",
                    "logDate" to dateFormat.parse(selectedDateString) as Date
                )
                Log.d(TAG, "logDate: $selectedDateString")
                navController.navigate(
                    R.id.action_diaryFragment_to_cameraFragment,
                    bundle
                )
            }
            addLunchDishInDiaryButton -> {
                val bundle = bundleOf(
                    "isBeingAddedToDiary" to true,
                    "mealName" to "lunch",
                    "logDate" to dateFormat.parse(selectedDateString) as Date
                )
                navController.navigate(
                    R.id.action_diaryFragment_to_cameraFragment,
                    bundle
                )
            }
            addDinnerDishInDiaryButton -> {
                val bundle = bundleOf(
                    "isBeingAddedToDiary" to true,
                    "mealName" to "dinner",
                    "logDate" to dateFormat.parse(selectedDateString) as Date
                )
                navController.navigate(
                    R.id.action_diaryFragment_to_cameraFragment,
                    bundle
                )
            }
        }
    }
}