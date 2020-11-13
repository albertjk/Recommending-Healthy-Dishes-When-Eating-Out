package com.albertjk.dishrecommender

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.core.os.bundleOf
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView

class RecommendedDishesAdapter(
    private val identifiedDishesData: List<Dish>,
    private val showHealthinessScores: Boolean
) : RecyclerView.Adapter<RecommendedDishesAdapter.RecommendedDishesViewHolder>() {

    private val TAG = RecommendedDishesAdapter::class.qualifiedName

    /**
     * Creates a row displaying a recommended dish.
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecommendedDishesViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.recommendation_row, parent, false)
        return RecommendedDishesViewHolder(view)
    }

    /**
     * Adds the dish's data to the row.
     */
    override fun onBindViewHolder(holder: RecommendedDishesViewHolder, position: Int) {
        holder.dishNameTextView.text = identifiedDishesData[position].dishName
        holder.rankingTextView.text = (position + 1).toString()

        /* Check if the generally healthy recommendation preference had been selected. If so,
        the healthiness score of each dish will be displayed rounded to three decimal places.
        Otherwise, it will not be displayed. */
        if (showHealthinessScores) {
            holder.healthinessScoreTitleTextView.visibility = View.VISIBLE
            holder.healthinessScoreTextView.visibility = View.VISIBLE
            holder.healthinessScoreTextView.text =
                (Math.round(identifiedDishesData[position].healthinessScore!! * 1000.0) / 1000.0).toString()
        }
        holder.caloriesTextView.text = identifiedDishesData[position].calories.toString()
        holder.totalFatTextView.text = identifiedDishesData[position].totalFat.toString() + "g"
        holder.saturatedFatTextView.text =
            identifiedDishesData[position].saturatedFat.toString() + "g"
        holder.carbsTextView.text = identifiedDishesData[position].carbohydrates.toString() + "g"
        holder.sugarsTextView.text = identifiedDishesData[position].sugars.toString() + "g"
        holder.saltTextView.text = identifiedDishesData[position].salt.toString() + "g"
        holder.fibreTextView.text = identifiedDishesData[position].fibre.toString() + "g"
        holder.proteinTextView.text = identifiedDishesData[position].protein.toString() + "g"
    }

    /**
     * Returns the number of rows needed by the RecyclerView.
     */
    override fun getItemCount(): Int = identifiedDishesData.size

    /**
     * Binds data to the text views and image button in all rows.
     */
    inner class RecommendedDishesViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var dishNameTextView: TextView = itemView.findViewById(R.id.dishNameTextView)
        var rankingTextView: TextView = itemView.findViewById(R.id.rankingTextView)
        var healthinessScoreTitleTextView: TextView =
            itemView.findViewById(R.id.healthinessScoreTitleTextView)
        var healthinessScoreTextView: TextView =
            itemView.findViewById(R.id.healthinessScoreTextView)
        var caloriesTextView: TextView = itemView.findViewById(R.id.caloriesTextView)
        var totalFatTextView: TextView = itemView.findViewById(R.id.totalFatTextView)
        var saturatedFatTextView: TextView = itemView.findViewById(R.id.saturatedFatTextView)
        var carbsTextView: TextView = itemView.findViewById(R.id.carbsTextView)
        var sugarsTextView: TextView = itemView.findViewById(R.id.sugarsTextView)
        var saltTextView: TextView = itemView.findViewById(R.id.saltTextView)
        var fibreTextView: TextView = itemView.findViewById(R.id.fibreTextView)
        var proteinTextView: TextView = itemView.findViewById(R.id.proteinTextView)

        var showBarChartButton: ImageButton = itemView.findViewById(R.id.showBarChartButton)

        // Initializer block
        init {

            /* If the user clicks on the bar chart image button next to the dish name in the list,
            navigate the user to RecommendationNutritionBarChartFragment. Pass the dish's nutrient data in a bundle. */
            showBarChartButton.setOnClickListener {
                Log.d(TAG, "identifiedDishesData[position]: " + identifiedDishesData[position])

                val bundle = bundleOf(
                    "dishNutrients" to identifiedDishesData[position]
                )
                it.findNavController().navigate(
                    R.id.action_ourRecommendationsFragment_to_recommendationNutritionBarChartFragment,
                    bundle
                )
            }
        }
    }
}