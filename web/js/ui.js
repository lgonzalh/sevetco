// ---------------------------------------------------------------------------
// Utilidades de interfaz: iconos, avisos, modales y formato.
// ---------------------------------------------------------------------------

import { ICONOS } from './firebase-config.js';

export const $  = (sel, ctx = document) => ctx.querySelector(sel);
export const $$ = (sel, ctx = document) => Array.from(ctx.querySelectorAll(sel));

/** Ruta de un icono. carpeta: 'shared' | 'web' | 'mobile'. */
export function icono(nombre, carpeta = 'shared') {
  return `${ICONOS}/${carpeta}/${nombre}.png`;
}

/** Escapa texto para insertarlo en HTML. */
export function esc(valor) {
  if (valor === null || valor === undefined) return '';
  return String(valor)
    .replaceAll('&', '&amp;')
    .replaceAll('<', '&lt;')
    .replaceAll('>', '&gt;')
    .replaceAll('"', '&quot;')
    .replaceAll("'", '&#39;');
}

/**
 * Foto del usuario para las tablas.
 * Si la ficha no tiene foto se usa el icono generico de cuenta.
 */
export function avatar(foto, nombre = '') {
  if (foto) {
    return `<img class="avatar-usuario" src="${esc(foto)}" alt="${esc(nombre)}">`;
  }
  return `<img class="avatar-usuario avatar-vacio" src="${icono('account_circle')}" alt="">`;
}

/**
 * Foto de la mascota para las tablas.
 * Si no tiene foto se usa el icono de huella.
 */
export function fotoMascota(foto, nombre = '') {
  if (foto) {
    return `<img class="avatar-mascota" src="${esc(foto)}" alt="${esc(nombre)}">`;
  }
  return `<img class="avatar-mascota avatar-vacio" src="${icono('pets')}" alt="">`;
}

/** Formatea una fecha ISO como dd/mm/aaaa. */
export function fecha(iso) {
  if (!iso) return '-';
  const d = aObjetoFecha(iso);
  if (Number.isNaN(d.getTime())) return '-';
  return d.toLocaleDateString('es-CO');
}

/** Formatea una fecha ISO como dd/mm/aaaa hh:mm. */
export function fechaHora(iso) {
  if (!iso) return '-';
  const d = aObjetoFecha(iso);
  if (Number.isNaN(d.getTime())) return '-';
  return d.toLocaleString('es-CO', {
    day: '2-digit', month: '2-digit', year: 'numeric',
    hour: '2-digit', minute: '2-digit'
  });
}

/** Acepta Timestamp de Firestore, Date, numero o texto. */
function aObjetoFecha(valor) {
  if (valor && typeof valor.toDate === 'function') return valor.toDate();
  return valor instanceof Date ? valor : new Date(valor);
}

/** Formatea un monto en pesos colombianos. */
export function moneda(valor) {
  const n = Number(valor || 0);
  return n.toLocaleString('es-CO', { style: 'currency', currency: 'COP', maximumFractionDigits: 0 });
}

const CLASE_ESTADO = {
  ACTIVO: 'verde', ACTIVA: 'verde', PAGADO: 'verde', FINALIZADA: 'verde', COMPLETADA: 'verde',
  PENDIENTE: 'naranja', EN_PROCESO: 'naranja', EN_RUTA: 'naranja', ASIGNADA: '',
  INACTIVO: 'rojo', INACTIVA: 'rojo', CANCELADA: 'rojo', CANCELADO: 'rojo', VENCIDA: 'rojo',
  ANULADO: 'rojo'
};

/** Devuelve el HTML de una etiqueta de estado. */
export function chip(estado) {
  const clase = CLASE_ESTADO[estado] ?? '';
  return `<span class="chip ${clase ? 'chip--' + clase : ''}">${esc(estado ?? '-')}</span>`;
}

let temporizadorAviso = null;

/** Muestra un aviso flotante. */
export function aviso(mensaje, esError = false) {
  const caja = $('#aviso');
  caja.textContent = mensaje;
  caja.classList.toggle('aviso--error', esError);
  caja.classList.remove('d-none');
  clearTimeout(temporizadorAviso);
  temporizadorAviso = setTimeout(() => caja.classList.add('d-none'), 4000);
}

/**
 * Abre el modal.
 * @param {{titulo:string, cuerpo:string, textoBoton?:string, alGuardar:Function}} opciones
 */
let instanciaModal = null;

export function abrirModal({ titulo, cuerpo, textoBoton = 'Guardar', alGuardar }) {
  $('#modal-titulo').textContent = titulo;
  $('#modal-cuerpo').innerHTML = cuerpo;

  const pie = $('#modal-pie');
  pie.innerHTML = '';

  const cancelar = document.createElement('button');
  cancelar.className = 'btn btn-outline-secondary';
  cancelar.textContent = 'Cancelar';
  cancelar.addEventListener('click', cerrarModal);

  const guardar = document.createElement('button');
  guardar.className = 'btn btn-sevetco';
  guardar.textContent = textoBoton;
  guardar.addEventListener('click', async () => {
    guardar.disabled = true;
    try {
      await alGuardar();
    } finally {
      guardar.disabled = false;
    }
  });

  pie.append(cancelar, guardar);

  if (!instanciaModal) {
    instanciaModal = new window.bootstrap.Modal(document.getElementById('modal'));
  }
  instanciaModal.show();
}

export function cerrarModal() {
  if (instanciaModal) instanciaModal.hide();
  $('#modal-cuerpo').innerHTML = '';
  $('#modal-pie').innerHTML = '';
}

/** Devuelve el HTML de un campo de formulario. */
export function campo({ nombre, etiqueta, tipo = 'text', valor = '', requerido = false, opciones = null, paso = null }) {
  const id = `campo-${nombre}`;
  if (opciones) {
    const items = opciones
      .map(o => `<option value="${esc(o.valor)}" `
        + `${String(o.valor) === String(valor ?? '') ? 'selected' : ''} `
        + `${o.deshabilitado ? 'disabled' : ''}>${esc(o.texto)}</option>`)
      .join('');
    return `<div class="campo"><label for="${id}">${esc(etiqueta)}</label>
      <select id="${id}" name="${nombre}" class="form-select" ${requerido ? 'required' : ''}>${items}</select></div>`;
  }
  if (tipo === 'textarea') {
    return `<div class="campo"><label for="${id}">${esc(etiqueta)}</label>
      <textarea id="${id}" name="${nombre}" class="form-control" rows="3" ${requerido ? 'required' : ''}>${esc(valor)}</textarea></div>`;
  }
  return `<div class="campo"><label for="${id}">${esc(etiqueta)}</label>
    <input id="${id}" name="${nombre}" class="form-control" type="${tipo}" value="${esc(valor)}"
      ${paso ? `step="${paso}"` : ''} ${requerido ? 'required' : ''}></div>`;
}

/** Lee los valores de un formulario dentro del modal. */
export function leerFormulario() {
  const datos = {};
  $$('#modal-cuerpo [name]').forEach(campoEl => {
    datos[campoEl.name] = campoEl.value.trim() === '' ? null : campoEl.value.trim();
  });
  return datos;
}

