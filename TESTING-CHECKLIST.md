# Testing checklist

Nothing below was run on Android. The build environment had no Android SDK, Gradle, Kotlin compiler or emulator.

## Checked in the build environment
- [x] All XML files (manifest, layouts, drawables, values) are well-formed (xmllint).
- [x] Every `@drawable`, `@color`, `@string`, `@id` and `R.*` reference used was cross-checked by script (see below).
- [x] Manifest declares the permissions, the activity and the foreground service.

## NOT tested: do on a real phone (or emulator)
Build
- [ ] Gradle sync succeeds in Android Studio
- [ ] Kotlin compiles with no errors
- [ ] Debug APK builds and installs

Counting
- [ ] +1 / -1 in the app change the count; it never goes below 0
- [ ] Tapping the app monster counts
- [ ] Count persists after closing the app and after reboot
- [ ] Count starts at 0 after midnight; yesterday appears in the chart
- [ ] Reset asks for confirmation and only clears today

Overlay
- [ ] Switch asks for overlay permission, then shows the monster on return
- [ ] Monster stays above Instagram and YouTube
- [ ] Dragging moves it; a still tap counts; a drag does not count
- [ ] Tapping the floating monster updates the dashboard live
- [ ] Monster grows and changes face at 6, 16, 31 and 51
- [ ] Speech bubble appears on tap and hides after a few seconds
- [ ] "Hide monster" in the notification removes it and turns the switch off
- [ ] Switch off removes it
- [ ] Works on Android 8, 12, 13 and 14+ (notification permission, foreground service type)
- [ ] Monster survives the screen turning off/on; check on a phone with aggressive battery saving
- [ ] Rotation and different screen sizes look right

Visual
- [ ] Dashboard looks right in light mode, large font size, small phones
- [ ] Launcher icon appears (adaptive and round)
