# Migracion de Supabase a Firebase

Se cambia Supabase por **Firebase Authentication + Cloud Firestore**, porque la
cuenta de Supabase ya tiene sus dos proyectos ocupados.

## Que se mantiene igual

* La estructura del repositorio (`web/`, `android/`, `bin/`, `assets/`).
* Los iconos de `assets/icons/` (no se usa ninguna otra libreria de iconos).
* Los nombres de las entidades del modelo entidad-relacion: `usuario`,
  `proveedor`, `mascota`, `plan`, `servicio`, `plan_servicio`, `suscripcion`,
  `cita`, `atencion`, `pago`, `trazabilidad_cita`, `ruta_desplazamiento`.
* Los requerimientos RF-01 a RF-17.

## Que cambia

| Antes (Supabase) | Ahora (Firebase) |
| --- | --- |
| PostgreSQL (tablas y filas) | Cloud Firestore (colecciones y documentos) |
| Row Level Security | Reglas de seguridad de Firestore |
| Supabase Auth | Firebase Authentication |
| Claves URL + anon | `firebaseConfig` (web) y `google-services.json` (Android) |

En Firestore no hay llaves foraneas ni uniones. Para poder aplicar las reglas de
seguridad, cada documento guarda ademas el propietario y el proveedor. Eso se
documenta en `firebase/firestore.rules`.

## Paso 1. Crear el proyecto en Firebase

1. Entrar a https://console.firebase.google.com y crear un proyecto llamado
   `sevetco`.
2. Desactivar Google Analytics (no se necesita).

## Paso 2. Habilitar la autenticacion

1. **Build > Authentication > Comenzar**.
2. En **Sign-in method** activar:
   * **Correo electronico/Contrasena**.
   * **Google** (elegir un correo de soporte del proyecto).
3. En **Authentication > Settings > Dominios autorizados** ya aparece
   `localhost`; agregar `127.0.0.1` para que funcione Live Server.

## Paso 3. Crear la base de datos

1. **Build > Firestore Database > Crear base de datos**.
2. Elegir **Modo de produccion**.
3. Ubicacion: `us-central1` o la mas cercana.
4. Luego pegar las reglas de `firebase/firestore.rules` en la pestana **Reglas**.

## Paso 4. Registrar la aplicacion web

1. **Configuracion del proyecto > Tus apps > Web (`</>`)**.
2. Nombre: `SEVETCO Web`.
3. Copiar el objeto `firebaseConfig` y pegarlo en
   `web/js/firebase-config.js`.

## Paso 5. Registrar la aplicacion Android

1. **Configuracion del proyecto > Tus apps > Android**.
2. Nombre del paquete: `com.sevetco.app`
3. Huella digital SHA-1 (certificado de depuracion de este equipo):

```
0F:7E:CF:D2:E1:01:63:C9:6A:08:AD:22:B0:40:E2:D1:01:07:9D:4B
```

   (SHA-256, por si lo pide: `8F:2A:86:CC:31:2E:C3:69:32:4B:03:9D:6B:6A:1E:DB:51:CB:40:01:59:71:7C:AC:60:8A:56:27:C5:AB:3C:0E`)

4. Descargar `google-services.json` y guardarlo en:

```
android\app\google-services.json
```

## Paso 6. Cargar los datos iniciales

En **Firestore Database > Datos**, crear la coleccion `servicio` con estos cinco
documentos (ID automatico):

| nombre | precio_base | duracion_minutos |
| --- | --- | --- |
| Consulta veterinaria a domicilio | 120000 | 60 |
| Vacunacion a domicilio | 80000 | 30 |
| Estetica canina y felina | 100000 | 90 |
| Entrenamiento basico | 150000 | 60 |
| Servicio funerario | 350000 | 120 |

Y la coleccion `plan`:

| nombre | precio | vigencia_dias | estado |
| --- | --- | --- | --- |
| Plan Basico | 250000 | 30 | ACTIVO |
| Plan Integral | 480000 | 30 | ACTIVO |
| Plan Premium | 780000 | 30 | ACTIVO |

## Paso 7. Convertir una cuenta en administrador

Al entrar con Google se crea el documento en `usuario` con tipo `PROPIETARIO`.
Para poder usar el panel web, cambiar en la consola de Firestore el campo
`tipo_usuario` a `ADMIN`.

## Estructura de las colecciones

| Coleccion | Documento |
| --- | --- |
| `usuario` | `id = uid de Firebase Auth` |
| `proveedor` | `id_usuario`, `tarjeta_profesional`, `especialidad` |
| `mascota` | `id_propietario`, `nombre`, `especie`, `raza`, `fecha_nacimiento` |
| `plan` | `nombre`, `precio`, `vigencia_dias`, `estado` |
| `servicio` | `nombre`, `precio_base`, `duracion_minutos` |
| `plan_servicio` | `id_plan`, `id_servicio`, `cantidad_incluida` |
| `suscripcion` | `id_propietario`, `id_plan`, `fecha_inicio`, `fecha_fin`, `estado` |
| `cita` | `id_propietario`, `id_mascota`, `id_servicio`, `id_proveedor`, `id_suscripcion`, `fecha_hora`, `estado` |
| `atencion` | `id_cita`, `id_propietario`, `id_proveedor`, `diagnostico`, `tratamiento`, `recomendaciones` |
| `pago` | `id_propietario`, `id_cita`, `id_suscripcion`, `monto`, `estado`, `referencia_externa` |
| `trazabilidad_cita` | `id_cita`, `id_propietario`, `id_proveedor`, `estado_nuevo`, `fecha_evento` |
| `ruta_desplazamiento` | `id_cita`, `id_propietario`, `id_proveedor`, `origen`, `destino`, `distancia_km`, `estado` |
| `agenda` | `id_cita`, `id_propietario`, `id_proveedor`, `inicio`, `fin`, `estado` |

## Problemas comunes (mensajes que ya aparecieron en las pruebas)

| Mensaje | Que significa | Solucion |
| --- | --- | --- |
| `CONFIGURATION_NOT_FOUND` | Authentication no esta activado en el proyecto | Authentication > **Comenzar** y habilitar los metodos de acceso |
| `Cloud Firestore API has not been used ... or it is disabled` | La base de datos Firestore todavia no existe | Firestore Database > **Crear base de datos** |
| `Missing or insufficient permissions` | Las reglas no estan pegadas o no corresponden | Pegar `firebase/firestore.rules` en la pestana **Reglas** |
| `auth/unauthorized-domain` (web) | El dominio no esta autorizado | Authentication > Settings > Dominios autorizados: agregar `127.0.0.1` |
| La app muestra "Falta google-services.json" | No se ha guardado el archivo de la app Android | Descargarlo y guardarlo en `android/app/google-services.json` |

Si al habilitar Google en Authentication se genera un cliente OAuth nuevo,
conviene volver a descargar `google-services.json`: el archivo debe incluir los
clientes OAuth de tipo Android y Web.

## Como se enlaza `ruta_desplazamiento` con las dos apps

La ruta de desplazamiento (RF-17) es **una sola por cita**, y ese es el enlace:

| Elemento | Valor |
| --- | --- |
| Donde vive | Coleccion `ruta_desplazamiento` de Firestore |
| ID del documento | **El mismo ID de la cita** |
| Campo de enlace | `id_cita` (ademas de `id_propietario` y `id_proveedor`) |

Los dos documentos quedan asi:

```
cita/ABC123
   id_propietario: "uid-del-cliente"
   id_mascota:     "doc-de-la-mascota"
   id_servicio:    "doc-del-servicio"
   fecha_hora:     10/10/2026 09:00
   estado:         PENDIENTE

ruta_desplazamiento/ABC123        <-- el mismo ID, por eso quedan ligados
   id_cita:        "ABC123"
   id_propietario: "uid-del-cliente"
   id_proveedor:   "uid-del-veterinario"   (al asignarlo)
   destino:        "4.613171, -74.082484"  (lo pone el cliente)
   origen:         "Sede norte"            (lo pone el proveedor)
   distancia_km:   6.2
   estado:         PENDIENTE | EN_RUTA | COMPLETADA
```

### Quien escribe que

| Momento | App | Que guarda |
| --- | --- | --- |
| El cliente agenda la cita | Movil | Crea `cita` y, si eligio ubicacion, crea `ruta_desplazamiento` con `destino` |
| El administrador asigna al veterinario | Web | Actualiza `cita.id_proveedor` y el `id_proveedor` de la ruta |
| El veterinario organiza el recorrido | Web | Completa `origen`, `distancia_km` y cambia el `estado` de la ruta |

Como el ID es compartido, las dos aplicaciones leen y escriben **el mismo
documento**: lo que el cliente marca con el mapa se ve de inmediato en el panel
web, y lo que el veterinario registra en el panel se ve en la app movil al
abrir la cita.

## Coleccion `agenda` y disponibilidad de los veterinarios

Para poder reagendar una cita hace falta saber que horarios tiene libres el
veterinario. El detalle de cada cita es privado (solo lo ve el cliente y el
veterinario asignado), por eso se agrego la coleccion **`agenda`**: una copia
minima, **sin datos personales**, que cualquier usuario con sesion puede leer.

```
agenda/ABC123
   id_cita:        "ABC123"
   id_propietario: "uid-del-cliente"
   id_proveedor:   "uid-del-veterinario"
   inicio:         10/10/2026 09:00
   fin:            10/10/2026 10:00
   estado:         PENDIENTE | ASIGNADA | EN_PROCESO | FINALIZADA | CANCELADA
```

Se escribe automaticamente cada vez que se crea, asigna o reagenda una cita, y
cuando se cancela el tramo queda marcado como `CANCELADA` (o sea, vuelve a
estar libre).

### Regla de disponibilidad (opcion B)

Un veterinario esta disponible para una cita cuando se cumplen las dos cosas:

1. Su usuario esta en estado `ACTIVO`.
2. No tiene otra cita en la `agenda` que se cruce con esa fecha y hora.

Ademas se definio un **horario de atencion de 8:00 a 18:00**, con citas de una
hora. Ese horario esta en un solo lugar de cada aplicacion
(`Horario.kt` en Android y las constantes al inicio de `views.js` en la web),
asi que se puede cambiar sin tocar nada mas.

Con eso funcionan:

* **Reagendar** en la app movil: solo muestra las horas libres del veterinario.
* **Asignar responsable** en el panel web: los veterinarios ocupados o
  inactivos aparecen marcados y no se pueden elegir.

