## Purpose

Fija el lenguaje visual compartido de Material Eleven en Android (paleta oscura, colores por familia de formato, tipografía), para que la app se reconozca como la misma de la PS Vita.

## ADDED Requirements

### Requirement: Paleta oscura fija
La interfaz SHALL usar siempre la paleta oscura de Material Eleven (fondo #120F17, superficies #1C1826 y #241F30, texto primario #F4EFEA, secundario #B0A8C0 y terciario #756C89), con independencia del modo claro u oscuro del sistema y del fondo de pantalla.

#### Scenario: Sistema en modo claro
- **WHEN** el teléfono está en modo claro y el usuario abre la app
- **THEN** la app se ve con el fondo oscuro de Material Eleven, igual que con el sistema en modo oscuro

### Requirement: Dibujo de borde a borde con barras del sistema legibles
La app SHALL dibujarse de borde a borde, bajo la barra de estado y la de navegación, sin que el contenido interactivo quede tapado por ellas. Los íconos de las barras del sistema MUST verse claros sobre el fondo oscuro.

#### Scenario: Navegación por gestos y por botones
- **WHEN** el usuario usa la app con navegación por gestos y luego con la barra de tres botones
- **THEN** en ambos casos el fondo llega a los bordes, el mini reproductor y los controles quedan por encima de la barra de navegación, y los íconos de las barras se leen

### Requirement: Colores por familia de formato
Los badges de formato SHALL mostrar el nombre del formato (por ejemplo FLAC, MP3, OPUS, WAV) como texto en el color de su familia sobre un fondo tenue del mismo color y sin contorno: verde #7FE0C1 para lossless (FLAC, WAV, ALAC) y morado #B79CE8 para lossy (MP3, OGG, OPUS, AAC, M4A). Un formato que no está en ninguna de las dos familias SHALL mostrarse en el color de texto secundario.

#### Scenario: Badges de dos familias
- **WHEN** la lista muestra un archivo FLAC y uno MP3
- **THEN** el FLAC lleva un badge "FLAC" verde y el MP3 un badge "MP3" morado, ambos legibles

### Requirement: Tipografía Manrope e IBM Plex Mono
La interfaz SHALL usar Manrope para títulos y texto, e IBM Plex Mono para los valores numéricos de tiempo y para los badges de formato. Los caracteres que esas fuentes no cubren (por ejemplo japonés, coreano o cirílico en los tags) MUST mostrarse con la fuente del sistema, nunca como cuadros vacíos.

#### Scenario: Tiempos en fuente monoespaciada
- **WHEN** suena una pista y avanza la barra de progreso
- **THEN** los tiempos transcurrido y restante no cambian de ancho mientras cambian los dígitos

#### Scenario: Tags con caracteres no latinos
- **WHEN** suena una pista cuyo título está en japonés
- **THEN** el título se ve completo y legible

### Requirement: El texto que no cabe se recorta de forma visible
Un título, artista o nombre de archivo más largo que su espacio SHALL recortarse con puntos suspensivos al final, sin desbordar sobre otros elementos.

#### Scenario: Título largo en la lista
- **WHEN** una canción tiene un título más largo que el ancho de su fila
- **THEN** la fila muestra el comienzo del título terminado en "…", y el badge de formato sigue visible
