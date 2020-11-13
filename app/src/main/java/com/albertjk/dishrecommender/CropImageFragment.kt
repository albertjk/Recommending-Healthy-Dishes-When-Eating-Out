package com.albertjk.dishrecommender

import android.app.AlertDialog
import android.content.pm.ActivityInfo
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import kotlinx.android.synthetic.main.fragment_crop_image.*
import java.util.*
import kotlin.collections.ArrayList

class CropImageFragment : Fragment() {

    private val TAG = CropImageFragment::class.qualifiedName

    private lateinit var navController: NavController

    private var recognisedText: String? = null

    private lateinit var viewModel: ViewModel

    // This list will store all the dishes retrieved from the database.
    private lateinit var dishList: List<Dish>

    /* These two booleans are used to decide if a dish is being added to the list of
    favourite dishes or to the user's diary. */
    private var isBeingAddedAsFavourite = false
    private var isBeingAddedToDiary = false

    // This string will store the meal name a dish is associated with if it is being added to the diary.
    private var mealName: String? = null

    // This date variable will store the user's selected date to log the dish for if the dish is being added to the diary.
    private var logDate: Date? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_crop_image, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Make the bottom navigation disappear from the screen until the crop image screen is visible.
        val bottomNavigationView =
            activity?.findViewById<BottomNavigationView>(R.id.bottomNavigationView)
        if (bottomNavigationView != null) {
            bottomNavigationView.visibility = View.GONE
        }

        // After the camera screen is not displayed anymore, unlock the screen orientation.
        this.activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED

        // Instantiate the navController which has a reference to the navigation graph.
        navController = Navigation.findNavController(view)

        Log.d(TAG, "isBeingAddedAsFavourite: " + arguments?.getBoolean("isBeingAddedAsFavourite"))
        Log.d(TAG, "isBeingAddedToDiary: " + arguments?.getBoolean("isBeingAddedToDiary"))
        Log.d(
            TAG,
            ("is the isBeingAddedToDiary argument not null?: " + (arguments?.getBoolean("isBeingAddedToDiary") != null))
        )
        Log.d(
            TAG,
            ("is the isBeingAddedAsFavourite argument not null?: " + (arguments?.getBoolean("isBeingAddedAsFavourite") != null))
        )

        /* If a boolean was passed in the bundle to either indicate if a dish is being added
        as the user's favourite or to their diary, get the boolean.
        If a dish is being added to the diary, get the meal name and the date to record these in the database. */
        if (arguments?.getBoolean("isBeingAddedAsFavourite") != null
            && arguments?.getBoolean("isBeingAddedAsFavourite") == true
        ) {

            isBeingAddedAsFavourite = arguments?.getBoolean("isBeingAddedAsFavourite")!!

        } else if (arguments?.getBoolean("isBeingAddedToDiary") != null
            && arguments?.getBoolean("isBeingAddedToDiary") == true
        ) {

            isBeingAddedToDiary = arguments?.getBoolean("isBeingAddedToDiary")!!
            mealName = arguments?.getString("mealName")
            logDate = arguments?.get("logDate") as Date?
        }

        Log.d(TAG, "isBeingAddedAsFavourite: $isBeingAddedAsFavourite")
        Log.d(TAG, "isBeingAddedToDiary: $isBeingAddedToDiary")

        /* Take the cropped image as a Bitmap
        and pass it to ML Kit to recognise the text on the image. */
        cropImageAndIdentifyDishButton.setOnClickListener {
            recogniseText(cropImageView.croppedImage!!)
        }

        // Get the Uri of the original image.
        val resultUri = Uri.parse(arguments?.getString("savedUri"))

        // Set the image to be cropped.
        cropImageView.setImageUriAsync(resultUri)

        // Get the cropped image.
        cropImageView.setOnCropImageCompleteListener { _, _ -> }
    }

    /**
     * The ViewModel of MainActivity is used here.
     */
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        // Assign the ViewModel.
        viewModel = activity?.let { ViewModelProvider(it).get(ViewModel::class.java) }!!

        /* Access the LiveData object. It will be scoped to the lifecycle of the fragment's view.
        Get all dishes from the database. */
        viewModel.allDishes.observe(viewLifecycleOwner, Observer {
            dishList = it
        })
    }

    /**
     * This method runs the pre-trained TextRecognizer model to recognise the text on the input Bitmap image.
     */
    private fun recogniseText(bitmap: Bitmap) {

        // Create an InputImage object from the cropped image Bitmap.
        val image = InputImage.fromBitmap(bitmap, 0)

        // Get an instance of TextRecognizer.
        val recogniser = TextRecognition.getClient()

        // Process the image and run text recognition.
        image.let {
            recogniser.process(it)
                .addOnSuccessListener { visionText ->
                    // Task completed successfully. Get the recognised dish name.
                    for (block in visionText.textBlocks) {
                        val text = block.text

                        Log.d(TAG, "Identified text: $text")
                        recognisedText = text
                    }
                }
                .addOnFailureListener { e ->
                    // Task failed with an exception.
                    Log.d(TAG, "Error recognising the text: ", e)
                }
                .addOnCompleteListener {

                    // If the text was successfully recognised, search the dish name in the database.
                    if (recognisedText != null) {
                        identifyDish(recognisedText!!)
                    }
                }
        }
    }

    /**
     * This method performs a database search to find the recognised dish name.
     * If a matching dish name was found, it passes the name back to StartScanningDishNamesFragment.
     */
    private fun identifyDish(recognisedText: String) {
        var dishFoundExactMatch = false

        Log.d(TAG, "isBeingAddedAsFavourite: $isBeingAddedAsFavourite")
        Log.d(TAG, "isBeingAddedToDiary: $isBeingAddedToDiary")

        // First, check if there is an exact match (except lower and uppercase characters)
        for (dish in dishList) {
            if (dish.dishName.toLowerCase() == recognisedText.toLowerCase()) {
                Log.d(TAG, "Exact match to " + dish.dishName)
                dishFoundExactMatch = true

                proceedAfterDishWasIdentified(dish.dishName)
            }
        }
        // If there is no exact match, try to find a similar match.
        if (!dishFoundExactMatch) {
            val similarDishNames: ArrayList<String> = ArrayList()

            for (dish in dishList) {
                if (dish.dishName.toLowerCase().contains(recognisedText.toLowerCase())) {
                    Log.d(TAG, "Similar match to " + dish.dishName)
                    similarDishNames.add(dish.dishName)
                }
            }

            // If there were no exact or similar matches, let the user know.
            if (similarDishNames.isEmpty()) {
                Toast.makeText(
                    activity,
                    "Sorry, the dish could not be identified",
                    Toast.LENGTH_SHORT
                )
                    .show()

                // If the user wanted to add a dish as their favourite, navigate to FavouritesFragment.
                if (isBeingAddedAsFavourite) {
                    navController.navigate(
                        R.id.action_cropImageFragment_to_favouritesFragment
                    )
                }
                // If the user wanted to add a dish to their diary, navigate to DiaryFragment.
                else if (isBeingAddedToDiary) {
                    navController.navigate(
                        R.id.action_cropImageFragment_to_diaryFragment
                    )
                }
                // Otherwise, navigate to InitiateScanningDishNamesFragment and pass on the received arguments.
                else {
                    val bundle = bundleOf(
                        "identifiedDishNames" to arguments?.getStringArrayList("identifiedDishNames")
                    )
                    navController.navigate(
                        R.id.action_cropImageFragment_to_initiateScanningDishNamesFragment,
                        bundle
                    )
                }
            }
            // If there is one similar match, this will be the dish used for evaluating the nutrients.
            else if (similarDishNames.size == 1) {
                proceedAfterDishWasIdentified(similarDishNames[0])
            }
            /* If there are multiple similar matches, ask the user to select from a list, in a dialog,
            which one of the similar ones should be used for getting the dish's nutrients. */
            else if (similarDishNames.size > 1) {

                val dialogBuilder = AlertDialog.Builder(this.context)
                dialogBuilder
                    .setTitle("Please select the best-matching dish name")
                    .setCancelable(false) // The dialog cannot be cancelled
                    .setItems(similarDishNames.toTypedArray()) { dialog, which ->

                        // The 'which' argument contains the index position of the selected item.
                        Log.d(TAG, "in dialog identifiedDishName: " + similarDishNames[which])
                        dialog.dismiss()
                        proceedAfterDishWasIdentified(similarDishNames[which])
                    }
                dialogBuilder.create().show()
            }
        }
    }

    /**
     * After a dish was identified, this method is called to proceed with either adding a dish as
     * a favourite, adding a dish to the diary, or navigating to InitiateScanningDishNamesFragment.kt.
     */
    private fun proceedAfterDishWasIdentified(identifiedDishName: String) {

        /* If the user is adding a dish as their favourite, update the database record,
        and navigate to FavouritesFragment. */
        if (isBeingAddedAsFavourite) {
            setDishAsFavourite(identifiedDishName)
        }
        /* If the user is adding a dish to their diary, add it to the corresponding database tables,
        and navigate to DiaryFragment. */
        else if (isBeingAddedToDiary) {
            addDishToDiary(identifiedDishName)
        }
        // Otherwise, navigate to InitiateScanningDishNamesFragment.
        else {
            storeDishNameAndNavigateToInitiateScanningDishNamesFragment(
                identifiedDishName
            )
        }
    }

    /**
     * Takes the identified dish name, finds it in the database and gets all its details,
     * updates its isFavourite attribute, and navigates to FavouritesFragment.
     */
    private fun setDishAsFavourite(identifiedDishName: String) {

        // Find the identified dish based on its name in the database.
        val dishList = viewModel.allDishes.value

        var identifiedDish: Dish? = null

        for (dish in dishList!!) {
            if (dish.dishName == identifiedDishName) {
                identifiedDish = dish
                break
            }
        }

        // Update the dish's isFavourite attribute in the database.
        identifiedDish!!.isFavourite = 1
        viewModel.updateDish(identifiedDish)

        Toast.makeText(activity, "Dish added to favourites", Toast.LENGTH_SHORT).show()

        // Redirect to FavouritesFragment.
        navController.navigate(
            R.id.action_cropImageFragment_to_favouritesFragment
        )
    }

    /**
     * Takes the identified dish name, finds it in the database and gets all its details, then
     * adds an entry into the DishLog table, and adds a new entry or updates an existing entry
     * in the DailyLog table in the database. Then, it navigates to DiaryFragment.
     */
    private fun addDishToDiary(identifiedDishName: String) {

        // Find the identified dish based on its name in the database.
        val dishList = viewModel.allDishes.value

        var identifiedDish: Dish? = null

        for (dish in dishList!!) {
            if (dish.dishName == identifiedDishName) {
                identifiedDish = dish
                break
            }
        }

        /* Record the user's daily nutrient intake in the DailyLog entity in the database,
        and in either the BreakfastLog, the LunchLog, or DinnerLog entities depending on the meal name. */

        /* If the user already consumed dishes on the given date, get the user's already recorded
        daily nutrient intake for this date from the database, and add the current dish's
        nutrient values to these. This process updates the user's recorded daily nutrient intake. */
        val dailyLogs = viewModel.dailyLogs.value

        // This boolean is used to decide if the DailyLog table already contains an instance with the specified logDate.
        var dailyLogInstanceExists = false

        Toast.makeText(activity, "Dish added to diary", Toast.LENGTH_SHORT).show()

        if (dailyLogs != null && dailyLogs.isNotEmpty()) {
            for (dailyLog in dailyLogs) {
                if (dailyLog.logDate == logDate!!) {
                    dailyLogInstanceExists = true
                    Log.d(TAG, "dailyLogInstanceExists: $dailyLogInstanceExists")
                    viewModel.updateDailyLog(
                        DailyLog(
                            logDate!!,
                            dailyLog.dailyCaloriesIntake + identifiedDish!!.calories,
                            dailyLog.dailyTotalFatIntake + identifiedDish.totalFat,
                            dailyLog.dailySaturatedFatIntake + identifiedDish.saturatedFat,
                            dailyLog.dailyCarbohydratesIntake + identifiedDish.carbohydrates,
                            dailyLog.dailySugarsIntake + identifiedDish.sugars,
                            dailyLog.dailySaltIntake + identifiedDish.salt,
                            dailyLog.dailyFibreIntake + identifiedDish.fibre,
                            dailyLog.dailyProteinIntake + identifiedDish.protein
                        )
                    ).invokeOnCompletion {
                        // Add the log to either BreakfastLogs, LunchLogs, or DinnerLogs, depending on the meal name.
                        storeMealLog(mealName, identifiedDish)

                        /* Add the dish, the received meal name, and the date to the DishLog entity in the database.
                        logId is 0, as it will be auto-generated by Room when inserting the entry into the database. */
                        viewModel.insertDishLog(
                            DishLog(
                                0,
                                identifiedDish.dishId,
                                mealName!!,
                                logDate!!
                            )
                        ).invokeOnCompletion {
                            // Redirect to DiaryFragment.
                            navController.navigate(
                                R.id.action_cropImageFragment_to_diaryFragment
                            )
                        }
                    }
                }
            }
        }

        // If there is no entry for the given date in the DailyLog table yet, insert a entry.
        if (!dailyLogInstanceExists) {

            Log.d(TAG, "dailyLogInstanceExists: $dailyLogInstanceExists")

            viewModel.insertDailyLog(
                DailyLog(
                    logDate!!,
                    identifiedDish!!.calories,
                    identifiedDish.totalFat,
                    identifiedDish.saturatedFat,
                    identifiedDish.carbohydrates,
                    identifiedDish.sugars,
                    identifiedDish.salt,
                    identifiedDish.fibre,
                    identifiedDish.protein
                )
            ).invokeOnCompletion {
                // Add the log to either BreakfastLogs, LunchLogs, or DinnerLogs, depending on the meal name.
                storeMealLog(mealName, identifiedDish)

                /* Add the dish and the received meal name and date to the DishLog entity in the database.
                logId is 0, as it will be auto-generated by Room when inserting the entry into the database. */
                viewModel.insertDishLog(
                    DishLog(
                        0,
                        identifiedDish.dishId,
                        mealName!!,
                        logDate!!
                    )
                ).invokeOnCompletion {

                    // Redirect to DiaryFragment.
                    navController.navigate(
                        R.id.action_cropImageFragment_to_diaryFragment
                    )
                }
            }
        }
    }

    /**
     * Adds the nutrients of a dish logged on a specified date
     * to the BreakfastLog, LunchLog, or DinnerLog table, depending on the meal the dish is associated with.
     */
    private fun storeMealLog(mealName: String?, identifiedDish: Dish?) {
        if (mealName == "breakfast") {

            /* If the user already consumed dishes on the given date for breakfast, get the user's already recorded
            breakfast nutrient intake for this date from the database, and add the current dish's
            nutrient values to these. This process updates the user's recorded breakfast nutrient intake. */
            val breakfastLogs = viewModel.breakfastLogs.value

            // This boolean is used to decide if the BreakfastLog table already contains an instance with the specified logDate.
            var breakfastLogInstanceExists = false

            if (breakfastLogs != null && breakfastLogs.isNotEmpty()) {
                for (breakfastLog in breakfastLogs) {
                    if (breakfastLog.logDate == logDate!!) {
                        breakfastLogInstanceExists = true
                        Log.d(TAG, "breakfastLogInstanceExists: $breakfastLogInstanceExists")
                        viewModel.updateBreakfastLog(
                            BreakfastLog(
                                logDate!!,
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
                        logDate!!,
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
                    if (lunchLog.logDate == logDate!!) {
                        lunchLogInstanceExists = true
                        Log.d(TAG, "lunchLogInstanceExists: $lunchLogInstanceExists")
                        viewModel.updateLunchLog(
                            LunchLog(
                                logDate!!,
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
                        logDate!!,
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
                    if (dinnerLog.logDate == logDate!!) {
                        dinnerLogInstanceExists = true
                        Log.d(TAG, "dinnerLogInstanceExists: $dinnerLogInstanceExists")
                        viewModel.updateDinnerLog(
                            DinnerLog(
                                logDate!!,
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
                        logDate!!,
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

    /**
     * Takes the identified dish name, adds it to the list of already scanned and identified dish names,
     * and navigates to InitiateScanningDishNamesFragment.
     */
    private fun storeDishNameAndNavigateToInitiateScanningDishNamesFragment(identifiedDishName: String) {
        Toast.makeText(activity, "Dish successfully identified", Toast.LENGTH_SHORT).show()

        val identifiedDishNames: ArrayList<String> =
            arguments?.getStringArrayList("identifiedDishNames") as ArrayList<String>

        /* Before adding an identified dish name to the list of scanned dish names, check
        if the dish is already in the list. Only add it if it is not in the list yet. */
        if (!identifiedDishNames.contains(identifiedDishName)) {
            identifiedDishNames.add(identifiedDishName)
        } else {
            Toast.makeText(activity, "The dish name is already in the list", Toast.LENGTH_SHORT)
                .show()
        }

        // Redirect to StartScanningDishNamesFragment. Pass the identified dish name list in a bundle.
        val bundle = bundleOf(
            "identifiedDishNames" to identifiedDishNames
        )
        navController.navigate(
            R.id.action_cropImageFragment_to_initiateScanningDishNamesFragment,
            bundle
        )
    }
}