# Playback Background Specification

## Purpose

Define cómo convive la reproducción con el resto del teléfono: seguir sonando fuera de la app, controles del sistema, botones multimedia y respeto por otras fuentes de audio. Reemplaza el "apagar la pantalla" de la PS Vita.

## Requirements

### Requirement: La reproducción continúa fuera de la app
La reproducción SHALL continuar cuando el usuario sale de la app, cambia a otra app o bloquea la pantalla, y MUST NOT detenerse por el ahorro de energía del sistema mientras está sonando.

#### Scenario: Bloquear la pantalla
- **WHEN** suena una pista y el usuario bloquea el teléfono durante 30 minutos
- **THEN** la música sigue sonando y pasa de una pista a otra sin cortes

#### Scenario: Usar otra app
- **WHEN** suena una pista y el usuario abre el navegador
- **THEN** la música sigue sonando

### Requirement: Controles en la notificación y en la pantalla de bloqueo
Mientras hay una cola cargada, el sistema SHALL mostrar una notificación de reproducción con la carátula, el título, el artista, la barra de progreso y los controles de anterior, reproducir/pausar y siguiente, también visibles en la pantalla de bloqueo. Tocar la notificación SHALL abrir la app en Reproduciendo.

#### Scenario: Controlar desde la pantalla de bloqueo
- **WHEN** el teléfono está bloqueado con música sonando y el usuario toca "siguiente" en la pantalla de bloqueo
- **THEN** suena la siguiente pista del orden vigente, y la pantalla de bloqueo muestra su título y su carátula

#### Scenario: Abrir desde la notificación
- **WHEN** el usuario toca la notificación de reproducción
- **THEN** se abre la app mostrando Reproduciendo

### Requirement: Botones multimedia de audífonos y Bluetooth
Los botones de reproducir/pausar, siguiente y anterior de audífonos con cable, audífonos Bluetooth y sistemas de auto SHALL controlar la reproducción, también con la pantalla bloqueada.

#### Scenario: Pausar desde audífonos Bluetooth
- **WHEN** suena música por audífonos Bluetooth y el usuario presiona el botón de pausa de los audífonos
- **THEN** la reproducción se pausa, y al presionarlo de nuevo continúa desde el mismo punto

### Requirement: Respeto del foco de audio
Cuando otra app empieza a reproducir audio de forma sostenida, o entra una llamada, la reproducción SHALL pausarse. Al terminar una interrupción breve (una llamada, una alarma) SHALL reanudarse sola. Ante sonidos cortos que admiten mezcla (una notificación o una indicación de navegación), el volumen SHALL bajar momentáneamente en vez de pausarse.

#### Scenario: Llamada entrante
- **WHEN** suena música y entra una llamada que el usuario contesta y luego corta
- **THEN** la música se pausa durante la llamada y se reanuda sola al cortar

#### Scenario: Otra app de música
- **WHEN** suena música en Material Eleven y el usuario reproduce un video en otra app
- **THEN** Material Eleven se pausa y no se reanuda sola

### Requirement: Pausa al desconectar los audífonos
Cuando se desconectan los audífonos con cable o Bluetooth por los que suena la música, la reproducción SHALL pausarse en vez de seguir por el altavoz.

#### Scenario: Desconectar los audífonos
- **WHEN** suena música por audífonos y el usuario los desconecta
- **THEN** la reproducción se pausa y no sale sonido por el altavoz

### Requirement: Cerrar la reproducción
Con la reproducción en pausa, el usuario SHALL poder descartar la notificación deslizándola, lo que detiene el servicio de reproducción. Al quitar la app de las recientes con la reproducción en pausa, la notificación MUST desaparecer.

#### Scenario: Descartar la notificación en pausa
- **WHEN** la reproducción está en pausa y el usuario desliza la notificación
- **THEN** la notificación desaparece y la app deja de reproducir
