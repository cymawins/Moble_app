# Implementation Plan - Fix Emulator Input and Hangul Support

This plan addresses the issue where name input is difficult and Korean (Hangul) characters cannot be typed in the emulator.

## User Review Required

> [!IMPORTANT]
> The "Hangul input" issue is primarily an emulator configuration problem rather than a code bug. I will provide a guide to fix this in the emulator settings.

## Proposed Changes

### [Component: UI Layout]

#### [MODIFY] [activity_signup.xml](file:///D:/CYMA/Team Project/WorkSpace/Mobile_app/app/src/main/res/layout/activity_signup.xml)
- Add `android:imeOptions="actionNext"` and `android:singleLine="true"` to `etEmail`, `etPassword`, `etName`, and `etProvince`.
- Add `android:imeOptions="actionDone"` and `android:singleLine="true"` to `etDistrict`.
- This ensures the keyboard "Enter" key moves the focus to the next field or finishes input, improving the UX.

#### [MODIFY] [activity_login.xml](file:///D:/CYMA/Team Project/WorkSpace/Mobile_app/app/src/main/res/layout/activity_login.xml)
- Apply similar improvements (`actionNext`, `actionDone`) to the login screen for consistency.

## Emulator Configuration Guide (Korean Input)

To enable Korean input in your emulator, please follow these steps:

1. **Open Settings** in the Android Emulator.
2. Navigate to **System** -> **Languages & input**.
3. Select **On-screen keyboard**.
4. Choose the active keyboard (e.g., **Gboard** or **Android Keyboard (AOSP)**).
5. Tap **Languages** and then **Add keyboard**.
6. Search for and select **Korean**.
7. Now you can switch to the Korean layout using the globe icon on the on-screen keyboard or `Ctrl + Space` (depending on settings).

## Verification Plan

### Manual Verification
1. **Input Flow**: Open the Signup screen and verify that pressing the "Next" button on the software keyboard moves focus correctly from Email -> Password -> Name -> Province -> District.
2. **Name Input**: Verify that typing (English) in the Name field works correctly.
3. **Hangul Input**: After following the Emulator Configuration Guide, verify that you can type Korean characters in the Name and other fields.
