# OK to Wake Clock – Android App

An Android app that shows a large clock and uses **green** and **orange** backgrounds to show when it’s “OK to wake”: green at wake time, orange during a pre-wake period, and black the rest of the time. The screen stays on while the app is open and brightness can be set from the app.

---

## What the app does

- **Default screen:** Large white time on a black background. Brightness follows the value you set (0–10).
- **Wake time:** At the configured wake time, the background turns **green** and stays green for the “wake alarm duration” (in minutes).
- **Pre-wake:** If you set a “pre-wake duration” (in minutes), the background turns **orange** that many minutes before wake time.
- **Screen:** Stays on (no sleep) while the app is in the foreground.
- **Settings:** Tap the gear icon (bottom right) to open settings. Use **Back** (button or action bar) to return to the clock.

---

## Prerequisites (no Android experience assumed)

1. **Computer:** Windows, macOS, or Linux.
2. **Android Studio:** This is the official IDE for Android. You will use it to open the project, build, and run the app.
3. **Device or emulator:** A physical Android phone/tablet (API 24+) or an Android emulator run from Android Studio.

### Install Android Studio

1. Go to [developer.android.com/studio](https://developer.android.com/studio).
2. Download **Android Studio** for your operating system.
3. Run the installer and follow the steps (default options are fine).
4. When the installer offers to install **Android SDK**, **Android SDK Platform**, and **Android Virtual Device**, accept them. These are needed to build and run the app.
5. Finish the setup and open Android Studio.

---

## How to open and run the project

### Option A: Open the existing project (recommended)

1. Start **Android Studio**.
2. Click **File → Open** (or “Open an Existing Project” on the welcome screen).
3. Select the folder that contains this project (the folder where `build.gradle.kts` and `app/` live).
4. Click **Open**.
5. Wait for the **Gradle sync** to finish (bottom status bar). Android Studio will download Gradle and dependencies if needed. If it says “Gradle sync failed”, see **Troubleshooting** below.
6. **Run the app:**
   - **On a physical device:** Enable **Developer options** and **USB debugging** on the device, connect it with USB, and when Android Studio shows the device in the toolbar, click the green **Run** (Play) button.
   - **On an emulator:** Click **Tools → Device Manager**, create a virtual device if you don’t have one (e.g. Pixel 6, API 34), start it, then click the green **Run** button and choose that device.

The app will build and install; the first screen you see is the default clock screen.

### Option B: Create the project from scratch and copy the app code

If you prefer to create a new project and then paste this app into it:

1. In Android Studio: **File → New → New Project**.
2. Choose **Empty Views Activity** (with **Kotlin** and **View** binding).
3. Set **Application name** to “OK to Wake Clock”, **Package name** to `com.oktowake.clock`, **Save location** to a folder of your choice. Set **Minimum SDK** to 24 or higher. Click **Finish**.
4. When the project is created, replace its contents with the contents of this project:
   - Copy the entire `app/src/main` folder (java, res, AndroidManifest.xml) from this project over the new project’s `app/src/main`.
   - Copy this project’s `app/build.gradle.kts` (and adjust the new project’s `app/build.gradle.kts` to match if needed).
   - Ensure the new project’s `build.gradle.kts` (root) and `settings.gradle.kts` are compatible (same Android Gradle Plugin and Kotlin versions as in this project).
5. Let Gradle sync, then run the app as in Option A (step 6).

---

## Using the app

1. **Clock screen:** Opens by default. Shows current time in large white text. Background is black unless you’re in the pre-wake (orange) or wake (green) window.
2. **Settings:** Tap the **gear icon** (bottom right). Set:
   - **Wake time:** e.g. `07:00` (24-hour format).
   - **Wake alarm duration:** How many minutes the screen stays green (e.g. 15).
   - **Brightness (0–10):** 0 = dimmest, 10 = brightest.
   - **Pre-wake duration:** Minutes before wake time when the screen turns orange. Use `0` to disable.
3. Tap **Back** (or the action bar back arrow) to save and return to the clock. The new settings apply immediately.

---

## Building a release APK (for sharing or installing without Android Studio)

1. Open the project in Android Studio and wait for Gradle sync.
2. **Build → Build Bundle(s) / APK(s) → Build APK(s)**.
3. When the build finishes, click **locate** in the notification to open the folder containing the APK (usually `app/build/outputs/apk/release/`).
4. The file will be named something like `app-release.apk`. You can copy it to your phone and install it (you may need to allow “Install from unknown sources” for the file manager or browser you use).

### Optional: signed APK for Play Store or long-term use

For a proper release (e.g. for Google Play), you should sign the APK:

1. **Build → Generate Signed Bundle / APK**.
2. Choose **APK** (or Android App Bundle if you prefer).
3. Create or choose a **keystore** (Android Studio can create one for you). Remember the keystore path, password, and key alias; you need them for future updates.
4. Follow the wizard (release build type, sign the APK). The signed APK will be in `app/release/` (or the path shown at the end of the wizard).

---

## Project structure (for reference)

- **`app/src/main/AndroidManifest.xml`** – App entry point, permissions, and activities.
- **`app/src/main/java/com/oktowake/clock/`**
  - **`MainActivity.kt`** – Clock screen: time display, brightness, keep-screen-on, background color logic (black / orange / green).
  - **`SettingsActivity.kt`** – Settings screen: wake time, durations, brightness, and saving to SharedPreferences.
- **`app/src/main/res/layout/`** – XML layouts for the clock and settings screens.
- **`app/src/main/res/values/`** – Strings, colors, themes.
- **`app/build.gradle.kts`** – App-level dependencies and SDK versions.
- **`build.gradle.kts`** (root) – Android Gradle Plugin and Kotlin versions.

---

## Troubleshooting

- **“Error while executing process …/bin/jlink” or “JdkImageTransform” fails**  
  This happens when Gradle runs on **JDK 21** (e.g. Android Studio’s bundled JBR). Use **JDK 17** for Gradle: in Android Studio go to **Settings/Preferences → Build, Execution, Deployment → Build Tools → Gradle**. Under **Gradle JDK**, choose **17** (or **Download JDK…** and pick **Version 17**, e.g. Eclipse Temurin 17). Click **Apply**, then **File → Sync Project with Gradle Files** and build again.

- **“Gradle sync failed” or “Unsupported class file major version”**  
  Make sure you’re using a recent JDK (e.g. JDK 17). In Android Studio: **File → Project Structure → SDK Location** and set **Gradle JDK** to a JDK 17 or newer (Android Studio often bundles one).

- **“SDK location not found”**  
  Set **ANDROID_HOME** (or **ANDROID_SDK_ROOT**) to your Android SDK path. In Android Studio: **File → Settings → Appearance & Behavior → System Settings → Android SDK** and note the “Android SDK Location”. Set that as `ANDROID_HOME` in your system environment variables.

- **Device not listed when clicking Run**  
  On the device: **Settings → About phone** → tap **Build number** 7 times to enable Developer options. Then **Settings → Developer options** → enable **USB debugging**. Reconnect the USB cable and accept the “Allow USB debugging?” prompt. On Windows, you may need to install device-specific USB drivers.

- **App installs but crashes on open**  
  Check **View → Tool Windows → Logcat** in Android Studio, select your device and app, and look for red error lines when you launch the app. That message will indicate what went wrong (e.g. missing permission or wrong minimum SDK).

- **Gradle wrapper missing (e.g. no `gradlew`)**  
  If you cloned or copied the project and the `gradlew` / `gradlew.bat` files are missing, open the project in Android Studio and let it sync; it can recreate the wrapper. Or install [Gradle](https://gradle.org/install/) and run `gradle wrapper` in the project root.

---

## Summary

- **Open** the project in Android Studio, **sync** Gradle, then **run** on a device or emulator.
- Use the **gear icon** to set wake time, wake duration, brightness, and pre-wake duration; **Back** saves and returns to the clock.
- Build an APK via **Build → Build Bundle(s) / APK(s) → Build APK(s)** for installation without Android Studio.

If you have no prior Android experience, following **Option A** and the **Using the app** section is enough to get the app running and adjust its behavior.
