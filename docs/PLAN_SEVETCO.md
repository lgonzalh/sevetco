# SEVETCO - Plan de proyecto

Plataforma Integral de Servicios Veterinarios a Domicilio en Colombia
Gerencia de Proyectos Informaticos - Politecnico Grancolombiano

Este documento es la hoja de ruta tecnica y de entregables del proyecto. Complementa
las entregas academicas ya presentadas:

* Entrega Previa 1 (Semana 3): acta de constitucion, EDT, diccionario de la EDT,
  requerimientos del producto y plan de tiempo.
* Entrega Previa 2 (Semana 5): plan de calidad, casos de uso, diagrama de clases,
  diagramas de secuencia, modelo entidad-relacion, diccionario de datos y plan de RRHH.
* Entrega final (Semana 7): producto demostrable (mockups + apps funcionales) y cierre.

---

## 1. Que es SEVETCO

SEVETCO conecta a duenos de mascotas con una red certificada de proveedores
veterinarios que prestan servicios **a domicilio**:

| Servicio | Descripcion |
| --- | --- |
| Consulta veterinaria | Valoracion clinica en el domicilio |
| Vacunacion | Aplicacion de vacunas y carnet |
| Estetica | Banio, corte y cuidado del pelaje |
| Entrenamiento | Sesiones de adiestramiento basico |
| Servicio funerario | Manejo y disposicion del cuerpo |

### Actores

* **Propietario de mascota** -> usa la **app movil**.
* **Proveedor veterinario** -> usa el **panel web** (y en la app movil ve sus citas).
* **Administrador** -> usa el **panel web**.

### Decision del alcance (profesora vs. equipo)

La indicacion original pedia app web + app movil + base de datos desplegada en un
servidor. La profesora ajusto el alcance a **mockups/prototipos**. El equipo decidira
entregar **ambos**: los mockups exigidos (Balsamiq y Adobe XD) + una version
**funcional** del producto (web + Android + Supabase). Eso cubre la nota de la rubrica
y ademas demuestra el producto real.

---

## 2. Herramientas y versiones del proyecto

| Frente | Herramienta | Version | Entregable |
| --- | --- | --- | --- |
| Mockup web | Balsamiq Wireframes | 4.7.1 | Archivo `.bmpr` + export PNG/PDF |
| Mockup movil | Adobe XD | 59.0.12.2 | Archivo `.xd` + prototipo navegable + export |
| Web funcional | HTML5 + CSS3 + JavaScript (ES Modules) | - | Sitio estatico en `web/` |
| App movil | Android nativo - Kotlin + Jetpack Compose | API 34 | APK en `bin/` |
| Base de datos | Cloud Firestore (proyecto Firebase) | - | `firebase/firestore.rules` |
| Editor web | Visual Studio Code + Live Server | - | `http://127.0.0.1:5500` |
| Prueba en dispositivo | Moto G23 (Android 13) | via ADB | APK instalada |

**Regla de oro del alcance:** sin .NET, sin backend propio. Firebase aporta
Authentication, Cloud Firestore y las reglas de seguridad.

> Cambio de plataforma: la cuenta de Supabase ya tenia sus dos proyectos
> ocupados, por lo que el proyecto migro a **Firebase Authentication + Cloud
> Firestore**. El detalle esta en `docs/FIREBASE.md`. La version anterior en
> PostgreSQL se conserva solo como referencia en `docs/anterior-supabase/`.

---

## 3. Arquitectura tecnica

```
                     +------------------------+
                     |     SUPABASE (nube)    |
                     |------------------------|
   App movil         |  PostgreSQL            |
   (Kotlin/Compose)  |  Auth (Google OAuth)   |
        |            |  REST / PostgREST      |
        |  HTTPS     |  Storage (fotos)       |
        +----------->|  Row Level Security    |
        |            +------------------------+
   Panel web         ^
   (HTML/JS)---------+
```

* **Autenticacion:** Supabase Auth con proveedor **Google** (OAuth 2.0). Un solo
  inicio de sesion sirve para la web y para la app movil.
* **Autorizacion:** vista/tabla `usuario` con columna `tipo`
  (`PROPIETARIO`, `PROVEEDOR`, `ADMIN`) y politicas **RLS** por rol.
* **Geolocalizacion:** se guarda latitud/longitud del domicilio y de la ruta
  (`ruta_desplazamiento`). En web con `navigator.geolocation`; en Android con
  `FusedLocationProviderClient`.
* **Mapas:** Leaflet + OpenStreetMap en web (gratis, sin API key). En Android,
  Google Maps SDK o simplemente la ubicacion + enlace a mapa.

### Decision de implementacion (rapida y mantenible)

* Web **sin build step**: se abre con Live Server. Supabase JS se importa por CDN
  (ESM) y las credenciales viven en `web/js/config.js`.
* Android con **Jetpack Compose** (menos XML, mas rapido de construir) y peticiones
  HTTPS a la API REST de Supabase. Alternativa con SDK comunitario de Supabase para
  Kotlin si se prefiere.

---

## 4. Modelo de datos (ya definido en la Entrega 2)

Entidades del modelo entidad-relacion, traducidas a tablas PostgreSQL:

| Tabla | Proposito | Campos clave |
| --- | --- | --- |
| `usuario` | Propietarios, proveedores y administradores | id, numero_documento, correo, nombre, telefono, tipo, estado |
| `proveedor` | Perfil profesional del prestador | id, id_usuario (FK), tarjeta_profesional, especialidad |
| `mascota` | Mascotas registradas | id, id_propietario (FK), nombre, especie, raza, fecha_nacimiento |
| `plan` | Planes comerciales | id, nombre, precio, vigencia_dias, estado |
| `servicio` | Catalogo de servicios | id, nombre, precio_base, duracion_minutos |
| `plan_servicio` | Relacion N:M plan <-> servicio | id_plan, id_servicio, cantidad_sesiones |
| `suscripcion` | Planes adquiridos | id, id_propietario, id_plan, fecha_inicio, fecha_fin, estado |
| `cita` | Solicitud, programacion y asignacion | id, id_propietario, id_mascota, id_servicio, id_proveedor, id_suscripcion, fecha_programada, direccion, lat, lng, estado |
| `atencion` | Diagnostico y tratamiento | id, id_cita, diagnostico, tratamiento, recomendaciones |
| `pago` | Pagos de citas o planes | id, id_cita, id_suscripcion, monto, metodo, estado, referencia_externa |
| `trazabilidad_cita` | Historial de estados | id, id_cita, id_usuario_responsable, estado_nuevo, fecha_evento |
| `ruta_desplazamiento` | Ruta del proveedor | id, id_cita, origen, destino, distancia_km, estado |

Reglas de integridad relevantes: correo/documento/referencia unicos; solo usuarios
`PROPIETARIO` son duenos de mascotas; solo proveedores aprobados y disponibles se
asignan a citas; `fecha_fin > fecha_inicio`; todo cambio de estado de cita se registra
en `trazabilidad_cita`.

Los scripts de creacion (`schema.sql`), datos de prueba (`seed.sql`) y politicas de
seguridad (`rls.sql`) van en `db/sql/`.

---

## 5. Requerimientos funcionales cubiertos

De los 17 requerimientos de la Entrega 1, el **MVP funcional** cubre RF-01 a RF-12
(los de prioridad **Alta**) y deja RF-13 a RF-17 como modulo administrativo ampliado.

| ID | Requerimiento | App | Prioridad |
| --- | --- | --- | --- |
| RF-01 | Registro e inicio de sesion (Google) | Ambas | Alta |
| RF-02 | Registrar una o varias mascotas | Movil | Alta |
| RF-03 | Datos basicos de la mascota | Movil | Alta |
| RF-04 | Mostrar planes disponibles | Movil | Alta |
| RF-05 | Consultar servicios de cada plan | Movil | Alta |
| RF-06 | Seleccionar servicio o plan | Movil | Alta |
| RF-07 | Programar cita (mascota, servicio, fecha, hora) | Movil | Alta |
| RF-08 | Consultar estado de las citas | Movil | Alta |
| RF-09 | Personal responsable del servicio | Ambas | Alta |
| RF-10 | Proveedor consulta citas asignadas | Web + Movil | Alta |
| RF-11 | Proveedor registra tratamiento | Web | Alta |
| RF-12 | Historial de tratamientos por mascota | Ambas | Alta |
| RF-13 | Gestion de proveedores | Web | Media |
| RF-14 | Gestion del portafolio de servicios | Web | Media |
| RF-15 | Trazabilidad del servicio | Web | Media |
| RF-16 | Gestion de pagos | Ambas | Media |
| RF-17 | Rutas y desplazamientos | Web | Baja |

---

## 6. Pantallas a mockupear

### 6.1 Balsamiq (panel web) - 14 pantallas

1. Login / inicio con Google
2. Dashboard general (indicadores)
3. Gestion de usuarios
4. Gestion de proveedores (listado)
5. Proveedor - crear / editar
6. Gestion del portafolio de servicios (listado)
7. Servicio - crear / editar
8. Gestion de planes (listado)
9. Plan - crear / editar + servicios incluidos
10. Citas - listado con filtros
11. Cita - detalle, asignar proveedor y cambiar estado
12. Trazabilidad de una cita (linea de tiempo)
13. Historial de atenciones por mascota
14. Reportes / pagos

### 6.2 Adobe XD (app movil) - 16 pantallas

1. Splash + onboarding
2. Login con Google
3. Registro / completar perfil
4. Home (proximas citas + accesos rapidos)
5. Mis mascotas (listado)
6. Registrar / editar mascota
7. Detalle de mascota
8. Catalogo de servicios
9. Detalle del servicio
10. Planes disponibles
11. Detalle del plan
12. Agendar cita - paso 1 (mascota + servicio)
13. Agendar cita - paso 2 (fecha y hora)
14. Agendar cita - paso 3 (direccion + ubicacion GPS)
15. Mis citas (listado + estado)
16. Detalle de cita (responsable, estado, historial)

Extras opcionales: pagos, notificaciones, perfil/configuracion, chat con el proveedor.

---

## 7. Estructura de carpetas del repositorio

```
C:\GitHub\sevetco\
|-- assets\
|   |-- icons\
|   |   |-- mobile\        Iconos para Adobe XD y la app Android
|   |   +-- web\           Iconos SVG para el panel web
|   |-- img\               Logos, fotos de ejemplo, ilustraciones
|   +-- ICONOS.md          Inventario de iconos (que descargar y donde va)
|-- bin\                   APK compilada para instalar en el Moto G23
|-- db\sql\                schema.sql / seed.sql / rls.sql
|-- docs\                  Este plan, manuales y evidencias
|-- mockups\
|   |-- balsamiq\          Archivos .bmpr (web)
|   |-- adobe-xd\          Archivos .xd (movil)
|   +-- export\            PNG/PDF exportados de los mockups
|-- web\                   App web ejecutable con Live Server
+-- android\               Proyecto Android Studio (Kotlin)
```

---

## 8. Plan de trabajo por fases

### Fase 0 - Preparacion del entorno (0.5 dia)

* Crear el proyecto en **Supabase** y copiar `Project URL` + `anon key`.
* Crear en **Google Cloud Console** el cliente OAuth:
  * Cliente **Web** (para el panel web).
  * Cliente **Android** con el `package name` y la huella **SHA-1** del equipo.
* Instalar Android Studio, JDK 17 y drivers USB del Moto G23.
* Activar **Opciones de desarrollador + Depuracion USB** en el Moto G23.

Entregable: credenciales y entorno listos (sin esto no hay login real ni APK).

### Fase 1 - Base de datos (0.5 dia)

* Ejecutar `schema.sql` en Supabase.
* Cargar catalogos con `seed.sql` (servicios, planes, plan_servicio).
* Activar **RLS** y politicas por rol (`rls.sql`).

Entregable: base de datos operativa y verificable.

### Fase 2 - Mockups (1 dia)

* Balsamiq: las 14 pantallas del panel web.
* Adobe XD: las 16 pantallas de la app movil + prototipo con enlaces.
* Exportar todo a `mockups/export/` (PNG para el documento, PDF para anexos).

Entregable: mockups navegables que responden al ajuste de la profesora.

### Fase 3 - App web funcional (1.5 dias)

* Estructura HTML/CSS/JS y navegacion entre vistas (SPA simple).
* Login con Google via Supabase Auth.
* Modulos: dashboard, proveedores, servicios, planes, citas, trazabilidad.
* Geolocalizacion del domicilio con `navigator.geolocation`.

Entregable: sitio que se abre con Live Server en Chrome.

### Fase 4 - App movil funcional (2 dias)

* Proyecto Android + Compose + navegacion por pantallas.
* Login con Google (Credential Manager -> Supabase ID token).
* Mascotas (CRUD), catalogo de servicios y planes, agendamiento de cita,
  mis citas, historial.
* Ubicacion con `FusedLocationProviderClient` al agendar.

Entregable: APK `bin/SEVETCO-<version>-debug.apk`.

### Fase 5 - Integracion y pruebas (0.5 dia)

* Pruebas funcionales de los flujos Alta del plan de calidad.
* Pruebas en dispositivo real (Moto G23): login, permisos, ubicacion, cita.
* Instalacion por ADB y verificacion.

### Fase 6 - Documentacion y cierre (0.5 dia)

* `README.md` con instrucciones de ejecucion.
* Manual de instalacion del APK y guia de Live Server.
* Evidencias (capturas de las apps y de la base de datos) en `docs/`.

---

## 9. Estado de la entrega (fecha: inmediata)

La entrega se adelanta a la fecha mas proxima posible. Estado actual:

| Frente | Estado |
| --- | --- |
| Mockups (web y movil) | Listos como imagenes PNG en `bin/mockups/` (31 pantallas) |
| Base de datos | Migrada a Firestore: reglas en `bin/firebase/` |
| App web funcional | Lista: 10 vistas en `bin/web/` |
| App movil funcional | Lista e instalada en el Moto G23; APK en `bin/` |
| Documentacion | `README.md`, `db/README.md`, `web/README.md`, `bin/LEEME.md` |

**Lo unico que falta para que funcione con datos reales** es pegar las
credenciales de Supabase (URL y anon key) en los dos archivos de configuracion y
activar el proveedor de Google. Sin eso, las apps abren pero muestran el aviso de
configuracion pendiente.

### Ruta critica restante

```
Crear proyecto Firebase -> habilitar Authentication y Firestore ->
pegar reglas -> registrar apps Web y Android -> pegar firebaseConfig y
guardar google-services.json -> recompilar APK -> probar
```

Recompilar la app movil despues de guardar `google-services.json`:

```
gradle -p android assembleDebug
powershell -File tools\publicar.ps1
adb install -r bin\SEVETCO-debug.apk
```

---

## 10. Entregables oficiales

| # | Entregable | Formato | Ubicacion |
| --- | --- | --- | --- |
| 1 | Mockups web | `.bmpr` + PNG/PDF | `mockups/balsamiq/`, `mockups/export/` |
| 2 | Mockups movil | `.xd` + PNG + prototipo | `mockups/adobe-xd/`, `mockups/export/` |
| 3 | App web funcional | Carpeta estatica | `web/` (Live Server + Chrome) |
| 4 | App Android | `.apk` instalable | `bin/` |
| 5 | Codigo fuente Android | Proyecto Gradle | `android/` |
| 6 | Base de datos | Scripts `.sql` + proyecto Supabase | `db/sql/` |
| 7 | Documentacion | `.md` / `.pdf` | `docs/` |
| 8 | Presentacion final | `.pptx` (opcional) | `docs/` |

Todo se publica automaticamente en la carpeta **`bin/`** con:

```
powershell -ExecutionPolicy Bypass -File tools\publicar.ps1
```

| En `bin/` | Contenido |
| --- | --- |
| `bin/SEVETCO-debug.apk` | App movil instalable |
| `bin/web/` | App web autocontenida (incluye sus iconos) |
| `bin/mockups/web/` y `bin/mockups/mobile/` | Los 31 mockups |
| `bin/firebase/` | Reglas de Firestore e instrucciones |
| `bin/LEEME.md` | Instrucciones para el usuario de pruebas |

### Checklist de aceptacion final

- [ ] Los mockups de Balsamiq y Adobe XD estan exportados y completos.
- [ ] La web abre en Chrome con Live Server y permite iniciar sesion con Google.
- [ ] La app se instala en el Moto G23 desde `bin/` y permite iniciar sesion.
- [ ] Ambos consumen la misma base de datos de Supabase.
- [ ] Se registra al menos una mascota, una cita y un tratamiento de prueba.
- [ ] La ubicacion se captura correctamente (web y movil).
- [ ] La documentacion explica como reproducir todo desde cero.

---

## 11. Riesgos y mitigacion

| Riesgo | Impacto | Mitigacion |
| --- | --- | --- |
| Login Google mal configurado (SHA-1 / paquete) | Alto | Configurar al inicio y probar en el Moto G23 desde el dia 1 |
| Poco tiempo antes de la entrega final | Alto | Congelar alcance en el MVP minimo de la seccion 9 |
| Sin conexion estable | Medio | Modo demostracion con datos de ejemplo precargados |
| Version de Adobe XD / Balsamiq distinta a la del aula | Bajo | Exportar siempre PNG + PDF |
| Cambios de ultima hora de la profesora | Medio | Mantener mockups y funcional desacoplados (misma UI en ambos) |

