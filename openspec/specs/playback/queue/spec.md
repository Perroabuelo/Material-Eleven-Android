# Playback Queue Specification

## Purpose

Define el orden en que suenan las pistas de la cola: orden natural y barajado, recorrido circular, repetición, saltos y el tratamiento de las pistas que no se pueden abrir. Mantiene el comportamiento de la PS Vita, adaptado a las convenciones de Android.

## Requirements

### Requirement: Origen y orden natural de la cola
La cola SHALL contener las pistas de la vista desde la que se inició la reproducción, en el orden de esa vista. Ese es su orden natural.

#### Scenario: Cola desde Canciones
- **WHEN** el usuario toca una canción en la vista Canciones
- **THEN** la cola son todas las canciones de esa vista, en orden alfabético por título

### Requirement: Orden barajado estable
Con el barajado encendido, la cola SHALL recorrerse en un orden aleatorio que contiene cada pista una sola vez y que empieza por la pista en curso. Ese orden MUST mantenerse estable mientras el barajado siga encendido: avanzar, retroceder o saltar a una pista no lo regenera. Encender o apagar el barajado MUST NOT cortar ni reiniciar la pista en curso.

#### Scenario: Una vuelta completa sin repetir
- **WHEN** el barajado está encendido en una cola de 20 pistas y el usuario avanza 19 veces
- **THEN** han sonado las 20 pistas, cada una una sola vez

#### Scenario: Encender el barajado
- **WHEN** suena una pista a mitad y el usuario enciende el barajado
- **THEN** la pista sigue sonando desde el mismo punto

#### Scenario: Apagar el barajado
- **WHEN** el usuario apaga el barajado
- **THEN** la pista en curso sigue sonando, y la siguiente es su vecina en el orden natural

### Requirement: Avanzar y retroceder por el orden vigente, de forma circular
Avanzar y retroceder SHALL moverse por el orden vigente (natural o barajado), desde cualquier superficie: la app, la notificación, la pantalla de bloqueo o los botones de audífonos y Bluetooth. Avanzar más allá del último puesto SHALL continuar por el primero, y retroceder más allá del primero SHALL continuar por el último. Cuando la pista termina por sí sola, la reproducción SHALL continuar con la siguiente del orden vigente, también de forma circular. Retroceder pasados los primeros 3 segundos de la pista SHALL volver al inicio de la pista en curso; dentro de esos 3 segundos SHALL ir a la pista anterior.

#### Scenario: El final de la cola continúa por el principio
- **WHEN** termina la última pista del orden vigente
- **THEN** suena la primera pista de ese orden

#### Scenario: Retroceder desde la primera pista
- **WHEN** suena la primera pista del orden vigente desde hace menos de 3 segundos y el usuario retrocede
- **THEN** suena la última pista de ese orden

#### Scenario: Retroceder a mitad de pista
- **WHEN** una pista lleva 40 segundos sonando y el usuario retrocede
- **THEN** la misma pista vuelve a empezar desde el inicio

#### Scenario: Retroceder con el barajado encendido
- **WHEN** el barajado está encendido, la pista en curso lleva menos de 3 segundos y el usuario retrocede
- **THEN** suena la pista que sonó inmediatamente antes, y no la vecina anterior en el orden natural

### Requirement: Repetir la pista en curso
Con la repetición encendida, cuando la pista en curso termina por sí sola SHALL volver a sonar la misma pista. Un avance o retroceso manual SHALL moverse por el orden vigente igual que con la repetición apagada. La repetición y el barajado MUST ser independientes: cambiar uno no altera el otro.

#### Scenario: La pista termina con la repetición encendida
- **WHEN** la repetición está encendida y la pista en curso termina
- **THEN** la misma pista vuelve a sonar desde el inicio

#### Scenario: Saltar con la repetición encendida
- **WHEN** la repetición está encendida y el usuario pide la pista siguiente
- **THEN** suena la siguiente pista del orden vigente

#### Scenario: Los dos modos son independientes
- **WHEN** la repetición está encendida y el usuario enciende el barajado
- **THEN** la repetición sigue encendida

### Requirement: Saltar a una pista elegida por el usuario
Elegir una pista de la vista previa de las próximas pistas SHALL hacerla sonar sin alterar el orden vigente: después de ella suena la que la sigue en ese orden.

#### Scenario: Elegir una próxima pista
- **WHEN** el usuario toca la tercera pista de la vista previa
- **THEN** suena esa pista, y la vista previa pasa a mostrar las que la siguen en el orden vigente

### Requirement: Pistas que no se pueden abrir
Cuando la pista a la que toca pasar no se puede abrir (fue borrada, está dañada o su formato no es compatible), la reproducción SHALL continuar por la siguiente del orden vigente en la misma dirección del movimiento, probando como mucho una vuelta completa. Si ninguna pista de la cola se puede abrir, la reproducción SHALL detenerse y la app SHALL mostrar un aviso breve, sin cerrarse ni quedarse en un estado del que no se pueda salir.

#### Scenario: Una pista ilegible no detiene la reproducción
- **WHEN** la pista siguiente fue borrada del teléfono después de armar la cola
- **THEN** se salta y suena la siguiente del orden vigente

#### Scenario: Ninguna pista se puede abrir
- **WHEN** todas las pistas de la cola están dañadas
- **THEN** la reproducción se detiene, aparece un aviso breve y la app sigue respondiendo
