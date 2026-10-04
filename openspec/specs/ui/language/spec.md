# Language Specification

## Purpose

Permite usar Material Eleven en inglés o en español, siguiendo el idioma del sistema, con un español neutro y bien escrito.

## Requirements

### Requirement: La interfaz está disponible en inglés y en español
Todos los textos de la interfaz, incluida la notificación de reproducción y la pantalla de permiso, SHALL existir en inglés y en español.

#### Scenario: Sistema en español
- **WHEN** el teléfono está en español y el usuario abre la app
- **THEN** todos los textos de la app se ven en español

### Requirement: El idioma sigue al del sistema, con el inglés como respaldo
La app SHALL mostrarse en el idioma del sistema cuando es inglés o español, en cualquier variante regional. Con cualquier otro idioma del sistema SHALL mostrarse en inglés. En Android 13 o superior, el idioma de la app MUST poder elegirse además desde los ajustes de idioma por aplicación del sistema.

#### Scenario: Variante regional
- **WHEN** el sistema está en español de Chile
- **THEN** la app se ve en español

#### Scenario: Idioma no soportado
- **WHEN** el sistema está en francés
- **THEN** la app se ve en inglés

#### Scenario: Idioma por aplicación
- **WHEN** el sistema está en inglés y el usuario elige español para Material Eleven en los ajustes de idioma de la app
- **THEN** la app se ve en español y el resto del sistema sigue en inglés

### Requirement: El español es neutro y tiene ortografía completa
Los textos en español SHALL usar un registro neutro (tuteo, sin voseo ni modismos regionales) y ortografía completa, con tildes, eñes y signos de apertura.

#### Scenario: Revisión de los textos
- **WHEN** se revisan todos los textos en español de la app
- **THEN** ninguno omite tildes, eñes ni signos de apertura, y ninguno usa voseo
