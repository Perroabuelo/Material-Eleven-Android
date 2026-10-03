## Purpose

Hace que el color de acento de toda la interfaz refleje la carátula de la pista en reproducción, igual que en la PS Vita, sin perder legibilidad sobre el fondo oscuro.

## ADDED Requirements

### Requirement: El acento de toda la interfaz refleja la carátula de la pista en reproducción
Cuando suena una pista con carátula, el sistema SHALL derivar de ella un color de acento y aplicarlo a todos los elementos de acento de la interfaz (control de reproducción principal, barra de progreso, controles activos, fila de la pista en curso y mini reproductor). Al cambiar de pista, el acento SHALL pasar al de la carátula nueva con una transición suave.

#### Scenario: Cambiar a una pista con otra carátula
- **WHEN** suena una pista de carátula predominantemente roja y el usuario pasa a una de carátula predominantemente azul
- **THEN** los elementos de acento pasan de un rojo a un azul, en Reproduciendo, en el mini reproductor y en la lista

### Requirement: Color de acento fijo cuando no hay carátula
Cuando no suena nada, o la pista en curso no tiene carátula, el acento SHALL ser el color fijo #FF9166.

#### Scenario: Pista sin carátula
- **WHEN** suena una pista sin carátula embebida ni asociada
- **THEN** los elementos de acento se ven en el naranja fijo #FF9166

### Requirement: Un detalle de color pequeño en la carátula define el acento
El color derivado SHALL ser el del matiz que más pesa en la carátula, ponderando cada muestra por su saturación e ignorando negros, blancos y grises, de modo que un detalle vivo y pequeño se imponga a una mayoría apagada. Basta con que al menos el 1 % de las muestras tenga color para que la carátula cuente como cromática.

#### Scenario: Carátula casi negra con un detalle rojo
- **WHEN** la carátula es casi toda negra con un pequeño logo rojo vivo
- **THEN** el acento es un rojo

### Requirement: Acento neutro para carátulas sin color
Cuando la carátula existe pero no tiene color suficiente (blanco y negro o escala de grises), el acento SHALL ser el neutro #E6E3EC, y el contenido dibujado sobre el acento SHALL pasar a un color oscuro.

#### Scenario: Carátula en blanco y negro
- **WHEN** suena una pista con carátula en escala de grises
- **THEN** los elementos de acento se ven en gris claro, y el ícono del botón de reproducción se dibuja oscuro sobre ellos

### Requirement: El color derivado se mantiene legible sobre el fondo oscuro
El acento derivado SHALL ajustarse para que su saturación quede entre 0,60 y 0,95, su luminosidad entre 0,58 y 0,76, y su contraste sobre el fondo de la app sea al menos 4,5:1, aclarándolo si hace falta.

#### Scenario: Carátula de un violeta oscuro
- **WHEN** la carátula es predominantemente violeta oscuro
- **THEN** el acento es un violeta lo bastante claro para tener al menos 4,5:1 de contraste sobre el fondo

### Requirement: El contenido dibujado sobre el acento se mantiene legible
El ícono o el texto dibujado sobre una superficie de color de acento SHALL usar el color de fondo oscuro cuando el acento es claro, y en todo caso MUST tener un contraste de al menos 4,5:1 con el acento.

#### Scenario: Ícono sobre el botón principal
- **WHEN** el acento es un amarillo claro
- **THEN** el ícono de reproducir o pausar se dibuja oscuro sobre él
