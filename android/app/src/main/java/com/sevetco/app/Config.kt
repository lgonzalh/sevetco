package com.sevetco.app

import android.content.Context
import com.google.firebase.FirebaseApp

/**
 * Comprueba si Firebase ya quedo configurado.
 *
 * Firebase se configura con el archivo android/app/google-services.json, que se
 * descarga de la consola de Firebase (ver docs/FIREBASE.md).
 */
object Config {
    fun disponible(context: Context): Boolean = try {
        FirebaseApp.initializeApp(context.applicationContext) != null
    } catch (e: Exception) {
        false
    }
}

/** Nombres de las colecciones (iguales a las entidades del ER). */
object Coleccion {
    const val USUARIO = "usuario"
    const val MASCOTA = "mascota"
    const val PLAN = "plan"
    const val SERVICIO = "servicio"
    const val PLAN_SERVICIO = "plan_servicio"
    const val CITA = "cita"
    const val ATENCION = "atencion"
    const val RUTA = "ruta_desplazamiento"
    const val TRAZABILIDAD = "trazabilidad_cita"
    const val AGENDA = "agenda"
    const val SUSCRIPCION = "suscripcion"
    const val EVENTOS = "eventos"
    const val PROMOCIONES = "promociones"
}

/**
 * Horario de atencion usado para calcular la disponibilidad de los
 * veterinarios. Se puede ajustar en un solo lugar.
 */
object Horario {
    const val HORA_INICIO = 8
    const val HORA_FIN = 18
    const val DURACION_CITA_MINUTOS = 60
}

// ---------------------------------------------------------------------------
// Modelos
// ---------------------------------------------------------------------------

data class Mascota(
    val id: String,
    val nombre: String,
    val especie: String?,
    val raza: String?,
    val fechaNacimiento: String?,
    val foto: String? = null
)

data class Servicio(
    val id: String,
    val nombre: String,
    val precioBase: Double,
    val duracionMinutos: Int
)

data class Plan(
    val id: String,
    val nombre: String,
    val precio: Double,
    val vigenciaDias: Int,
    val estado: String
)

data class Cita(
    val id: String,
    val fechaHora: String,
    val estado: String,
    val mascota: String?,
    val fotoMascota: String? = null,
    val servicio: String?,
    val responsable: String?,
    val destino: String?,
    val idProveedor: String? = null,
    val idPropietario: String? = null,
    val idServicio: String? = null,
    val propietario: String? = null,
    val fotoResponsable: String? = null,
    val fotoPropietario: String? = null,
    // Ruta del desplazamiento (RF-17): origen = ubicacion del proveedor.
    val idRuta: String? = null,
    val origen: String? = null,
    val latOrigen: Double? = null,
    val lngOrigen: Double? = null,
    val latDestino: Double? = null,
    val lngDestino: Double? = null,
    val distanciaKm: Double? = null
)

data class Atencion(
    val id: String,
    val fecha: String,
    val diagnostico: String?,
    val tratamiento: String?,
    val recomendaciones: String?,
    val mascota: String?,
    val fotoMascota: String? = null
)

data class Suscripcion(
    val id: String,
    val idPlan: String,
    val fechaFin: String?,
    val estado: String
)

data class EventoCita(
    val estado: String,
    val fecha: String,
    val responsable: String?,
    val nota: String? = null
)

data class Evento(
    val id: String,
    val nombre: String,
    val fecha: String?,
    val horaInicio: String?,
    val horaFin: String?,
    val texto: String?,
    val imagen: String?
)

data class Promocion(
    val id: String,
    val nombre: String,
    val descuento: String?,
    val texto: String?,
    val fecha: String?,
    val horaInicio: String?,
    val horaFin: String?,
    val imagen: String?
)

// ---------------------------------------------------------------------------
// Modelos del panel de administracion (app movil)
// ---------------------------------------------------------------------------

data class UsuarioAdmin(
    val id: String,
    val nombre: String,
    val correo: String,
    val documento: String?,
    val tipo: String,
    val estado: String,
    val foto: String? = null
)

data class Proveedor(
    val id: String,
    val nombre: String,
    val correo: String,
    val tarjeta: String?,
    val especialidad: String?,
    val estado: String,
    val foto: String? = null
)

data class MascotaAdmin(
    val id: String,
    val nombre: String,
    val especie: String?,
    val raza: String?,
    val fechaNacimiento: String?,
    val foto: String?,
    val propietario: String?,
    val idPropietario: String? = null,
    val fotoPropietario: String? = null
)

data class SuscripcionAdmin(
    val id: String,
    val propietario: String?,
    val fotoPropietario: String? = null,
    val plan: String?,
    val inicio: String?,
    val fin: String?,
    val estado: String
)

data class TratamientoAdmin(
    val id: String,
    val fecha: String?,
    val mascota: String?,
    val fotoMascota: String? = null,
    val propietario: String?,
    val fotoPropietario: String? = null,
    val proveedor: String?,
    val idProveedor: String? = null,
    val fotoProveedor: String? = null,
    val diagnostico: String?,
    val tratamiento: String?,
    val recomendaciones: String?
)

data class Pago(
    val id: String,
    val propietario: String?,
    val fotoPropietario: String? = null,
    val monto: Double,
    val estado: String,
    val referencia: String?,
    val fecha: String?
)

data class RutaAdmin(
    val id: String,
    val cita: Cita,
    val estado: String
)

data class ResumenAdmin(
    val citas: Int,
    val citasPorEstado: Map<String, Int>,
    val usuarios: Int,
    val proveedores: Int,
    val mascotas: Int,
    val servicios: Int,
    val planes: Int,
    val suscripciones: Int,
    val tratamientos: Int,
    val pagos: Int,
    val pagosCobrados: Double,
    val rutas: Int,
    val eventos: Int,
    val promociones: Int
)

