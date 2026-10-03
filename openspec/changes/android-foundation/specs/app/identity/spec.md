## Purpose

Define cómo se identifica Material Eleven en el teléfono (nombre, ícono) y cómo se distribuye, de modo que cada versión nueva se instale encima de la anterior sin perder nada.

## ADDED Requirements

### Requirement: La app se llama "Material Eleven" en el sistema
El sistema SHALL mostrar la app con el nombre "Material Eleven" en el lanzador, en los ajustes de aplicaciones y en la notificación de reproducción, en cualquier idioma del sistema.

#### Scenario: Nombre en el lanzador
- **WHEN** el usuario instala el APK y abre el cajón de aplicaciones
- **THEN** la app aparece como "Material Eleven"

### Requirement: Ícono adaptativo propio
La app SHALL tener un ícono adaptativo derivado del ícono de Material-Eleven, con capa de fondo y capa de primer plano, y un ícono monocromo para los íconos temáticos de Android 13 o superior. El motivo MUST quedar completo dentro de la zona segura con cualquier máscara del lanzador (círculo, cuadrado redondeado, gota).

#### Scenario: Ícono con la máscara del lanzador
- **WHEN** el usuario mira el ícono en el lanzador del Poco X7 Pro
- **THEN** el motivo se ve completo y centrado, sin recortes

#### Scenario: Íconos temáticos
- **WHEN** el usuario activa los íconos temáticos del sistema
- **THEN** la app muestra su versión monocroma, y no un cuadrado genérico

### Requirement: Cada release es un APK firmado con la misma clave
Cada GitHub Release SHALL adjuntar un APK firmado siempre con la misma clave de release, cuyo `versionName` coincide con el tag del release.

#### Scenario: Release publicado
- **WHEN** se publica el tag `v0.1.0`
- **THEN** el GitHub Release `v0.1.0` adjunta un APK firmado cuya versión, vista en los ajustes de aplicaciones, es 0.1.0

### Requirement: Actualizar sobre una instalación previa conserva los datos
Instalar un APK de una versión más nueva sobre una instalación previa SHALL actualizar la app sin pedir desinstalarla y sin perder los permisos concedidos ni los datos de la app.

#### Scenario: Instalar la versión siguiente
- **WHEN** el usuario tiene instalada una versión de release e instala encima el APK de la versión siguiente
- **THEN** el instalador ofrece "Actualizar", la app abre sin volver a pedir el permiso de música y conserva sus datos
