package com.albertjk.dishrecommender

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.LegendEntry
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.android.synthetic.main.fragment_recommendation_nutrition_bar_chart.*
import java.util.*

class RecommendationNutritionBarChartFragment : Fragment() {

    private val TAG = RecommendationNutritionBarChartFragment::class.qualifiedName

    // The received dish nutrient data will be stored in this object.
    private lateinit var dish: Dish

    private lateinit var barEntryList: ArrayList<BarEntry>

    /* This array list will store the daily reference intake percentages of
    the nutrients of a dish rounded to integers. These values will be the labels
    above the bars in the bar chart. */
    private lateinit var referenceIntakePercentages: ArrayList<Int>

    /* This array list will store the nutrient names. These will be the labels below
    the bars in the bar chart. */
    private lateinit var nutrientNames: ArrayList<String>

    // This array list will store the colours of the bars.
    private lateinit var barColours: ArrayList<Int>

    // This constant represents the boundary value between the acceptable and high values for certain nutrients.
    private val HIGH_RI_CATEGORY_PERCENTAGE = 30

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Get the dish's nutrient data.
        dish = arguments?.getParcelable("dishNutrients")!!

        Log.d(TAG, "Received dish data: $dish")
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(
            R.layout.fragment_recommendation_nutrition_bar_chart,
            container,
            false
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Make the bottom navigation disappear until this screen is visible.
        val bottomNavigationView =
            activity?.findViewById<BottomNavigationView>(R.id.bottomNavigationView)
        if (bottomNavigationView != null) {
            bottomNavigationView.visibility = View.GONE
        }

        // Set the dish name in the layout file.
        barChartTitle.text = "${dish.dishName} - per serving nutrition"

        // Calculate the daily reference intake (RI) percentages for each nutrient of the dish.
        referenceIntakePercentages = calculateRIpercentages(dish)

        nutrientNames = ArrayList(
            listOf(
                "Calories",
                "Fat",
                "Sat. fat",
                "Carbs",
                "Sugars",
                "Salt",
                "Fibre",
                "Protein"
            )
        )

        // Draw the bar chart on the screen.
        drawBarChart(view.findViewById(R.id.barChart))
    }

    /**
     * Draws the bar chart, including the nutrient and reference intake labels, and the legend, on the screen.
     */
    private fun drawBarChart(barChart: BarChart) {

        barEntryList = ArrayList()

        barColours = ArrayList()

        for (i in 0 until referenceIntakePercentages.size) {
            // BarEntry objects are used for setting the data to the bar chart.
            barEntryList.add(BarEntry(i.toFloat(), referenceIntakePercentages[i].toFloat()))
        }

        // Set the bar data and colours.
        assignBarColours()
        val barDataSet = BarDataSet(barEntryList, "Per serving nutrition")
        barDataSet.valueFormatter = RILabelValueFormatter() // Format the RI labels appropriately.
        barDataSet.setColors(barColours.toIntArray(), activity)

        // Set the text size of the RI labels above the bars in dp.
        barDataSet.valueTextSize = 12f

        // There will be no description label at the bottom of the chart.
        barChart.description.isEnabled = false

        // All touch interactions with the chart are disabled.
        barChart.setTouchEnabled(false)

        val barData = BarData(barDataSet)
        barChart.data = barData

        // Set the X axis value formatter. Set the nutrient names for this axis.
        val xAxis: XAxis = barChart.xAxis
        xAxis.valueFormatter = IndexAxisValueFormatter(nutrientNames)

        // Set the text size of the nutrient labels on the X axis in dp.
        xAxis.textSize = 12f

        // Set the position of the nutrient name labels.
        xAxis.position = XAxis.XAxisPosition.BOTTOM

        // X and Y axis lines and labels are not displayed.
        xAxis.setDrawGridLines(false)
        xAxis.setDrawAxisLine(false)
        xAxis.labelCount = nutrientNames.size

        val left: YAxis = barChart.axisLeft
        left.setDrawGridLines(false)
        left.setDrawAxisLine(false)
        left.setDrawLabels(false)
        left.setDrawZeroLine(false)
        left.axisMinimum = 0f

        // Set a custom legend to indicate what the three types of bar colours mean.
        val legendEntryHigh = LegendEntry()
        legendEntryHigh.label = "High"
        legendEntryHigh.formColor = resources.getColor(R.color.colorBarRed)

        val legendEntryAcceptable = LegendEntry()
        legendEntryAcceptable.label = "Acceptable"
        legendEntryAcceptable.formColor = resources.getColor(R.color.colorBarGreen)

        val legendEntryNeitherHighNorLow = LegendEntry()
        legendEntryNeitherHighNorLow.label = "Neither high nor low"
        legendEntryNeitherHighNorLow.formColor = resources.getColor(R.color.colorBarBlue)

        barChart.legend.setCustom(
            listOf(
                legendEntryHigh,
                legendEntryAcceptable,
                legendEntryNeitherHighNorLow
            )
        )

        // Set the space between the legend entries.
        barChart.legend.xEntrySpace = 16f

        // Set the text size of the legend labels in dp.
        barChart.legend.textSize = 11f

        // Set some offset between the chart and the legend.
        barChart.setExtraOffsets(0f, 0f, 0f, 16f)

        barChart.axisRight.isEnabled = false

        // Make the x-axis fit exactly all bars. No bars will be cut off on the sides.
        barChart.setFitBars(true)

        // Set an animation for the Y axis.
        barChart.animateY(800)

        // Redraw (refresh) the chart.
        barChart.invalidate()
    }

    /**
     * Dynamically fills the barColours list list with red, green, or blue,
     * depending on the nutrient name and its value.
     */
    private fun assignBarColours() {

        for (i in 0 until nutrientNames.size) {

            // Certain nutrients' bars will be coloured blue.
            if (nutrientNames[i] == "Calories" || nutrientNames[i] == "Protein" || nutrientNames[i] == "Carbs" || nutrientNames[i] == "Fibre") {
                barColours.add(R.color.colorBarBlue)
            } else {
                /* For other nutrients, if the RI percentage is greater than or equal to 30%,
                the bar is coloured red, meaning the value is high. Otherwise, if it is not 0%,
                it is coloured green, meaning the value is acceptable. */
                if (referenceIntakePercentages[i] >= HIGH_RI_CATEGORY_PERCENTAGE) {
                    barColours.add(R.color.colorBarRed)
                } else if (referenceIntakePercentages[i] in 1 until HIGH_RI_CATEGORY_PERCENTAGE) {
                    barColours.add(R.color.colorBarGreen)
                }
            }
        }
    }

    /**
     * Calculates the daily reference intake (RI) percentage values of a dish, rounds them to integers,
     * and returns them in an array list.
     */
    private fun calculateRIpercentages(dish: Dish): ArrayList<Int> {
        val riPercentages: ArrayList<Int> = ArrayList()

        /* Calculate and add the RI percentage values in the following order:
        calories RI, total fat RI, saturated fat RI, carbohydrates RI, sugars RI, salt RI, fibre RI, and protein RI. */
        riPercentages.add((dish.calories / DailyReferenceIntakeConstants.DAILY_REF_CALORIES_INTAKE * 100).toInt())
        riPercentages.add((dish.totalFat / DailyReferenceIntakeConstants.DAILY_REF_TOTAL_FAT_INTAKE * 100).toInt())
        riPercentages.add((dish.saturatedFat / DailyReferenceIntakeConstants.DAILY_REF_SATURATED_FAT_INTAKE * 100).toInt())
        riPercentages.add((dish.carbohydrates / DailyReferenceIntakeConstants.DAILY_REF_CARBOHYDRATES_INTAKE * 100).toInt())
        riPercentages.add((dish.sugars / DailyReferenceIntakeConstants.DAILY_REF_SUGARS_INTAKE * 100).toInt())
        riPercentages.add((dish.salt / DailyReferenceIntakeConstants.DAILY_REF_SALT_INTAKE * 100).toInt())
        riPercentages.add((dish.fibre / DailyReferenceIntakeConstants.DAILY_REF_FIBRE_INTAKE * 100).toInt())
        riPercentages.add((dish.protein / DailyReferenceIntakeConstants.DAILY_REF_PROTEIN_INTAKE * 100).toInt())

        return riPercentages
    }
}