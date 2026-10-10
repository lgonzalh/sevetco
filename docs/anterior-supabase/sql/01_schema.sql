-- ============================================================================
-- SEVETCO - Plataforma Integral de Servicios Veterinarios a Domicilio
-- 01. Esquema de base de datos (PostgreSQL / Supabase)
--
-- Basado en el modelo entidad-relacion de la Entrega Previa 2 (Semana 5).
-- Tablas: usuario, proveedor, mascota, plan, servicio, plan_servicio,
--         suscripcion, cita, atencion, pago, trazabilidad_cita,
--         ruta_desplazamiento
--
-- ATENCION: este script borra y vuelve a crear las tablas. Ejecutarlo en un
-- proyecto de Supabase vacio o de pruebas.
-- ============================================================================

create extension if not exists "pgcrypto";

drop table if exists ruta_desplazamiento cascade;
drop table if exists trazabilidad_cita   cascade;
drop table if exists pago                cascade;
drop table if exists atencion            cascade;
drop table if exists cita                cascade;
drop table if exists suscripcion         cascade;
drop table if exists plan_servicio       cascade;
drop table if exists servicio            cascade;
drop table if exists plan                cascade;
drop table if exists mascota             cascade;
drop table if exists proveedor           cascade;
drop table if exists usuario             cascade;

-- ----------------------------------------------------------------------------
-- USUARIO
-- Propietarios, proveedores y administradores.
-- El id es el mismo de auth.users, porque el acceso es con Google.
-- ----------------------------------------------------------------------------
create table usuario (
  id_usuario        uuid primary key references auth.users (id) on delete cascade,
  numero_documento  text unique,
  correo            text not null unique,
  tipo_usuario      text not null
                    check (tipo_usuario in ('PROPIETARIO', 'PROVEEDOR', 'ADMIN')),
  estado            text not null default 'ACTIVO'
                    check (estado in ('ACTIVO', 'INACTIVO')),
  -- Campo adicional requerido por la interfaz (ver db/README.md).
  nombre            text,
  creado_en         timestamptz not null default now()
);

comment on table usuario is
  'Usuarios de la plataforma. id_usuario corresponde a auth.users.id (login con Google).';

-- ----------------------------------------------------------------------------
-- PROVEEDOR
-- Perfil profesional del prestador. Un usuario PROVEEDOR tiene un solo perfil.
-- ----------------------------------------------------------------------------
create table proveedor (
  id_proveedor        uuid primary key default gen_random_uuid(),
  id_usuario          uuid not null unique references usuario (id_usuario) on delete cascade,
  tarjeta_profesional text,
  especialidad        text
);

-- ----------------------------------------------------------------------------
-- MASCOTA
-- ----------------------------------------------------------------------------
create table mascota (
  id_mascota       uuid primary key default gen_random_uuid(),
  id_propietario   uuid not null references usuario (id_usuario) on delete cascade,
  nombre           text not null,
  especie          text,
  raza             text,
  -- Campo adicional requerido por RF-03 (edad de la mascota).
  fecha_nacimiento date,
  creado_en        timestamptz not null default now()
);

create index idx_mascota_propietario on mascota (id_propietario);

-- ----------------------------------------------------------------------------
-- PLAN
-- ----------------------------------------------------------------------------
create table plan (
  id_plan       uuid primary key default gen_random_uuid(),
  nombre        text not null unique,
  precio        numeric(12,2) not null default 0 check (precio >= 0),
  vigencia_dias integer not null default 30 check (vigencia_dias > 0),
  estado        text not null default 'ACTIVO'
                check (estado in ('ACTIVO', 'INACTIVO'))
);

-- ----------------------------------------------------------------------------
-- SERVICIO
-- Catalogo de servicios ofrecidos a domicilio.
-- Nota: en el ER el campo aparece como "duracion_minutes"; aqui se usa
--       "duracion_minutos" (mismo dato).
-- ----------------------------------------------------------------------------
create table servicio (
  id_servicio      uuid primary key default gen_random_uuid(),
  nombre           text not null unique,
  precio_base      numeric(12,2) not null default 0 check (precio_base >= 0),
  duracion_minutos integer not null default 60 check (duracion_minutos > 0)
);

-- ----------------------------------------------------------------------------
-- PLAN_SERVICIO
-- Resuelve la relacion muchos a muchos entre plan y servicio.
-- ----------------------------------------------------------------------------
create table plan_servicio (
  id_plan           uuid not null references plan (id_plan) on delete cascade,
  id_servicio       uuid not null references servicio (id_servicio) on delete cascade,
  cantidad_incluida integer not null default 1 check (cantidad_incluida > 0),
  primary key (id_plan, id_servicio)
);

-- ----------------------------------------------------------------------------
-- SUSCRIPCION
-- Planes adquiridos por los propietarios.
-- ----------------------------------------------------------------------------
create table suscripcion (
  id_suscripcion uuid primary key default gen_random_uuid(),
  id_propietario uuid not null references usuario (id_usuario) on delete cascade,
  id_plan        uuid not null references plan (id_plan),
  fecha_inicio   date not null default current_date,
  -- Campo requerido por las reglas de integridad del ER
  -- ("la fecha de finalizacion debe ser posterior a la fecha de inicio").
  fecha_fin      date,
  estado         text not null default 'ACTIVA'
                 check (estado in ('ACTIVA', 'VENCIDA', 'CANCELADA')),
  constraint chk_suscripcion_fechas check (fecha_fin is null or fecha_fin > fecha_inicio)
);

create index idx_suscripcion_propietario on suscripcion (id_propietario);

-- ----------------------------------------------------------------------------
-- CITA
-- Solicitud, programacion y asignacion del servicio.
-- El proveedor y la suscripcion pueden asignarse despues: son opcionales.
-- ----------------------------------------------------------------------------
create table cita (
  id_cita        uuid primary key default gen_random_uuid(),
  id_propietario uuid not null references usuario (id_usuario),
  id_mascota     uuid not null references mascota (id_mascota),
  id_servicio    uuid not null references servicio (id_servicio),
  id_proveedor   uuid references proveedor (id_proveedor),
  id_suscripcion uuid references suscripcion (id_suscripcion),
  fecha_hora     timestamptz not null,
  estado         text not null default 'PENDIENTE'
                 check (estado in ('PENDIENTE', 'ASIGNADA', 'EN_PROCESO', 'FINALIZADA', 'CANCELADA')),
  creado_en      timestamptz not null default now()
);

create index idx_cita_propietario on cita (id_propietario);
create index idx_cita_proveedor   on cita (id_proveedor);
create index idx_cita_mascota     on cita (id_mascota);
create index idx_cita_fecha       on cita (fecha_hora);

-- ----------------------------------------------------------------------------
-- ATENCION
-- Diagnostico, tratamiento y recomendaciones. Una cita finalizada genera
-- como maximo una atencion.
-- ----------------------------------------------------------------------------
create table atencion (
  id_atencion     uuid primary key default gen_random_uuid(),
  id_cita         uuid not null unique references cita (id_cita) on delete cascade,
  diagnostico     text,
  tratamiento     text,
  recomendaciones text,
  fecha_registro  timestamptz not null default now()
);

-- ----------------------------------------------------------------------------
-- PAGO
-- Se asocia con una cita individual o con una suscripcion (al menos una).
-- ----------------------------------------------------------------------------
create table pago (
  id_pago            uuid primary key default gen_random_uuid(),
  id_propietario     uuid not null references usuario (id_usuario),
  id_cita            uuid references cita (id_cita),
  id_suscripcion     uuid references suscripcion (id_suscripcion),
  monto              numeric(12,2) not null check (monto >= 0),
  estado             text not null default 'PENDIENTE'
                     check (estado in ('PENDIENTE', 'PAGADO', 'ANULADO')),
  referencia_externa text unique,
  fecha_pago         timestamptz,
  constraint chk_pago_concepto check (id_cita is not null or id_suscripcion is not null)
);

create index idx_pago_propietario on pago (id_propietario);

-- ----------------------------------------------------------------------------
-- TRAZABILIDAD_CITA
-- Historial de estados y novedades de cada cita.
-- ----------------------------------------------------------------------------
create table trazabilidad_cita (
  id_evento             uuid primary key default gen_random_uuid(),
  id_cita               uuid not null references cita (id_cita) on delete cascade,
  id_usuario_responsable uuid references usuario (id_usuario),
  estado_nuevo          text not null,
  fecha_evento          timestamptz not null default now()
);

create index idx_trazabilidad_cita on trazabilidad_cita (id_cita, fecha_evento);

-- ----------------------------------------------------------------------------
-- RUTA_DESPLAZAMIENTO
-- Informacion de rutas y desplazamientos (RF-17). Una cita tiene como maximo
-- un registro. Origen y destino guardan la direccion y/o la coordenada
-- capturada por la ubicacion del dispositivo ("latitud, longitud").
-- ----------------------------------------------------------------------------
create table ruta_desplazamiento (
  id_ruta      uuid primary key default gen_random_uuid(),
  id_cita      uuid not null unique references cita (id_cita) on delete cascade,
  origen       text,
  destino      text,
  distancia_km numeric(8,2) check (distancia_km is null or distancia_km >= 0),
  estado       text not null default 'PENDIENTE'
               check (estado in ('PENDIENTE', 'EN_RUTA', 'COMPLETADA'))
);

-- ============================================================================
-- FUNCIONES Y DISPARADORES
-- ============================================================================

-- Devuelve el tipo del usuario conectado ('PROPIETARIO', 'PROVEEDOR', 'ADMIN').
create or replace function public.mi_tipo_usuario()
returns text
language sql
stable
security definer
set search_path = public
as $$
  select tipo_usuario from public.usuario where id_usuario = auth.uid();
$$;

-- Devuelve el id del perfil de proveedor del usuario conectado.
create or replace function public.mi_id_proveedor()
returns uuid
language sql
stable
security definer
set search_path = public
as $$
  select id_proveedor from public.proveedor where id_usuario = auth.uid();
$$;

-- Crea la fila en "usuario" cuando alguien se registra (por ejemplo con Google).
create or replace function public.fn_nuevo_usuario()
returns trigger
language plpgsql
security definer
set search_path = public
as $$
begin
  insert into public.usuario (id_usuario, correo, nombre, tipo_usuario, estado)
  values (
    new.id,
    coalesce(new.email, ''),
    coalesce(new.raw_user_meta_data ->> 'full_name', new.raw_user_meta_data ->> 'name'),
    coalesce(new.raw_user_meta_data ->> 'tipo_usuario', 'PROPIETARIO'),
    'ACTIVO'
  )
  on conflict do nothing;
  return new;
end;
$$;

drop trigger if exists trg_nuevo_usuario on auth.users;
create trigger trg_nuevo_usuario
  after insert on auth.users
  for each row execute function public.fn_nuevo_usuario();

-- Regla: la fecha programada de una cita no puede ser anterior a su creacion.
create or replace function public.fn_validar_fecha_cita()
returns trigger
language plpgsql
as $$
begin
  if new.fecha_hora < new.creado_en then
    raise exception 'La fecha programada de la cita no puede ser anterior a su creacion.';
  end if;
  return new;
end;
$$;

drop trigger if exists trg_validar_fecha_cita on cita;
create trigger trg_validar_fecha_cita
  before insert on cita
  for each row execute function public.fn_validar_fecha_cita();

-- Regla: todo cambio de estado de una cita queda registrado en la trazabilidad.
create or replace function public.fn_trazabilidad_cita()
returns trigger
language plpgsql
security definer
set search_path = public
as $$
begin
  if tg_op = 'INSERT' then
    insert into public.trazabilidad_cita (id_cita, id_usuario_responsable, estado_nuevo)
    values (new.id_cita, auth.uid(), new.estado);
  elsif new.estado is distinct from old.estado then
    insert into public.trazabilidad_cita (id_cita, id_usuario_responsable, estado_nuevo)
    values (new.id_cita, auth.uid(), new.estado);
  end if;
  return new;
end;
$$;

drop trigger if exists trg_trazabilidad_cita on cita;
create trigger trg_trazabilidad_cita
  after insert or update on cita
  for each row execute function public.fn_trazabilidad_cita();

-- Regla: solo se asigna a una cita un proveedor cuyo usuario este ACTIVO.
create or replace function public.fn_validar_proveedor_cita()
returns trigger
language plpgsql
as $$
begin
  if new.id_proveedor is not null then
    if not exists (
      select 1
      from public.proveedor p
      join public.usuario u on u.id_usuario = p.id_usuario
      where p.id_proveedor = new.id_proveedor
        and u.estado = 'ACTIVO'
    ) then
      raise exception 'El proveedor asignado no esta activo.';
    end if;
  end if;
  return new;
end;
$$;

drop trigger if exists trg_validar_proveedor_cita on cita;
create trigger trg_validar_proveedor_cita
  before insert or update on cita
  for each row execute function public.fn_validar_proveedor_cita();

