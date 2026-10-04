## Purpose

Define cómo Material Eleven obtiene permiso para leer la música del teléfono y qué pistas considera reproducibles.

## ADDED Requirements

### Requirement: Permiso de lectura de música con explicación previa
Sin permiso de lectura de audio, la app SHALL mostrar una pantalla que explica para qué lo necesita, con un botón que lo solicita al sistema. En Android 13 o superior, la app MUST pedir solo el permiso de audio (no el de fotos, videos ni todos los archivos). En Android 8 a 12, donde no existe un permiso solo de audio, MUST pedir el permiso de lectura de almacenamiento y nunca el de administrar todos los archivos.

#### Scenario: Primer arranque
- **WHEN** el usuario abre la app por primera vez
- **THEN** ve la explicación y, al tocar el botón, el diálogo del sistema pide acceso a música y audio (en Android 8 a 12, a fotos, contenido multimedia y archivos)

#### Scenario: Permiso concedido
- **WHEN** el usuario concede el permiso
- **THEN** la app pasa directamente a la lista de canciones, sin reiniciarse

### Requirement: Un permiso negado tiene salida
Si el usuario negó el permiso y el sistema ya no muestra el diálogo, la pantalla de permiso SHALL ofrecer abrir los ajustes de la app para concederlo. Al volver a la app con el permiso concedido, la app SHALL mostrar la lista sin pedir otro paso.

#### Scenario: Negado de forma permanente
- **WHEN** el usuario negó el permiso dos veces y toca de nuevo el botón
- **THEN** la app abre la pantalla de ajustes de Material Eleven, y al conceder el permiso ahí y volver, se ve la lista de canciones

### Requirement: Las pistas salen de la colección de música del sistema
La app SHALL listar los archivos que el sistema registra como música, con título, artista, álbum, duración y formato. Un archivo sin título SHALL mostrarse con su nombre de archivo sin extensión, y uno sin artista o sin álbum SHALL mostrarse como "Desconocido" ("Unknown" en inglés). Las notas de voz, los tonos de llamada, las alarmas y los sonidos de notificación MUST quedar fuera.

#### Scenario: Pista con tags
- **WHEN** el teléfono tiene un FLAC con título, artista y álbum en sus tags
- **THEN** aparece en la lista con ese título y ese artista

#### Scenario: Pista sin tags
- **WHEN** el teléfono tiene un MP3 sin tags llamado "demo-01.mp3"
- **THEN** aparece en la lista como "demo-01", con artista "Desconocido"

#### Scenario: Grabaciones y tonos
- **WHEN** el teléfono tiene grabaciones de voz y tonos de llamada
- **THEN** ninguno aparece en la lista

### Requirement: Los audios de otras apps, las grabaciones y lo que no se puede reproducir quedan fuera
Algunos fabricantes registran como música cualquier audio. Por eso la lista MUST dejar fuera, además de lo que el sistema marca como no musical:
- todo lo que está dentro de las carpetas de medios de otras apps (`Android/media/` y `Android/data/`), como los audios y documentos de WhatsApp o Telegram;
- todo lo que está dentro de una carpeta de grabaciones, en cualquier nivel: `Recordings`, `Grabaciones`, `Call recordings`, `sound_recorder` o `Voice Recorder`, sin distinguir mayúsculas;
- los formatos que la app no puede reproducir (MIDI y otros formatos de tonos).

Un audio excluido aparece en la lista si el usuario lo mueve a otra carpeta, por ejemplo `Music/`.

#### Scenario: Audios de WhatsApp
- **WHEN** el teléfono tiene audios recibidos por WhatsApp en `Android/media/com.whatsapp/WhatsApp/Media/WhatsApp Audio/`
- **THEN** ninguno aparece en la lista

#### Scenario: Carpeta de grabaciones
- **WHEN** el teléfono tiene un archivo en `Grabaciones/` y otro en `MIUI/sound_recorder/`
- **THEN** ninguno de los dos aparece en la lista

#### Scenario: Archivo MIDI
- **WHEN** el teléfono tiene un archivo `.mid` en `Download/`
- **THEN** no aparece en la lista

#### Scenario: Un audio movido a Música
- **WHEN** el usuario mueve un MP3 desde `WhatsApp Documents` a `Music/`
- **THEN** ese MP3 aparece en la lista

### Requirement: La lista refleja la música añadida o borrada
Cuando se añaden o se borran archivos de música en el teléfono mientras la app está abierta o en segundo plano, la lista SHALL actualizarse sola, sin reiniciar la app ni perder la reproducción en curso.

#### Scenario: Copiar música con la app abierta
- **WHEN** el usuario copia un álbum nuevo al teléfono por USB con la app abierta
- **THEN** las canciones nuevas aparecen en la lista en unos segundos, sin cortar lo que suena
