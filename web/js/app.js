// ---------------------------------------------------------------------------
// Arranque del panel: inicio de sesion (Google o correo) y navegacion.
// ---------------------------------------------------------------------------

import {
  configurado, observarSesion, entrarConGoogle, entrarConCorreo,
  registrarConCorreo, salir, miPerfil, recuperarClave
} from './datos.js';
import { $, icono, aviso } from './ui.js';
import { menuPara, renderVista } from './views.js';

const ctx = { perfil: null };
let modo = 'ingresar';

function mostrarLogin(mensaje = '') {
  $('#app').classList.add('d-none');
  $('#pantalla-login').classList.remove('d-none');
  $('#login-mensaje').textContent = mensaje;
}

function mostrarApp() {
  $('#pantalla-login').classList.add('d-none');
  $('#app').classList.remove('d-none');
}

function construirMenu() {
  const items = menuPara(ctx.perfil.tipo_usuario);
  const nav = $('#menu-nav');
  nav.innerHTML = '';

  items.forEach(item => {
    const boton = document.createElement('button');
    boton.dataset.vista = item.id;
    boton.innerHTML = `<img src="${icono(item.icono, item.carpeta)}" alt="">${item.titulo}`;
    boton.addEventListener('click', () => { window.location.hash = item.id; });
    nav.appendChild(boton);
  });
}

function vistaInicial() {
  return menuPara(ctx.perfil.tipo_usuario)[0].id;
}

function marcarMenuActivo() {
  const vista = window.location.hash.replace('#', '') || vistaInicial();
  document.querySelectorAll('#menu-nav button').forEach(boton => {
    boton.classList.toggle('activo', boton.dataset.vista === vista);
  });
}

async function navegar() {
  if (!ctx.perfil) return;
  const disponibles = menuPara(ctx.perfil.tipo_usuario).map(m => m.id);
  let vista = window.location.hash.replace('#', '') || vistaInicial();
  if (!disponibles.includes(vista)) vista = vistaInicial();

  marcarMenuActivo();
  await renderVista(vista, ctx);
}

window.addEventListener('hashchange', navegar);

async function entrarApp() {
  ctx.perfil = await miPerfil();

  if (!ctx.perfil) {
    mostrarLogin('No se pudo leer el perfil del usuario.');
    return;
  }

  mostrarApp();
  $('#usuario-nombre').textContent = ctx.perfil.nombre || ctx.perfil.correo || '-';
  $('#usuario-rol').textContent = ctx.perfil.tipo_usuario || '-';

  // Si la cuenta tiene foto (por ejemplo de Google) se muestra en el bloque
  // del usuario; si no, queda el icono generico.
  const fotoUsuario = $('#usuario-foto');
  if (ctx.perfil.foto) {
    fotoUsuario.src = ctx.perfil.foto;
    fotoUsuario.classList.add('foto-real');
  }

  if (ctx.perfil.tipo_usuario === 'PROPIETARIO') {
    $('#menu-nav').innerHTML = '';
    $('#cabecera-acciones').innerHTML = '';
    $('#titulo-vista').textContent = 'Acceso del panel';
    $('#vista').innerHTML = `
      <div class="card">
        <div class="card-body">
          <h5 class="card-title">Este panel es para administradores y proveedores</h5>
          <p class="text-secondary mb-0">
            Tu cuenta figura como PROPIETARIO, que corresponde a la aplicacion movil.
            Si eres veterinario, pidele al administrador que te cree desde el modulo
            Proveedores; alla se genera tu cuenta de acceso con tu correo y contrasena.
          </p>
        </div>
      </div>`;
    return;
  }

  construirMenu();
  await navegar();
}

async function iniciar() {
  if (!configurado()) {
    mostrarLogin('Falta configurar Firebase en web/js/firebase-config.js');
    $('#btn-google').disabled = true;
    $('#btn-acceso').disabled = true;
    return;
  }

  observarSesion(async usuario => {
    if (usuario) {
      try {
        await entrarApp();
      } catch (error) {
        mostrarLogin(error.message);
      }
    } else {
      ctx.perfil = null;
      mostrarLogin();
    }
  });
}

// Ojo para ver u ocultar la contrasena en el ingreso.
const botonVerClave = document.getElementById('btn-ver-clave');
botonVerClave?.addEventListener('click', () => {
  const campo = document.getElementById('acceso-clave');
  const icono = document.getElementById('icono-ver-clave');
  const mostrar = campo.type === 'password';

  campo.type = mostrar ? 'text' : 'password';
  icono.src = mostrar
    ? '../assets/icons/shared/visibility_off.png'
    : '../assets/icons/shared/visibility.png';
  botonVerClave.setAttribute(
    'aria-label',
    mostrar ? 'Ocultar contrasena' : 'Mostrar contrasena'
  );
});

document.querySelectorAll('#pestanas-acceso .nav-link').forEach(boton => {
  boton.addEventListener('click', () => {
    modo = boton.dataset.modo;
    document.querySelectorAll('#pestanas-acceso .nav-link')
      .forEach(b => b.classList.toggle('active', b === boton));
    $('#grupo-nombre').classList.toggle('d-none', modo !== 'registrar');
    $('#btn-acceso').textContent = modo === 'registrar' ? 'Crear cuenta' : 'Ingresar';
    $('#login-mensaje').textContent = '';
  });
});

$('#form-acceso').addEventListener('submit', async evento => {
  evento.preventDefault();
  const correo = $('#acceso-correo').value.trim();
  const clave = $('#acceso-clave').value;
  const nombre = $('#acceso-nombre').value.trim();
  $('#login-mensaje').textContent = '';

  try {
    if (modo === 'registrar') {
      await registrarConCorreo(correo, clave, nombre);
    } else {
      await entrarConCorreo(correo, clave);
    }
  } catch (error) {
    $('#login-mensaje').textContent = traducirError(error);
  }
});

$('#btn-google').addEventListener('click', async () => {
  $('#login-mensaje').textContent = '';
  try {
    await entrarConGoogle();
  } catch (error) {
    $('#login-mensaje').textContent = traducirError(error);
  }
});

$('#btn-olvide').addEventListener('click', async () => {
  const correo = $('#acceso-correo').value.trim();
  const aviso = $('#login-mensaje');
  if (!correo) {
    aviso.className = 'text-danger small text-center mt-3 mb-0';
    aviso.textContent = 'Escribe primero tu correo electronico.';
    return;
  }
  try {
    await recuperarClave(correo);
    aviso.className = 'text-success small text-center mt-3 mb-0';
    aviso.textContent = 'Te enviamos un correo para restablecer la contrasena.';
  } catch (error) {
    aviso.className = 'text-danger small text-center mt-3 mb-0';
    aviso.textContent = traducirError(error);
  }
});

$('#btn-salir').addEventListener('click', async () => {
  await salir();
});

$('#modal-cerrar').addEventListener('click', () => {
  window.bootstrap.Modal.getInstance(document.getElementById('modal'))?.hide();
});

function traducirError(error) {
  const codigo = error?.code ?? '';
  if (codigo.includes('invalid-credential') || codigo.includes('wrong-password')) {
    return 'Correo o contrasena incorrectos.';
  }
  if (codigo.includes('email-already-in-use')) return 'Ese correo ya tiene una cuenta.';
  if (codigo.includes('weak-password')) return 'La contrasena debe tener al menos 6 caracteres.';
  if (codigo.includes('popup-closed-by-user')) return 'Se cerro la ventana de Google.';
  if (codigo.includes('unauthorized-domain')) {
    return 'Agrega 127.0.0.1 en los dominios autorizados de Firebase.';
  }
  if (codigo.includes('operation-not-allowed')
    || String(error?.message).includes('CONFIGURATION_NOT_FOUND')) {
    return 'Activa Authentication (Correo y Google) en la consola de Firebase.';
  }
  return error?.message ?? 'No fue posible completar la operacion.';
}

iniciar().catch(error => aviso(error.message, true));

