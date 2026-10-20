# SEVETCO

Plataforma Integral de Servicios Veterinarios a Domicilio en Colombia.
Proyecto academico - Gerencia de Proyectos Informaticos, Politecnico Grancolombiano.

## Sitio en linea

**https://sevetco-dd27e.web.app** (Firebase Hosting, proyecto `sevetco-dd27e`)

## Contenido del repositorio

| Carpeta | Contenido |
| --- | --- |
| `docs/` | [Plan del proyecto](docs/PLAN_SEVETCO.md), manuales y evidencias |
| `mockups/` | Wireframes de Balsamiq (web) y Adobe XD (movil) + exportaciones |
| `web/` | App web ejecutable con VS Code + Live Server ([instrucciones](web/README.md)) |
| `android/` | App movil Android nativa (Kotlin + Jetpack Compose) |
| `firebase/` | Reglas de seguridad de Cloud Firestore ([guia](docs/FIREBASE.md)) |
| `assets/` | [Iconos](assets/ICONOS.md), logo e imagenes |
| `tools/` | Script de publicacion a `bin/` y generador de mockups |
| `bin/` | APK compilada para instalar en el dispositivo |

## Orden de puesta en marcha

1. **Firebase:** crear el proyecto, habilitar Authentication y Firestore, pegar
   las reglas y cargar los datos iniciales ([guia completa](docs/FIREBASE.md)).
2. **App web:** pegar `firebaseConfig` en `web/js/firebase-config.js` y abrir
   `web/index.html` con Live Server.
3. **App movil:** guardar `google-services.json` en `android/app/`, recompilar y
   publicar con `tools/publicar.ps1`.

Tecnologia: Firebase Authentication (correo y Google) + Cloud Firestore.

## Como ejecutar la app web

1. Abrir la carpeta `C:\GitHub\sevetco` en VS Code.
2. Ejecutar **Live Server** sobre `web/index.html`.
3. Se abre en Chrome en `http://127.0.0.1:5500/web/index.html`.

El detalle de la configuracion esta en [web/README.md](web/README.md).

Para publicar los cambios en el sitio en linea:

```
powershell -File tools\publicar.ps1
powershell -File tools\desplegar-web.ps1
```

## Como instalar la app movil

1. Conectar el movil por USB con la depuracion USB activada.
2. Instalar la APK de `bin/` con el comando `adb install -r bin/SEVETCO-debug.apk`.

## Estado del proyecto

Ver el [plan de trabajo](docs/PLAN_SEVETCO.md) para fases, tiempos y entregables.

