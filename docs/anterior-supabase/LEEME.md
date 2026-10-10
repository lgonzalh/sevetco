# [VERSION ANTERIOR] Base de datos SEVETCO en Supabase

Esta carpeta se conserva solo como referencia. La cuenta de Supabase ya tiene sus
dos proyectos ocupados, por lo que el proyecto migro a **Firebase**.

La version vigente esta en:

* `firebase/firestore.rules` (seguridad)
* `docs/FIREBASE.md` (estructura de las colecciones e instrucciones)

## Archivos

| Archivo | Que hace |
| --- | --- |
| `sql/01_schema.sql` | Crea las 12 tablas del modelo entidad-relacion |
| `sql/02_rls.sql` | Activa la seguridad por usuario (quien ve y quien edita que) |
| `sql/03_seed.sql` | Carga los 5 servicios y 3 planes de ejemplo |

## Como ejecutarlos en Supabase

1. Entrar a https://supabase.com y abrir el proyecto.
2. Ir a **SQL Editor > New query**.
3. Pegar el contenido de `01_schema.sql`, ejecutar.
4. Repetir con `02_rls.sql` y luego con `03_seed.sql`.

El orden importa: cada script depende del anterior.

## Tablas creadas

| Tabla | Contenido |
| --- | --- |
| `usuario` | Propietarios, proveedores y administradores |
| `proveedor` | Perfil profesional del prestador |
| `mascota` | Mascotas registradas |
| `plan` | Planes comerciales |
| `servicio` | Catalogo de servicios |
| `plan_servicio` | Servicios incluidos en cada plan |
| `suscripcion` | Planes adquiridos por los propietarios |
| `cita` | Solicitud, programacion y asignacion del servicio |
| `atencion` | Diagnostico, tratamiento y recomendaciones |
| `pago` | Pagos de citas o de planes |
| `trazabilidad_cita` | Historial de estados de cada cita |
| `ruta_desplazamiento` | Rutas y desplazamientos (ubicacion) |

## Registro de usuarios

Cada persona que entra con Google crea automaticamente su fila en `usuario` con
tipo `PROPIETARIO`. Para habilitar el panel de administracion hay que cambiar el
tipo de un usuario a `ADMIN` desde el SQL Editor:

```sql
update usuario set tipo_usuario = 'ADMIN' where correo = 'tucorreo@ejemplo.com';
```

Para habilitar a un proveedor:

```sql
-- 1) el usuario debe existir (ya entro con Google al menos una vez)
update usuario set tipo_usuario = 'PROVEEDOR' where correo = 'proveedor@ejemplo.com';

-- 2) crear su perfil profesional
insert into proveedor (id_usuario, tarjeta_profesional, especialidad)
select id_usuario, 'TP-12345', 'Medicina veterinaria'
from usuario where correo = 'proveedor@ejemplo.com';
```

## Campos que no estaban en el ER

Tres campos se agregaron porque el sistema no puede operar sin ellos. Conviene
mencionarlo al entregar:

| Campo | Por que |
| --- | --- |
| `usuario.nombre` | Las pantallas muestran el nombre de la persona (citas, responsable, trazabilidad) |
| `mascota.fecha_nacimiento` | RF-03 pide la edad de la mascota |
| `suscripcion.fecha_fin` | Regla de integridad del propio ER: "la fecha de finalizacion debe ser posterior a la fecha de inicio" |

Todo lo demas corresponde exactamente a las entidades y campos del modelo
entidad-relacion de la Entrega Previa 2.

