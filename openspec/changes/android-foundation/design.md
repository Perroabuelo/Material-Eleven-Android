## Context

El repo parte vacío; ver proposal.md (Why). La referencia de comportamiento y estética es Material-Eleven para PS Vita: sus specs (`ui/dynamic-accent`, `playback/queue`, `ui/now-playing`), sus tokens de color en `include/ui_theme.h` y su algoritmo de acento en `source/accent.c`. No se porta código C. El algoritmo de acento se reescribe en Kotlin con las mismas constantes (es del mismo proyecto y comparte la licencia GPL-3.0-or-later).

Herramientas disponibles en la máquina de desarrollo: Android Studio con JBR 21, SDK Platform 36 y build-tools 36. Dispositivo de prueba: Poco X7 Pro con Android 16 (HyperOS).

## Goals / Non-Goals

**Goals:**
- Una arquitectura que aguante los cambios siguientes (índice en Room, más vistas, carpetas, ajustes) sin reescribir la base.
- Toda la lógica con reglas (acento, orden, formato, respaldos de texto, barajado, salto de pistas ilegibles, vista previa) en Kotlin puro y cubierta por tests JVM.
- Delegar en Media3 todo lo que ya resuelve: notificación, pantalla de bloqueo, botones multimedia, foco de audio, desconexión de audífonos y gapless.

**Non-Goals:**
- Restaurar la cola y la posición después de que el sistema mate el proceso. Llega con el índice en Room.
- Tests instrumentados o de UI automatizados. La UI se verifica en el teléfono.
- Librería de navegación. Con una sola vista más la hoja de Reproduciendo no hace falta. Se evalúa al agregar la barra inferior.

## Decisions

### 1. Estructura: un módulo con paquetes por capa
Todo vive en `:app`, bajo `io.github.perroabuelo.materialeleven`:
- `core/`: Kotlin puro, sin imports de `android.*` ni `androidx.*`. Contiene `accent/` (histograma de matiz, legibilidad, contraste), `queue/` (orden barajado que empieza por la pista en curso, política de salto, cálculo de la vista previa), `library/` (familia de formato, respaldos de título y artista, comparador de orden) y `time/` (formato m:ss).
- `data/`: `MediaStoreSource` (consulta y observación de MediaStore, que se convierten en un `Flow<List<Track>>`) y carga de carátulas.
- `playback/`: `PlaybackService` (`MediaSessionService` con ExoPlayer) y el `ForwardingPlayer` que adapta barajado y repetición.
- `ui/`: Compose, con `theme/`, `permission/`, `songs/`, `nowplaying/` y `MainActivity`.

Un test de lint propio, o una regla simple en CI (`grep` sobre `core/`), falla si `core/` importa `android.` o `androidx.`.
*Alternativa descartada:* varios módulos Gradle (`:core`, `:data`, `:app`). Separan mejor, pero agregan configuración antes de que haga falta. Si se necesitan, los paquetes ya marcan las fronteras.

### 2. La UI controla la reproducción por `MediaController`
`PlaybackService` es el dueño del ExoPlayer y de la `MediaSession`. La UI se conecta con un `MediaController` (asíncrono, por `SessionToken`) desde un `PlayerViewModel` que expone el estado como `StateFlow`: pista actual, en reproducción o no, posición, barajado, repetición y orden vigente. Así la app, la notificación, la pantalla de bloqueo y los audífonos pasan todos por el mismo player, y el estado siempre coincide (spec `ui/now-playing`, "Estado coherente con la notificación").
*Alternativa descartada:* que la UI tenga su propio ExoPlayer y el servicio solo lo "acompañe". Duplica el estado y es la fuente clásica de desincronización con la notificación.

### 3. Semántica de la cola sobre Media3
- **Circular:** con la repetición apagada, el player real está en `REPEAT_MODE_ALL`; con la repetición encendida, en `REPEAT_MODE_ONE`. Un `ForwardingPlayer` mapea `setRepeatMode` de cualquier controlador a esos dos estados (OFF pasa a ALL). Los eventos no se traducen: los controladores externos ven "repetir todo" con la repetición apagada, lo que es exacto porque la cola da la vuelta, y la app trata solo `REPEAT_MODE_ONE` como "repetición encendida". (Envolver los listeners para traducir el modo se descartó: `Player.Listener` solo tiene métodos default de Java, que la delegación `by` de Kotlin no reenvía, y el envoltorio silenciaba todos los demás eventos.) En `REPEAT_MODE_ONE`, Media3 ya hace que `seekToNext` avance por el orden y que solo el final natural repita.
- **Barajado que empieza por la pista en curso:** el `DefaultShuffleOrder` de ExoPlayer no garantiza que la pista actual sea la primera del orden, y entonces una "vuelta completa" desde ella no recorre todo. Se implementa `StartingShuffleOrder`, un `ShuffleOrder` cuyo cálculo de permutación (Fisher-Yates con la pista actual fijada en la posición 0, semilla explícita) vive en `core/queue` y se testea. El `ForwardingPlayer` intercepta `setShuffleModeEnabled(true)` y fija ese orden antes de activarlo. El orden no se regenera mientras el barajado siga encendido.
- **Retroceder:** `seekToPrevious` de Media3 ya implementa la regla de los 3 segundos (`maxSeekToPreviousPositionMs` = 3000). Se deja explícito en la configuración del player. Es la convención de Android y reemplaza el comportamiento de la Vita (ver spec `playback/queue`).
- **Pistas ilegibles:** en `onPlayerError`, `SkipPolicy` (en `core/queue`) recibe el índice que falló, la dirección del último movimiento, el orden vigente y cuántos intentos van, y devuelve el índice siguiente o "detener" tras una vuelta completa. El servicio aplica `seekTo` + `prepare` y, al detenerse, emite un evento de sesión que la UI muestra como snackbar.
- **Vista previa:** función pura que, dado el orden vigente y la posición actual, devuelve las N siguientes con continuación circular. La UI la alimenta leyendo el `Timeline` y `getNextWindowIndex` del controller. Con repetición de pista, la vista previa muestra lo que viene al saltar, no la misma pista repetida.

### 4. Biblioteca desde MediaStore, sin índice propio todavía
`MediaStoreSource` consulta `MediaStore.Audio.Media.EXTERNAL_CONTENT_URI` con `IS_MUSIC != 0` (eso deja fuera grabaciones, tonos, alarmas y notificaciones) y lee id, título, artista, álbum, album id, duración, MIME, nombre de archivo y fecha de alta. Un `ContentObserver` sobre esa URI, con debounce, vuelve a emitir el `Flow` (spec `library/media-access`, "La lista refleja la música añadida o borrada"). La consulta corre en `Dispatchers.IO`.
- `IS_MUSIC` no basta: en HyperOS, los audios de WhatsApp, las grabaciones y hasta los tonos guardados en `Music/` vienen con `IS_MUSIC = 1`. `core/library/LibraryFilter` decide además, por ruta y por MIME, qué se lista: fuera `Android/media/` y `Android/data/`, las carpetas de grabaciones en cualquier nivel y los formatos no reproducibles (MIDI y tonos). La ruta sale de `RELATIVE_PATH` en API 29+ y de `DATA` (absoluta, a la que se le quita el prefijo del volumen) en API 26–28. Más adelante, Ajustes podrá permitir elegir carpetas.
- Los respaldos (nombre de archivo sin extensión, "Desconocido") se resuelven en `core/library`. MediaStore entrega `<unknown>` para artistas vacíos, y eso también se trata como ausente.
- El orden usa `java.text.Collator` con fuerza `PRIMARY` (ignora mayúsculas y tildes) y el artista como desempate. Es JVM puro, así que se testea.
- La familia de formato se deduce de la extensión, con el MIME como respaldo, en una tabla de `core/library`.
- Las `MediaItem` de la cola llevan URI `content://` y `MediaMetadata` (título, artista, álbum, artworkUri), así la notificación muestra datos y carátula sin trabajo extra.
*Alternativa descartada para este cambio:* Room más lectura propia de tags. Es la decisión acordada para la biblioteca, pero pertenece al cambio siguiente. Aquí MediaStore alcanza para Canciones, y el `Track` del dominio ya está pensado para que su origen cambie después sin tocar la UI.

### 5. Carátulas y acento
- **Carga:** Coil 3 con un `Fetcher` propio por id de pista. En API 29+ usa `ContentResolver.loadThumbnail(uri, size)`, que extrae la carátula embebida o la del álbum. En API 26–28 usa `content://media/external/audio/albumart/<albumId>` y, si falla, `MediaMetadataRetriever.embeddedPicture`. Las miniaturas de la lista se piden a 128 px y la de Reproduciendo a 1024 px. Coil cachea en memoria y en disco.
- **Acento:** al cambiar la pista actual, el `PlayerViewModel` pide la carátula a 96 px como `Bitmap`, saca sus píxeles a un `IntArray` ARGB y llama a `core/accent`. Esa función es una traducción directa de `accent.c`, con las mismas constantes: rejilla de unas 48×48 muestras, 24 cubetas de matiz, voto ponderado por saturación, umbral cromático del 1 %, banda de saturación y luminosidad y contraste mínimo de 4,5:1 contra #120F17. Devuelve `Chromatic(color)`, `Achromatic` o `None`. La UI anima el cambio con `animateColorAsState` (unos 400 ms).
- **Color sobre el acento:** se elige entre #120F17 y #F4EFEA según el contraste con el acento, con un mínimo de 4,5:1.
*Alternativa descartada:* la librería Palette de AndroidX. Tiene criterios propios (vibrant, muted) que no reproducen el comportamiento de la Vita ("un detalle pequeño define el acento"), y no se testea en JVM.

### 6. Tema
- `MaterialTheme` con un `darkColorScheme` construido desde los tokens de la Vita: `background`/`surface` #120F17, `surfaceContainer` #1C1826, `surfaceContainerHigh` #241F30 y `onSurface` #F4EFEA. El acento entra como `primary` y como `CompositionLocal` propio, para los elementos que no son de Material. No hay `dynamicColor` y se ignora el modo claro del sistema.
- Fuentes Manrope (pesos estáticos 400 a 800 del repo googlefonts/manrope; Compose no aplica el eje de peso de una fuente variable cargada desde recursos) e IBM Plex Mono Medium (copiada del repo de la Vita) en `res/font`, con sus licencias OFL. El renderizador de texto de Android ya recurre a la fuente del sistema para los glifos que faltan.
- `enableEdgeToEdge(SystemBarStyle.dark(...))` y los insets aplicados con `WindowInsets.safeDrawing` en el contenedor raíz.

### 7. Reproduciendo como hoja expandible
Un contenedor raíz con la vista Canciones y una capa inferior que va del estado "mini" (unos 64 dp sobre la barra de navegación) al estado "expandido" (pantalla completa), con `AnchoredDraggable`. La lista recibe un padding inferior igual a la altura del mini reproductor. `BackHandler` colapsa la hoja cuando está expandida. Tocar la notificación abre la `MainActivity` con un extra que expande la hoja.
*Alternativa descartada:* `BottomSheetScaffold` de Material 3. No ofrece bien un estado "mini" persistente con contenido distinto al expandido.

### 8. Permiso
Se declaran `READ_MEDIA_AUDIO` (API 33+) y `READ_EXTERNAL_STORAGE` con `maxSdkVersion=32`, y nada más de almacenamiento. Se pide con el Activity Result API. El estado se vuelve a evaluar en cada `ON_RESUME`, así que conceder el permiso en Ajustes y volver basta. "Negado de forma permanente" se detecta cuando, después de al menos una negación, `shouldShowRequestPermissionRationale` devuelve `false`. Ese caso abre `ACTION_APPLICATION_DETAILS_SETTINGS`. No hace falta `POST_NOTIFICATIONS`: las notificaciones de una `MediaSession` están exentas desde Android 13.
El servicio declara `foregroundServiceType="mediaPlayback"` y el permiso `FOREGROUND_SERVICE_MEDIA_PLAYBACK`, como exige targetSdk 34+.

### 9. Idiomas
`res/values` (inglés, respaldo) y `res/values-es` (cualquier variante regional del español cae ahí). Con `androidResources.generateLocaleConfig = true` y `res/resources.properties` (`unqualifiedResLocale=en-US`), el sistema ofrece la selección de idioma por app en Android 13+, sin código propio.

### 10. Ícono
El ícono de la Vita es una corchea blanca sobre fondo #FF9166. Se redibuja como vector: capa de fondo de color sólido #FF9166 y capa de primer plano con la corchea en `VectorDrawable`, dentro de la zona segura de 66 dp. La capa monocroma es la misma corchea. El PNG de 128 px de la Vita no tiene resolución suficiente para escalarlo.

### 11. Build, versiones y firma
- compileSdk 37.2 y targetSdk 36: las versiones actuales de AndroidX (core 1.19, Compose 1.12) exigen compilar contra la API 37. El targetSdk se queda en 36, el Android del teléfono de prueba, para no activar comportamientos de Android 17 sin probarlos.
- Gradle Kotlin DSL con `gradle/libs.versions.toml`. AGP, Kotlin (con el plugin del compilador de Compose), el BOM de Compose, Media3 y Coil se fijan a sus últimas versiones estables al crear el proyecto. Gradle corre sobre JDK 21 y `jvmTarget` es 17.
- `versionName`/`versionCode` según la regla de archive de config.yaml. Este cambio queda en 0.1.0 / 100.
- Release: `isMinifyEnabled = true` y `isShrinkResources = true`. Media3 y Coil traen sus reglas de consumidor.
- Firma: la keystore la genera el usuario con `keytool`, fuera del repo, y la respalda (perderla impide publicar actualizaciones, ver spec `app/identity`). En CI se reconstruye desde el secret `RELEASE_KEYSTORE_BASE64`, con `RELEASE_KEYSTORE_PASSWORD`, `RELEASE_KEY_ALIAS` y `RELEASE_KEY_PASSWORD`. En local se lee desde `keystore.properties` (en `.gitignore`). Sin esos datos, `assembleRelease` falla con un mensaje claro en vez de firmar con la clave de debug. La única excepción es la propiedad `-PunsignedRelease`, que genera un release sin firmar, solo para el CI de PR.

### 12. Licencias
`LICENSE` (GPL-3.0), `NOTICE` (origen: rediseño de Material-Eleven, algoritmo de acento traducido de `accent.c`, fuentes OFL, dependencias Apache-2.0), y en `LICENSES/` los textos `Apache-2.0.txt`, `OFL-Manrope.txt` y `OFL-IBMPlexMono.txt`. Apache-2.0 y OFL-1.1 son compatibles con GPLv3.

## Estrategia de pruebas

- **Tests JVM** (`./gradlew testDebugUnitTest`), sobre `core/`:
  - Acento: carátula casi negra con un detalle rojo → rojo; escala de grises → `Achromatic`; sin píxeles → `None`; violeta oscuro → contraste ≥ 4,5:1 después de `makeLegible`; casos con valores esperados calculados con la versión C para verificar paridad.
  - Color sobre el acento: amarillo claro → oscuro; contraste ≥ 4,5:1 en un barrido de matices.
  - `StartingShuffleOrder`: es permutación, la actual queda primera, es estable con la misma semilla y una vuelta recorre todas.
  - `SkipPolicy`: salta en la dirección del movimiento, se detiene tras una vuelta, cola de 1 pista ilegible.
  - Vista previa: orden natural, barajado y continuación circular en la última pista.
  - Formato y respaldos: tabla de extensiones y MIME, `<unknown>`, título vacío → nombre de archivo sin extensión.
  - Orden: "Álamo", "beta", "zeta"; desempate por artista.
  - `m:ss` y `h:mm:ss`.
- **Pruebas en el teléfono** (Poco X7 Pro, Android 16), siguiendo los scenarios de las specs. Cada tarea de UI o de reproducción indica qué scenarios verifica. Se repiten algunos en un emulador API 26 para el camino de permiso y de carátulas antiguo.
- HyperOS es agresivo con los procesos en segundo plano: el scenario "Bloquear la pantalla 30 minutos" se prueba con la configuración de batería por defecto, sin agregar excepciones.

## CI

- `.github/workflows/ci.yml`, en `pull_request` y `push` a `main`: checkout, `actions/setup-java` (Temurin 21), `gradle/actions/setup-gradle`, el chequeo de que `core/` no importa Android, `./gradlew lint testDebugUnitTest assembleDebug` y `./gradlew assembleRelease -PunsignedRelease` (release sin firma, para que R8 corra en cada PR). Sube el APK de debug como artifact.
- `.github/workflows/release.yml`, en `push` de tags `v*`: verifica que `versionName` coincide con el tag, reconstruye la keystore desde secrets, corre `./gradlew assembleRelease` y crea o actualiza el GitHub Release del tag con `material-eleven-vX.Y.Z.apk` adjunto (con `gh release`).

## Risks / Trade-offs

- [HyperOS mata servicios en segundo plano o retrasa la notificación] → Servicio en primer plano de tipo `mediaPlayback` mientras suena, que es lo que el sistema respeta. Si aun así falla en la prueba de 30 minutos, se documenta el ajuste de batería en el README. No se piden excepciones de batería desde la app.
- [MediaStore trae mal artista del álbum, disco o pista] → Aceptado en este cambio, porque Canciones solo usa título y artista. El cambio de índice agrega la lectura propia de tags.
- [`loadThumbnail` es lento con colecciones grandes al desplazar rápido] → Miniaturas pequeñas (128 px), caché en disco de Coil y carga cancelable por fila. Se verifica con el scenario de 5.000 canciones.
- [R8 rompe algo solo en release] → El CI de PR también compila el release sin firma (`-PunsignedRelease`), para que R8 corra en cada PR. El APK firmado se prueba en el teléfono antes de publicar el tag.
- [Perder la keystore] → Se respalda fuera de la máquina. Si se pierde, la única salida es desinstalar y reinstalar, y se pierden los datos. Va escrito en el README de desarrollo.
- [La traducción del acento difiere de la versión C por redondeos de float] → Tests de paridad con valores calculados desde `accent.c`, con tolerancia de ±1 por canal.

## Migration Plan

No hay instalación previa. El primer release es v0.1.0. Hacer rollback de un release consiste en borrar el GitHub Release y su tag. Quien ya instaló la versión puede reinstalar una anterior desinstalando primero, porque Android no permite bajar `versionCode` encima.

## Open Questions

- Si los nombres de archivo del APK publicados deben incluir la arquitectura. Por ahora es un APK universal, porque no hay código nativo hasta los formatos tracker.
