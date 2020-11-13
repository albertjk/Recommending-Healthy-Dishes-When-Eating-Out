package com.albertjk.dishrecommender

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.LegendEntry
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.android.synthetic.main.fragment_daily_nutrition_pie_chart.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


class DailyNutritionPieChartFragment : Fragment(), View.OnClickListener {

    private val TAG = DailyNutritionPieChartFragment::class.qualifiedName

    private lateinit var viewModel: ViewModel

    private lateinit var pieColours: ArrayList<Int>

    // These variables will store the breakfast log, lunch log, and dinner log retrieved for the specified date.
    private var selectedBreakfastLog: BreakfastLog? = null
    private var selectedLunchLog: LunchLog? = null
    private var selectedDinnerLog: DinnerLog? = null

    // This variable will store the currently selected nutrient's name. Initially, this is calories.
    private var selectedNutrient = "Calories"

    // This variable will store the total daily reference intake of the selected nutrient.
    private var selectedNutrientTotalRI: Float = 0f

    // These variables will store the reference intakes for breakfast, lunch, and dinner of the selected nutrient.
    private var selectedNutrientBreakfastRI: Float = 0f
    private var selectedNutrientLunchRI: Float = 0f
    private var selectedNutrientDinnerRI: Float = 0f

    /* These variables will store the total daily intake and the breakfast, lunch, and dinner intakes
    of the selected nutrient in grams (except for calories). */
    private var selectedNutrientTotalIntake: String? = "0"
    private var selectedNutrientBreakfastIntake: String? = "0"
    private var selectedNutrientLunchIntake: String? = "0"
    private var selectedNutrientDinnerIntake: String? = "0"

    /* This variable stores the desired date format:
    Weekday name, day number, month name, and year number. */
    private val dateFormat = SimpleDateFormat("EE, dd MMM yyyy", Locale.UK)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_daily_nutrition_pie_chart, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Make the bottom navigation disappear until this screen is visible.
        val bottomNavigationView =
            activity?.findViewById<BottomNavigationView>(R.id.bottomNavigationView)
        if (bottomNavigationView != null) {
            bottomNavigationView.visibility = View.GONE
        }

        Log.d(TAG, "savedInstanceState: $savedInstanceState")

        /* If the fragment was re-created after selecting a different nutrient by clicking on one of the buttons,
        get the selected nutrient from the bundle. */
        if (savedInstanceState != null && savedInstanceState["selectedNutrient"] != null) {
            selectedNutrient = savedInstanceState.getString("selectedNutrient")!!
        }

        Log.d(TAG, "selectedNutrient: $selectedNutrient")

        // Assign the ViewModel.
        viewModel = activity?.let { ViewModelProvider(it).get(ViewModel::class.java) }!!

        // Assign click listeners to the buttons representing the nutrients.
        caloriesPieChartButton.setOnClickListener(this)
        totalFatPieChartButton.setOnClickListener(this)
        saturatedFatPieChartButton.setOnClickListener(this)
        carbsPieChartButton.setOnClickListener(this)
        sugarsPieChartButton.setOnClickListener(this)
        saltPieChartButton.setOnClickListener(this)
        fibrePieChartButton.setOnClickListener(this)
        proteinPieChartButton.setOnClickListener(this)

        // Get the selected date passed to this fragment.
        val selectedDateString = arguments?.getString("selectedDateString")!!

        // Display the selected date near the top of the screen.
        dailyNutritionDate.text = selectedDateString

        // Get the daily log for the specified date.
        val dailyLogs = viewModel.dailyLogs.value!!
        var selectedDailyLog: DailyLog? = null
        for (dailyLog in dailyLogs) {
            if (dailyLog.logDate == dateFormat.parse(selectedDateString) as Date) {
                selectedDailyLog = dailyLog
                break
            }
        }

        // Get the breakfast log, lunch log, and dinner log for the specified date.
        val breakfastLogs = viewModel.breakfastLogs.value!!
        for (breakfastLog in breakfastLogs) {
            if (breakfastLog.logDate == dateFormat.parse(selectedDateString) as Date) {
                selectedBreakfastLog = breakfastLog
                break
            }
        }

        val lunchLogs = viewModel.lunchLogs.value!!
        for (lunchLog in lunchLogs) {
            if (lunchLog.logDate == dateFormat.parse(selectedDateString) as Date) {
                selectedLunchLog = lunchLog
                break
            }
        }

        val dinnerLogs = viewModel.dinnerLogs.value!!
        for (dinnerLog in dinnerLogs) {
            if (dinnerLog.logDate == dateFormat.parse(selectedDateString) as Date) {
                selectedDinnerLog = dinnerLog
                break
            }
        }

        Log.d(TAG, "selectedDailyLog: $selectedDailyLog")
        Log.d(TAG, "selectedBreakfastLog: $selectedBreakfastLog")
        Log.d(TAG, "selectedLunchLog: $selectedLunchLog")
        Log.d(TAG, "selectedDinnerLog: $selectedDinnerLog")

        // If the selected nutrient is Calories, it is not measured in grams. But every other nutrient is.
        var gram = ""
        if (selectedNutrient != "Calories") {
            gram = "g"
        }

        /* Depending on what meals contain logs and the selected nutrient, calculate the reference intake percentage,
        and set the logged total, and the breakfast, lunch, and dinner values. */
        if (selectedDailyLog != null) {
            selectedNutrientTotalRI =
                (selectedDailyLog.getProperty(selectedNutrient) / getDailyRefConstant(
                    selectedNutrient
                ) * 100).toFloat()

            // Round the total daily intake of the nutrient to two decimal places.
            selectedNutrientTotalIntake =
                (Math.round(selectedDailyLog.getProperty(selectedNutrient) * 1000.0) / 1000.0).toString()
        }

        dailyTotalIntake.text =
            "Total daily ${selectedNutrient.toLowerCase()} intake: " + selectedNutrientTotalRI.toInt()
                .toString() + "% RI (" + selectedNutrientTotalIntake + gram + ")"

        if (selectedBreakfastLog != null) {
            selectedNutrientBreakfastRI =
                (selectedBreakfastLog!!.getProperty(selectedNutrient) / getDailyRefConstant(
                    selectedNutrient
                ) * 100).toFloat()
            selectedNutrientBreakfastIntake =
                selectedBreakfastLog!!.getProperty(selectedNutrient).toInt().toString() + gram
        }
        if (selectedLunchLog != null) {
            selectedNutrientLunchRI =
                (selectedLunchLog!!.getProperty(selectedNutrient) / getDailyRefConstant(
                    selectedNutrient
                ) * 100).toFloat()
            selectedNutrientLunchIntake =
                selectedLunchLog!!.getProperty(selectedNutrient).toInt().toString() + gram
        }
        if (selectedDinnerLog != null) {
            selectedNutrientDinnerRI =
                (selectedDinnerLog!!.getProperty(selectedNutrient) / getDailyRefConstant(
                    selectedNutrient
                ) * 100).toFloat()
            selectedNutrientDinnerIntake =
                selectedDinnerLog!!.getProperty(selectedNutrient).toInt().toString() + gram
        }

        // Draw the pie chart on the screen.
        drawPieChart(view.findViewById(R.id.pieChart))
    }

    /**
     * Draws the pie chart, including the pie slices, the associated labels, and the legend, on the screen.
     */
    private fun drawPieChart(pieChart: PieChart) {

        // The values in PieEntry will be displayed as percentage values.
        pieChart.setUsePercentValues(true)

        // All touch interactions with the chart are disabled.
        pieChart.setTouchEnabled(false)

        /* Meal reference intake data are added to the chart in the following order: breakfast data,
        lunch data, and dinner data. To prevent errors, the data are only added if a given meal contains logs. */
        val values: ArrayList<PieEntry> = ArrayList()

        if (selectedBreakfastLog != null) {
            values.add(PieEntry(selectedNutrientBreakfastRI, "Breakfast"))
        }
        if (selectedLunchLog != null) {
            values.add(PieEntry(selectedNutrientLunchRI, "Lunch"))
        }
        if (selectedDinnerLog != null) {
            values.add(PieEntry(selectedNutrientDinnerRI, "Dinner"))
        }

        val pieDataSet = PieDataSet(values, "Nutrition per meal")

        // Show the %RI values inside the pie slices.
        pieDataSet.setDrawValues(true)

        // Do not show the meal name labels inside the pie slices.
        pieChart.setDrawEntryLabels(false)

        // There will be no description label at the bottom of the chart.
        pieChart.description.isEnabled = false

        // There will be no hole in the middle of the pie chart.
        pieChart.isDrawHoleEnabled = false

        /* Set a custom legend to indicate what meals the types of colours mean.
        Only show those labels and colours in the legend which have pie slices in the chart. */
        val legendList: ArrayList<LegendEntry> = ArrayList()

        // Also, set the colours for the pie slices.
        pieColours = ArrayList()

        for (value in values) {
            val legendEntry = LegendEntry()
            when (value.label) {
                "Breakfast" -> {
                    legendEntry.label =
                        "Breakfast: " + value.value.toInt() + "% RI (" + selectedNutrientBreakfastIntake + ")"
                    legendEntry.formColor = resources.getColor(R.color.colorPieBreakfast)

                    pieColours.add(resources.getColor(R.color.colorPieBreakfast))
                }
                "Lunch" -> {
                    legendEntry.label =
                        "Lunch: " + value.value.toInt() + "% RI (" + selectedNutrientLunchIntake + ")"
                    legendEntry.formColor = resources.getColor(R.color.colorPieLunch)

                    pieColours.add(resources.getColor(R.color.colorPieLunch))
                }
                "Dinner" -> {
                    legendEntry.label =
                        "Dinner: " + value.value.toInt() + "% RI (" + selectedNutrientDinnerIntake + ")"
                    legendEntry.formColor = resources.getColor(R.color.colorPieDinner)

                    pieColours.add(resources.getColor(R.color.colorPieDinner))
                }
            }
            legendList.add(legendEntry)
        }
        pieDataSet.colors = pieColours

        // Set the RI% data on the pie slices.
        val pieData = PieData(pieDataSet)
        pieData.setValueFormatter(RILabelValueFormatter())
        pieData.setValueTextSize(16f)
        pieChart.data = pieData

        pieChart.legend.isWordWrapEnabled = true
        pieChart.legend.setCustom(legendList)

        // Set the space between the legend entries.
        pieChart.legend.xEntrySpace = 18f

        pieChart.legend.textSize = 12f
        pieChart.legend.verticalAlignment = Legend.LegendVerticalAlignment.TOP
        pieChart.legend.horizontalAlignment = Legend.LegendHorizontalAlignment.CENTER

        // Set some offset around the chart.
        pieChart.setExtraOffsets(24f, 0f, 24f, 0f)

        // Animate the chart.
        pieChart.animateXY(800, 800)

        // Redraw (refresh) the chart.
        pieChart.invalidate()
    }

    /***
     * Returns the daily reference intake constant value associated with the string nutrient name.
     * This function is called during the calculation of the daily reference intake values for the selected nutrient.
     */
    private fun getDailyRefConstant(nutrientName: String): Double {
        return when (nutrientName) {
            "Calories" -> {
                DailyReferenceIntakeConstants.DAILY_REF_CALORIES_INTAKE
            }
            "Fat" -> {
                DailyReferenceIntakeConstants.DAILY_REF_TOTAL_FAT_INTAKE
            }
            "Sat. fat" -> {
                DailyReferenceIntakeConstants.DAILY_REF_SATURATED_FAT_INTAKE
            }
            "Carbs" -> {
                DailyReferenceIntakeConstants.DAILY_REF_CARBOHYDRATES_INTAKE
            }
            "Sugars" -> {
                DailyReferenceIntakeConstants.DAILY_REF_SUGARS_INTAKE
            }
            "Salt" -> {
                DailyReferenceIntakeConstants.DAILY_REF_SALT_INTAKE
            }
            "Fibre" -> {
                DailyReferenceIntakeConstants.DAILY_REF_FIBRE_INTAKE
            }
            else -> {
                DailyReferenceIntakeConstants.DAILY_REF_PROTEIN_INTAKE
            }
        }
    }

    /**
     * When a button of a nutrient's name is clicked, this fragment is re-created to display
     * the data and the pie chart relevant to the clicked nutrient.
     */
    override fun onClick(v: View?) {
        when (v) {
            caloriesPieChartButton -> {
                selectedNutrient = "Calories"

                this.onViewCreated(
                    this.requireView(),
                    bundleOf("selectedNutrient" to selectedNutrient)
                )
            }
            totalFatPieChartButton -> {
                selectedNutrient = "Fat"

                this.onViewCreated(
                    this.requireView(),
                    bundleOf("selectedNutrient" to selectedNutrient)
                )
            }
            saturatedFatPieChartButton -> {
                selectedNutrient = "Sat. fat"

                this.onViewCreated(
                    this.requireView(),
                    bundleOf("selectedNutrient" to selectedNutrient)
                )
            }
            carbsPieChartButton -> {
                selectedNutrient = "Carbs"

                this.onViewCreated(
                    this.requireView(),
                    bundleOf("selectedNutrient" to selectedNutrient)
                )
            }
            sugarsPieChartButton -> {
                selectedNutrient = "Sugars"

                this.onViewCreated(
                    this.requireView(),
                    bundleOf("selectedNutrient" to selectedNutrient)
                )
            }
            saltPieChartButton -> {
                selectedNutrient = "Salt"

                this.onViewCreated(
                    this.requireView(),
                    bundleOf("selectedNutrient" to selectedNutrient)
                )
            }
            fibrePieChartButton -> {
                selectedNutrient = "Fibre"

                this.onViewCreated(
                    this.requireView(),
                    bundleOf("selectedNutrient" to selectedNutrient)
                )
            }
            proteinPieChartButton -> {
                selectedNutrient = "Protein"

                this.onViewCreated(
                    this.requireView(),
                    bundleOf("selectedNutrient" to selectedNutrient)
                )
            }
        }
    }
}