// ---------------------------------------------------------------------------
// Vistas del panel. Cada vista corresponde a requerimientos del proyecto.
//   RF-09 / RF-10  citas y responsable asignado
//   RF-11          registro del tratamiento (atencion)
//   RF-12          historial de tratamientos
//   RF-13          gestion de proveedores
//   RF-14          gestion del portafolio de servicios y planes
//   RF-15          trazabilidad del servicio
//   RF-16          gestion de pagos
//   RF-17          rutas y desplazamientos (ubicacion)
//
// Datos: Cloud Firestore. Cada coleccion conserva el nombre de la entidad del
// modelo entidad-relacion del proyecto.
// ---------------------------------------------------------------------------

import {
  leerColeccion, leerDocumento, crear, crearConId, actualizar, eliminar,
  aTimestamp, aFecha, leerDonde, crearCuenta, cambiarClave
} from './datos.js';
import { recuperarClave } from './datos.js';

// Colecciones que guardan datos de un propietario. En Firestore no existe el
// borrado en cascada, asi que se eliminan una por una antes de borrar el perfil.
const COLECCIONES_DEL_PROPIETARIO = [
  'mascota', 'cita', 'suscripcion', 'pago', 'atencion',
  'ruta_desplazamiento', 'trazabilidad_cita', 'agenda'
];

/** Junta todos los documentos que dependen de un usuario. */
async function datosDelUsuario(uid) {
  const encontrados = [];
  for (const nombre of COLECCIONES_DEL_PROPIETARIO) {
    const documentos = await leerColeccion(nombre);
    documentos
      .filter(documento => documento.id_propietario === uid)
      .forEach(documento => encontrados.push({ coleccion: nombre, id: documento.id }));
  }
  return encontrados;
}
import {
  $, esc, fecha, fechaHora, moneda, chip, aviso, icono, avatar, fotoMascota,
  abrirModal, cerrarModal, campo, leerFormulario
} from './ui.js';
import { MAPS_KEY } from './firebase-config.js';

let promesaGoogle = null;

/**
 * Carga Google Maps solo cuando hace falta. Si por cualquier motivo no se
 * puede cargar, el mapa se dibuja con OpenStreetMap: la ruta siempre se ve.
 */
function cargarGoogleMaps() {
  if (window.google?.maps?.Map) return Promise.resolve();
  if (promesaGoogle) return promesaGoogle;
  promesaGoogle = new Promise(resolve => {
    // Con loading=async Google pide un callback: se avisa cuando la libreria
    // esta lista de verdad, y asi no sale el aviso en la consola.
    window.__sevetcoMapaListo = () => resolve();
    const guion = document.createElement('script');
    guion.src = 'https://maps.googleapis.com/maps/api/js?key=' + MAPS_KEY
      + '&language=es&region=CO&loading=async&callback=__sevetcoMapaListo';
    guion.async = true;
    guion.onerror = () => resolve();
    document.head.appendChild(guion);
    setTimeout(resolve, 9000);
  });
  return promesaGoogle;
}

// ---------------------------------------------------------------------------
// Menu
// ---------------------------------------------------------------------------

const MENU_ADMIN = [
  { id: 'panel', titulo: 'Panel', icono: 'dashboard', carpeta: 'web' },
  { id: 'eventos', titulo: 'Eventos', icono: 'event', carpeta: 'shared' },
  { id: 'promociones', titulo: 'Promociones', icono: 'price_check', carpeta: 'shared' },
  { id: 'citas', titulo: 'Citas', icono: 'event', carpeta: 'shared' },
  { id: 'proveedores', titulo: 'Proveedores', icono: 'badge', carpeta: 'shared' },
  { id: 'mascotas', titulo: 'Mascotas', icono: 'pets', carpeta: 'shared' },
  { id: 'servicios', titulo: 'Servicios', icono: 'medical_services', carpeta: 'shared' },
  { id: 'planes', titulo: 'Planes', icono: 'storefront', carpeta: 'shared' },
  { id: 'suscripciones', titulo: 'Suscripciones', icono: 'card_membership', carpeta: 'shared' },
  { id: 'atenciones', titulo: 'Tratamientos', icono: 'list_alt', carpeta: 'shared' },
  { id: 'trazabilidad', titulo: 'Trazabilidad', icono: 'insights', carpeta: 'web' },
  { id: 'pagos', titulo: 'Pagos', icono: 'payments', carpeta: 'shared' },
  { id: 'rutas', titulo: 'Rutas', icono: 'location_on', carpeta: 'shared' },
  { id: 'usuarios', titulo: 'Usuarios', icono: 'person', carpeta: 'shared' }
];

const MENU_PROVEEDOR = [
  { id: 'mis-citas', titulo: 'Mis citas', icono: 'event', carpeta: 'shared' },
  { id: 'atenciones', titulo: 'Tratamientos', icono: 'medical_services', carpeta: 'shared' },
  { id: 'rutas', titulo: 'Rutas', icono: 'location_on', carpeta: 'shared' },
  { id: 'mi-cuenta', titulo: 'Mi cuenta', icono: 'account_circle', carpeta: 'shared' }
];

export function menuPara(tipo) {
  return tipo === 'ADMIN' ? MENU_ADMIN : MENU_PROVEEDOR;
}

export function tituloVista(id) {
  const todas = [...MENU_ADMIN, ...MENU_PROVEEDOR];
  return todas.find(m => m.id === id)?.titulo ?? 'Panel';
}

// ---------------------------------------------------------------------------
// Utilidades
// ---------------------------------------------------------------------------

function pintar(html) {
  $('#vista').innerHTML = html;
}

// ---------------------------------------------------------------------------
// Mapa interactivo (OpenStreetMap) para elegir la ubicacion del servicio
// ---------------------------------------------------------------------------

// Horario de atencion (debe coincidir con Horario.kt de la app movil).
const HORARIO_INICIO = 8;
const HORARIO_FIN = 18;
const HorarioMinutos = 60;

function leerCoordenadas(texto) {
  const coincidencia = String(texto ?? '').match(/(-?\d+[.,]\d+)\s*,\s*(-?\d+[.,]\d+)/);
  if (!coincidencia) return null;
  return [
    Number(coincidencia[1].replace(',', '.')),
    Number(coincidencia[2].replace(',', '.'))
  ];
}

/** Convierte coordenadas en una direccion legible (calle, barrio, ciudad). */
async function direccionDe(lat, lng) {
  try {
    const url = 'https://nominatim.openstreetmap.org/reverse'
      + `?format=jsonv2&zoom=18&addressdetails=1&accept-language=es&lat=${lat}&lon=${lng}`;
    const respuesta = await fetch(url, { headers: { Accept: 'application/json' } });
    if (!respuesta.ok) return null;
    const datos = await respuesta.json();
    const d = datos.address ?? {};
    const partes = [d.road, d.house_number, d.neighbourhood ?? d.suburb,
      d.city ?? d.town ?? d.municipality].filter(Boolean);
    return partes.length ? partes.join(', ') : (datos.display_name ?? null);
  } catch (error) {
    return null;
  }
}

/** Mensaje claro segun el motivo por el que fallo la ubicacion. */
function mensajeUbicacion(error) {
  if (error?.code === 1) return 'El navegador tiene bloqueada la ubicacion para este sitio.';
  if (error?.code === 3) return 'Se agoto el tiempo para obtener la ubicacion.';
  return 'No se pudo obtener la ubicacion.';
}

function mapaPor(lista, llave = 'id') {
  return new Map(lista.map(elemento => [elemento[llave], elemento]));
}

/** Fecha en el formato que espera un input datetime-local. */
function aInputFecha(valor) {
  const fecha = aFecha(valor);
  if (!fecha) return '';
  const dos = numero => String(numero).padStart(2, '0');
  return `${fecha.getFullYear()}-${dos(fecha.getMonth() + 1)}-${dos(fecha.getDate())}`
    + `T${dos(fecha.getHours())}:${dos(fecha.getMinutes())}`;
}

/** Edad en anios y meses a partir de una fecha. */
function edadDe(fechaNacimiento) {
  const nacimiento = aFecha(fechaNacimiento);
  if (!nacimiento) return '-';
  const hoy = new Date();
  let meses = (hoy.getFullYear() - nacimiento.getFullYear()) * 12
    + (hoy.getMonth() - nacimiento.getMonth());
  if (hoy.getDate() < nacimiento.getDate()) meses -= 1;
  if (meses < 0) meses = 0;
  const anios = Math.floor(meses / 12);
  const resto = meses % 12;
  if (anios === 0 && resto === 0) return 'Menos de 1 mes';
  if (anios === 0) return `${resto} ${resto === 1 ? 'mes' : 'meses'}`;
  const textoAnios = `${anios} ${anios === 1 ? 'año' : 'años'}`;
  return resto === 0 ? textoAnios : `${textoAnios} y ${resto} ${resto === 1 ? 'mes' : 'meses'}`;
}

// ---------------------------------------------------------------------------
// Contenido de las cards (eventos y promociones)
// ---------------------------------------------------------------------------

let imagenEnEdicion = '';

// Punto elegido en el mapa de la ruta (destino) y ubicacion del veterinario
// (origen). Van a nivel del modulo porque los usan el formulario de rutas, el
// mapa y el guardado de la ruta.
let puntoRuta = null;
let puntoOrigen = null;

/** Reduce la imagen elegida y la devuelve como texto (data URL). */
function comprimirImagen(archivo, anchoMax = 900, calidad = 0.75) {
  return new Promise((resolve, reject) => {
    const lector = new FileReader();
    lector.onload = () => {
      const imagen = new Image();
      imagen.onload = () => {
        const escala = Math.min(1, anchoMax / imagen.width);
        const lienzo = document.createElement('canvas');
        lienzo.width = Math.round(imagen.width * escala);
        lienzo.height = Math.round(imagen.height * escala);
        lienzo.getContext('2d').drawImage(imagen, 0, 0, lienzo.width, lienzo.height);
        resolve(lienzo.toDataURL('image/jpeg', calidad));
      };
      imagen.onerror = () => reject(new Error('No se pudo leer la imagen.'));
      imagen.src = lector.result;
    };
    lector.onerror = () => reject(new Error('No se pudo leer el archivo.'));
    lector.readAsDataURL(archivo);
  });
}

function bloqueImagen(
  nombre,
  etiqueta = 'Imagen de la card',
  ayuda = 'Si subes un archivo se guarda comprimido dentro de la card.',
  ancho = 150,
  alto = 100
) {
  const esUrl = imagenEnEdicion && !imagenEnEdicion.startsWith('data:');
  const forma = Math.abs(ancho - alto) <= 8 ? '50%' : '12px';
  return `<div class="col-12">
    <label class="form-label small fw-semibold">${etiqueta}</label>
    <div class="d-flex gap-3">
      <img id="vista-imagen" src="${imagenEnEdicion}" alt=""
        style="width:${ancho}px;height:${alto}px;object-fit:cover;border-radius:${forma};background:#eaeef7">
      <div class="flex-grow-1">
        <input class="form-control form-control-sm mb-2" type="file"
          accept="image/*" id="archivo-imagen">
        <input class="form-control form-control-sm" type="text" name="${nombre}"
          id="url-imagen" value="${esUrl ? esc(imagenEnEdicion) : ''}"
          placeholder="...o pega aqui una URL de imagen">
        <div class="form-text">${ayuda}</div>
      </div>
    </div>
  </div>`;
}

/** Conecta el selector de archivo y la URL con la vista previa. */
function conectarImagen(nombreCampo) {
  const archivo = document.querySelector('#modal-cuerpo #archivo-imagen');
  const url = document.querySelector('#modal-cuerpo #url-imagen');
  const vista = document.querySelector('#modal-cuerpo #vista-imagen');

  archivo?.addEventListener('change', async () => {
    const elegido = archivo.files?.[0];
    if (!elegido) return;
    try {
      imagenEnEdicion = await comprimirImagen(elegido);
      if (url) url.value = '';
      if (vista) vista.src = imagenEnEdicion;
      aviso('Imagen lista.');
    } catch (error) {
      aviso(error.message, true);
    }
  });

  url?.addEventListener('input', () => {
    imagenEnEdicion = url.value.trim();
    if (vista) vista.src = imagenEnEdicion;
  });
}

/** Card igual a la que muestra la app movil. */
function tarjetaContenido(fila) {
  const horario = [fila.horaInicio, fila.horaFin].filter(Boolean).join(' - ');
  const cuando = [fila.fecha, horario].filter(Boolean).join('   ');
  const fondo = fila.imagen
    ? `style="background-image:url('${esc(fila.imagen)}')"`
    : 'style="background:#1f3864"';

  return `<div class="card-contenido" ${fondo}>
    <div class="card-contenido__velo"></div>
    <div class="card-contenido__texto">
      <h5>${esc(fila.nombre)}</h5>
      ${cuando ? `<div class="card-contenido__cuando">${esc(cuando)}</div>` : ''}
      ${fila.texto ? `<p>${esc(fila.texto)}</p>` : ''}
      ${fila.descuento ? `<span class="card-contenido__descuento">${esc(fila.descuento)}% de descuento</span>` : ''}
    </div>
  </div>`;
}

function botonAccion(accion, id, iconoNombre = 'edit', titulo = 'Editar') {
  return `<button class="btn btn-sm btn-outline-secondary" data-accion="${accion}" data-id="${esc(id)}"
    title="${esc(titulo)}"><img src="${icono(iconoNombre)}" alt="" class="icono-16"></button>`;
}

/** Boton para enviar el correo de restablecimiento de contrasena. */
function botonClave(correo) {
  return `<button class="btn btn-sm btn-outline-secondary" data-accion="clave"
    data-correo="${esc(correo)}" title="Enviar correo para restablecer la contrasena">
    <img src="${icono('key')}" alt="" class="icono-16"></button>`;
}

function tabla(cabeceras, filas) {
  if (!filas.length) {
    return '<p class="text-secondary text-center py-4 mb-0">No hay registros para mostrar.</p>';
  }
  return `<div class="table-responsive"><table class="table table-hover align-middle mb-0">
    <thead><tr>${cabeceras.map(c => `<th>${esc(c)}</th>`).join('')}</tr></thead>
    <tbody>${filas.join('')}</tbody>
  </table></div>`;
}

function nombreDeUsuario(usuarios, id) {
  const usuario = usuarios.get(id);
  return usuario?.nombre || usuario?.correo || '-';
}

/** Avatar + nombre de una persona, para las tablas. */
function celdaPersona(usuarios, id) {
  const usuario = usuarios.get(id);
  const nombre = usuario?.nombre || usuario?.correo || '-';
  return `<div class="celda-usuario">${avatar(usuario?.foto, nombre)}<span>${esc(nombre)}</span></div>`;
}

/** Foto + nombre de la mascota, para las tablas. */
function celdaMascota(mascotas, id) {
  const ficha = mascotas.get(id);
  const nombre = ficha?.nombre ?? '-';
  return `<div class="celda-usuario">${fotoMascota(ficha?.foto, nombre)}<span>${esc(nombre)}</span></div>`;
}

async function registrarTrazabilidad(cita, estadoNuevo, responsableId) {
  await crear('trazabilidad_cita', {
    id_cita: cita.id,
    id_propietario: cita.id_propietario ?? null,
    id_proveedor: cita.id_proveedor ?? null,
    id_usuario_responsable: responsableId,
    estado_nuevo: estadoNuevo,
    fecha_evento: aTimestamp(new Date())
  });
}

// ---------------------------------------------------------------------------
// PANEL
// ---------------------------------------------------------------------------

async function vistaPanel() {
  const [citas, proveedores, servicios, planes] = await Promise.all([
    leerColeccion('cita'), leerColeccion('proveedor'),
    leerColeccion('servicio'), leerColeccion('plan')
  ]);

  const porEstado = citas.reduce((acumulado, cita) => {
    acumulado[cita.estado] = (acumulado[cita.estado] ?? 0) + 1;
    return acumulado;
  }, {});

  pintar(`
    <div class="row g-3 mb-4">
      ${tarjetaResumen('calendar_month', citas.length, 'Citas registradas')}
      ${tarjetaResumen('pending_actions', porEstado.PENDIENTE ?? 0, 'Citas pendientes')}
      ${tarjetaResumen('task_alt', porEstado.FINALIZADA ?? 0, 'Citas finalizadas')}
      ${tarjetaResumen('badge', proveedores.length, 'Proveedores')}
      ${tarjetaResumen('medical_services', servicios.length, 'Servicios')}
      ${tarjetaResumen('storefront', planes.length, 'Planes')}
    </div>
    <div class="card">
      <div class="card-body">
        <h5 class="card-title mb-3">Citas por estado</h5>
        ${tabla(['Estado', 'Cantidad'],
          Object.entries(porEstado).map(([estado, cantidad]) =>
            `<tr><td>${chip(estado)}</td><td>${cantidad}</td></tr>`))}
      </div>
    </div>
  `);
}

function tarjetaResumen(iconoNombre, valor, etiqueta) {
  return `<div class="col-6 col-md-4 col-xl-2">
    <div class="tarjeta-resumen p-3 d-flex align-items-center gap-3 h-100">
      <img src="${icono(iconoNombre)}" alt="">
      <div>
        <div class="tarjeta-resumen__valor">${valor}</div>
        <div class="tarjeta-resumen__etiqueta">${esc(etiqueta)}</div>
      </div>
    </div>
  </div>`;
}

// ---------------------------------------------------------------------------
// CITAS (RF-09, RF-10, RF-15)
// ---------------------------------------------------------------------------

async function vistaCitas(ctx) {
  const [citas, mascotas, servicios, usuarios, proveedores, agenda] = await Promise.all([
    leerColeccion('cita'), leerColeccion('mascota'), leerColeccion('servicio'),
    leerColeccion('usuario'), leerColeccion('proveedor'), leerColeccion('agenda')
  ]);

  const mapaMascotas = mapaPor(mascotas);
  const mapaServicios = mapaPor(servicios);
  const mapaUsuarios = mapaPor(usuarios);
  const idsProveedores = proveedores.map(p => p.id);

  // Un veterinario esta disponible si su usuario esta ACTIVO y no tiene otra
  // cita que se cruce con la fecha y hora de esta cita.
  const proveedorLibre = (idProveedor, idCita, inicio) => {
    if (inicio == null) return true;
    const fin = inicio.getTime() + HorarioMinutos * 60_000;
    return !agenda.some(entrada =>
      entrada.id_proveedor === idProveedor
      && entrada.id_cita !== idCita
      && entrada.estado !== 'CANCELADA'
      && inicio.getTime() < (aFecha(entrada.fin)?.getTime() ?? 0)
      && fin > (aFecha(entrada.inicio)?.getTime() ?? 0)
    );
  };

  const ordenadas = [...citas].sort(
    (a, b) => (aFecha(b.fecha_hora)?.getTime() ?? 0) - (aFecha(a.fecha_hora)?.getTime() ?? 0)
  );

  $('#cabecera-acciones').innerHTML =
    `<button class="btn btn-sevetco" data-accion="nueva"><img src="${icono('add_circle')}" alt="" class="icono-18">Nueva cita</button>`;

  pintar(`
    <div class="card">
      <div class="card-body">
        <h5 class="card-title mb-3">Citas</h5>
        ${tabla(['Fecha', 'Propietario', 'Mascota', 'Servicio', 'Responsable', 'Estado', ''],
          ordenadas.map(cita => `<tr>
            <td>${fechaHora(cita.fecha_hora)}</td>
            <td>${celdaPersona(mapaUsuarios, cita.id_propietario)}</td>
            <td>${celdaMascota(mapaMascotas, cita.id_mascota)}</td>
            <td>${esc(mapaServicios.get(cita.id_servicio)?.nombre ?? '-')}</td>
            <td>${cita.id_proveedor ? celdaPersona(mapaUsuarios, cita.id_proveedor) : 'Sin asignar'}</td>
            <td>${chip(cita.estado)}</td>
            <td class="text-nowrap">
              ${botonAccion('editar', cita.id, 'edit_calendar', 'Asignar responsable y estado')}
              ${botonAccion('trazabilidad', cita.id, 'list_alt', 'Ver trazabilidad')}
              ${botonAccion('borrar', cita.id, 'delete', 'Eliminar')}
            </td>
          </tr>`))}
      </div>
    </div>
  `);

  const manejar = async boton => {
    const id = boton.dataset.id;
    const cita = citas.find(c => c.id === id);
    if (!cita) return;

    if (boton.dataset.accion === 'trazabilidad') {
      const eventos = (await leerColeccion('trazabilidad_cita'))
        .filter(e => e.id_cita === id)
        .sort((a, b) => (aFecha(a.fecha_evento)?.getTime() ?? 0) - (aFecha(b.fecha_evento)?.getTime() ?? 0));

      const lista = eventos.length
        ? `<ul class="linea-tiempo">${eventos.map(e => `<li>
            <img src="${icono('event_available')}" alt="">
            <div><div>${chip(e.estado_nuevo)}</div>
            <time>${fechaHora(e.fecha_evento)}</time>
            <div class="celda-usuario mt-1">
              ${avatar(mapaUsuarios.get(e.id_usuario_responsable)?.foto,
                nombreDeUsuario(mapaUsuarios, e.id_usuario_responsable))}
              <span>${esc(nombreDeUsuario(mapaUsuarios, e.id_usuario_responsable))}</span>
            </div></div>
          </li>`).join('')}</ul>`
        : '<p class="text-secondary text-center py-4 mb-0">Sin eventos registrados.</p>';

      abrirModal({ titulo: 'Trazabilidad de la cita', cuerpo: lista, textoBoton: 'Cerrar', alGuardar: async () => cerrarModal() });
      return;
    }

    if (boton.dataset.accion === 'borrar') {
      if (!confirm('¿Eliminar esta cita?')) return;
      await eliminar('cita', id);
      aviso('Cita eliminada.');
      renderVista('citas', ctx);
      return;
    }

    abrirModal({
      titulo: 'Cita de ' + (mapaMascotas.get(cita.id_mascota)?.nombre ?? ''),
      cuerpo: `<div class="row g-3">
        <div class="col-12">
          <div class="celda-usuario">
            ${avatar(mapaUsuarios.get(cita.id_propietario)?.foto,
              nombreDeUsuario(mapaUsuarios, cita.id_propietario))}
            <span class="text-secondary small">
              ${fechaHora(cita.fecha_hora)} - ${esc(mapaServicios.get(cita.id_servicio)?.nombre ?? '')}
              - ${esc(nombreDeUsuario(mapaUsuarios, cita.id_propietario))}
            </span>
          </div>
        </div>
        ${campo({ nombre: 'id_proveedor', etiqueta: 'Responsable del servicio', valor: cita.id_proveedor ?? '',
          opciones: [{ valor: '', texto: 'Sin asignar' },
            ...idsProveedores.map(idProv => {
              const usuario = mapaUsuarios.get(idProv);
              const activo = usuario?.estado === 'ACTIVO';
              const libre = proveedorLibre(idProv, cita.id, aFecha(cita.fecha_hora));
              const esActual = cita.id_proveedor === idProv;
              const nota = !activo ? ' (inactivo)' : (libre ? '' : ' (ocupado)');
              return {
                valor: idProv,
                texto: (usuario?.nombre || usuario?.correo || 'Proveedor') + nota,
                deshabilitado: (!activo || !libre) && !esActual
              };
            })] })}
        ${campo({ nombre: 'estado', etiqueta: 'Estado de la cita', valor: cita.estado,
          opciones: ['PENDIENTE', 'ASIGNADA', 'EN_PROCESO', 'FINALIZADA', 'CANCELADA']
            .map(e => ({ valor: e, texto: e })) })}
      </div>`,
      alGuardar: async () => {
        const datos = leerFormulario();
        const anterior = cita.estado;
        await actualizar('cita', id, {
          id_proveedor: datos.id_proveedor || null,
          estado: datos.estado
        });

        // La agenda guarda los tramos ocupados para que la app movil pueda
        // calcular los horarios libres.
        const inicio = aFecha(cita.fecha_hora);
        if (inicio) {
          await crearConId('agenda', cita.id, {
            id_cita: cita.id,
            id_propietario: cita.id_propietario ?? null,
            id_proveedor: datos.id_proveedor || null,
            inicio: aTimestamp(inicio),
            fin: aTimestamp(new Date(inicio.getTime() + HorarioMinutos * 60_000)),
            estado: datos.estado
          });
        }

        if (datos.estado !== anterior) {
          await registrarTrazabilidad({ ...cita, id_proveedor: datos.id_proveedor || null },
            datos.estado, ctx.perfil?.id);
        }
        cerrarModal();
        aviso('Cita actualizada.');
        renderVista('citas', ctx);
      }
    });
  };

  document.querySelector('#cabecera-acciones [data-accion="nueva"]').addEventListener('click', async () => {
    if (!mascotas.length) return aviso('Primero registra una mascota en la app movil.', true);
    abrirModal({
      titulo: 'Nueva cita',
      cuerpo: `<div class="row g-3">
        ${campo({ nombre: 'id_propietario', etiqueta: 'Propietario', requerido: true,
          opciones: [{ valor: '', texto: 'Seleccione...' },
            ...usuarios.filter(u => u.tipo_usuario === 'PROPIETARIO')
              .map(u => ({ valor: u.id, texto: u.nombre || u.correo }))] })}
        ${campo({ nombre: 'id_mascota', etiqueta: 'Mascota', requerido: true,
          opciones: [{ valor: '', texto: 'Seleccione...' },
            ...mascotas.map(m => ({ valor: m.id, texto: m.nombre }))] })}
        ${campo({ nombre: 'id_servicio', etiqueta: 'Servicio', requerido: true,
          opciones: [{ valor: '', texto: 'Seleccione...' },
            ...servicios.map(s => ({ valor: s.id, texto: s.nombre }))] })}
        ${campo({ nombre: 'fecha_hora', etiqueta: 'Fecha y hora', tipo: 'datetime-local', requerido: true })}
      </div>`,
      alGuardar: async () => {
        const datos = leerFormulario();
        if (!datos.id_propietario || !datos.id_mascota || !datos.id_servicio || !datos.fecha_hora) {
          return aviso('Completa todos los campos.', true);
        }
        const idCita = await crear('cita', {
          id_propietario: datos.id_propietario,
          id_mascota: datos.id_mascota,
          id_servicio: datos.id_servicio,
          id_proveedor: null,
          id_suscripcion: null,
          fecha_hora: aTimestamp(datos.fecha_hora),
          estado: 'PENDIENTE'
        });

        const inicioCita = aFecha(datos.fecha_hora);
        if (inicioCita) {
          await crearConId('agenda', idCita, {
            id_cita: idCita,
            id_propietario: datos.id_propietario,
            id_proveedor: null,
            inicio: aTimestamp(inicioCita),
            fin: aTimestamp(new Date(inicioCita.getTime() + HorarioMinutos * 60_000)),
            estado: 'PENDIENTE'
          });
        }

        cerrarModal();
        aviso('Cita creada.');
        renderVista('citas', ctx);
      }
    });
  });

  $('#vista').addEventListener('click', evento => {
    const boton = evento.target.closest('[data-accion]');
    if (boton) manejar(boton);
  });
}

// ---------------------------------------------------------------------------
// PROVEEDORES (RF-13)
// ---------------------------------------------------------------------------

// ---------------------------------------------------------------------------
// EVENTOS (contenido de la pantalla de Inicio en la app movil)
// ---------------------------------------------------------------------------

// ---------------------------------------------------------------------------
// MASCOTAS (el administrador ve y corrige las de todos los clientes)
// ---------------------------------------------------------------------------

async function vistaMascotas(ctx) {
  const [mascotas, usuarios] = await Promise.all([
    leerColeccion('mascota'), leerColeccion('usuario')
  ]);
  const mapaUsuarios = mapaPor(usuarios);

  $('#cabecera-acciones').innerHTML = '';

  const filas = mascotas
    .slice()
    .sort((a, b) => (a.nombre ?? '').localeCompare(b.nombre ?? ''))
    .map(mascota => `<tr>
      <td>
        <div class="celda-usuario">
          ${fotoMascota(mascota.foto, mascota.nombre ?? '')}
          <span>${esc(mascota.nombre ?? '-')}</span>
        </div>
      </td>
      <td>${esc(mascota.especie ?? '-')}</td>
      <td>${esc(mascota.raza ?? '-')}</td>
      <td>${esc(edadDe(mascota.fecha_nacimiento))}</td>
      <td>${celdaPersona(mapaUsuarios, mascota.id_propietario)}</td>
      <td class="text-nowrap">
        ${botonAccion('editar', mascota.id)}
        ${botonAccion('borrar', mascota.id, 'delete', 'Eliminar')}
      </td>
    </tr>`);

  pintar(`
    <div class="card">
      <div class="card-body">
        <h5 class="card-title mb-3">Mascotas registradas</h5>
        ${tabla(['Nombre', 'Especie', 'Raza', 'Edad', 'Propietario', ''], filas)}
      </div>
    </div>
  `);

  const manejar = async boton => {
    const id = boton.dataset.id;

    if (boton.dataset.accion === 'borrar') {
      if (!confirm('¿Eliminar esta mascota?')) return;
      await eliminar('mascota', id);
      aviso('Mascota eliminada.');
      renderVista('mascotas', ctx);
      return;
    }

    const mascota = mascotas.find(m => m.id === id);
    imagenEnEdicion = mascota?.foto ?? '';
    const propietarios = usuarios.filter(u => u.tipo_usuario === 'PROPIETARIO');

    abrirModal({
      titulo: 'Editar mascota',
      cuerpo: `<div class="row g-3">
        ${campo({ nombre: 'id_propietario', etiqueta: 'Propietario', requerido: true,
          valor: mascota?.id_propietario ?? '',
          opciones: propietarios.map(u => ({ valor: u.id, texto: u.nombre || u.correo })) })}
        ${campo({ nombre: 'nombre', etiqueta: 'Nombre', valor: mascota?.nombre ?? '', requerido: true })}
        ${campo({ nombre: 'especie', etiqueta: 'Especie', valor: mascota?.especie ?? '' })}
        ${campo({ nombre: 'raza', etiqueta: 'Raza', valor: mascota?.raza ?? '' })}
        ${campo({ nombre: 'fecha_nacimiento', etiqueta: 'Fecha de nacimiento', tipo: 'date', valor: mascota?.fecha_nacimiento ?? '' })}
        ${bloqueImagen('foto')}
      </div>`,
      alGuardar: async () => {
        const datos = leerFormulario();
        if (!datos.nombre) return aviso('Escribe el nombre de la mascota.', true);
        await actualizar('mascota', id, {
          id_propietario: datos.id_propietario,
          nombre: datos.nombre,
          especie: datos.especie,
          raza: datos.raza,
          fecha_nacimiento: datos.fecha_nacimiento,
          foto: imagenEnEdicion || ''
        });
        cerrarModal();
        aviso('Mascota actualizada.');
        renderVista('mascotas', ctx);
      }
    });
    conectarImagen('foto');
  };

  $('#vista').addEventListener('click', evento => {
    const boton = evento.target.closest('[data-accion]');
    if (boton) manejar(boton);
  });
}

// ---------------------------------------------------------------------------
// SUSCRIPCIONES (planes adquiridos por los clientes)
// ---------------------------------------------------------------------------

async function vistaSuscripciones(ctx) {
  const [suscripciones, usuarios, planes] = await Promise.all([
    leerColeccion('suscripcion'), leerColeccion('usuario'), leerColeccion('plan')
  ]);
  const mapaUsuarios = mapaPor(usuarios);
  const mapaPlanes = mapaPor(planes);

  $('#cabecera-acciones').innerHTML = '';

  pintar(`
    <div class="card">
      <div class="card-body">
        <h5 class="card-title mb-3">Suscripciones</h5>
        ${tabla(['Propietario', 'Plan', 'Inicio', 'Fin', 'Estado', ''],
          suscripciones.map(suscripcion => `<tr>
            <td>${celdaPersona(mapaUsuarios, suscripcion.id_propietario)}</td>
            <td>${esc(mapaPlanes.get(suscripcion.id_plan)?.nombre ?? '-')}</td>
            <td>${fecha(suscripcion.fecha_inicio)}</td>
            <td>${fecha(suscripcion.fecha_fin)}</td>
            <td>${chip(suscripcion.estado)}</td>
            <td class="text-nowrap">
              ${botonAccion('editar', suscripcion.id)}
              ${botonAccion('borrar', suscripcion.id, 'delete', 'Eliminar')}
            </td>
          </tr>`))}
      </div>
    </div>
  `);

  const manejar = async boton => {
    const id = boton.dataset.id;

    if (boton.dataset.accion === 'borrar') {
      if (!confirm('¿Eliminar esta suscripcion?')) return;
      await eliminar('suscripcion', id);
      aviso('Suscripcion eliminada.');
      renderVista('suscripciones', ctx);
      return;
    }

    const suscripcion = suscripciones.find(s => s.id === id);

    abrirModal({
      titulo: 'Editar suscripcion',
      cuerpo: `<div class="row g-3">
        ${campo({ nombre: 'id_propietario', etiqueta: 'Propietario', requerido: true,
          valor: suscripcion?.id_propietario ?? '',
          opciones: usuarios.filter(u => u.tipo_usuario === 'PROPIETARIO')
            .map(u => ({ valor: u.id, texto: u.nombre || u.correo })) })}
        ${campo({ nombre: 'id_plan', etiqueta: 'Plan', requerido: true,
          valor: suscripcion?.id_plan ?? '',
          opciones: planes.map(p => ({ valor: p.id, texto: p.nombre })) })}
        ${campo({ nombre: 'fecha_inicio', etiqueta: 'Fecha de inicio', tipo: 'datetime-local',
          valor: aInputFecha(suscripcion?.fecha_inicio) })}
        ${campo({ nombre: 'fecha_fin', etiqueta: 'Fecha de fin', tipo: 'datetime-local',
          valor: aInputFecha(suscripcion?.fecha_fin) })}
        ${campo({ nombre: 'estado', etiqueta: 'Estado', valor: suscripcion?.estado ?? 'ACTIVA',
          opciones: ['ACTIVA', 'VENCIDA', 'CANCELADA'].map(e => ({ valor: e, texto: e })) })}
      </div>`,
      alGuardar: async () => {
        const datos = leerFormulario();
        await actualizar('suscripcion', id, {
          id_propietario: datos.id_propietario,
          id_plan: datos.id_plan,
          fecha_inicio: aTimestamp(datos.fecha_inicio),
          fecha_fin: aTimestamp(datos.fecha_fin),
          estado: datos.estado
        });
        cerrarModal();
        aviso('Suscripcion actualizada.');
        renderVista('suscripciones', ctx);
      }
    });
  };

  $('#vista').addEventListener('click', evento => {
    const boton = evento.target.closest('[data-accion]');
    if (boton) manejar(boton);
  });
}

async function vistaEventos(ctx) {
  const eventos = await leerColeccion('eventos');

  $('#cabecera-acciones').innerHTML =
    `<button class="btn btn-sevetco" data-accion="nuevo">
      <img src="${icono('add_circle')}" alt="" class="icono-18">Nuevo evento
    </button>`;

  pintar(`
    <div class="row g-3">
      ${eventos.length === 0
        ? '<div class="col-12"><div class="card"><div class="card-body text-center text-secondary">Todavia no hay eventos publicados.</div></div></div>'
        : eventos.map(evento => `<div class="col-12 col-lg-6">
            <div class="position-relative">
              ${tarjetaContenido({
                nombre: evento.nombre_evento ?? '',
                fecha: evento.fecha_evento,
                horaInicio: evento.horario_inievento ?? evento.horario_ini_evento,
                horaFin: evento.horariofin_evento ?? evento.horario_fin_evento,
                texto: evento.texto_evento,
                imagen: evento.imagen_evento
              })}
              <div class="position-absolute top-0 end-0 m-2 d-flex gap-1">
                ${botonAccion('editar', evento.id, 'edit', 'Editar')}
                ${botonAccion('borrar', evento.id, 'delete', 'Eliminar')}
              </div>
            </div>
          </div>`).join('')}
    </div>
  `);

  const manejar = async boton => {
    const id = boton.dataset.id;

    if (boton.dataset.accion === 'borrar') {
      if (!confirm('¿Eliminar este evento?')) return;
      await eliminar('eventos', id);
      aviso('Evento eliminado.');
      renderVista('eventos', ctx);
      return;
    }

    const evento = eventos.find(e => e.id === id);
    imagenEnEdicion = evento?.imagen_evento ?? '';

    abrirModal({
      titulo: evento ? 'Editar evento' : 'Nuevo evento',
      cuerpo: `<div class="row g-3">
        ${campo({ nombre: 'nombre_evento', etiqueta: 'Nombre del evento', valor: evento?.nombre_evento ?? '', requerido: true })}
        ${campo({ nombre: 'fecha_evento', etiqueta: 'Fecha (por ejemplo 31/10/2026)', valor: evento?.fecha_evento ?? '', requerido: true })}
        ${campo({ nombre: 'horario_inievento', etiqueta: 'Hora de inicio (9:00 a.m)', valor: evento?.horario_inievento ?? '' })}
        ${campo({ nombre: 'horariofin_evento', etiqueta: 'Hora de fin (12:00 p.m)', valor: evento?.horariofin_evento ?? '' })}
        ${campo({ nombre: 'texto_evento', etiqueta: 'Texto de la card', tipo: 'textarea', valor: evento?.texto_evento ?? '' })}
        ${bloqueImagen('imagen_evento')}
      </div>`,
      alGuardar: async () => {
        const datos = leerFormulario();
        if (!datos.nombre_evento) return aviso('Escribe el nombre del evento.', true);
        const fila = {
          nombre_evento: datos.nombre_evento,
          fecha_evento: datos.fecha_evento,
          horario_inievento: datos.horario_inievento,
          horariofin_evento: datos.horariofin_evento,
          texto_evento: datos.texto_evento,
          imagen_evento: imagenEnEdicion || ''
        };
        if (evento) await actualizar('eventos', evento.id, fila);
        else await crear('eventos', fila);
        cerrarModal();
        aviso('Evento guardado.');
        renderVista('eventos', ctx);
      }
    });
    conectarImagen('imagen_evento');
  };

  document.querySelector('#cabecera-acciones [data-accion="nuevo"]')
    .addEventListener('click', () => manejar({ dataset: { accion: 'nuevo', id: '' } }));

  $('#vista').addEventListener('click', evento => {
    const boton = evento.target.closest('[data-accion]');
    if (boton && boton.dataset.accion !== 'nuevo') manejar(boton);
  });
}

// ---------------------------------------------------------------------------
// PROMOCIONES (contenido de la pantalla de Inicio en la app movil)
// ---------------------------------------------------------------------------

async function vistaPromociones(ctx) {
  const promociones = await leerColeccion('promociones');

  $('#cabecera-acciones').innerHTML =
    `<button class="btn btn-sevetco" data-accion="nuevo">
      <img src="${icono('add_circle')}" alt="" class="icono-18">Nueva promocion
    </button>`;

  pintar(`
    <div class="row g-3">
      ${promociones.length === 0
        ? '<div class="col-12"><div class="card"><div class="card-body text-center text-secondary">Todavia no hay promociones publicadas.</div></div></div>'
        : promociones.map(promo => `<div class="col-12 col-lg-6">
            <div class="position-relative">
              ${tarjetaContenido({
                nombre: promo.nombre_promo ?? '',
                fecha: promo.fecha_promo,
                horaInicio: promo.horaini_promo,
                horaFin: promo.horafin_promo,
                texto: promo.texto_promo,
                descuento: promo.descuento_promo,
                imagen: promo.imagen_promo
              })}
              <div class="position-absolute top-0 end-0 m-2 d-flex gap-1">
                ${botonAccion('editar', promo.id, 'edit', 'Editar')}
                ${botonAccion('borrar', promo.id, 'delete', 'Eliminar')}
              </div>
            </div>
          </div>`).join('')}
    </div>
  `);

  const manejar = async boton => {
    const id = boton.dataset.id;

    if (boton.dataset.accion === 'borrar') {
      if (!confirm('¿Eliminar esta promocion?')) return;
      await eliminar('promociones', id);
      aviso('Promocion eliminada.');
      renderVista('promociones', ctx);
      return;
    }

    const promo = promociones.find(p => p.id === id);
    imagenEnEdicion = promo?.imagen_promo ?? '';

    abrirModal({
      titulo: promo ? 'Editar promocion' : 'Nueva promocion',
      cuerpo: `<div class="row g-3">
        ${campo({ nombre: 'nombre_promo', etiqueta: 'Nombre de la promocion', valor: promo?.nombre_promo ?? '', requerido: true })}
        ${campo({ nombre: 'fecha_promo', etiqueta: 'Fecha (por ejemplo 31/10/2026)', valor: promo?.fecha_promo ?? '' })}
        ${campo({ nombre: 'horaini_promo', etiqueta: 'Hora de inicio (3:00 p.m.)', valor: promo?.horaini_promo ?? '' })}
        ${campo({ nombre: 'horafin_promo', etiqueta: 'Hora de fin (6:00 p.m.)', valor: promo?.horafin_promo ?? '' })}
        ${campo({ nombre: 'descuento_promo', etiqueta: 'Descuento en % (por ejemplo 10)', valor: promo?.descuento_promo ?? '' })}
        ${campo({ nombre: 'texto_promo', etiqueta: 'Texto de la card', tipo: 'textarea', valor: promo?.texto_promo ?? '' })}
        ${bloqueImagen('imagen_promo')}
      </div>`,
      alGuardar: async () => {
        const datos = leerFormulario();
        if (!datos.nombre_promo) return aviso('Escribe el nombre de la promocion.', true);
        const fila = {
          nombre_promo: datos.nombre_promo,
          fecha_promo: datos.fecha_promo,
          horaini_promo: datos.horaini_promo,
          horafin_promo: datos.horafin_promo,
          descuento_promo: datos.descuento_promo,
          texto_promo: datos.texto_promo,
          imagen_promo: imagenEnEdicion || ''
        };
        if (promo) await actualizar('promociones', promo.id, fila);
        else await crear('promociones', fila);
        cerrarModal();
        aviso('Promocion guardada.');
        renderVista('promociones', ctx);
      }
    });
    conectarImagen('imagen_promo');
  };

  document.querySelector('#cabecera-acciones [data-accion="nuevo"]')
    .addEventListener('click', () => manejar({ dataset: { accion: 'nuevo', id: '' } }));

  $('#vista').addEventListener('click', evento => {
    const boton = evento.target.closest('[data-accion]');
    if (boton && boton.dataset.accion !== 'nuevo') manejar(boton);
  });
}

async function vistaProveedores(ctx) {
  const [proveedores, usuarios] = await Promise.all([
    leerColeccion('proveedor'), leerColeccion('usuario')
  ]);
  const mapaUsuarios = mapaPor(usuarios);

  $('#cabecera-acciones').innerHTML =
    `<button class="btn btn-sevetco" data-accion="nuevo"><img src="${icono('add_circle')}" alt="" class="icono-18">Nuevo proveedor</button>`;

  pintar(`
    <div class="card">
      <div class="card-body">
        <h5 class="card-title mb-3">Proveedores registrados</h5>
        ${tabla(['Nombre', 'Correo', 'Tarjeta profesional', 'Especialidad', 'Estado', ''],
          proveedores.map(proveedor => {
            const usuario = mapaUsuarios.get(proveedor.id);
            return `<tr>
              <td>${celdaPersona(mapaUsuarios, proveedor.id)}</td>
              <td>${esc(usuario?.correo ?? '-')}</td>
              <td>${esc(proveedor.tarjeta_profesional ?? '-')}</td>
              <td>${esc(proveedor.especialidad ?? '-')}</td>
              <td>${chip(usuario?.estado)}</td>
              <td class="text-nowrap">
                ${botonAccion('editar', proveedor.id)}
                ${botonAccion('borrar', proveedor.id, 'delete', 'Eliminar')}
              </td>
            </tr>`;
          }))}
      </div>
    </div>
  `);

  const manejar = proveedor => {
    const usuario = proveedor ? mapaUsuarios.get(proveedor.id) : null;
    const esNuevo = !proveedor;
    imagenEnEdicion = usuario?.foto ?? '';

    abrirModal({
      titulo: esNuevo ? 'Nuevo proveedor' : 'Editar proveedor',
      cuerpo: `<div class="row g-3">
        ${campo({ nombre: 'nombre', etiqueta: 'Nombre completo', requerido: true,
          valor: usuario?.nombre ?? '' })}
        ${campo({ nombre: 'numero_documento', etiqueta: 'Numero de documento',
          valor: usuario?.numero_documento ?? '' })}
        ${esNuevo
          ? campo({ nombre: 'correo', etiqueta: 'Correo electronico', requerido: true })
          : ''}
        ${esNuevo
          ? campo({ nombre: 'clave', etiqueta: 'Contrasena inicial (minimo 6 caracteres)', requerido: true })
          : ''}
        ${campo({ nombre: 'tarjeta_profesional', etiqueta: 'Tarjeta profesional', valor: proveedor?.tarjeta_profesional ?? '' })}
        ${campo({ nombre: 'especialidad', etiqueta: 'Especialidad',
          valor: proveedor?.especialidad ?? 'Medico Veterinario' })}
        ${bloqueImagen('foto', 'Foto del proveedor',
          'Si no subes ninguna, la puede tomar despues desde la app movil.', 84, 84)}
      </div>`,
      alGuardar: async () => {
        const datos = leerFormulario();
        if (!datos.nombre) return aviso('Escribe el nombre completo.', true);

        if (esNuevo) {
          if (!datos.correo) return aviso('Escribe el correo electronico.', true);
          if (!datos.clave || datos.clave.length < 6) {
            return aviso('La contrasena inicial debe tener al menos 6 caracteres.', true);
          }

          let uid;
          try {
            uid = await crearCuenta(datos.correo.trim(), datos.clave, datos.nombre.trim());
          } catch (error) {
            if (String(error?.code).includes('email-already-in-use')) {
              return aviso('Ese correo ya tiene una cuenta.', true);
            }
            throw error;
          }

          await crearConId('usuario', uid, {
            nombre: datos.nombre.trim(),
            numero_documento: datos.numero_documento || null,
            correo: datos.correo.trim(),
            foto: imagenEnEdicion || '',
            tipo_usuario: 'PROVEEDOR',
            estado: 'ACTIVO'
          });
          await crearConId('proveedor', uid, {
            id_usuario: uid,
            tarjeta_profesional: datos.tarjeta_profesional || null,
            especialidad: datos.especialidad || 'Medico Veterinario'
          });

          cerrarModal();
          aviso('Proveedor creado con su cuenta de acceso.');
          renderVista('proveedores', ctx);
          return;
        }

        await actualizar('usuario', proveedor.id, {
          nombre: datos.nombre.trim(),
          numero_documento: datos.numero_documento || null,
          foto: imagenEnEdicion || '',
          tipo_usuario: 'PROVEEDOR'
        });
        await crearConId('proveedor', proveedor.id, {
          id_usuario: proveedor.id,
          tarjeta_profesional: datos.tarjeta_profesional || null,
          especialidad: datos.especialidad || 'Medico Veterinario'
        });

        cerrarModal();
        aviso('Proveedor guardado.');
        renderVista('proveedores', ctx);
      }
    });
    conectarImagen('foto');
  };

  document.querySelector('#cabecera-acciones [data-accion="nuevo"]').addEventListener('click', () => manejar(null));
  $('#vista').addEventListener('click', async evento => {
    const boton = evento.target.closest('[data-accion]');
    if (!boton) return;
    if (boton.dataset.accion === 'borrar') {
      if (!confirm('¿Eliminar este proveedor?')) return;
      await eliminar('proveedor', boton.dataset.id);
      aviso('Proveedor eliminado.');
      renderVista('proveedores', ctx);
      return;
    }
    manejar(proveedores.find(p => p.id === boton.dataset.id));
  });
}

// ---------------------------------------------------------------------------
// SERVICIOS (RF-14)
// ---------------------------------------------------------------------------

async function vistaServicios(ctx) {
  const servicios = await leerColeccion('servicio');

  $('#cabecera-acciones').innerHTML =
    `<button class="btn btn-sevetco" data-accion="nuevo"><img src="${icono('add_circle')}" alt="" class="icono-18">Nuevo servicio</button>`;

  pintar(`
    <div class="card">
      <div class="card-body">
        <h5 class="card-title mb-3">Portafolio de servicios</h5>
        ${tabla(['Servicio', 'Precio base', 'Duracion', ''],
          servicios.map(servicio => `<tr>
            <td>${esc(servicio.nombre)}</td>
            <td>${moneda(servicio.precio_base)}</td>
            <td>${servicio.duracion_minutos} min</td>
            <td class="text-nowrap">
              ${botonAccion('editar', servicio.id)}
              ${botonAccion('borrar', servicio.id, 'delete', 'Eliminar')}
            </td>
          </tr>`))}
      </div>
    </div>
  `);

  const manejar = servicio => {
    abrirModal({
      titulo: servicio ? 'Editar servicio' : 'Nuevo servicio',
      cuerpo: `<div class="row g-3">
        ${campo({ nombre: 'nombre', etiqueta: 'Nombre del servicio', valor: servicio?.nombre ?? '', requerido: true })}
        ${campo({ nombre: 'precio_base', etiqueta: 'Precio base (COP)', tipo: 'number', valor: servicio?.precio_base ?? '', requerido: true })}
        ${campo({ nombre: 'duracion_minutos', etiqueta: 'Duracion (minutos)', tipo: 'number', valor: servicio?.duracion_minutos ?? 60, requerido: true })}
      </div>`,
      alGuardar: async () => {
        const datos = leerFormulario();
        const fila = {
          nombre: datos.nombre,
          precio_base: Number(datos.precio_base),
          duracion_minutos: Number(datos.duracion_minutos)
        };
        if (servicio) await actualizar('servicio', servicio.id, fila);
        else await crear('servicio', fila);

        cerrarModal();
        aviso('Servicio guardado.');
        renderVista('servicios', ctx);
      }
    });
  };

  document.querySelector('#cabecera-acciones [data-accion="nuevo"]').addEventListener('click', () => manejar(null));
  $('#vista').addEventListener('click', async evento => {
    const boton = evento.target.closest('[data-accion]');
    if (!boton) return;
    if (boton.dataset.accion === 'borrar') {
      if (!confirm('¿Eliminar este servicio?')) return;
      await eliminar('servicio', boton.dataset.id);
      aviso('Servicio eliminado.');
      renderVista('servicios', ctx);
      return;
    }
    manejar(servicios.find(s => s.id === boton.dataset.id));
  });
}

// ---------------------------------------------------------------------------
// PLANES (RF-04, RF-05, RF-14)
// ---------------------------------------------------------------------------

async function vistaPlanes(ctx) {
  const [planes, servicios, planServicio] = await Promise.all([
    leerColeccion('plan'), leerColeccion('servicio'), leerColeccion('plan_servicio')
  ]);
  const nombreServicio = mapaPor(servicios);

  $('#cabecera-acciones').innerHTML =
    `<button class="btn btn-sevetco" data-accion="nuevo"><img src="${icono('add_circle')}" alt="" class="icono-18">Nuevo plan</button>`;

  pintar(`
    <div class="card">
      <div class="card-body">
        <h5 class="card-title mb-3">Planes</h5>
        ${tabla(['Plan', 'Precio', 'Vigencia', 'Estado', 'Servicios incluidos', ''],
          planes.map(plan => {
            const incluidos = planServicio
              .filter(relacion => relacion.id_plan === plan.id)
              .map(relacion => `${esc(nombreServicio.get(relacion.id_servicio)?.nombre ?? '')} (${relacion.cantidad_incluida})`)
              .join(', ') || 'Sin servicios';
            return `<tr>
              <td>${esc(plan.nombre)}</td>
              <td>${moneda(plan.precio)}</td>
              <td>${plan.vigencia_dias} dias</td>
              <td>${chip(plan.estado)}</td>
              <td class="text-secondary">${incluidos}</td>
              <td class="text-nowrap">
                ${botonAccion('editar', plan.id)}
                ${botonAccion('borrar', plan.id, 'delete', 'Eliminar')}
              </td>
            </tr>`;
          }))}
      </div>
    </div>
  `);

  const manejar = plan => {
    const actuales = plan ? planServicio.filter(r => r.id_plan === plan.id) : [];

    const casillas = servicios.map(servicio => {
      const incluido = actuales.find(a => a.id_servicio === servicio.id);
      return `<div class="d-flex align-items-center gap-2 border-bottom py-2">
        <input class="form-check-input mt-0" type="checkbox" name="servicio_${servicio.id}" ${incluido ? 'checked' : ''}>
        <span class="flex-grow-1 small">${esc(servicio.nombre)}</span>
        <input class="form-control form-control-sm" style="width:80px" type="number" min="1"
          name="cantidad_${servicio.id}" value="${incluido?.cantidad_incluida ?? 1}">
      </div>`;
    }).join('');

    abrirModal({
      titulo: plan ? 'Editar plan' : 'Nuevo plan',
      cuerpo: `<div class="row g-3">
        ${campo({ nombre: 'nombre', etiqueta: 'Nombre del plan', valor: plan?.nombre ?? '', requerido: true })}
        ${campo({ nombre: 'precio', etiqueta: 'Precio (COP)', tipo: 'number', valor: plan?.precio ?? '', requerido: true })}
        ${campo({ nombre: 'vigencia_dias', etiqueta: 'Vigencia (dias)', tipo: 'number', valor: plan?.vigencia_dias ?? 30, requerido: true })}
        ${campo({ nombre: 'estado', etiqueta: 'Estado', valor: plan?.estado ?? 'ACTIVO',
          opciones: ['ACTIVO', 'INACTIVO'].map(e => ({ valor: e, texto: e })) })}
        <div class="col-12">
          <label class="form-label small fw-semibold text-secondary">Servicios incluidos</label>
          ${casillas || '<p class="text-secondary small mb-0">Todavia no hay servicios registrados.</p>'}
        </div>
      </div>`,
      alGuardar: async () => {
        const datos = leerFormulario();
        const fila = {
          nombre: datos.nombre,
          precio: Number(datos.precio),
          vigencia_dias: Number(datos.vigencia_dias),
          estado: datos.estado
        };

        let idPlan = plan?.id;
        if (plan) await actualizar('plan', idPlan, fila);
        else idPlan = await crear('plan', fila);

        const anteriores = planServicio.filter(r => r.id_plan === idPlan);
        for (const relacion of anteriores) await eliminar('plan_servicio', relacion.id);

        for (const servicio of servicios) {
          const marcado = document.querySelector(`#modal-cuerpo [name="servicio_${servicio.id}"]`)?.checked;
          if (!marcado) continue;
          const cantidad = Number(document.querySelector(`#modal-cuerpo [name="cantidad_${servicio.id}"]`)?.value || 1);
          await crearConId('plan_servicio', `${idPlan}_${servicio.id}`, {
            id_plan: idPlan,
            id_servicio: servicio.id,
            cantidad_incluida: cantidad
          });
        }

        cerrarModal();
        aviso('Plan guardado.');
        renderVista('planes', ctx);
      }
    });
  };

  document.querySelector('#cabecera-acciones [data-accion="nuevo"]').addEventListener('click', () => manejar(null));
  $('#vista').addEventListener('click', async evento => {
    const boton = evento.target.closest('[data-accion]');
    if (!boton) return;
    if (boton.dataset.accion === 'borrar') {
      if (!confirm('¿Eliminar este plan?')) return;
      await eliminar('plan', boton.dataset.id);
      aviso('Plan eliminado.');
      renderVista('planes', ctx);
      return;
    }
    manejar(planes.find(p => p.id === boton.dataset.id));
  });
}

// ---------------------------------------------------------------------------
// ATENCIONES / HISTORIAL (RF-11, RF-12)
// ---------------------------------------------------------------------------

async function vistaAtenciones(ctx) {
  // El proveedor solo puede leer sus propios tratamientos y citas: hay que
  // pedirlos filtrados por su identificador.
  const esAdmin = ctx.perfil.tipo_usuario === 'ADMIN';
  const [atenciones, citas, mascotas, servicios, usuarios] = await Promise.all([
    esAdmin ? leerColeccion('atencion') : leerDonde('atencion', 'id_proveedor', ctx.perfil.id),
    esAdmin ? leerColeccion('cita') : leerDonde('cita', 'id_proveedor', ctx.perfil.id),
    leerColeccion('mascota'), leerColeccion('servicio'), leerColeccion('usuario')
  ]);
  const mapaCitas = mapaPor(citas);
  const mapaMascotas = mapaPor(mascotas);
  const mapaServicios = mapaPor(servicios);
  const mapaUsuarios = mapaPor(usuarios);

  // RF-11: el tratamiento lo registra unicamente el veterinario que atendio
  // la cita, o un administrador. Un propietario nunca puede registrarlo.
  const tipo = ctx.perfil.tipo_usuario;
  // El tratamiento lo registra unicamente el veterinario que atendio la cita.
  const puedeRegistrar = tipo === 'PROVEEDOR';
  const disponibles = citas;

  $('#cabecera-acciones').innerHTML = puedeRegistrar
    ? `<button class="btn btn-sevetco" data-accion="nuevo"><img src="${icono('medical_services')}" alt="" class="icono-18">Registrar tratamiento</button>`
    : '';

  pintar(`
    <div class="card">
      <div class="card-body">
        <h5 class="card-title mb-3">Historial de tratamientos</h5>
        ${tabla(['Fecha', 'Mascota', 'Veterinario', 'Servicio', 'Diagnostico', 'Tratamiento', 'Recomendaciones'],
          atenciones.map(atencion => {
            const cita = mapaCitas.get(atencion.id_cita);
            return `<tr>
              <td>${fecha(atencion.fecha_registro)}</td>
              <td>${celdaMascota(mapaMascotas, cita?.id_mascota)}</td>
              <td>${celdaPersona(mapaUsuarios, atencion.id_proveedor)}</td>
              <td>${esc(mapaServicios.get(cita?.id_servicio)?.nombre ?? '-')}</td>
              <td>${esc(atencion.diagnostico ?? '-')}</td>
              <td>${esc(atencion.tratamiento ?? '-')}</td>
              <td>${esc(atencion.recomendaciones ?? '-')}</td>
            </tr>`;
          }))}
      </div>
    </div>
  `);

  document.querySelector('#cabecera-acciones [data-accion="nuevo"]')?.addEventListener('click', () => {
    if (!puedeRegistrar) return aviso('Solo el veterinario que atendio la cita puede registrar el tratamiento.', true);
    if (!disponibles.length) return aviso('No hay citas disponibles.', true);

    abrirModal({
      titulo: 'Registrar tratamiento',
      cuerpo: `<div class="row g-3">
        ${campo({ nombre: 'id_cita', etiqueta: 'Cita', requerido: true,
          opciones: [{ valor: '', texto: 'Seleccione...' },
            ...disponibles.map(cita => ({
              valor: cita.id,
              texto: `${fechaHora(cita.fecha_hora)} - ${mapaMascotas.get(cita.id_mascota)?.nombre ?? ''} / ${mapaServicios.get(cita.id_servicio)?.nombre ?? ''}`
            }))] })}
        ${campo({ nombre: 'diagnostico', etiqueta: 'Diagnostico', tipo: 'textarea', requerido: true })}
        ${campo({ nombre: 'tratamiento', etiqueta: 'Tratamiento', tipo: 'textarea', requerido: true })}
        ${campo({ nombre: 'recomendaciones', etiqueta: 'Recomendaciones', tipo: 'textarea' })}
      </div>`,
      alGuardar: async () => {
        const datos = leerFormulario();
        if (!datos.id_cita) return aviso('Seleccione la cita.', true);
        const cita = mapaCitas.get(datos.id_cita);

        await crearConId('atencion', datos.id_cita, {
          id_cita: datos.id_cita,
          id_propietario: cita?.id_propietario ?? null,
          id_proveedor: cita?.id_proveedor ?? ctx.perfil.id,
          diagnostico: datos.diagnostico,
          tratamiento: datos.tratamiento,
          recomendaciones: datos.recomendaciones,
          fecha_registro: aTimestamp(new Date())
        });

        await actualizar('cita', datos.id_cita, { estado: 'FINALIZADA' });
        await registrarTrazabilidad(cita ?? { id: datos.id_cita }, 'FINALIZADA', ctx.perfil.id);

        cerrarModal();
        aviso('Tratamiento registrado.');
        renderVista('atenciones', ctx);
      }
    });
  });
}

// ---------------------------------------------------------------------------
// PAGOS (RF-16)
// ---------------------------------------------------------------------------

async function vistaPagos(ctx) {
  const [pagos, usuarios, citas, suscripciones, planes] = await Promise.all([
    leerColeccion('pago'), leerColeccion('usuario'), leerColeccion('cita'),
    leerColeccion('suscripcion'), leerColeccion('plan')
  ]);
  const mapaUsuarios = mapaPor(usuarios);
  const mapaPlanes = mapaPor(planes);

  $('#cabecera-acciones').innerHTML =
    `<button class="btn btn-sevetco" data-accion="nuevo"><img src="${icono('payments')}" alt="" class="icono-18">Registrar pago</button>`;

  pintar(`
    <div class="card">
      <div class="card-body">
        <h5 class="card-title mb-3">Pagos</h5>
        ${tabla(['Propietario', 'Concepto', 'Monto', 'Estado', 'Referencia', ''],
          pagos.map(pago => {
            const suscripcion = suscripciones.find(s => s.id === pago.id_suscripcion);
            const concepto = suscripcion
              ? 'Suscripcion: ' + (mapaPlanes.get(suscripcion.id_plan)?.nombre ?? '')
              : 'Cita';
            return `<tr>
            <td>${celdaPersona(mapaUsuarios, pago.id_propietario)}</td>
              <td>${esc(concepto)}</td>
              <td>${moneda(pago.monto)}</td>
              <td>${chip(pago.estado)}</td>
              <td class="text-secondary">${esc(pago.referencia_externa ?? '-')}</td>
              <td class="text-nowrap">
                ${botonAccion('editar', pago.id)}
                ${botonAccion('borrar', pago.id, 'delete', 'Eliminar')}
              </td>
            </tr>`;
          }))}
      </div>
    </div>
  `);

  document.querySelector('#cabecera-acciones [data-accion="nuevo"]').addEventListener('click', () => {
    abrirModal({
      titulo: 'Registrar pago',
      cuerpo: `<div class="row g-3">
        ${campo({ nombre: 'id_propietario', etiqueta: 'Propietario', requerido: true,
          opciones: [{ valor: '', texto: 'Seleccione...' },
            ...usuarios.filter(u => u.tipo_usuario === 'PROPIETARIO')
              .map(u => ({ valor: u.id, texto: u.nombre || u.correo }))] })}
        ${campo({ nombre: 'id_cita', etiqueta: 'Cita (opcional)',
          opciones: [{ valor: '', texto: 'Ninguna' },
            ...citas.map(c => ({ valor: c.id, texto: fechaHora(c.fecha_hora) }))] })}
        ${campo({ nombre: 'monto', etiqueta: 'Monto (COP)', tipo: 'number', requerido: true })}
        ${campo({ nombre: 'estado', etiqueta: 'Estado', valor: 'PENDIENTE',
          opciones: ['PENDIENTE', 'PAGADO', 'ANULADO'].map(e => ({ valor: e, texto: e })) })}
        ${campo({ nombre: 'referencia_externa', etiqueta: 'Referencia externa' })}
      </div>`,
      alGuardar: async () => {
        const datos = leerFormulario();
        if (!datos.id_propietario) return aviso('Seleccione el propietario.', true);
        if (!datos.id_cita) return aviso('Regla del modelo: el pago debe estar asociado a una cita o a una suscripcion.', true);

        await crear('pago', {
          id_propietario: datos.id_propietario,
          id_cita: datos.id_cita,
          id_suscripcion: null,
          monto: Number(datos.monto),
          estado: datos.estado,
          referencia_externa: datos.referencia_externa,
          fecha_pago: datos.estado === 'PAGADO' ? aTimestamp(new Date()) : null
        });

        cerrarModal();
        aviso('Pago registrado.');
        renderVista('pagos', ctx);
      }
    });
  });

  $('#vista').addEventListener('click', async evento => {
    const boton = evento.target.closest('[data-accion]');
    if (!boton) return;
    const pago = pagos.find(p => p.id === boton.dataset.id);
    if (!pago) return;

    if (boton.dataset.accion === 'borrar') {
      if (!confirm('¿Eliminar este pago?')) return;
      await eliminar('pago', pago.id);
      aviso('Pago eliminado.');
      renderVista('pagos', ctx);
      return;
    }

    abrirModal({
      titulo: 'Editar pago',
      cuerpo: `<div class="row g-3">
        <div class="col-12">
          <div class="celda-usuario">
            ${avatar(mapaUsuarios.get(pago.id_propietario)?.foto,
              nombreDeUsuario(mapaUsuarios, pago.id_propietario))}
            <span class="text-secondary small">
              ${esc(nombreDeUsuario(mapaUsuarios, pago.id_propietario))}
            </span>
          </div>
        </div>
        ${campo({ nombre: 'monto', etiqueta: 'Monto (COP)', tipo: 'number', valor: pago.monto ?? '', requerido: true })}
        ${campo({ nombre: 'estado', etiqueta: 'Estado', valor: pago.estado,
          opciones: ['PENDIENTE', 'PAGADO', 'ANULADO'].map(e => ({ valor: e, texto: e })) })}
        ${campo({ nombre: 'referencia_externa', etiqueta: 'Referencia externa', valor: pago.referencia_externa ?? '' })}
      </div>`,
      alGuardar: async () => {
        const datos = leerFormulario();
        await actualizar('pago', pago.id, {
          monto: Number(datos.monto),
          estado: datos.estado,
          referencia_externa: datos.referencia_externa,
          fecha_pago: datos.estado === 'PAGADO'
            ? (pago.fecha_pago ?? aTimestamp(new Date()))
            : null
        });
        cerrarModal();
        aviso('Pago actualizado.');
        renderVista('pagos', ctx);
      }
    });
  });
}

// ---------------------------------------------------------------------------
// RUTAS Y DESPLAZAMIENTOS (RF-17) con ubicacion
// ---------------------------------------------------------------------------

async function vistaRutas(ctx) {
  const esAdminRutas = ctx.perfil.tipo_usuario === 'ADMIN';
  const [rutas, citas, mascotas] = await Promise.all([
    leerColeccion('ruta_desplazamiento'),
    esAdminRutas ? leerColeccion('cita') : leerDonde('cita', 'id_proveedor', ctx.perfil.id),
    leerColeccion('mascota')
  ]);
  const mapaCitas = mapaPor(citas);
  const mapaMascotas = mapaPor(mascotas);

  const esAdmin = esAdminRutas;
  const disponibles = citas;

  $('#cabecera-acciones').innerHTML =
    `<button class="btn btn-sevetco" data-accion="nuevo"><img src="${icono('location_on')}" alt="" class="icono-18">Registrar ruta</button>`;

  pintar(`
    <div class="card">
      <div class="card-body">
        <h5 class="card-title mb-3">Rutas y desplazamientos</h5>
        ${tabla(['Cita', 'Origen', 'Destino', 'Distancia', 'Estado', ''],
          rutas.map(ruta => {
            const cita = mapaCitas.get(ruta.id_cita);
            return `<tr>
              <td>
                <div class="celda-usuario">
                  ${fotoMascota(mapaMascotas.get(cita?.id_mascota)?.foto,
                    mapaMascotas.get(cita?.id_mascota)?.nombre ?? '')}
                  <span>${fechaHora(cita?.fecha_hora)}${cita
                    ? ' - ' + esc(mapaMascotas.get(cita.id_mascota)?.nombre ?? '') : ''}</span>
                </div>
              </td>
              <td>${esc(ruta.origen ?? '-')}</td>
              <td>${esc(ruta.destino ?? '-')}</td>
              <td>${ruta.distancia_km ?? '-'} km</td>
              <td>${chip(ruta.estado)}</td>
              <td class="text-nowrap">
                ${botonAccion('editar', ruta.id)}
                ${botonAccion('borrar', ruta.id, 'delete', 'Eliminar')}
              </td>
            </tr>`;
          }))}
      </div>
    </div>
  `);

  const abrirFormulario = ruta => {
    if (!ruta && !disponibles.length) return aviso('No hay citas disponibles.', true);
    const citaDeLaRuta = ruta ? mapaCitas.get(ruta.id_cita) : null;

    abrirModal({
      titulo: ruta ? 'Editar ruta' : 'Registrar ruta',
      cuerpo: `<div class="row g-3">
        ${ruta
          ? `<div class="col-12">
              <div class="celda-usuario">
                ${fotoMascota(mapaMascotas.get(citaDeLaRuta?.id_mascota)?.foto,
                  mapaMascotas.get(citaDeLaRuta?.id_mascota)?.nombre ?? '')}
                <span class="text-secondary small">Cita: ${fechaHora(citaDeLaRuta?.fecha_hora)}
                  - ${esc(mapaMascotas.get(citaDeLaRuta?.id_mascota)?.nombre ?? '')}</span>
              </div>
            </div>`
          : campo({ nombre: 'id_cita', etiqueta: 'Cita', requerido: true,
              opciones: [{ valor: '', texto: 'Seleccione...' },
                ...disponibles.map(cita => ({
                  valor: cita.id,
                  texto: `${fechaHora(cita.fecha_hora)} - ${mapaMascotas.get(cita.id_mascota)?.nombre ?? ''}`
                }))] })}
        ${campo({ nombre: 'origen', etiqueta: 'Origen', valor: ruta?.origen ?? '' })}
        ${campo({ nombre: 'destino', etiqueta: 'Destino (direccion del domicilio)', valor: ruta?.destino ?? '' })}
        <div class="col-12">
          <div class="d-flex gap-2">
            <button type="button" class="btn btn-outline-primary btn-sm" id="btn-ubicacion">
              <img src="${icono('my_location')}" alt="" class="icono-16"> Mi ubicacion
            </button>
            <button type="button" class="btn btn-outline-primary btn-sm" id="btn-mapa">
              <img src="${icono('location_on')}" alt="" class="icono-16"> Ver mapa
            </button>
          </div>
          <div id="mapa-ruta" class="mapa-ruta"></div>
          <div class="form-text" id="mapa-ruta-nota">
            Toca el mapa o arrastra el pin para ubicar el domicilio.
          </div>
        </div>
        ${campo({ nombre: 'distancia_km', etiqueta: 'Distancia (km)', tipo: 'number', paso: '0.1',
          valor: ruta?.distancia_km ?? '' })}
        ${campo({ nombre: 'estado', etiqueta: 'Estado', valor: ruta?.estado ?? 'PENDIENTE',
          opciones: ['PENDIENTE', 'EN_RUTA', 'COMPLETADA'].map(e => ({ valor: e, texto: e })) })}
      </div>`,
      alGuardar: async () => {
        const datos = leerFormulario();
        const idCita = ruta?.id_cita ?? datos.id_cita;
        if (!idCita) return aviso('Seleccione la cita.', true);
        const cita = mapaCitas.get(idCita);

        const fila = {
          id_cita: idCita,
          id_propietario: cita?.id_propietario ?? null,
          id_proveedor: cita?.id_proveedor ?? null,
          origen: datos.origen,
          destino: datos.destino,
          lat: puntoRuta?.lat ?? null,
          lng: puntoRuta?.lng ?? null,
          lat_origen: puntoOrigen?.lat ?? ruta?.lat_origen ?? null,
          lng_origen: puntoOrigen?.lng ?? ruta?.lng_origen ?? null,
          distancia_km: datos.distancia_km ? Number(datos.distancia_km) : null,
          estado: datos.estado
        };

        if (ruta) await actualizar('ruta_desplazamiento', ruta.id, fila);
        else await crearConId('ruta_desplazamiento', idCita, fila);

        cerrarModal();
        aviso(ruta ? 'Ruta actualizada.' : 'Ruta registrada.');
        renderVista('rutas', ctx);
      }
    });

    puntoRuta = (ruta?.lat != null && ruta?.lng != null)
      ? { lat: ruta.lat, lng: ruta.lng }
      : null;
    puntoOrigen = (ruta?.lat_origen != null && ruta?.lng_origen != null)
      ? { lat: ruta.lat_origen, lng: ruta.lng_origen }
      : null;

    const contenedorMapa = $('#modal-cuerpo #mapa-ruta');
    const notaMapa = $('#modal-cuerpo #mapa-ruta-nota');
    const campoDestino = $('#modal-cuerpo [name="destino"]');
    let mapaGoogle = null;
    let marcadorGoogle = null;
    let lineaGoogle = null;
    let mapaLeaflet = null;
    let pinLeaflet = null;

    // Al elegir un punto (se mueve el pin o se toca el mapa) se guarda y se
    // busca la direccion para escribirla en el campo del domicilio.
    async function aplicarPunto(lat, lng) {
      puntoRuta = { lat, lng };
      notaMapa.textContent = 'Buscando la direccion...';
      const texto = await direccionDe(lat, lng);
      campoDestino.value = texto ?? `${lat.toFixed(6)}, ${lng.toFixed(6)}`;
      notaMapa.textContent = 'Domicilio ubicado en el mapa.';
    }

    // Google Maps primero; si no carga, se dibuja con OpenStreetMap (Leaflet).
    const dibujarGoogle = base => {
      const google = window.google.maps;
      if (!mapaGoogle) {
        mapaGoogle = new google.Map(contenedorMapa, {
          center: base,
          zoom: 16,
          mapTypeControl: false,
          streetViewControl: false,
          fullscreenControl: false
        });

        if (puntoOrigen) {
          new google.Marker({ position: puntoOrigen, map: mapaGoogle, title: 'Sale el veterinario' });
        }

        marcadorGoogle = new google.Marker({
          position: base,
          map: mapaGoogle,
          draggable: true,
          title: 'Domicilio del servicio'
        });
        marcadorGoogle.addListener('dragend', () => {
          const punto = marcadorGoogle.getPosition();
          aplicarPunto(punto.lat(), punto.lng());
        });
        mapaGoogle.addListener('click', evento => {
          marcadorGoogle.setPosition(evento.latLng);
          aplicarPunto(evento.latLng.lat(), evento.latLng.lng());
        });

        if (puntoOrigen) {
          lineaGoogle = new google.Polyline({
            path: [puntoOrigen, base],
            strokeColor: '#1F3864',
            strokeWeight: 5,
            map: mapaGoogle
          });
        }
      } else {
        mapaGoogle.setCenter(base);
        marcadorGoogle.setPosition(base);
      }
    };

    const dibujarLeaflet = base => {
      if (!window.L) {
        notaMapa.textContent = 'No se pudo cargar el mapa.';
        return;
      }
      if (!mapaLeaflet) {
        mapaLeaflet = window.L.map('mapa-ruta').setView([base.lat, base.lng], 16);
        window.L.tileLayer('https://tile.openstreetmap.org/{z}/{x}/{y}.png', {
          maxZoom: 19,
          attribution: 'OpenStreetMap'
        }).addTo(mapaLeaflet);
        if (puntoOrigen) {
          window.L.marker([puntoOrigen.lat, puntoOrigen.lng]).addTo(mapaLeaflet)
            .bindPopup('Sale el veterinario');
        }
        pinLeaflet = window.L.marker([base.lat, base.lng], { draggable: true })
          .addTo(mapaLeaflet).bindPopup('Domicilio del servicio');
        pinLeaflet.on('dragend', () => {
          const punto = pinLeaflet.getLatLng();
          aplicarPunto(punto.lat, punto.lng);
        });
        if (puntoOrigen) {
          window.L.polyline(
            [[puntoOrigen.lat, puntoOrigen.lng], [base.lat, base.lng]],
            { color: '#1F3864', weight: 5 }
          ).addTo(mapaLeaflet);
        }
        mapaLeaflet.on('click', evento => {
          pinLeaflet.setLatLng(evento.latlng);
          aplicarPunto(evento.latlng.lat, evento.latlng.lng);
        });
      } else {
        mapaLeaflet.setView([base.lat, base.lng], 16);
        pinLeaflet.setLatLng([base.lat, base.lng]);
      }
      mapaLeaflet.invalidateSize();
    };

    const dibujarMapa = async () => {
      const escrito = leerCoordenadas(campoDestino.value);
      const base = puntoRuta ?? (escrito ? { lat: escrito[0], lng: escrito[1] } : null)
        ?? puntoOrigen ?? { lat: 4.6097, lng: -74.0817 };

      contenedorMapa.classList.add('visible');
      notaMapa.textContent = 'Cargando el mapa...';

      await cargarGoogleMaps();

      if (window.google?.maps?.Map) {
        dibujarGoogle(base);
      } else {
        dibujarLeaflet(base);
      }
      notaMapa.textContent = 'Toca el mapa o arrastra el pin para ubicar el domicilio.';
    };

    $('#modal-cuerpo #btn-mapa').addEventListener('click', dibujarMapa);

    // La ruta se dibuja de una al abrir el formulario, igual que en la app.
    $('#modal').addEventListener('shown.bs.modal', () => {
      dibujarMapa();
    }, { once: true });

    // Al cerrar el formulario se suelta el mapa para no dejar recursos vivos.
    $('#modal').addEventListener('hidden.bs.modal', () => {
      if (mapaLeaflet) {
        mapaLeaflet.remove();
        mapaLeaflet = null;
        pinLeaflet = null;
      }
      mapaGoogle = null;
      marcadorGoogle = null;
      lineaGoogle = null;
    }, { once: true });

    $('#modal-cuerpo #btn-ubicacion').addEventListener('click', () => {
      if (!navigator.geolocation) {
        return aviso('El navegador no permite la ubicacion.', true);
      }
      notaMapa.textContent = 'Buscando tu ubicacion...';
      navigator.geolocation.getCurrentPosition(
        async posicion => {
          const { latitude, longitude } = posicion.coords;
          puntoOrigen = { lat: latitude, lng: longitude };
          const texto = await direccionDe(latitude, longitude);
          $('#modal-cuerpo [name="origen"]').value =
            texto ?? `${latitude.toFixed(6)}, ${longitude.toFixed(6)}`;
          notaMapa.textContent = 'Ubicacion tomada como origen de la ruta.';
          aviso('Ubicacion tomada como origen de la ruta.');
        },
        error => aviso(mensajeUbicacion(error), true),
        { enableHighAccuracy: true, timeout: 15000 }
      );
    });

    // Al elegir la cita se cargan los datos de la ruta que ya existe
    // (la que creo la app movil al agendar).
    const selectorCita = $('#modal-cuerpo [name="id_cita"]');
    selectorCita?.addEventListener('change', () => {
      const existente = rutas.find(r => r.id_cita === selectorCita.value);
      if (!existente) return;
      puntoRuta = (existente.lat != null && existente.lng != null)
        ? { lat: existente.lat, lng: existente.lng }
        : null;
      $('#modal-cuerpo [name="origen"]').value = existente.origen ?? '';
      $('#modal-cuerpo [name="destino"]').value = existente.destino ?? '';
      $('#modal-cuerpo [name="distancia_km"]').value = existente.distancia_km ?? '';
      $('#modal-cuerpo [name="estado"]').value = existente.estado ?? 'PENDIENTE';
    });
  };

  document.querySelector('#cabecera-acciones [data-accion="nuevo"]')
    ?.addEventListener('click', () => abrirFormulario(null));

  $('#vista').addEventListener('click', evento => {
    const boton = evento.target.closest('[data-accion]');
    if (!boton) return;
    if (boton.dataset.accion === 'borrar') {
      if (!confirm('¿Eliminar esta ruta?')) return;
      eliminar('ruta_desplazamiento', boton.dataset.id)
        .then(() => {
          aviso('Ruta eliminada.');
          renderVista('rutas', ctx);
        })
        .catch(error => aviso(error.message, true));
      return;
    }
    abrirFormulario(rutas.find(r => r.id === boton.dataset.id));
  });
}

// ---------------------------------------------------------------------------
// USUARIOS
// ---------------------------------------------------------------------------

async function vistaUsuarios(ctx) {
  const usuarios = await leerColeccion('usuario');

  $('#cabecera-acciones').innerHTML = '';

  pintar(`
    <div class="card">
      <div class="card-body">
        <h5 class="card-title mb-3">Usuarios</h5>
        <p class="text-secondary small">
          Las contrasenas no se pueden ver ni editar: Firebase las guarda cifradas y
          ninguna aplicacion puede leerlas. Si un usuario la olvido, usa el boton
          de la llave para enviarle un correo y que la cambie el mismo.
        </p>
        ${tabla(['Nombre', 'Correo', 'Documento', 'Tipo', 'Estado', '', ''],
          usuarios.map(usuario => `<tr>
            <td>
              <div class="celda-usuario">
                ${avatar(usuario.foto, usuario.nombre ?? '')}
                <span>${esc(usuario.nombre ?? '-')}</span>
              </div>
            </td>
            <td>${esc(usuario.correo ?? '-')}</td>
            <td>${esc(usuario.numero_documento ?? '-')}</td>
            <td>${chip(usuario.tipo_usuario)}</td>
            <td>${chip(usuario.estado)}</td>
            <td>${botonAccion('editar', usuario.id)}</td>
            <td>${botonClave(usuario.correo ?? '')}</td>
            <td>${botonAccion('borrar', usuario.id, 'delete', 'Eliminar')}</td>
          </tr>`))}
      </div>
    </div>
  `);

  $('#vista').addEventListener('click', async evento => {
    const botonClavePresionado = evento.target.closest('[data-accion="clave"]');
    if (botonClavePresionado) {
      const correo = botonClavePresionado.dataset.correo;
      if (!correo) return aviso('Ese usuario no tiene correo registrado.', true);
      if (!confirm(`¿Enviar a ${correo} el correo para restablecer su contrasena?`)) return;
      recuperarClave(correo)
        .then(() => aviso('Correo enviado a ' + correo + '.'))
        .catch(error => aviso(error.message, true));
      return;
    }

    const botonBorrar = evento.target.closest('[data-accion="borrar"]');
    if (botonBorrar) {
      const usuario = usuarios.find(u => u.id === botonBorrar.dataset.id);
      const nombre = usuario?.nombre || usuario?.correo || 'este usuario';

      aviso('Revisando los datos relacionados...');
      const relacionados = await datosDelUsuario(botonBorrar.dataset.id);
      const resumen = relacionados.length
        ? `Se borraran tambien ${relacionados.length} registros suyos:\n`
          + 'mascotas, citas, pagos, rutas, tratamientos, trazabilidad y agenda.'
        : 'No tiene registros relacionados.';

      if (!confirm(`¿Eliminar a ${nombre}?\n\n${resumen}\n\n`
        + 'La cuenta de acceso (Google o correo) NO se elimina, solo los datos'
        + ' de la plataforma.\n\nEsta accion no se puede deshacer.')) {
        return;
      }

      for (const documento of relacionados) {
        await eliminar(documento.coleccion, documento.id);
      }
      if (await leerDocumento('proveedor', botonBorrar.dataset.id)) {
        await eliminar('proveedor', botonBorrar.dataset.id);
      }
      await eliminar('usuario', botonBorrar.dataset.id);

      aviso(`Usuario eliminado junto con ${relacionados.length} registros relacionados.`);
      renderVista('usuarios', ctx);
      return;
    }

    const boton = evento.target.closest('[data-accion="editar"]');
    if (!boton) return;
    const usuario = usuarios.find(u => u.id === boton.dataset.id);
    imagenEnEdicion = usuario.foto ?? '';

    abrirModal({
      titulo: 'Editar usuario',
      cuerpo: `<div class="row g-3">
        ${campo({ nombre: 'nombre', etiqueta: 'Nombre', valor: usuario.nombre ?? '' })}
        ${campo({ nombre: 'numero_documento', etiqueta: 'Numero de documento', valor: usuario.numero_documento ?? '' })}
        ${campo({ nombre: 'tipo_usuario', etiqueta: 'Tipo de usuario', valor: usuario.tipo_usuario,
          opciones: ['PROPIETARIO', 'PROVEEDOR', 'ADMIN'].map(t => ({ valor: t, texto: t })) })}
        ${campo({ nombre: 'estado', etiqueta: 'Estado', valor: usuario.estado,
          opciones: ['ACTIVO', 'INACTIVO'].map(t => ({ valor: t, texto: t })) })}
        ${bloqueImagen('foto', 'Foto del usuario',
          'Si no subes ninguna se usa la foto de la cuenta de Google y, si no tiene, el icono generico.',
          84, 84)}
      </div>`,
      alGuardar: async () => {
        const datos = leerFormulario();
        datos.foto = imagenEnEdicion || '';
        await actualizar('usuario', usuario.id, datos);
        cerrarModal();
        aviso('Usuario actualizado.');
        renderVista('usuarios', ctx);
      }
    });
    conectarImagen('foto');
  });
}

// ---------------------------------------------------------------------------
// MIS CITAS (proveedor) - RF-10
// ---------------------------------------------------------------------------

async function vistaMisCitas(ctx) {
  // Las reglas solo dejan ver al proveedor las citas que tiene asignadas.
  const [citas, mascotas, servicios, usuarios] = await Promise.all([
    leerDonde('cita', 'id_proveedor', ctx.perfil.id),
    leerColeccion('mascota'), leerColeccion('servicio'), leerColeccion('usuario')
  ]);
  const mapaMascotas = mapaPor(mascotas);
  const mapaServicios = mapaPor(servicios);
  const mapaUsuarios = mapaPor(usuarios);

  const mias = citas;

  $('#cabecera-acciones').innerHTML = '';

  pintar(`
    <div class="card">
      <div class="card-body">
        <h5 class="card-title mb-3">Citas asignadas</h5>
        ${tabla(['Fecha', 'Mascota', 'Especie / raza', 'Servicio', 'Propietario', 'Estado', ''],
          mias.map(cita => {
            const mascota = mapaMascotas.get(cita.id_mascota);
            return `<tr>
              <td>${fechaHora(cita.fecha_hora)}</td>
              <td>${celdaMascota(mapaMascotas, cita.id_mascota)}</td>
              <td>${esc([mascota?.especie, mascota?.raza].filter(Boolean).join(' / ') || '-')}</td>
              <td>${esc(mapaServicios.get(cita.id_servicio)?.nombre ?? '-')}</td>
              <td>${celdaPersona(mapaUsuarios, cita.id_propietario)}</td>
              <td>${chip(cita.estado)}</td>
              <td>${botonAccion('estado', cita.id, 'task_alt', 'Cambiar estado')}</td>
            </tr>`;
          }))}
      </div>
    </div>
  `);

  $('#vista').addEventListener('click', evento => {
    const boton = evento.target.closest('[data-accion="estado"]');
    if (!boton) return;
    const cita = mias.find(c => c.id === boton.dataset.id);

    abrirModal({
      titulo: 'Estado de la cita',
      cuerpo: `<div class="row g-3">${campo({ nombre: 'estado', etiqueta: 'Estado', valor: cita.estado,
        opciones: ['ASIGNADA', 'EN_PROCESO', 'FINALIZADA', 'CANCELADA'].map(x => ({ valor: x, texto: x })) })}</div>`,
      alGuardar: async () => {
        const datos = leerFormulario();
        await actualizar('cita', cita.id, { estado: datos.estado });
        if (datos.estado !== cita.estado) {
          await registrarTrazabilidad(cita, datos.estado, ctx.perfil.id);
        }
        cerrarModal();
        aviso('Estado actualizado.');
        renderVista('mis-citas', ctx);
      }
    });
  });
}

// ---------------------------------------------------------------------------
// Enrutador
// ---------------------------------------------------------------------------

// ---------------------------------------------------------------------------
// MI CUENTA (proveedor): sus propios datos.
//
// La foto de seguridad NO se puede cambiar desde el panel: se toma desde la
// aplicacion movil, donde el sistema obliga a tomarla con la camara.
// ---------------------------------------------------------------------------

async function vistaMiCuenta(ctx) {
  const [usuario, profesional] = await Promise.all([
    leerDocumento('usuario', ctx.perfil.id),
    leerDocumento('proveedor', ctx.perfil.id)
  ]);

  $('#cabecera-acciones').innerHTML = '';

  pintar(`
    <div class="row g-3">
      <div class="col-lg-4">
        <div class="card h-100">
          <div class="card-body">
            <div class="celda-usuario">
              ${avatar(usuario?.foto, usuario?.nombre ?? '')}
              <div>
                <div class="fw-semibold">${esc(usuario?.nombre ?? '-')}</div>
                <div class="text-secondary small">${esc(usuario?.correo ?? '-')}</div>
                <div class="small fw-semibold" style="color:#1f3864">
                  ${esc(usuario?.tipo_usuario ?? '-')}
                </div>
              </div>
            </div>
            <p class="text-secondary small mt-3 mb-0">
              La foto de seguridad se toma desde la aplicacion movil. Desde este
              panel no se puede cambiar.
            </p>
          </div>
        </div>
      </div>

      <div class="col-lg-8">
        <div class="card h-100">
          <div class="card-body">
            <h5 class="card-title mb-3">Datos de la cuenta</h5>
            <div class="row g-3">
              ${campo({ nombre: 'nombre', etiqueta: 'Nombre completo', valor: usuario?.nombre ?? '' })}
              ${campo({ nombre: 'numero_documento', etiqueta: 'Numero de documento',
                valor: usuario?.numero_documento ?? '' })}
              ${campo({ nombre: 'tarjeta_profesional', etiqueta: 'Tarjeta profesional',
                valor: profesional?.tarjeta_profesional ?? '' })}
              ${campo({ nombre: 'especialidad', etiqueta: 'Especialidad',
                valor: profesional?.especialidad ?? '' })}
            </div>
            <button class="btn btn-sevetco mt-3" id="btn-guardar-cuenta">
              Guardar cambios
            </button>
          </div>
        </div>
      </div>

      <div class="col-12">
        <div class="card">
          <div class="card-body">
            <h5 class="card-title mb-3">Cambiar contrasena</h5>
            <div class="row g-3">
              ${campo({ nombre: 'clave_actual', etiqueta: 'Contrasena actual', tipo: 'password' })}
              ${campo({ nombre: 'clave_nueva', etiqueta: 'Contrasena nueva', tipo: 'password' })}
              ${campo({ nombre: 'clave_repetir', etiqueta: 'Repetir la contrasena nueva', tipo: 'password' })}
            </div>
            <button class="btn btn-outline-primary mt-3" id="btn-guardar-clave">
              Cambiar contrasena
            </button>
          </div>
        </div>
      </div>
    </div>
  `);

  const valor = nombre => $('#vista [name="' + nombre + '"]')?.value.trim() ?? '';

  $('#vista #btn-guardar-cuenta').addEventListener('click', async () => {
    const nombre = valor('nombre');
    if (!nombre) return aviso('Escribe tu nombre completo.', true);

    try {
      await actualizar('usuario', ctx.perfil.id, {
        nombre,
        numero_documento: valor('numero_documento') || null
      });
      await crearConId('proveedor', ctx.perfil.id, {
        id_usuario: ctx.perfil.id,
        tarjeta_profesional: valor('tarjeta_profesional') || null,
        especialidad: valor('especialidad') || null
      });
      aviso('Datos actualizados.');
      renderVista('mi-cuenta', ctx);
    } catch (error) {
      aviso(error.message, true);
    }
  });

  $('#vista #btn-guardar-clave').addEventListener('click', async () => {
    const actual = $('#vista [name="clave_actual"]').value;
    const nueva = $('#vista [name="clave_nueva"]').value;
    const repetir = $('#vista [name="clave_repetir"]').value;

    if (!actual) return aviso('Escribe tu contrasena actual.', true);
    if (nueva.length < 6) {
      return aviso('La contrasena nueva debe tener al menos 6 caracteres.', true);
    }
    if (nueva !== repetir) return aviso('Las contrasenas nuevas no coinciden.', true);

    try {
      await cambiarClave(actual, nueva);
      aviso('Contrasena actualizada.');
      renderVista('mi-cuenta', ctx);
    } catch (error) {
      aviso(mensajeClave(error), true);
    }
  });
}

/** Mensaje claro cuando no se puede cambiar la contrasena. */
function mensajeClave(error) {
  const codigo = String(error?.code ?? '');
  if (codigo.includes('wrong-password') || codigo.includes('invalid-credential')) {
    return 'La contrasena actual no es correcta.';
  }
  if (codigo.includes('weak-password')) {
    return 'La contrasena nueva es muy facil; usa al menos 6 caracteres.';
  }
  if (codigo.includes('requires-recent-login')) {
    return 'Por seguridad, cierra sesion y vuelve a entrar antes de cambiarla.';
  }
  return error?.message ?? 'No se pudo cambiar la contrasena.';
}

const VISTAS = {
  panel: vistaPanel,
  eventos: vistaEventos,
  promociones: vistaPromociones,
  mascotas: vistaMascotas,
  suscripciones: vistaSuscripciones,
  citas: vistaCitas,
  proveedores: vistaProveedores,
  servicios: vistaServicios,
  planes: vistaPlanes,
  atenciones: vistaAtenciones,
  trazabilidad: vistaCitas,
  pagos: vistaPagos,
  rutas: vistaRutas,
  usuarios: vistaUsuarios,
  'mis-citas': vistaMisCitas,
  'mi-cuenta': vistaMiCuenta
};

export async function renderVista(id, ctx) {
  const vista = VISTAS[id] ?? vistaPanel;
  $('#titulo-vista').textContent = tituloVista(id);
  $('#cabecera-acciones').innerHTML = '';

  const contenedor = $('#vista');
  contenedor.replaceWith(contenedor.cloneNode(false));

  try {
    await vista(ctx);
  } catch (error) {
    pintar(`<div class="card"><div class="card-body">
      <p class="text-danger mb-0">${esc(error.message)}</p></div></div>`);
  }
}

