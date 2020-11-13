# MSc Dissertation Project - Recommending Healthy Dishes When Eating Out

## Overview

This repository contains the code for my MSc Computer Science dissertation project I did while studying at the University of Southampton. As part of this project, a native Android nutrition application was designed, implemented, and tested. The application uses the Model-View-ViewModel (MVVM) architecture. This application promotes consuming healthy dishes in restaurants and uses deep learning text recognition for identifying dish names on restaurant menus. The application allows an end user, who is a restaurant customer, to set personal dish recommendation preferences and then add a dish to be considered for recommendation. Adding a dish involves taking a photo of a restaurant menu, cropping it, and identifying a dish on it. The user can add several dishes to be considered for recommendation, and then receive dish recommendations based on the preferences and observe bar charts about the recommended dishes’ nutrients. The user can also log dishes into a diary to track their daily and weekly nutrient intakes, observe charts about their intakes, and add dishes to their favourites for quick access to dish nutrient information.

## Screenshots of the application's main functionalities

### Tutorial screen

<img src="https://github.com/albertjk/Recommending-Healthy-Dishes-When-Eating-Out/blob/master/Images/screenshot1.png" height="510">

### Selecting dish recommendation preferences

<img src="https://github.com/albertjk/Recommending-Healthy-Dishes-When-Eating-Out/blob/master/Images/screenshot2.png" height="510">

### Adding dishes to be considered for recommendation

<img src="https://github.com/albertjk/Recommending-Healthy-Dishes-When-Eating-Out/blob/master/Images/screenshot3.png" height="510">

### Dish recommendations and nutrient bar chart of a dish

<img src="https://github.com/albertjk/Recommending-Healthy-Dishes-When-Eating-Out/blob/master/Images/screenshot4.png" height="510">

### Diary screen and nutrient intake charts

<img src="https://github.com/albertjk/Recommending-Healthy-Dishes-When-Eating-Out/blob/master/Images/screenshot5.png" height="510">

### Favourites screen

<img src="https://github.com/albertjk/Recommending-Healthy-Dishes-When-Eating-Out/blob/master/Images/screenshot6.png" height="510">

## Built With

* Kotlin
* XML

Other tools and technologies used:

* Android Jetpack components: CameraX, Navigation, and Architecture components
* SQLite database
* ML Kit Text Recognition API
* Material Design components

## Getting Started

To run the application, the following steps must be performed:
1. Make sure that the Java JDK is installed on your machine. If it is not, you can download it for your OS from [here](
https://www.oracle.com/java/technologies/javase-jdk15-downloads.html). Install the JDK, and then set the environment variable for the Java command.
2. Download the latest version of Android Studio for your OS from [here](https://developer.android.com/studio).
3. Install Android Studio.
4. Get the project files from this repository.
5. Open the project in Android Studio. To do this, in Android Studio click on File -> Open -> then browse the project directory and click OK.
6. Once the project is opened in Android Studio, after the Gradle build is finished, ensure that there is an emulator (virtual device) configured to run the application, or connect a physical Android device via a USB cable. If a physical device is used, USB debugging must be enabled on the device.
7. To run the application, click on ‘Run app’, which is the green right arrow icon.