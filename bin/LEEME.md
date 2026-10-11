# SEVETCO - Carpeta de pruebas

Aqui esta todo lo que se necesita para probar el proyecto. No hace falta abrir
ninguna otra carpeta.

## Sitio web publicado

El panel web esta en linea en Firebase Hosting:

**https://sevetco-dd27e.web.app**

(Direccion alternativa: https://sevetco-dd27e.firebaseapp.com)

Se entra con el mismo usuario de la app movil. Para ver el panel completo hay que
cambiar el campo `tipo_usuario` de tu documento en la coleccion `usuario` a
`ADMIN` (ver `firebase\LEEME.md`).

```
bin\
|-- apk\sevetco.apk        aplicacion movil (se descarga desde la pagina web)
|-- web\                   pagina web (abrir con Live Server)
|-- mockups\               los mockups de las dos apps
|   |-- web\               panel de administracion y proveedores (15 pantallas)
|   +-- mobile\            aplicacion de propietarios (16 pantallas)
|-- firebase\              reglas de seguridad e instrucciones de la base de datos
+-- evidencia-app-login.png  captura de la app funcionando en el Moto G23
```

Base de datos: **Firebase (Authentication + Cloud Firestore)**.

Mapas: **Google Maps** en la app movil (clave en
`android\app\src\main\res\values\strings.xml`, restringida al paquete
`com.sevetco.app` y a la huella SHA-1 del equipo de pruebas). En la pagina web
se usa OpenStreetMap con Leaflet.

## Cuentas para probar

| Rol | Correo | Contrasena |
| --- | --- | --- |
| Propietario (app movil) | lgonzalh@outlook.com | Alfa1234 |
| Proveedor (app movil) | langonzalezh@poligran.edu.co | Beta1234 |
| Administrador (panel web) | lantonium@gmail.com | entra con Google |

El correo de acceso vive en Firebase Authentication y la ficha de la coleccion
`usuario` se mantiene igual. Si desde la app se cambia el correo, el cambio se
aplica solo cuando se abre el enlace que llega al correo nuevo, y la ficha de la
base de datos se actualiza sola en la siguiente entrada.

Para cambiar una contrasena no hay que borrar la cuenta: se usa **Olvide mi
contrasena** en la app o *Restablecer contrasena* en la consola. Si una cuenta
se borra y se crea otra vez, la ficha de la base de datos queda desconectada y se
vuelve a enlazar con:

```
powershell -File tools\reasignar-cuenta.ps1 -Correo CORREO -UidNuevo IDENTIFICADOR
```

## Que quedo probado en el Moto G23 (10/10/2026)

- Entrar con el correo y la contrasena de la base de datos.
- Mascotas: foto desde la camara, edad y edicion.
- Agendar cita eligiendo mascota, servicio, fecha, hora y ubicacion.
- Reagendar con la lista de horas disponibles (8:00 a.m. a 6:00 p.m.).
- Cancelar cita con confirmacion.
- Trazabilidad de la cita (quien y cuando).
- Planes, servicios e historial de tratamientos.
- Mi cuenta: ver el correo de la base de datos, cambiarlo con enlace de
  confirmacion y cambiar la contrasena.
- Recuperar la contrasena desde el ingreso.
- Boton "i" con la pantalla "Acerca de esta App".

### Vista del proveedor veterinario (RF-10)

Al entrar con la cuenta de proveedor, la app muestra una barra mas corta
(Inicio, Citas, Mi cuenta). En **Citas** aparecen los servicios que el
administrador le asigno, con la foto y el nombre de la mascota, el dueno, la
fecha, el servicio y la direccion del domicilio. El registro del tratamiento se
hace desde el panel web (RF-11), por eso esas citas se consultan sin los botones
de reagendar ni cancelar, que son acciones del dueno.

## 1. App movil (Moto G23)

Ya esta instalada en el dispositivo conectado. Para volver a instalarla:

```
adb install -r apk\sevetco.apk
```

El telefono debe estar conectado por USB con la **depuracion USB** activada.

## 2. Pagina web

**En linea:** abrir https://sevetco-dd27e.web.app

En la pantalla de ingreso hay un boton **Descargar la aplicacion movil** que baja
`sevetco.apk` (enlace directo: https://sevetco-dd27e.web.app/sevetco.apk).

**En el equipo (Live Server):**

1. Abrir la carpeta `bin\web` en Visual Studio Code.
2. Clic derecho sobre `index.html` > **Open with Live Server**.
3. Se abre en Chrome.

Para volver a publicar los cambios de la web:

```
powershell -File tools\publicar.ps1
powershell -File tools\desplegar-web.ps1
```

## 3. Antes de probar: crear el proyecto de Firebase

Los pasos completos estan en `firebase\LEEME.md`. Resumen:

1. Crear el proyecto en https://console.firebase.google.com.
2. Habilitar **Authentication** con *Correo electronico* y *Google*.
3. Crear **Cloud Firestore** en modo produccion.
4. Pegar las reglas de `firebase\firestore.rules`.
5. Registrar la app **Web** y pegar su configuracion en
   `web\js\firebase-config.js`.
6. Registrar la app **Android** con el paquete `com.sevetco.app` y la huella
   SHA-1 `0F:7E:CF:D2:E1:01:63:C9:6A:08:AD:22:B0:40:E2:D1:01:07:9D:4B`,
   descargar `google-services.json` y guardarlo en
   `android\app\google-services.json`.
7. Cargar los servicios y planes de ejemplo (tablas en `firebase\LEEME.md`).

Mientras eso no este hecho, las dos apps abren pero muestran el aviso de
configuracion pendiente.

## 4. Convertir una cuenta en administrador

Al entrar por primera vez se crea el documento en la coleccion `usuario` con
tipo `PROPIETARIO`. Para usar el panel web, cambiar el campo `tipo_usuario` a
`ADMIN` desde la consola de Firestore.

## 5. Recompilar despues de agregar google-services.json

```
gradle -p android assembleDebug
powershell -File tools\publicar.ps1
adb install -r bin\apk\sevetco.apk
```

## 6. Mockups

Se generaron como imagenes PNG a partir de las pantallas definidas en los
documentos del proyecto. Se pueden importar en Balsamiq y Adobe XD.

