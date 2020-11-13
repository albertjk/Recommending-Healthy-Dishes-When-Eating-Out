package com.albertjk.dishrecommender

import android.app.AlertDialog
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ScannedDishNamesAdapter(var scannedDishNameList: ArrayList<String>) :
    RecyclerView.Adapter<ScannedDishNamesAdapter.ScannedDishNamesViewHolder>() {

    /**
     * Creates scanned dish name row.
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ScannedDishNamesViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.scanned_dish_name_row, parent, false)
        return ScannedDishNamesViewHolder(view)
    }

    /**
     * Adds the dish name to the row.
     */
    override fun onBindViewHolder(holder: ScannedDishNamesViewHolder, position: Int) {
        holder.scannedDishNameTextView.text = scannedDishNameList[position]
    }

    /**
     * Returns the number of rows needed by the RecyclerView.
     */
    override fun getItemCount(): Int = scannedDishNameList.size

    /**
     * Binds data to the text view and image button in all rows.
     */
    inner class ScannedDishNamesViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var scannedDishNameTextView: TextView = itemView.findViewById(R.id.scannedDishNameTextView)
        private var deleteDishFromListButton: ImageButton =
            itemView.findViewById(R.id.deleteDishFromListButton)

        // Initializer block
        init {
            /* If the user clicks on the delete image button next to the dish name in the list,
            show a dialog, asking the user to confirm the deletion. */
            deleteDishFromListButton.setOnClickListener {
                val dialogBuilder = AlertDialog.Builder(itemView.context)

                dialogBuilder.setTitle("Are you sure you want to delete this item?")
                dialogBuilder.setMessage(scannedDishNameList[adapterPosition] + " will be removed from the list.")

                // Cancel button click listener
                dialogBuilder.setNeutralButton("Cancel") { dialog, _ ->
                    dialog.cancel()
                }
                // Positive button click listener
                dialogBuilder.setPositiveButton("Delete") { dialog, _ ->

                    // Remove the dish name from the list.
                    scannedDishNameList.removeAt(
                        adapterPosition
                    )
                    notifyItemRemoved(adapterPosition)
                    dialog.dismiss()
                }
                dialogBuilder.create().show()
            }
        }
    }
}