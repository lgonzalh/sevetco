// ---------------------------------------------------------------------------
// Capa de datos: Firebase Authentication + Cloud Firestore.
// Sustituye a la conexion de Supabase.
//
// Las colecciones conservan los nombres del modelo entidad-relacion:
//   usuario, proveedor, mascota, plan, servicio, plan_servicio, suscripcion,
//   cita, atencion, pago, trazabilidad_cita, ruta_desplazamiento
// ---------------------------------------------------------------------------

import {
  initializeApp, deleteApp
} from 'https://www.gstatic.com/firebasejs/10.12.5/firebase-app.js';
import {
  getAuth, GoogleAuthProvider, signInWithPopup, signOut, onAuthStateChanged,
  signInWithEmailAndPassword, createUserWithEmailAndPassword, updateProfile,
  sendPasswordResetEmail, EmailAuthProvider, reauthenticateWithCredential,
  updatePassword
} from 'https://www.gstatic.com/firebasejs/10.12.5/firebase-auth.js';
import {
  getFirestore, collection, getDocs, getDoc, doc, addDoc, setDoc, updateDoc,
  deleteDoc, query, where, orderBy, serverTimestamp, Timestamp
} from 'https://www.gstatic.com/firebasejs/10.12.5/firebase-firestore.js';
import { FIREBASE_CONFIG } from './firebase-config.js';

let app = null;
let servicioAuth = null;
let baseDatos = null;

export { Timestamp };

/** Indica si firebase-config.js todavia tiene los valores de ejemplo. */
export function configurado() {
  return !FIREBASE_CONFIG.apiKey.includes('TU-API-KEY')
    && !String(FIREBASE_CONFIG.projectId).includes('TU-PROYECTO');
}

function asegurar() {
  if (!configurado()) {
    throw new Error('Falta configurar Firebase en web/js/firebase-config.js');
  }
  if (!app) {
    app = initializeApp(FIREBASE_CONFIG);
    servicioAuth = getAuth(app);
    baseDatos = getFirestore(app);
  }
  return { auth: servicioAuth, db: baseDatos };
}

// ---------------------------------------------------------------------------
// Autenticacion
// ---------------------------------------------------------------------------

export function observarSesion(callback) {
  const { auth } = asegurar();
  return onAuthStateChanged(auth, callback);
}

export function usuarioActual() {
  if (!configurado()) return null;
  const { auth } = asegurar();
  return auth.currentUser;
}

export async function entrarConGoogle() {
  const { auth } = asegurar();
  await signInWithPopup(auth, new GoogleAuthProvider());
}

export async function entrarConCorreo(correo, clave) {
  const { auth } = asegurar();
  await signInWithEmailAndPassword(auth, correo, clave);
}

export async function registrarConCorreo(correo, clave, nombre) {
  const { auth } = asegurar();
  const creado = await createUserWithEmailAndPassword(auth, correo, clave);
  if (nombre) await updateProfile(creado.user, { displayName: nombre });
}

/** Envia el correo de Firebase para restablecer la contrasena. */
export async function recuperarClave(correo) {
  const { auth } = asegurar();
  await sendPasswordResetEmail(auth, correo);
}

export async function salir() {
  const { auth } = asegurar();
  await signOut(auth);
}

/**
 * Cambia la contrasena del usuario conectado. Firebase exige confirmar la
 * contrasena actual antes de aceptar la nueva.
 */
export async function cambiarClave(actual, nueva) {
  const { auth } = asegurar();
  const usuario = auth.currentUser;
  if (!usuario?.email) throw new Error('La cuenta no tiene correo electronico.');

  const credencial = EmailAuthProvider.credential(usuario.email, actual);
  await reauthenticateWithCredential(usuario, credencial);
  await updatePassword(usuario, nueva);
}

/** Lee (y crea si no existe) el documento del usuario conectado. */
export async function miPerfil() {
  const { auth, db } = asegurar();
  const usuario = auth.currentUser;
  if (!usuario) return null;

  const referencia = doc(db, 'usuario', usuario.uid);
  const actual = await getDoc(referencia);

  if (!actual.exists()) {
    await setDoc(referencia, {
      numero_documento: null,
      correo: usuario.email ?? '',
      nombre: usuario.displayName ?? usuario.email ?? '',
      foto: usuario.photoURL ?? '',
      tipo_usuario: 'PROPIETARIO',
      estado: 'ACTIVO',
      creado_en: serverTimestamp()
    });
    const creado = await getDoc(referencia);
    return { id: usuario.uid, ...creado.data() };
  }

  const perfil = { id: usuario.uid, ...actual.data() };

  // La foto de la cuenta (la de Google, por ejemplo) se guarda cuando la
  // ficha todavia no tiene ninguna: si el administrador la cambia desde el
  // panel, esa manda.
  if (usuario.photoURL && !perfil.foto) {
    await updateDoc(referencia, { foto: usuario.photoURL });
    perfil.foto = usuario.photoURL;
  }

  return perfil;
}

// ---------------------------------------------------------------------------
// Firestore
// ---------------------------------------------------------------------------

/** Devuelve todos los documentos de una coleccion. */
export async function leerColeccion(nombre, orden = null) {
  const { db } = asegurar();
  const referencia = collection(db, nombre);
  const consulta = orden ? query(referencia, orderBy(orden)) : referencia;
  const resultado = await getDocs(consulta);
  return resultado.docs.map(documento => ({ id: documento.id, ...documento.data() }));
}

/**
 * Devuelve solo los documentos de una coleccion que cumplen la condicion.
 * Es obligatorio para el proveedor: las reglas no lo dejan leer las citas ni
 * los tratamientos de los demas, solo los suyos.
 */
export async function leerDonde(nombre, campo, valor) {
  const { db } = asegurar();
  const resultado = await getDocs(
    query(collection(db, nombre), where(campo, '==', valor))
  );
  return resultado.docs.map(documento => ({ id: documento.id, ...documento.data() }));
}

/**
 * Crea la cuenta de acceso (correo y contrasena) de otra persona sin cerrar la
 * sesion del administrador: se abre una segunda conexion a Firebase solo para
 * el alta y se cierra al terminar. Devuelve el identificador del usuario nuevo.
 */
export async function crearCuenta(correo, clave, nombre) {
  const secundaria = initializeApp(FIREBASE_CONFIG, `alta-${Date.now()}`);
  const autenticacion = getAuth(secundaria);
  try {
    const creado = await createUserWithEmailAndPassword(autenticacion, correo, clave);
    const uid = creado.user.uid;
    if (nombre) await updateProfile(creado.user, { displayName: nombre });
    await signOut(autenticacion);
    return uid;
  } finally {
    try {
      await deleteApp(secundaria);
    } catch (error) {
      // Si no se puede cerrar la conexion secundaria no afecta el alta.
    }
  }
}

export async function leerDocumento(nombre, id) {
  const { db } = asegurar();
  const resultado = await getDoc(doc(db, nombre, id));
  return resultado.exists() ? { id: resultado.id, ...resultado.data() } : null;
}

export async function crear(nombre, datos) {
  const { db } = asegurar();
  const creado = await addDoc(collection(db, nombre), {
    ...datos,
    creado_en: serverTimestamp()
  });
  return creado.id;
}

export async function crearConId(nombre, id, datos) {
  const { db } = asegurar();
  // merge: no borra los campos que ya haya escrito la otra aplicacion.
  await setDoc(doc(db, nombre, id), datos, { merge: true });
  return id;
}

export async function actualizar(nombre, id, datos) {
  const { db } = asegurar();
  await updateDoc(doc(db, nombre, id), datos);
}

export async function eliminar(nombre, id) {
  const { db } = asegurar();
  await deleteDoc(doc(db, nombre, id));
}

/** Comprueba si existe un documento (para los que son uno a uno). */
export async function existe(nombre, id) {
  const { db } = asegurar();
  const resultado = await getDoc(doc(db, nombre, id));
  return resultado.exists();
}

// ---------------------------------------------------------------------------
// Utilidades de fecha (Firestore usa Timestamp)
// ---------------------------------------------------------------------------

/** Convierte texto "2026-10-10T09:00" a Timestamp de Firestore. */
export function aTimestamp(valor) {
  if (!valor) return null;
  const fecha = valor instanceof Date ? valor : new Date(valor);
  return Number.isNaN(fecha.getTime()) ? null : Timestamp.fromDate(fecha);
}

/** Convierte un Timestamp de Firestore a Date (o devuelve el valor original). */
export function aFecha(valor) {
  if (!valor) return null;
  if (typeof valor?.toDate === 'function') return valor.toDate();
  const fecha = new Date(valor);
  return Number.isNaN(fecha.getTime()) ? null : fecha;
}

