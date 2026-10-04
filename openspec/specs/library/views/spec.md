# Library Views Specification

## Purpose

Presenta la música del teléfono en vistas navegables y define cómo una vista se convierte en la cola de reproducción. En este cambio la única vista es Canciones.

## Requirements

### Requirement: Vista Canciones
La app SHALL mostrar todas las canciones obtenidas de la colección de música en una lista ordenada por título sin distinguir mayúsculas ni tildes, y con el artista como desempate. Cada fila SHALL mostrar la carátula en miniatura (o un marcador de posición si no hay), el título, el artista y el badge de formato. La lista MUST desplazarse con fluidez con colecciones de varios miles de canciones.

#### Scenario: Orden alfabético
- **WHEN** la colección tiene las canciones "zeta", "Álamo" y "beta"
- **THEN** la lista las muestra en el orden "Álamo", "beta", "zeta"

#### Scenario: Colección grande
- **WHEN** el teléfono tiene 5.000 canciones y el usuario recorre la lista rápido de punta a punta
- **THEN** la lista se desplaza sin saltos visibles y las carátulas van apareciendo sin bloquearla

### Requirement: La pista en curso se distingue en la lista
La fila de la pista que está sonando SHALL distinguirse de las demás con el color de acento.

#### Scenario: Pista en curso visible
- **WHEN** suena una canción y el usuario mira la lista
- **THEN** la fila de esa canción se ve resaltada con el acento, y ninguna otra

### Requirement: Reproducir desde una vista convierte esa vista en la cola
Tocar una canción de la vista SHALL reemplazar la cola por todas las canciones de la vista, en el orden en que la vista las muestra, y empezar a reproducir la canción tocada.

#### Scenario: Tocar una canción
- **WHEN** el usuario toca la tercera canción de la lista
- **THEN** suena esa canción, y al terminar suena la cuarta de la lista

### Requirement: La lista dice en qué estado está
Mientras se leen las canciones, la vista SHALL indicar que está cargando. Si el teléfono no tiene música, SHALL mostrar un mensaje que lo dice y sugiere copiar archivos de audio al teléfono, en lugar de una lista vacía sin explicación.

#### Scenario: Teléfono sin música
- **WHEN** el permiso está concedido y el teléfono no tiene ningún archivo de música
- **THEN** la vista muestra un mensaje que explica que no se encontró música
