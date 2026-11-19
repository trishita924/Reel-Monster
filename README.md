# Reel Monster (Android)

A cute floating monster that sits above Instagram, YouTube and other apps. Tap it each time you watch a reel or Short. It grows, changes mood and makes comments. Everything stays on your phone: no login, no internet, no analytics.

**Status:** Level 1 (manual counting) only. Automatic detection (Level 2) is **not implemented**. See the end of this file.

## 1. Open the project
1. Install [Android Studio](https://developer.android.com/studio) (a current stable release, JDK 17 is bundled).
2. Unzip `Reel-Monster-Android.zip`.
3. In Android Studio choose **File > Open** and select the `ReelMonster` folder.
4. Let Gradle sync. The first sync downloads Gradle 8.7, the Android Gradle Plugin 8.5.2 and libraries, so you need internet for this step only. If Android Studio asks to install the Android SDK Platform 34, accept.

> The zip has `gradle-wrapper.properties` but not the binary `gradle-wrapper.jar`, so there is no `./gradlew`. Android Studio does not need it. If you want one, run `gradle wrapper --gradle-version 8.7` once, or ignore it.

## 2. Build the APK
- **Quickest:** **Build > Build Bundle(s) / APK(s) > Build APK(s)**. Android Studio shows a "locate" link; the file is `app/build/outputs/apk/debug/app-debug.apk`.
- **Or run directly:** plug in your phone (see below) and press the green **Run** button.

## 3. Install on your phone
1. On the phone: **Settings > About phone**, tap **Build number** 7 times to enable Developer options.
2. **Settings > Developer options > USB debugging** on. Connect by USB and accept the prompt.
3. Select your phone at the top of Android Studio and press **Run**.
   - Or copy `app-debug.apk` to the phone and open it (allow "Install unknown apps" for your file manager).

## 4. Allow the floating monster
1. Open Reel Monster and switch on **Floating monster**.
2. A dialog explains the permission. Tap **Open settings**, find **Reel Monster**, and enable **Allow display over other apps**. Press back.
3. On Android 13+ allow notifications when asked. This only shows the small "monster is floating" notification (it has a **Hide monster** button).
4. The monster appears. Open Instagram or YouTube: it stays on top. Some phone makers (Xiaomi, Oppo, Vivo, Samsung) have extra battery or "pop-up" settings. If the monster vanishes, set Reel Monster's battery usage to **Unrestricted**.

## 5. Use the counter
- **Tap the floating monster** every time you watch a reel. It squishes, the count goes up and it says something.
- **Drag it** anywhere. A tap with no movement counts; a drag does not.
- Inside the app: **+1 reel**, **-1** (fix a mistake), and **Start a new day** to reset today.
- A new calendar day starts at 0 on its own. Past days stay in the **Last 7 days** chart. Counts survive closing the app and rebooting.
- Time scrolled is an estimate at 30 seconds per reel, not a measurement.

## Growth stages
| Reels | Stage |
|---|---|
| 0-5 | Tiny and happy |
| 6-15 | Excited |
| 16-30 | Confused |
| 31-50 | Huge and sleepy |
| 51+ | Exhausted |

## Project map
- `ReelRepository.kt` stores counts per date in SharedPreferences.
- `MonsterStage.kt` has stages, sizes and messages.
- `MonsterView.kt` draws the original monster. `WeekChartView.kt` draws the chart.
- `FloatingMonsterService.kt` is the foreground service that owns the overlay.
- `MainActivity.kt` is the dashboard.

## Level 2: automatic detection (not built)
Automatic counting would need an **AccessibilityService**, which can read what is on screen in other apps. That is a big privacy step and the detection would be fragile (apps change their screens). It is deliberately not included, not declared in the manifest and not claimed to work. If added later it should be off by default, read only the minimum needed to spot a scroll event, never read messages, passwords or other content, and be tested on real devices before any accuracy claim.
