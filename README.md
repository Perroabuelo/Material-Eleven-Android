# Material Eleven for Android

A local music player for Android with a Material You interface. It is the phone version of
[Material-Eleven](https://github.com/Perroabuelo/Material-Eleven), the PS Vita homebrew music player,
rewritten from scratch in Kotlin with the same look.

> Work in progress. The first release (0.1.0) is not out yet.


# Features (planned for 0.1.0)

- Lists every song on the phone, with cover art, artist and a format badge.
- Plays in the background, with controls in the notification, on the lock screen and from headphones
  or Bluetooth devices.
- Shuffle and repeat, independent of each other, and an "Up next" preview.
- The accent colour follows the cover art of the track that is playing.
- Available in English and Spanish.

Requires Android 8.0 or newer.


# Installing

Download the APK from the [Releases](https://github.com/Perroabuelo/Material-Eleven-Android/releases)
page and open it on the phone. Android asks to allow installing apps from that source the first time.


# Building

Requirements: JDK 21 (the one bundled with Android Studio works) and the Android SDK with platform
37.2.

```
./gradlew assembleDebug             # debug APK in app/build/outputs/apk/debug/
./gradlew lint testDebugUnitTest    # lint and JVM tests
```

On Windows, `pwsh scripts/install-debug.ps1` builds the debug APK, installs it on the phone connected
over USB and opens the app (`-SkipBuild` reinstalls the last build).

## Release signing

Release builds are signed with a private keystore that is never committed. Losing it means future
releases can no longer be installed over existing ones, so keep a backup somewhere safe.

Locally, create `keystore.properties` in the project root:

```
storeFile=C:/path/to/material-eleven-release.jks
storePassword=...
keyAlias=material-eleven
keyPassword=...
```

In CI, the release workflow reads the same values from the repository secrets
`RELEASE_KEYSTORE_BASE64`, `RELEASE_KEYSTORE_PASSWORD`, `RELEASE_KEY_ALIAS` and
`RELEASE_KEY_PASSWORD`.

`./gradlew assembleRelease -PunsignedRelease` builds an unsigned release APK, which CI uses to check
that the minified build works.


# License

GNU GPL version 3 or later. See [LICENSE](LICENSE) and [NOTICE](NOTICE).
