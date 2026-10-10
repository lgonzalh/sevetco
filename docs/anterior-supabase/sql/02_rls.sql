-- ============================================================================
-- SEVETCO - 02. Politicas de seguridad (Row Level Security)
--
-- Ejecutar DESPUES de 01_schema.sql.
-- Requiere que 01_schema.sql ya haya creado las funciones
-- public.mi_tipo_usuario() y public.mi_id_proveedor().
--
-- Reglas de negocio aplicadas (tomadas del ER de la Entrega 2):
--   * Solo los usuarios con tipo PROPIETARIO son duenos de mascotas,
--     compradores de planes y solicitantes de citas.
--   * Solo los proveedores ACTIVOS pueden ser asignados a una cita.
--   * El administrador gestiona proveedores, servicios, planes y usuarios.
-- ============================================================================

alter table usuario               enable row level security;
alter table proveedor             enable row level security;
alter table mascota               enable row level security;
alter table plan                  enable row level security;
alter table servicio              enable row level security;
alter table plan_servicio         enable row level security;
alter table suscripcion           enable row level security;
alter table cita                  enable row level security;
alter table atencion              enable row level security;
alter table pago                  enable row level security;
alter table trazabilidad_cita     enable row level security;
alter table ruta_desplazamiento   enable row level security;

-- ----------------------------------------------------------------------------
-- Protege el tipo de usuario: nadie puede cambiarse a ADMIN por su cuenta.
-- ----------------------------------------------------------------------------
create or replace function public.fn_proteger_tipo_usuario()
returns trigger
language plpgsql
security definer
set search_path = public
as $$
begin
  if new.tipo_usuario is distinct from old.tipo_usuario
     and public.mi_tipo_usuario() is distinct from 'ADMIN' then
    raise exception 'Solo un administrador puede cambiar el tipo de usuario.';
  end if;
  return new;
end;
$$;

drop trigger if exists trg_proteger_tipo_usuario on usuario;
create trigger trg_proteger_tipo_usuario
  before update on usuario
  for each row execute function public.fn_proteger_tipo_usuario();

-- ============================================================================
-- USUARIO
-- ============================================================================
create policy p_usuario_select on usuario
  for select to authenticated
  using (true);   -- los nombres se muestran en citas, tratamientos y trazabilidad

create policy p_usuario_update_propio on usuario
  for update to authenticated
  using (id_usuario = auth.uid())
  with check (id_usuario = auth.uid());

create policy p_usuario_admin on usuario
  for all to authenticated
  using (public.mi_tipo_usuario() = 'ADMIN')
  with check (public.mi_tipo_usuario() = 'ADMIN');

-- ============================================================================
-- PROVEEDOR
-- ============================================================================
create policy p_proveedor_select on proveedor
  for select to authenticated
  using (true);

create policy p_proveedor_admin on proveedor
  for all to authenticated
  using (public.mi_tipo_usuario() = 'ADMIN')
  with check (public.mi_tipo_usuario() = 'ADMIN');

-- ============================================================================
-- MASCOTA
-- ============================================================================
create policy p_mascota_select on mascota
  for select to authenticated
  using (
    id_propietario = auth.uid()
    or public.mi_tipo_usuario() = 'ADMIN'
    or exists (
      select 1 from cita c
      where c.id_mascota = mascota.id_mascota
        and c.id_proveedor = public.mi_id_proveedor()
    )
  );

create policy p_mascota_insert on mascota
  for insert to authenticated
  with check (
    id_propietario = auth.uid()
    and public.mi_tipo_usuario() = 'PROPIETARIO'
  );

create policy p_mascota_update on mascota
  for update to authenticated
  using (id_propietario = auth.uid() or public.mi_tipo_usuario() = 'ADMIN')
  with check (id_propietario = auth.uid() or public.mi_tipo_usuario() = 'ADMIN');

create policy p_mascota_delete on mascota
  for delete to authenticated
  using (id_propietario = auth.uid() or public.mi_tipo_usuario() = 'ADMIN');

-- ============================================================================
-- PLAN, SERVICIO y PLAN_SERVICIO  (catalogos)
-- ============================================================================
create policy p_plan_select on plan
  for select to authenticated using (true);
create policy p_plan_admin on plan
  for all to authenticated
  using (public.mi_tipo_usuario() = 'ADMIN')
  with check (public.mi_tipo_usuario() = 'ADMIN');

create policy p_servicio_select on servicio
  for select to authenticated using (true);
create policy p_servicio_admin on servicio
  for all to authenticated
  using (public.mi_tipo_usuario() = 'ADMIN')
  with check (public.mi_tipo_usuario() = 'ADMIN');

create policy p_plan_servicio_select on plan_servicio
  for select to authenticated using (true);
create policy p_plan_servicio_admin on plan_servicio
  for all to authenticated
  using (public.mi_tipo_usuario() = 'ADMIN')
  with check (public.mi_tipo_usuario() = 'ADMIN');

-- ============================================================================
-- SUSCRIPCION
-- ============================================================================
create policy p_suscripcion_select on suscripcion
  for select to authenticated
  using (id_propietario = auth.uid() or public.mi_tipo_usuario() = 'ADMIN');

create policy p_suscripcion_insert on suscripcion
  for insert to authenticated
  with check (
    (id_propietario = auth.uid() and public.mi_tipo_usuario() = 'PROPIETARIO')
    or public.mi_tipo_usuario() = 'ADMIN'
  );

create policy p_suscripcion_update on suscripcion
  for update to authenticated
  using (public.mi_tipo_usuario() = 'ADMIN')
  with check (public.mi_tipo_usuario() = 'ADMIN');

create policy p_suscripcion_delete on suscripcion
  for delete to authenticated
  using (public.mi_tipo_usuario() = 'ADMIN');

-- ============================================================================
-- CITA
-- ============================================================================
create policy p_cita_select on cita
  for select to authenticated
  using (
    id_propietario = auth.uid()
    or public.mi_tipo_usuario() = 'ADMIN'
    or id_proveedor = public.mi_id_proveedor()
  );

create policy p_cita_insert on cita
  for insert to authenticated
  with check (
    id_propietario = auth.uid()
    and public.mi_tipo_usuario() = 'PROPIETARIO'
  );

create policy p_cita_update on cita
  for update to authenticated
  using (
    id_propietario = auth.uid()
    or public.mi_tipo_usuario() = 'ADMIN'
    or id_proveedor = public.mi_id_proveedor()
  )
  with check (
    id_propietario = auth.uid()
    or public.mi_tipo_usuario() = 'ADMIN'
    or id_proveedor = public.mi_id_proveedor()
  );

create policy p_cita_delete on cita
  for delete to authenticated
  using (public.mi_tipo_usuario() = 'ADMIN');

-- ============================================================================
-- ATENCION
-- ============================================================================
create policy p_atencion_select on atencion
  for select to authenticated
  using (
    public.mi_tipo_usuario() = 'ADMIN'
    or exists (
      select 1 from cita c
      where c.id_cita = atencion.id_cita
        and (c.id_propietario = auth.uid() or c.id_proveedor = public.mi_id_proveedor())
    )
  );

create policy p_atencion_insert on atencion
  for insert to authenticated
  with check (
    public.mi_tipo_usuario() = 'ADMIN'
    or exists (
      select 1 from cita c
      where c.id_cita = atencion.id_cita
        and c.id_proveedor = public.mi_id_proveedor()
    )
  );

create policy p_atencion_update on atencion
  for update to authenticated
  using (
    public.mi_tipo_usuario() = 'ADMIN'
    or exists (
      select 1 from cita c
      where c.id_cita = atencion.id_cita
        and c.id_proveedor = public.mi_id_proveedor()
    )
  )
  with check (
    public.mi_tipo_usuario() = 'ADMIN'
    or exists (
      select 1 from cita c
      where c.id_cita = atencion.id_cita
        and c.id_proveedor = public.mi_id_proveedor()
    )
  );

-- ============================================================================
-- PAGO
-- ============================================================================
create policy p_pago_select on pago
  for select to authenticated
  using (id_propietario = auth.uid() or public.mi_tipo_usuario() = 'ADMIN');

create policy p_pago_insert on pago
  for insert to authenticated
  with check (id_propietario = auth.uid() or public.mi_tipo_usuario() = 'ADMIN');

create policy p_pago_update on pago
  for update to authenticated
  using (id_propietario = auth.uid() or public.mi_tipo_usuario() = 'ADMIN')
  with check (id_propietario = auth.uid() or public.mi_tipo_usuario() = 'ADMIN');

create policy p_pago_delete on pago
  for delete to authenticated
  using (public.mi_tipo_usuario() = 'ADMIN');

-- ============================================================================
-- TRAZABILIDAD_CITA
-- ============================================================================
create policy p_trazabilidad_select on trazabilidad_cita
  for select to authenticated
  using (
    public.mi_tipo_usuario() = 'ADMIN'
    or exists (
      select 1 from cita c
      where c.id_cita = trazabilidad_cita.id_cita
        and (c.id_propietario = auth.uid() or c.id_proveedor = public.mi_id_proveedor())
    )
  );

create policy p_trazabilidad_insert on trazabilidad_cita
  for insert to authenticated
  with check (
    public.mi_tipo_usuario() = 'ADMIN'
    or exists (
      select 1 from cita c
      where c.id_cita = trazabilidad_cita.id_cita
        and (c.id_propietario = auth.uid() or c.id_proveedor = public.mi_id_proveedor())
    )
  );

-- ============================================================================
-- RUTA_DESPLAZAMIENTO
-- ============================================================================
create policy p_ruta_select on ruta_desplazamiento
  for select to authenticated
  using (
    public.mi_tipo_usuario() = 'ADMIN'
    or exists (
      select 1 from cita c
      where c.id_cita = ruta_desplazamiento.id_cita
        and (c.id_propietario = auth.uid() or c.id_proveedor = public.mi_id_proveedor())
    )
  );

create policy p_ruta_insert on ruta_desplazamiento
  for insert to authenticated
  with check (
    public.mi_tipo_usuario() = 'ADMIN'
    or exists (
      select 1 from cita c
      where c.id_cita = ruta_desplazamiento.id_cita
        and c.id_proveedor = public.mi_id_proveedor()
    )
  );

create policy p_ruta_update on ruta_desplazamiento
  for update to authenticated
  using (
    public.mi_tipo_usuario() = 'ADMIN'
    or exists (
      select 1 from cita c
      where c.id_cita = ruta_desplazamiento.id_cita
        and c.id_proveedor = public.mi_id_proveedor()
    )
  )
  with check (
    public.mi_tipo_usuario() = 'ADMIN'
    or exists (
      select 1 from cita c
      where c.id_cita = ruta_desplazamiento.id_cita
        and c.id_proveedor = public.mi_id_proveedor()
    )
  );

