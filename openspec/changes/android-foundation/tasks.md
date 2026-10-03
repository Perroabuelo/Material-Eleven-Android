## 0. Requisitos del usuario (antes de las tareas 1.4 y 9.3)

- [ ] 0.1 Crear el repo `Perroabuelo/Material-Eleven-Android` en GitHub y agregarlo como `origin`. Listo cuando `git push -u origin main` funciona.
- [ ] 0.2 Generar la keystore de release con `keytool`, respaldarla fuera de la máquina y cargar los secrets `RELEASE_KEYSTORE_BASE64`, `RELEASE_KEYSTORE_PASSWORD`, `RELEASE_KEY_ALIAS` y `RELEASE_KEY_PASSWORD` en el repo. Listo cuando `gh secret list` muestra los cuatro.

## 1. Esqueleto del proyecto y CI

- [ ] 1.1 Crear el proyecto Gradle (Kotlin DSL, `libs.versions.toml`, wrapper) con el módulo `:app`, `applicationId` `io.github.perroabuelo.materialeleven`, minSdk 26, compile/targetSdk 36, versión 0.1.0 / 100, Compose y una `MainActivity` vacía con fondo #120F17. Listo cuando `./gradlew assembleDebug` compila y el APK abre en el Poco X7 Pro.
- [ ] 1.2 Agregar `LICENSE` (GPL-3.0), `NOTICE`, `LICENSES/` (Apache-2.0, OFL de Manrope e IBM Plex Mono), `README.md` en inglés (qué es, cómo compilar, cómo firmar) y `CHANGELOG.md` con la sección "Unreleased". Listo cuando los archivos existen y `./gradlew lint` pasa.
- [ ] 1.3 Configurar la firma de release (`keystore.properties` local o variables de entorno en CI, `-PunsignedRelease` para PRs, error claro sin credenciales) y R8 con `isMinifyEnabled` e `isShrinkResources`. Listo cuando `./gradlew assembleRelease -PunsignedRelease` compila y `./gradlew assembleRelease` sin credenciales falla con el mensaje esperado.
- [ ] 1.4 Agregar `.github/workflows/ci.yml` (Temurin 21, setup-gradle, chequeo de que `core/` no importa `android.`/`androidx.`, `lint testDebugUnitTest assembleDebug`, `assembleRelease -PunsignedRelease`, artifact del APK de debug). Listo cuando el primer PR de la rama muestra el CI en verde.

## 2. Lógica pura en `core/` (con sus tests JVM)

- [ ] 2.1 `core/library`: familia de formato por extensión con el MIME como respaldo (lossless, lossy, otro), respaldos de título (nombre de archivo sin extensión) y de artista y álbum ("Desconocido", con `<unknown>` tratado como ausente), comparador de orden con `Collator` PRIMARY y desempate por artista. Listo cuando pasan los tests de la tabla de formatos, los respaldos y el orden "Álamo", "beta", "zeta".
- [ ] 2.2 `core/time`: formato `m:ss` y `h:mm:ss` de tiempo transcurrido y restante. Listo cuando pasan los tests de 0, 59 s, 4:00, 1:02:03 y valores negativos o desconocidos.
- [ ] 2.3 `core/accent`: traducir `accent.c` (histograma de 24 cubetas sobre una rejilla de unas 48×48 muestras, voto ponderado por saturación, umbral cromático del 1 %, `makeLegible` con banda HSL y contraste ≥ 4,5:1 sobre #120F17, clasificación `Chromatic`/`Achromatic`/`None`) y el color sobre el acento. Listo cuando pasan los tests del detalle rojo, la escala de grises, la imagen vacía, el violeta oscuro, el barrido de contraste y la paridad con valores calculados desde la versión C (±1 por canal).
- [ ] 2.4 `core/queue`: permutación barajada que deja la pista actual primera (semilla explícita), `SkipPolicy` (dirección del movimiento, como mucho una vuelta) y cálculo de la vista previa circular para N pistas. Listo cuando pasan los tests de permutación, estabilidad, vuelta completa, salto en ambas direcciones, detención tras una vuelta y vista previa en la última pista.

## 3. Tema e identidad

- [ ] 3.1 `ui/theme`: tokens de color de la Vita, `darkColorScheme`, colores de familia de formato, fuentes Manrope e IBM Plex Mono en `res/font`, tipografía y `CompositionLocal` de acento con su color "sobre acento". Activar edge-to-edge con barras oscuras. Listo cuando, con una pantalla de muestra, en el teléfono se verifican "Sistema en modo claro" y "Navegación por gestos y por botones" (spec `ui/theme`).
- [ ] 3.2 Componente `FormatBadge` (texto Plex Mono en el color de su familia sobre un fondo tenue, sin contorno). Listo cuando una vista previa de Compose muestra FLAC verde, MP3 morado y un formato desconocido en gris, y lint pasa.
- [ ] 3.3 Ícono adaptativo vectorial (fondo #FF9166, corchea blanca dentro de la zona segura, capa monocroma) y nombre "Material Eleven" en `strings.xml`. Listo cuando en el teléfono se verifican "Nombre en el lanzador", "Ícono con la máscara del lanzador" e "Íconos temáticos" (spec `app/identity`).
- [ ] 3.4 Idiomas: `values` (inglés) y `values-es` con todos los textos hasta este punto, `generateLocaleConfig` y `resources.properties`. Listo cuando en el teléfono se verifican "Variante regional", "Idioma no soportado" e "Idioma por aplicación" (spec `ui/language`).

## 4. Permiso y fuente de datos

- [ ] 4.1 Declarar `READ_MEDIA_AUDIO` y `READ_EXTERNAL_STORAGE` (`maxSdkVersion=32`) y crear la pantalla de permiso (explicación, botón, detección de negación permanente que abre los ajustes de la app, reevaluación en `ON_RESUME`). Listo cuando en el teléfono se verifican "Primer arranque", "Permiso concedido" y "Negado de forma permanente" (spec `library/media-access`), y el primero se repite en un emulador API 26.
- [ ] 4.2 `data/MediaStoreSource`: consulta con `IS_MUSIC != 0` en `Dispatchers.IO`, mapeo al `Track` del dominio con los respaldos de `core/library`, y `ContentObserver` con debounce expuesto como `Flow`. Listo cuando en el teléfono se verifican "Pista con tags", "Pista sin tags", "Grabaciones y tonos" y "Copiar música con la app abierta".
- [ ] 4.3 Carga de carátulas con Coil 3 y un `Fetcher` propio (`loadThumbnail` en API 29+; albumart y luego `MediaMetadataRetriever` en API 26–28), en tamaños de 128 px y 1024 px. Listo cuando las miniaturas aparecen en el teléfono y en un emulador API 26 con un FLAC que tiene carátula embebida.

## 5. Vista Canciones

- [ ] 5.1 Pantalla Canciones: `LazyColumn` ordenada con el comparador de `core/library` y filas con miniatura, título, artista y `FormatBadge` (recorte con "…"), más los estados de carga y de "no se encontró música". Listo cuando en el teléfono se verifican "Orden alfabético", "Título largo en la lista", "Teléfono sin música" y "Colección grande" (con unas 5.000 canciones de prueba).

## 6. Servicio de reproducción

- [ ] 6.1 `PlaybackService` (`MediaSessionService`, ExoPlayer con atributos de audio de música, `handleAudioFocus = true`, `handleAudioBecomingNoisy = true`, `maxSeekToPreviousPositionMs = 3000`), declarado con `foregroundServiceType="mediaPlayback"` y su permiso. `PendingIntent` de sesión hacia `MainActivity` con el extra de expandir. Listo cuando, cargando una cola de prueba, en el teléfono se verifican "Bloquear la pantalla", "Usar otra app", "Controlar desde la pantalla de bloqueo", "Pausar desde audífonos Bluetooth", "Llamada entrante", "Otra app de música" y "Desconectar los audífonos" (spec `playback/background`).
- [ ] 6.2 `ForwardingPlayer` de la cola: repetición expuesta como sí o no sobre `REPEAT_MODE_ALL`/`REPEAT_MODE_ONE`, y barajado con el `ShuffleOrder` que usa la permutación de `core/queue`. Listo cuando pasan los tests JVM del mapeo de modos y en el teléfono se verifican "Una vuelta completa sin repetir", "Encender el barajado", "Apagar el barajado", "El final de la cola continúa por el principio", "Retroceder desde la primera pista", "Retroceder a mitad de pista", "Retroceder con el barajado encendido" y los tres scenarios de repetición (spec `playback/queue`).
- [ ] 6.3 Salto de pistas ilegibles en `onPlayerError` con `SkipPolicy`, más el evento de sesión "no se pudo reproducir nada". Listo cuando en el teléfono se verifican "Una pista ilegible no detiene la reproducción" (borrando un archivo después de armar la cola) y "Ninguna pista se puede abrir" (con archivos dañados de prueba).
- [ ] 6.4 Cierre de la reproducción: descartar la notificación en pausa detiene el servicio, y quitar la app de recientes en pausa quita la notificación. Listo cuando en el teléfono se verifica "Descartar la notificación en pausa".

## 7. Conexión de la UI con la reproducción

- [ ] 7.1 `PlayerViewModel` con `MediaController` asíncrono y `StateFlow` (pista, reproduciendo, posición, barajado, repetición, vista previa a partir del `Timeline`). Tocar una fila arma la cola con la vista y reproduce. La fila de la pista en curso se resalta. Listo cuando en el teléfono se verifican "Tocar una canción", "Cola desde Canciones" y "Pista en curso visible".
- [ ] 7.2 Acento dinámico: al cambiar la pista, carátula a 96 px → píxeles → `core/accent` fuera del hilo principal, con `animateColorAsState` sobre el `CompositionLocal`. Listo cuando en el teléfono se verifican todos los scenarios de `ui/dynamic-accent` con carátulas de prueba (roja, azul, casi negra con detalle rojo, gris, violeta oscuro, amarilla y sin carátula).

## 8. Mini reproductor y Reproduciendo

- [ ] 8.1 Mini reproductor (miniatura, título, artista, reproducir/pausar, siguiente, línea de progreso) fijo sobre la barra de navegación, con padding inferior en la lista. Listo cuando en el teléfono se verifican "Aparece al reproducir" y "Pausar desde el mini reproductor".
- [ ] 8.2 Hoja expandible con `AnchoredDraggable` (tocar o deslizar para abrir, deslizar o Atrás para cerrar, conservando el desplazamiento de la lista), y apertura expandida desde la notificación. Listo cuando en el teléfono se verifican "Abrir y cerrar" y "Abrir desde la notificación".
- [ ] 8.3 Pantalla Reproduciendo: carátula grande, título, artista, álbum, badge, transporte (anterior, reproducir/pausar, siguiente, barajado, repetición con áreas de 48 dp o más) y barra de progreso con arrastre y tiempos en Plex Mono. Listo cuando en el teléfono se verifican "Pista con tags y carátula", "Encender el barajado", "Estado coherente con la notificación", "Arrastrar la barra" y "Tiempos en fuente monoespaciada".
- [ ] 8.4 Vista previa de las próximas pistas, de solo lectura, en la que tocar una pista salta a ella. Listo cuando en el teléfono se verifican "Orden barajado en la vista previa", "Última pista del orden" y "Elegir una próxima pista".
- [ ] 8.5 Snackbar para el evento "no se pudo reproducir nada" y revisión de todos los textos nuevos en `values-es` (tildes, eñes, signos de apertura, sin voseo). Listo cuando se verifica "Revisión de los textos" (spec `ui/language`) y "Tags con caracteres no latinos" con una pista de título en japonés.

## 9. Release

- [ ] 9.1 Agregar `.github/workflows/release.yml` (tag `v*`, verificación de que `versionName` coincide con el tag, keystore desde secrets, `assembleRelease`, `gh release` con `material-eleven-vX.Y.Z.apk`). Listo cuando el workflow pasa `actionlint` o la validación de sintaxis de GitHub en la rama.
- [ ] 9.2 Pasada final en el teléfono con el APK de release firmado localmente, recorriendo todos los scenarios de las nueve specs. Listo cuando todos pasan, o los que fallen tienen su corrección commiteada y verificada.
- [ ] 9.3 Después del merge y del archive: tag `v0.1.0`, verificar que el GitHub Release adjunta el APK y que se instala encima de una instalación de release previa sin perder el permiso. Listo cuando se verifican "Release publicado" e "Instalar la versión siguiente" (con un APK 0.1.1 de prueba sin publicar).
