package com.albertjk.dishrecommender

import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.ValueFormatter
import java.text.DecimalFormat

class RILabelValueFormatter() : ValueFormatter() {

    private val format = DecimalFormat("###,##0")

    /**
     * Formats the reference intake percentage labels displayed above the bars in the bar chart.
     */
    override fun getBarLabel(barEntry: BarEntry?): String {
        return format.format(barEntry?.y) + "% RI"
    }

    /**
     * Formats the reference intake percentage labels displayed in the pie slices of the pie chart.
     */
    override fun getPieLabel(value: Float, pieEntry: PieEntry?): String? {
        return format.format(pieEntry?.y) + "% RI"
    }

    /**
     * Formats the reference intake percentage labels displayed above the line chart.
     */
    override fun getPointLabel(entry: Entry?): String {
        return format.format(entry?.y) + "% RI"
    }
}