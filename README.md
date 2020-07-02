# QuietTime
An Android app for scheduling auto silence times on your phone

[Available on Google Play Store!](https://play.google.com/store/apps/details?id=com.firatyildiz.quiettime)

Quiet Time is an application that can auto silence your phone during the times you have set in the week. These silent times will also be repeated every week until
you decide to delete them, or reboot your phone.

# How It Works

Here's how the main screen looks

<img src="https://i.imgur.com/1GOLzAM.png" width="300" />

From this screen you can press the clock button on the top right to be taken to the quiet time creation screen

<img src="https://i.imgur.com/2OZf7vl.png" width="300" />

Here you can set the start time and the end time for this quiet time. The phone will either go into vibrate or mute mode depending on the option you select.
You should also coohse the days of the week you would like this quiet time to repeat. The quiet times will be triggered every week on the set days.
Finally after you are done, you can press the Done button on the top right to save this quiet time, and it will be scheduled accordingly.

<img src="https://i.imgur.com/qNVnRRI.png" width="300"/>

On the main screen, you can open up the detaisl of a quiet time by pressing the edit button on the top right of it. Here you are given the option to either delete or edit 
all of it's details. You can also quickly change the days this quiet time gets triggered on, and save it without even opening the edit screen.

Lastly, the app will warn you if any of the quiet times you are trying to schedule overlaps with the ones you already have. You can choose to continue to create it if this
happens, but unexpected things might occur, for example the phone might go out of silent mode when you do not expect it to.

The app needs do not disturb permissions on Android 6.0 and above, and will ask for them the first time you open it via a snackbar message.

# Used Technologies

On this project I've used:

- Android MVVM Pattern
- Android Room
- Timber
- LiveData

The project also contains unit and instrument tests that were used to test features before using them in the app.
