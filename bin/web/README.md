# App web SEVETCO (panel de administracion y proveedores)

HTML + CSS + JavaScript + Bootstrap 5, con **Firebase Authentication** y
**Cloud Firestore**. No necesita compilacion: se abre con Live Server.

## Direccion en linea

**https://sevetco-dd27e.web.app**

Para volver a publicar despues de un cambio:

```
powershell -File tools\publicar.ps1
powershell -File tools\desplegar-web.ps1
```

## Requisitos

* Visual Studio Code con la extension **Live Server**.
* Google Chrome.
* Un proyecto de Firebase con Firestore y Authentication habilitados
  (pasos en `../docs/FIREBASE.md`).

## Configurar la conexion

Abrir `js/firebase-config.js` y pegar el objeto `firebaseConfig` que entrega la
consola de Firebase:

```js
export const FIREBASE_CONFIG = {
  apiKey: "...",
  authDomain: "...",
  projectId: "...",
  storageBucket: "...",
  messagingSenderId: "...",
  appId: "..."
};
```

## Ejecutar

1. Abrir la carpeta `C:\GitHub\sevetco` en VS Code.
2. Clic derecho sobre `web/index.html` > **Open with Live Server**.
3. Se abre en Chrome en `http://127.0.0.1:5500/web/index.html`.

Abrir el **repositorio completo** en VS Code (no solo la carpeta `web`), porque
los iconos estan en `assets/icons/`.

## Habilitar el acceso

En la consola de Firebase:

* **Authentication > Sign-in method**: activar *Correo electronico/Contrasena* y
  *Google*.
* **Authentication > Settings > Dominios autorizados**: agregar `127.0.0.1`.

El panel web es para administradores y proveedores. Al entrar por primera vez
la cuenta queda como `PROPIETARIO`; hay que cambiarla a `ADMIN` en la consola de
Firestore (ver `docs/FIREBASE.md`).

## Que incluye el panel

| Vista | Requerimiento |
| --- | --- |
| Panel | Resumen general de la operacion |
| Citas | RF-09 (responsable), RF-10, RF-15 |
| Proveedores | RF-13 |
| Servicios | RF-14 |
| Planes | RF-04, RF-05, RF-14 |
| Tratamientos | RF-11, RF-12 |
| Trazabilidad | RF-15 |
| Pagos | RF-16 |
| Rutas | RF-17 (incluye boton "Usar mi ubicacion") |
| Usuarios | Administracion de usuarios y responsable del servicio |

