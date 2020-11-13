package com.albertjk.dishrecommender

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.android.synthetic.main.fragment_weekly_nutrition_line_chart.*
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.temporal.TemporalAdjusters
import java.time.temporal.WeekFields
import java.util.*
import kotlin.collections.ArrayList

class WeeklyNutritionLineChartFragment : Fragment(), View.OnClickListener {

    private val TAG = WeeklyNutritionLineChartFragment::class.qualifiedName

    private lateinit var viewModel: ViewModel

    // This variable will store the currently selected nutrient's name. Initially, this is calories.
    private var selectedNutrient = "Calories"

    /* This variable stores the desired date format:
    Weekday name, day number, month name, and year number. */
    private val dateFormat = SimpleDateFormat("EE, dd MMM yyyy", Locale.UK)

    // This variable will store the selected date in a LocalDate format.
    private var selectedDateAsLocalDate: LocalDate? = null

    // This variable stores the locale for getting the first and last days of the week. It is set to be the UK.
    private val locale: Locale? = Locale.UK

    // These variables store the first and last days of the week that in the UK are Monday and Sunday, respectively.
    private var firstDayOfWeek = WeekFields.of(locale).firstDayOfWeek
    private var lastDayOfWeek =
        DayOfWeek.of(((firstDayOfWeek!!.value + 5) % DayOfWeek.values().size) + 1)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_weekly_nutrition_line_chart, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Make the bottom navigation disappear until this screen is visible.
        val bottomNavigationView =
            activity?.findViewById<BottomNavigationView>(R.id.bottomNavigationView)
        if (bottomNavigationView != null) {
            bottomNavigationView.visibility = View.GONE
        }

        /* If the fragment was re-created after selecting a different nutrient by clicking on one of the buttons,
        get the selected nutrient from the bundle. */
        if (savedInstanceState != null && savedInstanceState["selectedNutrient"] != null) {
            selectedNutrient = savedInstanceState.getString("selectedNutrient")!!
        }

        Log.d(TAG, "selectedNutrient: $selectedNutrient")

        // Assign the ViewModel.
        viewModel = activity?.let { ViewModelProvider(it).get(ViewModel::class.java) }!!

        // Assign click listeners to the buttons representing the nutrients.
        caloriesLineChartButton.setOnClickListener(this)
        totalFatLineChartButton.setOnClickListener(this)
        saturatedFatLineChartButton.setOnClickListener(this)
        carbsLineChartButton.setOnClickListener(this)
        sugarsLineChartButton.setOnClickListener(this)
        saltLineChartButton.setOnClickListener(this)
        fibreLineChartButton.setOnClickListener(this)
        proteinLineChartButton.setOnClickListener(this)

        // Get the selected date passed to this fragment.
        val selectedDate = arguments?.getSerializable("selectedDate") as Calendar

        Log.d(TAG, "selectedDate.time: ${selectedDate.time}")

        // Convert selectedDate from a Calendar object to a LocalDate object.
        selectedDateAsLocalDate =
            LocalDateTime.ofInstant(selectedDate.toInstant(), selectedDate.timeZone.toZoneId())
                .toLocalDate()

        // Get the start and end date of the week associated with the selected date.
        val firstDayOfThisWeek = getFirstDayOfWeek()
        val lastDayOfThisWeek = getLastDayOfWeek()

        Log.d(TAG, "firstDayOfThisWeek: $firstDayOfThisWeek")
        Log.d(TAG, "lastDayOfThisWeek: $lastDayOfThisWeek")

        // Convert the LocalDate objects to Calendar objects.
        val firstDayOfTheWeek = Calendar.getInstance()
        firstDayOfTheWeek.set(Calendar.YEAR, firstDayOfThisWeek.year)
        firstDayOfTheWeek.set(Calendar.MONTH, firstDayOfThisWeek.monthValue - 1)
        firstDayOfTheWeek.set(Calendar.DAY_OF_MONTH, firstDayOfThisWeek.dayOfMonth)

        val lastDayOfTheWeek = Calendar.getInstance()
        lastDayOfTheWeek.set(Calendar.YEAR, lastDayOfThisWeek.year)
        lastDayOfTheWeek.set(Calendar.MONTH, lastDayOfThisWeek.monthValue - 1)
        lastDayOfTheWeek.set(Calendar.DAY_OF_MONTH, lastDayOfThisWeek.dayOfMonth)

        Log.d(TAG, "firstDayOfTheWeek: ${firstDayOfTheWeek.time}")
        Log.d(TAG, "lastDayOfTheWeek: ${lastDayOfTheWeek.time}")

        // Convert the Calendar objects to String variables.
        val firstDayOfTheWeekString =
            DateFormat.getDateInstance(DateFormat.FULL).format(firstDayOfTheWeek.time)
        val lastDayOfTheWeekString =
            DateFormat.getDateInstance(DateFormat.FULL).format(lastDayOfTheWeek.time)

        Log.d(TAG, "firstDayOfTheWeekString: $firstDayOfTheWeekString")
        Log.d(TAG, "lastDayOfTheWeekString: $lastDayOfTheWeekString")

        // Display the selected week near the top of the screen.
        weeklyNutritionDateRange.text = "$firstDayOfTheWeekString -\n $lastDayOfTheWeekString"

        // This list wil store the dates as strings for the seven days.
        val weekDays: ArrayList<String> = ArrayList()
        weekDays.add(firstDayOfTheWeekString)
        firstDayOfTheWeek.add(Calendar.HOUR_OF_DAY, 24)
        weekDays.add(DateFormat.getDateInstance(DateFormat.FULL).format(firstDayOfTheWeek.time))
        firstDayOfTheWeek.add(Calendar.HOUR_OF_DAY, 24)
        weekDays.add(DateFormat.getDateInstance(DateFormat.FULL).format(firstDayOfTheWeek.time))
        firstDayOfTheWeek.add(Calendar.HOUR_OF_DAY, 24)
        weekDays.add(DateFormat.getDateInstance(DateFormat.FULL).format(firstDayOfTheWeek.time))
        firstDayOfTheWeek.add(Calendar.HOUR_OF_DAY, 24)
        weekDays.add(DateFormat.getDateInstance(DateFormat.FULL).format(firstDayOfTheWeek.time))
        firstDayOfTheWeek.add(Calendar.HOUR_OF_DAY, 24)
        weekDays.add(DateFormat.getDateInstance(DateFormat.FULL).format(firstDayOfTheWeek.time))
        weekDays.add(lastDayOfTheWeekString)

        Log.d(TAG, "weekDays: $weekDays")

        // This list will store the daily logs for each of the seven days.
        val weekDailyLogs: ArrayList<DailyLog> = ArrayList()

        // Get all daily logs from the database.
        val dailyLogs = viewModel.dailyLogs.value!!

        /* Get the total daily intakes of each nutrient for the seven days of the week.
        If there are no logs for a given day, store 0 for each nutrient for that day. */
        for (weekDay in weekDays) {
            var dailyLogAdded = false
            for (dailyLog in dailyLogs) {
                if (dateFormat.parse(weekDay) as Date == dailyLog.logDate) {
                    weekDailyLogs.add(dailyLog)
                    dailyLogAdded = true
                    break
                }
            }
            if (!dailyLogAdded)
                weekDailyLogs.add(
                    DailyLog(
                        dateFormat.parse(weekDay) as Date,
                        0,
                        0f,
                        0f,
                        0f,
                        0f,
                        0f,
                        0f,
                        0f
                    )
                )
        }
        
        /* Depending on the selectedNutrient, get the daily intake of that nutrient for each day of the week.
        Also, calculate the RI values for that nutrient for each day. */
        val intakeOfSelectedNutrientPerDay: ArrayList<Float> = ArrayList()
        val riValuesOfSelectedNutrientPerDay: ArrayList<Float> = ArrayList()

        for (weekDailyLog in weekDailyLogs) {
            intakeOfSelectedNutrientPerDay.add(weekDailyLog.getProperty(selectedNutrient))
            riValuesOfSelectedNutrientPerDay.add((weekDailyLog.getProperty(selectedNutrient) / getDailyRefConstant(selectedNutrient) * 100).toFloat())
        }

        // Calculate the average daily intake in RI% and grams of the nutrient.
        val averageDailyRIOfSelectedNutrient = riValuesOfSelectedNutrientPerDay.sum() / riValuesOfSelectedNutrientPerDay.size
        var averageDailyIntakeOfSelectedNutrient = intakeOfSelectedNutrientPerDay.sum() / intakeOfSelectedNutrientPerDay.size

        // Round the average daily intake of the nutrient to two decimal places.
        averageDailyIntakeOfSelectedNutrient = (Math.round(averageDailyIntakeOfSelectedNutrient * 1000.0) / 1000.0).toFloat()

        // Display the average daily intake of the nutrient at the bottom of the screen.
        
        // If the selected nutrient is Calories, it is not measured in grams. But every other nutrient is.
        var gram = ""
        if (selectedNutrient != "Calories") {
            gram = "g"
        }
        
        averageDailyIntake.text = "Average daily " + selectedNutrient.toLowerCase() + " intake: " + averageDailyRIOfSelectedNutrient.toInt() + "% RI (" + averageDailyIntakeOfSelectedNutrient + gram + ")"
        
        // Draw the line chart on the screen.
        drawLineChart(view.findViewById(R.id.lineChart), getDataValues(riValuesOfSelectedNutrientPerDay))
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
     * Sets the nutrient reference intake percentage data for each day of the week to be displayed in the line chart.
     */
    private fun getDataValues(riValuesOfSelectedNutrientPerDay: ArrayList<Float>): ArrayList<Entry> {

        val dataValues: ArrayList<Entry> = ArrayList()

        for (i in 0 until riValuesOfSelectedNutrientPerDay.size) {
            dataValues.add(Entry(i.toFloat(), riValuesOfSelectedNutrientPerDay[i]))
        }

        return dataValues
    }

    /**
     * Returns the date of the first day of the week with regards to the selected date.
     */
    private fun getFirstDayOfWeek() = LocalDate.of(
        selectedDateAsLocalDate!!.year,
        selectedDateAsLocalDate!!.month,
        selectedDateAsLocalDate!!.dayOfMonth
    ).with(TemporalAdjusters.previousOrSame(firstDayOfWeek))

    /**
     * Returns the date of the last day of the week with regards to the selected date.
     */
    private fun getLastDayOfWeek() = LocalDate.of(
        selectedDateAsLocalDate!!.year,
        selectedDateAsLocalDate!!.month,
        selectedDateAsLocalDate!!.dayOfMonth
    ).with(TemporalAdjusters.nextOrSame(lastDayOfWeek))

    /**
     * Draws the line chart, including the associated labels and the legend, on the screen.
     */
    private fun drawLineChart(lineChart: LineChart, dataValues: ArrayList<Entry>) {

        // All touch interactions with the chart are disabled.
        lineChart.setTouchEnabled(false)

        // There will be no description label at the bottom of the chart.
        lineChart.description.isEnabled = false

        // Set the data.
        val lineDataSet = LineDataSet(dataValues,"$selectedNutrient intake on each day of the week")

        // Set the line colour and width..
        lineDataSet.color = resources.getColor(R.color.colorBarBlue)
        lineDataSet.lineWidth = 2f

        // For each value in the line chart, a circle is drawn on the chart with colorPrimaryDark colour.
        lineDataSet.setCircleColor(resources.getColor(R.color.colorBarBlue))

        // There will be no hole in the middle of the circles.
        lineDataSet.setDrawCircleHole(false)

        // Set the radius of the circles.
        lineDataSet.circleRadius = 4.5f

        val dataSets: ArrayList<ILineDataSet> = ArrayList()
        dataSets.add(lineDataSet)

        // Set the RI% data above the chart and the size of the RI labels.
        val lineData = LineData(dataSets)
        lineData.setValueFormatter(RILabelValueFormatter())
        lineData.setValueTextSize(14f)

        // Add the data to the chart.
        lineChart.data = lineData

        // Set the X axis value formatter. Set the weekday names as labels for this axis.
        val weekdayNames: List<String> = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
        val xAxis: XAxis = lineChart.xAxis
        xAxis.valueFormatter = IndexAxisValueFormatter(weekdayNames)

        // Set the text size of the weekday name labels on the X axis in dp.
        xAxis.textSize = 12f

        // Position the weekday name labels to the bottom of the chart.
        xAxis.position = XAxis.XAxisPosition.BOTTOM

        // Left and right axis lines and labels are not displayed.
        val left: YAxis = lineChart.axisLeft
        left.setDrawGridLines(false)
        left.setDrawAxisLine(false)
        left.setDrawLabels(false)

        val right: YAxis = lineChart.axisRight
        right.setDrawGridLines(false)
        right.setDrawAxisLine(false)
        right.setDrawLabels(false)

        lineChart.isAutoScaleMinMaxEnabled = true

        lineChart.legend.textSize = 12f
        lineChart.legend.verticalAlignment = Legend.LegendVerticalAlignment.TOP
        lineChart.legend.horizontalAlignment = Legend.LegendHorizontalAlignment.CENTER

        // Set some offset around the chart.
        lineChart.setExtraOffsets(24f, 16f, 24f, 0f)

        lineChart.animateXY(800, 800)

        // Redraw (refresh) the chart.
        lineChart.invalidate()
    }

    /**
     * When a button of a nutrient's name is clicked, this fragment is re-created to display
     * the data and the line chart relevant to the clicked nutrient.
     */
    override fun onClick(v: View?) {
        when (v) {
            caloriesLineChartButton -> {
                selectedNutrient = "Calories"

                this.onViewCreated(
                    this.requireView(),
                    bundleOf("selectedNutrient" to selectedNutrient)
                )
            }
            totalFatLineChartButton -> {
                selectedNutrient = "Fat"

                this.onViewCreated(
                    this.requireView(),
                    bundleOf("selectedNutrient" to selectedNutrient)
                )
            }
            saturatedFatLineChartButton -> {
                selectedNutrient = "Sat. fat"

                this.onViewCreated(
                    this.requireView(),
                    bundleOf("selectedNutrient" to selectedNutrient)
                )
            }
            carbsLineChartButton -> {
                selectedNutrient = "Carbs"

                this.onViewCreated(
                    this.requireView(),
                    bundleOf("selectedNutrient" to selectedNutrient)
                )
            }
            sugarsLineChartButton -> {
                selectedNutrient = "Sugars"

                this.onViewCreated(
                    this.requireView(),
                    bundleOf("selectedNutrient" to selectedNutrient)
                )
            }
            saltLineChartButton -> {
                selectedNutrient = "Salt"

                this.onViewCreated(
                    this.requireView(),
                    bundleOf("selectedNutrient" to selectedNutrient)
                )
            }
            fibreLineChartButton -> {
                selectedNutrient = "Fibre"

                this.onViewCreated(
                    this.requireView(),
                    bundleOf("selectedNutrient" to selectedNutrient)
                )
            }
            proteinLineChartButton -> {
                selectedNutrient = "Protein"

                this.onViewCreated(
                    this.requireView(),
                    bundleOf("selectedNutrient" to selectedNutrient)
                )
            }
        }
    }
}