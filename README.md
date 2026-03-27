# Terminal-0

A fully functional Android terminal emulator with real device RAM & storage access.

## Features
- Real shell passthrough via `/system/bin/sh`
- Built-in commands: `sysinfo`, `ram`, `storage`, `df`, `ls`, `cd`, `cat`, `mkdir`, `rm`, `touch`, `cp`, `mv`, `find`, `head`, `tail`, `wc`, `ps`, `top`, `cpu`, `battery`, `ping`, `ifconfig`, `date`, `uname`, `env`, `echo`, `id`, `whoami`, `hostname`, `uptime`, `version`, `help`, `clear`
- Live RAM usage with progress bar
- Internal & external storage stats
- Process list via `top` and `ps`
- Command history (↑ ↓ arrow keys)
- Red/black terminal aesthetic

## Build Requirements
- Android Studio Hedgehog (2023.1.1) or newer
- JDK 8+
- Android SDK 34
- Gradle 8.2

## Build Steps
1. Open Android Studio
2. File → Open → select the `Terminal-0` folder
3. Wait for Gradle sync to complete
4. Connect Android device (USB debug ON) or start emulator
5. Run → Run 'app'

## Permissions Required
- `READ_EXTERNAL_STORAGE` — browse SD card
- `WRITE_EXTERNAL_STORAGE` — create files
- `INTERNET` — for ping/network commands
- `ACCESS_NETWORK_STATE` — network info

## Logo
The Terminal-0 logo is an Arch Linux–inspired triangle with red accent slashes, white on black.

## Min SDK
Android 7.0 (API 24) and above.
