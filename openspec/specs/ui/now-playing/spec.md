# Now Playing Specification

## Purpose

Presenta la pista en curso y sus controles: un mini reproductor siempre visible bajo las vistas y una pantalla Reproduciendo completa, con el lenguaje visual compartido de Material Eleven.

## Requirements

### Requirement: Mini reproductor fijo bajo las vistas
Mientras hay una cola cargada, la app SHALL mostrar un mini reproductor fijo en la parte inferior de las vistas, sobre la barra de navegación del sistema, con la carátula en miniatura, el título, el artista, un botón de reproducir/pausar, un botón de siguiente y una línea de progreso. Sin cola cargada MUST NOT mostrarse. El mini reproductor MUST NOT tapar la última fila de la lista.

#### Scenario: Aparece al reproducir
- **WHEN** el usuario toca una canción por primera vez desde que abrió la app
- **THEN** aparece el mini reproductor con esa canción, y la última fila de la lista sigue alcanzable

#### Scenario: Pausar desde el mini reproductor
- **WHEN** el usuario toca el botón de pausa del mini reproductor
- **THEN** la reproducción se pausa y el botón pasa a mostrar "reproducir"

### Requirement: El mini reproductor se expande a Reproduciendo
Tocar el mini reproductor, o deslizarlo hacia arriba, SHALL abrir Reproduciendo. Desde Reproduciendo, deslizar hacia abajo o usar el gesto o botón Atrás del sistema SHALL volver a la vista anterior, conservando su posición de desplazamiento.

#### Scenario: Abrir y cerrar
- **WHEN** el usuario toca el mini reproductor y después hace el gesto Atrás
- **THEN** se abre Reproduciendo y luego vuelve a la lista, en el mismo punto en que estaba

### Requirement: Identidad de la pista y formato
Reproduciendo SHALL mostrar la carátula grande de la pista (o un marcador de posición si no tiene), el título, el artista, el álbum y el badge de formato.

#### Scenario: Pista con tags y carátula
- **WHEN** suena un FLAC con carátula, título, artista y álbum
- **THEN** Reproduciendo muestra esa carátula, esos tres datos y un badge "FLAC" verde

### Requirement: Controles de transporte
Reproduciendo SHALL ofrecer anterior, reproducir/pausar, siguiente, barajado y repetición, cada uno con un área táctil de al menos 48 dp. El barajado y la repetición SHALL mostrarse marcados con el acento cuando están encendidos.

#### Scenario: Encender el barajado
- **WHEN** el usuario toca el control de barajado
- **THEN** el control queda marcado con el acento, la pista en curso no se corta y la vista previa pasa a mostrar el orden barajado

#### Scenario: Estado coherente con la notificación
- **WHEN** el usuario pausa desde la notificación y luego abre Reproduciendo
- **THEN** Reproduciendo muestra la reproducción en pausa

### Requirement: Barra de progreso con búsqueda
Reproduciendo SHALL mostrar el tiempo transcurrido, el tiempo restante y una barra de progreso que permite ir a otro punto de la pista tocándola o arrastrándola. Mientras se arrastra, el tiempo transcurrido SHALL mostrar el punto al que se va a saltar, y la reproducción SHALL saltar al soltar.

#### Scenario: Arrastrar la barra
- **WHEN** el usuario arrastra la barra hasta la mitad de una pista de 4:00 y suelta
- **THEN** mientras arrastra se ve 2:00 como tiempo transcurrido, y al soltar la reproducción continúa desde 2:00

### Requirement: Vista previa de las próximas pistas, de solo lectura
Reproduciendo SHALL mostrar las próximas pistas en el orden en que van a sonar según el orden vigente (natural o barajado), incluida la continuación circular por el principio de la cola. La vista previa MUST NOT ofrecer controles para reordenar ni quitar pistas.

#### Scenario: Orden barajado en la vista previa
- **WHEN** el barajado está encendido y quedan pistas por delante
- **THEN** la vista previa muestra las próximas pistas del orden barajado

#### Scenario: Última pista del orden
- **WHEN** suena la última pista del orden vigente
- **THEN** la vista previa muestra las primeras pistas de ese orden, por las que va a continuar
