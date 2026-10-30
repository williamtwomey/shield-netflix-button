# How it works

Notes from disassembling SHIELD firmware build `RQ1A.210105.003.7825230_4387.0822`
(SHIELD TV 2019, Android TV 11).

## How the SHIELD handles the button

- The 2019 remote sends scan code 393. `/system/usr/keylayout/Vendor_0955_Product_7217.kl`
  maps it to `BUTTON_12` (keycode 199).
- `com.nvidia.shieldtech.NvNetflix.handleKeyEvent` (in `/system/priv-app/NvShieldTech`,
  process `com.nvidia.shieldtech.system`) registers as a `KEY_EXCLUSIVE` handler, so it
  gets the key before apps or accessibility services do.
- If `com.netflix.ninja` has a leanback launch intent, it sends the broadcast
  `com.netflix.ninja.intent.action.NETFLIX_KEY` with `setPackage("com.netflix.ninja")`,
  plus the extra `power_on=true` if the display was off. It does **not** launch the app.
- If there's no leanback launch intent, it opens `market://details?id=com.netflix.ninja`.
- `ActivityStarter.shouldAbortBackgroundActivityStart` in `services.jar` special-cases
  `com.netflix.ninja`. That exemption is what lets a broadcast receiver in that package
  start an activity from the background. It applies to the caller, so the target can be
  any app.
- The only remote button NVIDIA lets you remap is Menu (the `customize_menu_*` global
  settings). There's no setting for the Netflix button.

## What the stub does

- `Picker` is a `LEANBACK_LAUNCHER` activity. Its existence satisfies NVIDIA's "is Netflix
  installed" check, and NVIDIA never launches it. Opening it lists every app with a TV
  launcher entry and saves your choice.
- `KeyReceiver` receives `NETFLIX_KEY` and opens the saved app, falling back to YouTube.
- The manifest declares a `<queries>` intent for `MAIN` + `LEANBACK_LAUNCHER`. Without it,
  Android 11's package visibility rules would hide the other apps from the stub.

## Approaches that didn't work

- `adb shell input keyevent KEYCODE_BUTTON_12` goes to the focused app, not through
  NVIDIA's handler, so it doesn't reproduce the real button.
- `sendevent` to the remote's `/dev/input/event*` node is blocked by SELinux, even though
  the `shell` user is in the `input` group.
- Remapping apps that use an accessibility service (button mappers, launcher remap
  features) can't intercept the button, because NVIDIA acts on key-down first.
- A stub with only the launcher activity makes NVIDIA think Netflix is installed. It then
  sends the broadcast, nothing receives it, and the button does nothing.

## Stopped state

Android doesn't deliver broadcasts to an app in the "stopped" state unless the sender
opts in, and NVIDIA's doesn't. A newly installed app that hasn't been opened yet is
stopped, and so is one that was Force stopped. Opening the app once clears it, and reboots
don't put apps back into that state.
