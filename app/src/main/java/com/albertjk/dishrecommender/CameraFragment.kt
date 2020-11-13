package com.albertjk.dishrecommender

import android.Manifest
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.android.synthetic.main.fragment_camera.*
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class CameraFragment : Fragment() {

    private val TAG = CameraFragment::class.qualifiedName

    private lateinit var navController: NavController

    private var preview: Preview? = null
    private var imageCapture: ImageCapture? = null
    private var camera: Camera? = null

    private lateinit var outputDirectory: File
    private lateinit var cameraExecutor: ExecutorService

    companion object {
        private const val FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS"
        private const val REQUEST_CODE_PERMISSIONS = 10
        private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment.
        return inflater.inflate(R.layout.fragment_camera, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Make the bottom navigation disappear from the screen until the camera screen is visible.
        val bottomNavigationView =
            activity?.findViewById<BottomNavigationView>(R.id.bottomNavigationView)
        if (bottomNavigationView != null) {
            bottomNavigationView.visibility = View.GONE
        }

        // Lock the camera screen's orientation to portrait mode.
        this.activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        // Instantiate the navController which has a reference to the navigation graph.
        navController = Navigation.findNavController(view)

        // Request camera permissions, then start the camera.
        if (allPermissionsGranted()) {
            startCamera()
        } else {
            activity?.let {
                ActivityCompat.requestPermissions(
                    it, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS
                )
            }
        }

        // Set up the click listener for camera icon (take photo) button.
        cameraCaptureButton.setOnClickListener { takePhoto() }

        outputDirectory = getOutputDirectory()

        cameraExecutor = Executors.newSingleThreadExecutor()
    }

    private fun startCamera() {
        val cameraProviderFuture = activity?.let { ProcessCameraProvider.getInstance(it) }

        cameraProviderFuture?.addListener(Runnable {
            // Used to bind the lifecycle of cameras to the lifecycle owner.
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            // Initialise the Preview object.
            preview = Preview.Builder()
                .build()
                .also {
                    it.setSurfaceProvider(viewFinder.createSurfaceProvider())
                }

            // For preview:
            imageCapture = ImageCapture.Builder()
                .build()

            // Select the device's back camera.
            val cameraSelector =
                CameraSelector.Builder().requireLensFacing(CameraSelector.LENS_FACING_BACK).build()

            try {
                // Unbind use cases before rebinding.
                cameraProvider.unbindAll()

                // Bind use cases to camera.
                camera = cameraProvider.bindToLifecycle(
                    this, cameraSelector, preview, imageCapture
                )
            } catch (exc: Exception) {
                Log.e(TAG, "Use case binding failed", exc)
            }

        }, ContextCompat.getMainExecutor(activity))
    }

    private fun takePhoto() {
        // Get a reference of the modifiable image capture use case.
        val imageCapture = imageCapture ?: return

        /* Create an output file to hold the image.
        The timestamp is specified as the name, so it is unique. */
        val photoFile = File(
            outputDirectory,
            SimpleDateFormat(
                FILENAME_FORMAT, Locale.US
            ).format(System.currentTimeMillis()) + ".jpg"
        )

        // Create an output options object which contains the file and metadata.
        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

        // Set up the image capture listener which is triggered after a photo has been taken.
        imageCapture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(activity),
            object : ImageCapture.OnImageSavedCallback {
                override fun onError(exc: ImageCaptureException) {
                    Log.e(TAG, "Photo capture failed: ${exc.message}", exc)
                }

                // If the capture was successful, save the photo to the file.
                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    val savedUri = Uri.fromFile(photoFile)
                    val msg = "Photo capture succeeded: $savedUri"
                    Log.d(TAG, msg)

                    Log.d(TAG, "isBeingAddedAsFavourite: " + arguments?.getBoolean("isBeingAddedAsFavourite"))
                    Log.d(TAG, "isBeingAddedToDiary: " + arguments?.getBoolean("isBeingAddedToDiary"))

                    /* If the user was redirected to CameraFragment from FavouritesFragment,
                    pass the saved image URI and isBeingAddedAsFavourite in a bundle to CropImageFragment. */
                    if (arguments?.getBoolean("isBeingAddedAsFavourite") != null
                        && arguments?.getBoolean("isBeingAddedAsFavourite") == true) {

                        val bundle = bundleOf(
                            "savedUri" to savedUri.toString(),
                            "isBeingAddedAsFavourite" to arguments?.getBoolean("isBeingAddedAsFavourite")
                        )
                        navController.navigate(
                            R.id.action_cameraFragment_to_cropImageFragment,
                            bundle
                        )
                    }
                    /* If the user was redirected to CameraFragment from DiaryFragment,
                    pass the saved image URI alongside the received arguments in a bundle to CropImageFragment. */
                    else if (arguments?.getBoolean("isBeingAddedToDiary") != null
                        && arguments?.getBoolean("isBeingAddedToDiary") == true) {

                        Log.d(TAG, "isBeingAddedToDiary: " + arguments?.getBoolean("isBeingAddedToDiary"))

                        val bundle = bundleOf(
                            "savedUri" to savedUri.toString(),
                            "isBeingAddedToDiary" to arguments?.getBoolean("isBeingAddedToDiary"),
                            "mealName" to arguments?.getString("mealName"),
                            "logDate" to arguments?.get("logDate")
                        )
                        navController.navigate(
                            R.id.action_cameraFragment_to_cropImageFragment,
                            bundle
                        )
                    }
                    /* Otherwise, the user was redirected to CameraFragment from InitiateScanningDishNamesFragment.
                    The identifiedDishNames argument must not be null and this is passed,
                    alongside the saved image URI, in a bundle to CropImageFragment. */
                    else {
                        // Start the image cropping fragment.
                        val bundle = bundleOf(
                            "savedUri" to savedUri.toString(),
                            // Pass on the list of scanned and identified dish names to CropImageFragment.
                            "identifiedDishNames" to arguments?.getStringArrayList("identifiedDishNames")
                            )
                        navController.navigate(
                            R.id.action_cameraFragment_to_cropImageFragment,
                            bundle
                        )
                    }

                }
            })
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        activity?.let { it1 ->
            ContextCompat.checkSelfPermission(
                it1, it
            )
        } == PackageManager.PERMISSION_GRANTED
    }

    private fun getOutputDirectory(): File {
        val mediaDir = activity?.externalMediaDirs!!.firstOrNull()?.let {
            File(it, resources.getString(R.string.app_name)).apply { mkdirs() }
        }
        return if (mediaDir != null && mediaDir.exists())
            mediaDir else activity?.filesDir!!
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>, grantResults:
        IntArray
    ) {
        // Check if the request code is correct.
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            // If permissions are granted, start the camera.
            if (allPermissionsGranted()) {
                startCamera()
            } else {
                Toast.makeText(
                    activity,
                    "Permissions not granted by the user.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
}