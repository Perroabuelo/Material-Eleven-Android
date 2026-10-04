## Why

Material-Eleven tiene una estética que funciona, pero vive solo en la PS Vita. Este primer cambio crea la versión Android desde cero y deja **una app instalable en el teléfono que reproduce la música del dispositivo con la misma identidad visual**. Sobre esa base se montan después la biblioteca completa, las carpetas y los ajustes.

Rama git: `change/android-foundation`

## What Changes

- Proyecto Android nuevo (Kotlin, Jetpack Compose, Material 3, Media3) con `applicationId` `io.github.perroabuelo.materialeleven`, minSdk 26 y licencia GPL-3.0-or-later, con NOTICE y LICENSES/.
- CI en GitHub Actions: lint, tests JVM y compilación del APK en cada PR. Un workflow de release compila un APK firmado y lo adjunta al GitHub Release cuando se publica un tag `vX.Y.Z`.
- Tema de la app tomado de la Vita: fondo oscuro fijo, superficies, colores de texto, colores por familia de formato (lossless, lossy) y las fuentes Manrope e IBM Plex Mono.
- Color de acento extraído de la carátula de la pista en reproducción, con el mismo criterio que en la Vita (voto de matiz ponderado por saturación, ajuste de legibilidad, acento fijo sin carátula y acento neutro para carátulas sin color).
- Permiso de lectura de música (READ_MEDIA_AUDIO desde Android 13, READ_EXTERNAL_STORAGE antes), con una pantalla que explica para qué sirve y cómo concederlo si se negó.
- Lista **Canciones** leída directamente de MediaStore, ordenada por título, con carátula, artista y badge de formato.
- Reproducción con Media3 en un servicio en segundo plano: controles en la notificación y en la pantalla de bloqueo, botones de audífonos y Bluetooth, pausa al perder el foco de audio y al desconectar los audífonos.
- Cola: reproducir desde la lista la convierte en la cola. El recorrido es circular, el barajado y la repetición son independientes y las pistas que no se pueden abrir se saltan.
- Mini reproductor fijo bajo la lista que se expande a **Reproduciendo**: carátula, título, artista, badge de formato, barra de progreso con tiempo transcurrido y restante, transporte, barajado, repetición y vista previa de las próximas pistas (solo lectura).
- Interfaz en inglés y en español, según el idioma del sistema.
- Nombre "Material Eleven" e ícono adaptativo propio, derivado del ícono de la Vita.

## Capabilities

### New Capabilities
- `app/identity`: nombre de la app, ícono adaptativo, firma del APK y actualización sobre una instalación previa.
- `ui/theme`: paleta oscura fija, colores por familia de formato, tipografía Manrope e IBM Plex Mono.
- `ui/dynamic-accent`: acento derivado de la carátula, legibilidad sobre el fondo y respaldos fijo y neutro.
- `ui/language`: interfaz en inglés y español, idioma inicial según el sistema e inglés como respaldo.
- `library/media-access`: permiso de lectura de audio y obtención de las pistas desde MediaStore.
- `library/views`: vista Canciones (única vista en este cambio), y reproducir desde ella convierte la vista en la cola.
- `playback/queue`: orden natural y barajado, recorrido circular, repetición, salto de pistas ilegibles.
- `playback/background`: reproducción en segundo plano, notificación, pantalla de bloqueo, botones multimedia, foco de audio y desconexión de audífonos.
- `ui/now-playing`: mini reproductor y pantalla Reproduciendo con transporte, progreso y próximas pistas.

### Modified Capabilities
<!-- Ninguna: el repo no tiene specs todavía. -->

## Impact

- Repo nuevo `Perroabuelo/Material-Eleven-Android`. Hay que crearlo en GitHub antes de tener CI.
- Dependencias (todas Apache-2.0, compatibles con GPLv3): AndroidX Core, Activity y Lifecycle, Jetpack Compose con Material 3, Media3 (ExoPlayer, Session), Coil para las carátulas y Kotlin Coroutines.
- Assets: Manrope e IBM Plex Mono (OFL-1.1, copiados del repo de la Vita junto a sus licencias). El ícono se deriva del ícono propio de Material-Eleven.
- Firma de releases: una keystore creada por el usuario y guardada como secrets de GitHub. Nunca se versiona.
- Sin impacto en el repo de la Vita.

## Fuera de alcance

Queda para cambios siguientes, en este orden aproximado:
1. **Índice de biblioteca** (siguiente cambio): Room, lectura propia de tags (artista del álbum, disco y pista), vistas Artistas, Álbumes y Recientes, orden de álbum, reproducción continua y la barra de navegación inferior.
2. **Navegador de carpetas** simple.
3. **Ajustes** (idioma manual, acento del sistema, otras opciones). Con esto se cierra la v1.0.0.
4. v2: formatos tracker (MOD, XM, IT, S3M, vía libopenmpt), ecualizador y normalizador, diseño para tablet o pantalla horizontal.

Tampoco entran: tema claro, Android Auto, widgets, listas de reproducción, letras, ni publicación en Play Store o F-Droid.

## Notas de version

- Versión objetivo: **v0.1.0**
- Salto SemVer: **minor** (primera funcionalidad de la app)

- First Android release of Material Eleven, with the Material You look of the PS Vita version.
- Lists every song on the phone, with cover art, artist and a format badge.
- Plays in the background, with controls in the notification, on the lock screen and from headphones or Bluetooth devices.
- Pauses when another app takes the audio or when headphones are unplugged.
- Shuffle and repeat, independent of each other, and an "Up next" preview.
- The accent colour follows the cover art of the track that is playing.
- Available in English and Spanish.
