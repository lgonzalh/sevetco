# Iconos de SEVETCO

Inventario revisado icono por icono. Abre
[hoja_de_contacto.png](icons/hoja_de_contacto.png) para ver los iconos con su
nombre debajo: es la forma mas rapida de saber cual es cual.

## 1. Como estan organizados

```
assets/icons/
|-- shared/          54  los usan la app web Y la app movil
|-- web/              8  solo panel de administracion y proveedores
|-- mobile/           2  solo app del propietario
|-- _descartados/     4  se descargaron con el nombre equivocado (ver seccion 4)
+-- hoja_de_contacto.png  los iconos con su nombre
```

**No hay que duplicar nada.** Un icono se guarda una sola vez y las dos apps lo
referencian. Solo se separan los que pertenecen a una sola app.

### Regla para clasificar

1. Se usa en las dos apps -> `shared/`
2. Solo en el panel web -> `web/`
3. Solo en la app del propietario -> `mobile/`

## 2. Nombres corregidos

Al revisar los dibujos se corrigieron los siguientes nombres. Ahora **el nombre
describe lo que el icono realmente dibuja**:

| Nombre anterior | Nombre actual | Que dibuja |
| --- | --- | --- |
| `cancel.png` | `event_busy.png` | Calendario con una X |
| `event_busy.png` | `calendar_checklist.png` | Calendario con varias marcas |
| `delete.png` | `cancel.png` | Circulo con una X |
| `visibility.png` | `visibility_off.png` | Ojo tachado |
| `schelude.png` | `schedule.png` | Calendario con reloj |
| `volver.png` | `arrow_back.png` | Flecha de regresar |
| `notifications_0.png` | `notifications.png` | Campana |
| `notifications_1.png` | `notifications_active.png` | Campana con aviso |

Ademas se apartaron en `_descartados/` los archivos `hourglass_top.png` (dibujaba un
sol) y `key.png` (dibujaba flechas de sincronizacion), y se agregaron correctamente
`key.png`, `delete.png` y `visibility.png`.

## 3. Que es cada icono

### 3.1 Autenticacion y cuenta

| Icono | Funcionalidad | Web | Movil | Carpeta |
| --- | --- | --- | --- | --- |
| `google.png` | Boton "Continuar con Google" | Si | Si | shared |
| `login.png` | Iniciar sesion | Si | Si | shared |
| `logout.png` | Cerrar sesion | Si | Si | shared |
| `person.png` | Usuario / perfil | Si | Si | shared |
| `person_add.png` | Registro de usuario | Si | Si | shared |
| `account_circle.png` | Avatar de la sesion | Si | Si | shared |
| `key.png` | Campo de contrasena | Si | Si | shared |
| `visibility.png` | Ver contrasena | Si | Si | shared |
| `visibility_off.png` | Ocultar contrasena | Si | Si | shared |
| `shield_person.png` | Privacidad y permisos | Si | - | web |

### 3.2 Navegacion

| Icono | Funcionalidad | Web | Movil | Carpeta |
| --- | --- | --- | --- | --- |
| `home.png` | Inicio | Si | Si | shared |
| `menu.png` | Menu lateral | Si | Si | shared |
| `arrow_back.png` | Volver | Si | Si | shared |
| `close.png` | Cerrar | Si | Si | shared |
| `search.png` | Buscar | Si | Si | shared |
| `settings.png` | Configuracion | Si | Si | shared |
| `expand_more.png` | Desplegable | Si | Si | shared |
| `chevron_left.png` | Pagina anterior | Si | - | web |
| `chevron_right.png` | Pagina siguiente | Si | - | web |
| `dashboard.png` | Panel principal | Si | - | web |

### 3.3 Mascotas

| Icono | Funcionalidad | Web | Movil | Carpeta |
| --- | --- | --- | --- | --- |
| `pets.png` | Mascota | Si | Si | shared |
| `add_circle.png` | Agregar mascota | Si | Si | shared |
| `edit.png` | Editar | Si | Si | shared |
| `delete.png` | Eliminar | Si | Si | shared |
| `photo_camera.png` | Foto de la mascota | Si | Si | shared |
| `cake.png` | Fecha de nacimiento / edad | Si | Si | shared |
| `badge.png` | Raza / tarjeta profesional | Si | Si | shared |
| `list_alt.png` | Historial / listado | Si | Si | shared |
| `straighten.png` | Peso y talla | - | Si | mobile |

### 3.4 Servicios y planes

| Icono | Funcionalidad | Web | Movil | Carpeta |
| --- | --- | --- | --- | --- |
| `medical_services.png` | Consulta veterinaria | Si | Si | shared |
| `vaccines.png` | Vacunacion | Si | Si | shared |
| `content_cut.png` | Estetica | Si | Si | shared |
| `fitness_center.png` | Entrenamiento | Si | Si | shared |
| `local_florist.png` | Servicio funerario | Si | Si | shared |
| `house.png` | Servicio a domicilio | Si | Si | shared |
| `schedule.png` | Duracion del servicio | Si | Si | shared |
| `storefront.png` | Catalogo de planes | Si | Si | shared |
| `card_membership.png` | Plan / suscripcion | Si | Si | shared |
| `workspace_premium.png` | Plan premium | - | Si | mobile |
| `checklist.png` | Servicios incluidos | Si | Si | shared |
| `price_check.png` | Precio / tarifa | Si | Si | shared |
| `star.png` | Destacado / favorito | Si | Si | shared |

### 3.5 Citas

| Icono | Funcionalidad | Web | Movil | Carpeta |
| --- | --- | --- | --- | --- |
| `calendar_month.png` | Calendario | Si | Si | shared |
| `calendar_checklist.png` | Agenda del dia | Si | Si | shared |
| `edit_calendar.png` | Programar cita | Si | Si | shared |
| `event.png` | Citas | Si | Si | shared |
| `event_available.png` | Cita confirmada | Si | Si | shared |
| `event_busy.png` | Cita cancelada / ocupada | Si | Si | shared |
| `pending_actions.png` | Cita pendiente | Si | Si | shared |
| `task_alt.png` | Cita finalizada | Si | Si | shared |
| `autorenew.png` | Reprogramar cita | Si | Si | shared |

### 3.6 Ubicacion

| Icono | Funcionalidad | Web | Movil | Carpeta |
| --- | --- | --- | --- | --- |
| `location_on.png` | Direccion del servicio | Si | Si | shared |
| `my_location.png` | Usar mi ubicacion (GPS) | Si | Si | shared |

### 3.7 Pagos

| Icono | Funcionalidad | Web | Movil | Carpeta |
| --- | --- | --- | --- | --- |
| `payments.png` | Pagos | Si | Si | shared |
| `download.png` | Descargar / exportar | Si | - | web |
| `print.png` | Imprimir | Si | - | web |

### 3.8 Proveedores y administracion

| Icono | Funcionalidad | Web | Movil | Carpeta |
| --- | --- | --- | --- | --- |
| `support_agent.png` | Soporte | Si | Si | shared |
| `star_rate.png` | Calificacion del proveedor | Si | - | web |
| `insights.png` | Indicadores | Si | - | web |

### 3.9 Notificaciones y estados

| Icono | Funcionalidad | Web | Movil | Carpeta |
| --- | --- | --- | --- | --- |
| `notifications.png` | Notificaciones | Si | Si | shared |
| `notifications_active.png` | Notificaciones nuevas | Si | Si | shared |
| `check_circle.png` | Mensaje de exito | Si | Si | shared |
| `warning.png` | Advertencia | Si | Si | shared |

## 4. Estado de los iconos

Ya estan completos los tres que faltaban: `key.png` (llave), `delete.png` (caneca) y
`visibility.png` (ojo abierto).

Queda pendiente un solo icono, que no bloquea el desarrollo:

| Que se necesita | Nombre a guardar | Que paso |
| --- | --- | --- |
| Circulo con "!" (mensaje de error) | `error.png` | El archivo descargado era identico al circulo con X. Esta apartado en `_descartados/`. |

Busca ese nombre en https://fonts.google.com/icons y guarda el PNG en
`assets/icons/shared/`.

## 5. Como usar los iconos en cada herramienta

| Herramienta | Que hacer con los PNG |
| --- | --- |
| **Adobe XD** | Arrastrar el PNG al panel de recursos; el fondo transparente se respeta. |
| **Balsamiq** | Importar como imagen (`Project > Add Asset`) y reutilizar. |
| **Web** | Referenciar por ruta, por ejemplo `../assets/icons/shared/pets.png`. |
| **Android** | No hace falta el archivo: se usa `Icons.Filled.Pets` con la libreria `material-icons-extended`. |

