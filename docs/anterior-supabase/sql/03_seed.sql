-- ============================================================================
-- SEVETCO - 03. Datos iniciales (catalogos)
--
-- Ejecutar DESPUES de 01_schema.sql y 02_rls.sql.
--
-- Contiene los cinco servicios descritos en los documentos del proyecto y
-- planes de ejemplo para que la plataforma tenga informacion desde el inicio.
-- Los precios son datos de ejemplo (pesos colombianos).
-- ============================================================================

-- ----------------------------------------------------------------------------
-- SERVICIOS  (los cinco del alcance del proyecto)
-- ----------------------------------------------------------------------------
insert into servicio (nombre, precio_base, duracion_minutos) values
  ('Consulta veterinaria a domicilio', 120000, 60),
  ('Vacunacion a domicilio',            80000, 30),
  ('Estetica canina y felina',         100000, 90),
  ('Entrenamiento basico',             150000, 60),
  ('Servicio funerario',               350000, 120)
on conflict (nombre) do update
  set precio_base      = excluded.precio_base,
      duracion_minutos = excluded.duracion_minutos;

-- ----------------------------------------------------------------------------
-- PLANES
-- ----------------------------------------------------------------------------
insert into plan (nombre, precio, vigencia_dias, estado) values
  ('Plan Basico',   250000, 30, 'ACTIVO'),
  ('Plan Integral', 480000, 30, 'ACTIVO'),
  ('Plan Premium',  780000, 30, 'ACTIVO')
on conflict (nombre) do update
  set precio        = excluded.precio,
      vigencia_dias = excluded.vigencia_dias,
      estado        = excluded.estado;

-- ----------------------------------------------------------------------------
-- PLAN_SERVICIO  (servicios incluidos en cada plan)
-- ----------------------------------------------------------------------------
insert into plan_servicio (id_plan, id_servicio, cantidad_incluida)
select p.id_plan, s.id_servicio, v.cantidad
from (values
  ('Plan Basico',   'Consulta veterinaria a domicilio', 1),
  ('Plan Basico',   'Vacunacion a domicilio',           1),
  ('Plan Integral', 'Consulta veterinaria a domicilio', 2),
  ('Plan Integral', 'Vacunacion a domicilio',           2),
  ('Plan Integral', 'Estetica canina y felina',         1),
  ('Plan Premium',  'Consulta veterinaria a domicilio', 4),
  ('Plan Premium',  'Vacunacion a domicilio',           4),
  ('Plan Premium',  'Estetica canina y felina',         2),
  ('Plan Premium',  'Entrenamiento basico',             2)
) as v (plan, servicio, cantidad)
join plan p     on p.nombre = v.plan
join servicio s on s.nombre = v.servicio
on conflict (id_plan, id_servicio) do update
  set cantidad_incluida = excluded.cantidad_incluida;

