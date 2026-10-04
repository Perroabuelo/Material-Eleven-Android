#!/usr/bin/env sh
# core/ holds the pure Kotlin logic tested on the JVM. It must not depend on Android.
core="app/src/main/java/io/github/perroabuelo/materialeleven/core"
[ -d "$core" ] || exit 0
if grep -rnE '^import (android|androidx)\.' "$core"; then
    echo "core/ must not import android.* or androidx.*" >&2
    exit 1
fi
