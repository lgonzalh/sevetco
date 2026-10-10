package com.sevetco.app

import com.google.firebase.Timestamp
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Acceso a Cloud Firestore. Las reglas de seguridad de
 * firebase/firestore.rules son las que controlan los permisos.
 */
class Datos {

    private val db: FirebaseFirestore get() = FirebaseFirestore.getInstance()

    private val formato = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", Locale.US)

    private fun texto(fecha: Date?): String = fecha?.let { formato.format(it) } ?: ""

    // -----------------------------------------------------------------------
    // Perfil
    // -----------------------------------------------------------------------

    /** Crea el documento del usuario la primera vez que entra. */
    suspend fun asegurarPerfil(uid: String, correo: String?, nombre: String?, foto: String?) {
        val referencia = db.collection(Coleccion.USUARIO).document(uid)
        val actual = referencia.get().await()
        if (!actual.exists()) {
            referencia.set(
                mapOf(
                    "numero_documento" to null,
                    "correo" to (correo ?: ""),
                    "nombre" to (nombre ?: correo ?: ""),
                    "foto" to (foto ?: ""),
                    "tipo_usuario" to "PROPIETARIO",
                    "estado" to "ACTIVO"
                ),
                SetOptions.merge()
            ).await()
        } else {
            // La ficha de la base de datos manda: el correo solo se actualiza
            // aqui cuando el propio usuario confirmo un cambio pedido desde la
            // app (queda anotado en "correo_pendiente" al solicitar el cambio).
            val cambios = mutableMapOf<String, Any>()

            // La foto que trae la cuenta de Google (o de otro proveedor) se
            // guarda cuando la ficha todavia no tiene ninguna: si el
            // administrador la cambia desde el panel web, esa manda.
            if (!foto.isNullOrBlank() && actual.getString("foto").isNullOrBlank()) {
                cambios["foto"] = foto
            }

            val pendiente = actual.getString("correo_pendiente")
            if (!correo.isNullOrBlank() && correo != actual.getString("correo") && correo == pendiente) {
                cambios["correo"] = correo
                cambios["correo_pendiente"] = FieldValue.delete()
            }

            if (cambios.isNotEmpty()) {
                referencia.update(cambios).await()
            }
        }
    }

    suspend fun perfil(uid: String): Map<String, Any?>? {
        val documento = db.collection(Coleccion.USUARIO).document(uid).get().await()
        return if (documento.exists()) documento.data else null
    }

    /**
     * Cambia el correo de la cuenta de acceso.
     *
     * El campo "correo" guardado en Firestore es solo un dato de contacto. El
     * correo con el que se inicia sesion y al que llega el enlace para
     * restablecer la contrasena vive en Firebase Authentication, y se cambia
     * desde aqui.
     */
    suspend fun cambiarCorreo(uid: String, nuevoCorreo: String) {
        val usuario = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser
            ?: error("Sesion no iniciada")

        // Firebase tiene activada la proteccion contra enumeracion de correos:
        // por eso no permite cambiar la direccion de una vez. Lo que hace es
        // enviar un enlace al correo NUEVO y el cambio se aplica solo cuando el
        // usuario abre ese enlace y lo confirma.
        val referencia = db.collection(Coleccion.USUARIO).document(uid)
        referencia.update("correo_pendiente", nuevoCorreo.trim()).await()
        try {
            usuario.verifyBeforeUpdateEmail(nuevoCorreo.trim()).await()
        } catch (e: Exception) {
            referencia.update("correo_pendiente", FieldValue.delete()).await()
            throw e
        }
    }

    /**
     * Cambia la contrasena del usuario conectado.
     *
     * Para hacerlo hay que confirmar la contrasena actual (Firebase lo exige
     * por seguridad). Si la cuenta se creo con Google no tiene contrasena.
     */
    suspend fun cambiarClave(correoFicha: String?, actual: String, nueva: String) {
        val autenticacion = com.google.firebase.auth.FirebaseAuth.getInstance()
        val usuario = autenticacion.currentUser ?: error("Sesion no iniciada")

        // La cuenta guardada en el telefono puede traer el correo anterior:
        // se refresca contra el servidor antes de validar la contrasena.
        try {
            usuario.reload().await()
        } catch (e: Exception) {
            // sin conexion: se sigue con lo que haya en el telefono
        }

        val candidatos = listOfNotNull(correoFicha, usuario.email)
            .map { it.trim() }
            .filter { it.isNotBlank() }
            .distinct()
        if (candidatos.isEmpty()) error("La cuenta no tiene correo")

        var ultimo: Exception? = null
        var entro = false
        for (candidato in candidatos) {
            try {
                val credencial = com.google.firebase.auth.EmailAuthProvider
                    .getCredential(candidato, actual)
                usuario.reauthenticate(credencial).await()
                entro = true
                break
            } catch (e: Exception) {
                ultimo = e
            }
        }
        if (!entro) throw (ultimo ?: error("No se pudo validar la contrasena"))
        usuario.updatePassword(nueva).await()
    }

    /** Indica si la cuenta entra con correo y contrasena (no con Google). */
    fun tieneClave(): Boolean =
        com.google.firebase.auth.FirebaseAuth.getInstance().currentUser
            ?.providerData
            ?.any { it.providerId == "password" } == true

    /**
     * Guarda la foto de seguridad del usuario. Siempre se toma con la camara
     * del telefono en el momento (igual que la foto de la mascota).
     */
    suspend fun guardarFotoUsuario(uid: String, foto: String) {
        db.collection(Coleccion.USUARIO).document(uid).update("foto", foto).await()
    }

    // -----------------------------------------------------------------------
    // Mascotas (RF-02, RF-03)
    // -----------------------------------------------------------------------

    suspend fun mascotas(uid: String): List<Mascota> =
        db.collection(Coleccion.MASCOTA)
            .whereEqualTo("id_propietario", uid)
            .get().await()
            .documents
            .map { documento ->
                Mascota(
                    id = documento.id,
                    nombre = documento.getString("nombre") ?: "",
                    especie = documento.getString("especie"),
                    raza = documento.getString("raza"),
                    fechaNacimiento = documento.getString("fecha_nacimiento"),
                    foto = documento.getString("foto")
                )
            }
            .sortedBy { it.nombre.lowercase() }

    suspend fun guardarMascota(
        uid: String,
        id: String?,
        nombre: String,
        especie: String?,
        raza: String?,
        fechaNacimiento: String?,
        foto: String?
    ) {
        val datos = mapOf(
            "id_propietario" to uid,
            "nombre" to nombre,
            "especie" to especie,
            "raza" to raza,
            "fecha_nacimiento" to fechaNacimiento,
            "foto" to foto
        )
        if (id == null) {
            db.collection(Coleccion.MASCOTA).add(datos).await()
        } else {
            db.collection(Coleccion.MASCOTA).document(id).set(datos, SetOptions.merge()).await()
        }
    }

    suspend fun eliminarMascota(id: String) {
        db.collection(Coleccion.MASCOTA).document(id).delete().await()
    }

    /** RF-08: el propietario cancela una cita propia. */
    suspend fun cancelarCita(uid: String, idCita: String, idProveedor: String?) {
        // Todo en un solo lote y el horario vuelve a quedar libre.
        val lote = db.batch()
        lote.update(
            db.collection(Coleccion.CITA).document(idCita),
            "estado",
            "CANCELADA"
        )
        lote.set(
            db.collection(Coleccion.AGENDA).document(idCita),
            mapOf("estado" to "CANCELADA"),
            SetOptions.merge()
        )
        lote.set(
            db.collection(Coleccion.TRAZABILIDAD).document(),
            mapOf(
                "id_cita" to idCita,
                "id_propietario" to uid,
                "id_proveedor" to idProveedor,
                "id_usuario_responsable" to uid,
                "estado_nuevo" to "CANCELADA",
                "fecha_evento" to Timestamp(Date())
            )
        )
        lote.commit().await()
    }

    // -----------------------------------------------------------------------
    // Servicios y planes (RF-04, RF-05, RF-06)
    // -----------------------------------------------------------------------

    suspend fun servicios(): List<Servicio> =
        db.collection(Coleccion.SERVICIO).get().await().documents
            .map { documento ->
                Servicio(
                    id = documento.id,
                    nombre = documento.getString("nombre") ?: "",
                    precioBase = documento.numero("precio_base"),
                    duracionMinutos = documento.entero("duracion_minutos")
                )
            }
            .sortedBy { it.nombre.lowercase() }

    suspend fun planes(): List<Plan> =
        db.collection(Coleccion.PLAN).get().await().documents
            .map { documento ->
                Plan(
                    id = documento.id,
                    nombre = documento.getString("nombre") ?: "",
                    precio = documento.numero("precio"),
                    vigenciaDias = documento.entero("vigencia_dias"),
                    estado = documento.getString("estado") ?: ""
                )
            }
            .sortedBy { it.precio }

    /** Servicios incluidos en un plan, con su cantidad (RF-05). */
    suspend fun serviciosDePlan(idPlan: String): List<Pair<String, Int>> {
        val nombres = db.collection(Coleccion.SERVICIO).get().await().documents
            .associateBy({ it.id }, { it.getString("nombre") ?: "" })

        return db.collection(Coleccion.PLAN_SERVICIO)
            .whereEqualTo("id_plan", idPlan)
            .get().await()
            .documents
            .map { documento ->
                val nombre = nombres[documento.getString("id_servicio")] ?: ""
                nombre to documento.entero("cantidad_incluida").coerceAtLeast(1)
            }
    }

    // -----------------------------------------------------------------------
    // Citas (RF-07, RF-08, RF-09)
    // -----------------------------------------------------------------------

    suspend fun citas(uid: String): List<Cita> {
        val mascotas = db.collection(Coleccion.MASCOTA)
            .whereEqualTo("id_propietario", uid).get().await().documents
            .associateBy({ it.id }, { it })

        val servicios = db.collection(Coleccion.SERVICIO).get().await().documents
            .associateBy({ it.id }, { it.getString("nombre") ?: "" })

        val documentosUsuarios = db.collection(Coleccion.USUARIO).get().await().documents
        val usuarios = documentosUsuarios
            .associateBy({ it.id }, { it.getString("nombre") ?: it.getString("correo") ?: "" })
        val fotosUsuarios = documentosUsuarios
            .associateBy({ it.id }, { it.getString("foto") })

        val rutas = db.collection(Coleccion.RUTA)
            .whereEqualTo("id_propietario", uid).get().await().documents
            .associateBy({ it.getString("id_cita") ?: "" }, { it })

        return db.collection(Coleccion.CITA)
            .whereEqualTo("id_propietario", uid)
            .get().await()
            .documents
            .map { documento ->
                val idProveedor = documento.getString("id_proveedor")
                val ruta = rutas[documento.id]
                Cita(
                    id = documento.id,
                    fechaHora = texto(documento.getTimestamp("fecha_hora")?.toDate()),
                    estado = documento.getString("estado") ?: "",
                    mascota = mascotas[documento.getString("id_mascota")]
                        ?.getString("nombre"),
                    fotoMascota = mascotas[documento.getString("id_mascota")]
                        ?.getString("foto"),
                    servicio = servicios[documento.getString("id_servicio")],
                    responsable = idProveedor?.let { usuarios[it] },
                    propietario = usuarios[documento.getString("id_propietario")],
                    fotoResponsable = idProveedor?.let { fotosUsuarios[it] },
                    destino = ruta?.getString("destino"),
                    origen = ruta?.getString("origen"),
                    latOrigen = ruta?.getDouble("lat_origen"),
                    lngOrigen = ruta?.getDouble("lng_origen"),
                    latDestino = ruta?.getDouble("lat"),
                    lngDestino = ruta?.getDouble("lng"),
                    distanciaKm = ruta?.getDouble("distancia_km"),
                    idRuta = ruta?.id,
                    idProveedor = idProveedor,
                    idPropietario = documento.getString("id_propietario"),
                    idServicio = documento.getString("id_servicio")
                )
            }
            .sortedByDescending { it.fechaHora }
    }

    /**
     * RF-10: citas que el administrador le asigno al proveedor veterinario.
     * Trae el nombre y la foto de la mascota, el dueno y la direccion del
     * servicio, que es lo que el veterinario necesita para desplazarse.
     */
    suspend fun citasAsignadas(uid: String): List<Cita> {
        val servicios = db.collection(Coleccion.SERVICIO).get().await().documents
            .associateBy({ it.id }, { it.getString("nombre") ?: "" })

        val documentosUsuarios = db.collection(Coleccion.USUARIO).get().await().documents
        val usuarios = documentosUsuarios
            .associateBy({ it.id }, { it.getString("nombre") ?: it.getString("correo") ?: "" })
        val fotosUsuarios = documentosUsuarios
            .associateBy({ it.id }, { it.getString("foto") })

        val mascotas = db.collection(Coleccion.MASCOTA).get().await().documents
            .associateBy({ it.id }, { it })

        val rutas = db.collection(Coleccion.RUTA).get().await().documents
            .associateBy({ it.getString("id_cita") ?: "" }, { it })

        return db.collection(Coleccion.CITA)
            .whereEqualTo("id_proveedor", uid)
            .get().await()
            .documents
            .map { documento ->
                val idProveedor = documento.getString("id_proveedor")
                val ruta = rutas[documento.id]
                Cita(
                    id = documento.id,
                    fechaHora = texto(documento.getTimestamp("fecha_hora")?.toDate()),
                    estado = documento.getString("estado") ?: "",
                    mascota = mascotas[documento.getString("id_mascota")]
                        ?.getString("nombre"),
                    fotoMascota = mascotas[documento.getString("id_mascota")]
                        ?.getString("foto"),
                    servicio = servicios[documento.getString("id_servicio")],
                    responsable = idProveedor?.let { usuarios[it] },
                    propietario = usuarios[documento.getString("id_propietario")],
                    fotoPropietario = fotosUsuarios[documento.getString("id_propietario")],
                    destino = ruta?.getString("destino"),
                    origen = ruta?.getString("origen"),
                    latOrigen = ruta?.getDouble("lat_origen"),
                    lngOrigen = ruta?.getDouble("lng_origen"),
                    latDestino = ruta?.getDouble("lat"),
                    lngDestino = ruta?.getDouble("lng"),
                    distanciaKm = ruta?.getDouble("distancia_km"),
                    idRuta = ruta?.id,
                    idProveedor = idProveedor,
                    idPropietario = documento.getString("id_propietario"),
                    idServicio = documento.getString("id_servicio")
                )
            }
            .sortedByDescending { it.fechaHora }
    }

    /**
     * RF-17: marca la ubicacion actual del proveedor como origen de la ruta
     * de una cita asignada y guarda la distancia hasta el domicilio.
     */
    suspend fun marcarOrigenRuta(
        idRuta: String,
        origen: String,
        lat: Double,
        lng: Double,
        distanciaKm: Double,
        latDestino: Double? = null,
        lngDestino: Double? = null
    ) {
        val cambios = mutableMapOf<String, Any>(
            "origen" to origen,
            "lat_origen" to lat,
            "lng_origen" to lng,
            "distancia_km" to distanciaKm,
            "origen_en" to Timestamp(Date())
        )

        // Si la ruta venia sin las coordenadas del domicilio (por ejemplo las
        // creadas antes desde el panel) se completan aqui.
        if (latDestino != null && lngDestino != null) {
            cambios["lat"] = latDestino
            cambios["lng"] = lngDestino
        }

        db.collection(Coleccion.RUTA).document(idRuta).update(cambios).await()
    }

    suspend fun crearCita(
        uid: String,
        idMascota: String,
        idServicio: String,
        fecha: Date,
        destino: String?,
        lat: Double? = null,
        lng: Double? = null
    ) {
        val referencia = db.collection(Coleccion.CITA).add(
            mapOf(
                "id_propietario" to uid,
                "id_mascota" to idMascota,
                "id_servicio" to idServicio,
                "id_proveedor" to null,
                "id_suscripcion" to null,
                "fecha_hora" to Timestamp(fecha),
                "estado" to "PENDIENTE"
            )
        ).await()

        if (!destino.isNullOrBlank()) {
            db.collection(Coleccion.RUTA).document(referencia.id).set(
                mapOf(
                    "id_cita" to referencia.id,
                    "id_propietario" to uid,
                    "id_proveedor" to null,
                    "origen" to null,
                    "destino" to destino,
                    "lat" to lat,
                    "lng" to lng,
                    "distancia_km" to null,
                    "estado" to "PENDIENTE"
                ),
                SetOptions.merge()
            ).await()
        }

        guardarEnAgenda(
            idCita = referencia.id,
            idPropietario = uid,
            idProveedor = null,
            inicio = fecha,
            duracionMinutos = Horario.DURACION_CITA_MINUTOS,
            estado = "PENDIENTE"
        )

        // Primer evento de la trazabilidad: la cita quedo creada.
        db.collection(Coleccion.TRAZABILIDAD).add(
            mapOf(
                "id_cita" to referencia.id,
                "id_propietario" to uid,
                "id_proveedor" to null,
                "id_usuario_responsable" to uid,
                "estado_nuevo" to "PENDIENTE",
                "fecha_evento" to Timestamp(Date())
            )
        ).await()
    }

    // -----------------------------------------------------------------------
    // Agenda: horarios ocupados de cada veterinario (RF-09 / reagendamiento)
    // -----------------------------------------------------------------------

    /** Coleccion publica con los tramos ocupados, sin datos personales. */
    suspend fun guardarEnAgenda(
        idCita: String,
        idPropietario: String,
        idProveedor: String?,
        inicio: Date,
        duracionMinutos: Int,
        estado: String
    ) {
        val fin = Date(inicio.time + duracionMinutos * 60_000L)
        db.collection(Coleccion.AGENDA).document(idCita).set(
            mapOf(
                "id_cita" to idCita,
                "id_propietario" to idPropietario,
                "id_proveedor" to idProveedor,
                "inicio" to Timestamp(inicio),
                "fin" to Timestamp(fin),
                "estado" to estado
            ),
            SetOptions.merge()
        ).await()
    }

    /** Tramos ocupados de un veterinario en un dia concreto. */
    suspend fun ocupacionDelDia(idProveedor: String, dia: Date): List<Pair<Date, Date>> {
        val inicioDia = inicioDelDia(dia)
        val finDia = Date(inicioDia.time + 24 * 60 * 60 * 1000L)

        return db.collection(Coleccion.AGENDA)
            .whereEqualTo("id_proveedor", idProveedor)
            .get().await()
            .documents
            .mapNotNull { documento ->
                if (documento.getString("estado") == "CANCELADA") return@mapNotNull null
                val inicio = documento.getTimestamp("inicio")?.toDate() ?: return@mapNotNull null
                val fin = documento.getTimestamp("fin")?.toDate() ?: return@mapNotNull null
                if (inicio.before(inicioDia) || !inicio.before(finDia)) return@mapNotNull null
                inicio to fin
            }
    }

    /** Horas libres del dia para un veterinario (o todas si aun no tiene). */
    suspend fun horasLibres(idProveedor: String?, dia: Date): List<Int> {
        val ocupadas = if (idProveedor.isNullOrBlank()) emptyList()
        else ocupacionDelDia(idProveedor, dia)

        return (Horario.HORA_INICIO until Horario.HORA_FIN).filter { hora ->
            val inicio = inicioDelDia(dia).time + hora * 60 * 60 * 1000L
            val fin = inicio + Horario.DURACION_CITA_MINUTOS * 60_000L
            ocupadas.none { (ocupadoInicio, ocupadoFin) ->
                inicio < ocupadoFin.time && fin > ocupadoInicio.time
            }
        }
    }

    /** Cambia la fecha y hora de una cita validando disponibilidad. */
    suspend fun reagendarCita(
        uid: String,
        idCita: String,
        idProveedor: String?,
        nuevaFecha: Date,
        estadoActual: String
    ) {
        val calendario = java.util.Calendar.getInstance().apply { time = nuevaFecha }
        val hora = calendario.get(java.util.Calendar.HOUR_OF_DAY)
        require(hora in Horario.HORA_INICIO until Horario.HORA_FIN) {
            "Solo se puede agendar entre ${Horario.HORA_INICIO}:00 y ${Horario.HORA_FIN}:00."
        }

        val libres = horasLibres(idProveedor, nuevaFecha)
        require(libres.contains(hora)) { "Ese horario ya esta ocupado." }

        // Se escriben las tres cosas en un solo lote: si algo falla, no queda
        // la cita cambiada a medias.
        val fin = Date(nuevaFecha.time + Horario.DURACION_CITA_MINUTOS * 60_000L)
        val lote = db.batch()

        lote.update(
            db.collection(Coleccion.CITA).document(idCita),
            "fecha_hora",
            Timestamp(nuevaFecha)
        )
        lote.set(
            db.collection(Coleccion.AGENDA).document(idCita),
            mapOf(
                "id_cita" to idCita,
                "id_propietario" to uid,
                "id_proveedor" to idProveedor,
                "inicio" to Timestamp(nuevaFecha),
                "fin" to Timestamp(fin),
                "estado" to estadoActual
            ),
            SetOptions.merge()
        )
        lote.set(
            db.collection(Coleccion.TRAZABILIDAD).document(),
            mapOf(
                "id_cita" to idCita,
                "id_propietario" to uid,
                "id_proveedor" to idProveedor,
                "id_usuario_responsable" to uid,
                "estado_nuevo" to estadoActual,
                "fecha_evento" to Timestamp(Date()),
                "nota" to "Cita reprogramada"
            )
        )

        lote.commit().await()
    }

    private fun inicioDelDia(dia: Date): Date {
        val calendario = java.util.Calendar.getInstance().apply {
            time = dia
            set(java.util.Calendar.HOUR_OF_DAY, 0)
            set(java.util.Calendar.MINUTE, 0)
            set(java.util.Calendar.SECOND, 0)
            set(java.util.Calendar.MILLISECOND, 0)
        }
        return calendario.time
    }

    // -----------------------------------------------------------------------
    // Historial de tratamientos (RF-12)
    // -----------------------------------------------------------------------

    // -----------------------------------------------------------------------
    // Planes adquiridos (RF-06: seleccionar un plan)
    // -----------------------------------------------------------------------

    // -----------------------------------------------------------------------
    // Eventos y promociones (contenido de la pantalla de Inicio)
    // -----------------------------------------------------------------------

    suspend fun eventos(): List<Evento> =
        db.collection(Coleccion.EVENTOS).get().await().documents
            .map { documento ->
                Evento(
                    id = documento.id,
                    nombre = documento.getString("nombre_evento") ?: "",
                    fecha = documento.getString("fecha_evento"),
                    horaInicio = documento.getString("horario_inievento")
                        ?: documento.getString("horario_ini_evento"),
                    horaFin = documento.getString("horariofin_evento")
                        ?: documento.getString("horario_fin_evento"),
                    texto = documento.getString("texto_evento"),
                    imagen = documento.getString("imagen_evento")
                )
            }
            .sortedBy { it.fecha ?: "" }

    suspend fun promociones(): List<Promocion> =
        db.collection(Coleccion.PROMOCIONES).get().await().documents
            .map { documento ->
                Promocion(
                    id = documento.id,
                    nombre = documento.getString("nombre_promo") ?: "",
                    descuento = documento.getString("descuento_promo"),
                    texto = documento.getString("texto_promo"),
                    fecha = documento.getString("fecha_promo"),
                    horaInicio = documento.getString("horaini_promo"),
                    horaFin = documento.getString("horafin_promo"),
                    imagen = documento.getString("imagen_promo")
                )
            }
            .sortedBy { it.fecha ?: "" }

    suspend fun suscripciones(uid: String): List<Suscripcion> =
        db.collection(Coleccion.SUSCRIPCION)
            .whereEqualTo("id_propietario", uid)
            .get().await()
            .documents
            .map { documento ->
                Suscripcion(
                    id = documento.id,
                    idPlan = documento.getString("id_plan") ?: "",
                    fechaFin = texto(documento.getTimestamp("fecha_fin")?.toDate()),
                    estado = documento.getString("estado") ?: ""
                )
            }
            .sortedByDescending { it.fechaFin ?: "" }

    /** Registra la adquisicion de un plan por parte del propietario. */
    suspend fun adquirirPlan(uid: String, plan: Plan) {
        val inicio = Date()
        val fin = Date(inicio.time + plan.vigenciaDias * 24L * 60 * 60 * 1000)
        db.collection(Coleccion.SUSCRIPCION).add(
            mapOf(
                "id_propietario" to uid,
                "id_plan" to plan.id,
                "fecha_inicio" to Timestamp(inicio),
                "fecha_fin" to Timestamp(fin),
                "estado" to "ACTIVA"
            )
        ).await()
    }

    /** Trazabilidad de una cita (RF-15 visto por el propietario). */
    suspend fun trazabilidad(uid: String, idCita: String): List<EventoCita> {
        val usuarios = db.collection(Coleccion.USUARIO).get().await().documents
            .associateBy({ it.id }, { it.getString("nombre") ?: it.getString("correo") ?: "" })

        return db.collection(Coleccion.TRAZABILIDAD)
            .whereEqualTo("id_propietario", uid)
            .get().await()
            .documents
            .filter { it.getString("id_cita") == idCita }
            .map { documento ->
                EventoCita(
                    estado = documento.getString("estado_nuevo") ?: "",
                    fecha = texto(documento.getTimestamp("fecha_evento")?.toDate()),
                    responsable = documento.getString("id_usuario_responsable")
                        ?.let { usuarios[it] },
                    nota = documento.getString("nota")
                )
            }
            .sortedBy { it.fecha }
    }

    suspend fun atenciones(uid: String): List<Atencion> {
        val mascotas = db.collection(Coleccion.MASCOTA)
            .whereEqualTo("id_propietario", uid).get().await().documents
            .associateBy({ it.id }, { it })

        val citas = db.collection(Coleccion.CITA)
            .whereEqualTo("id_propietario", uid).get().await().documents
            .associateBy({ it.id }, { it.getString("id_mascota") ?: "" })

        return db.collection(Coleccion.ATENCION)
            .whereEqualTo("id_propietario", uid)
            .get().await()
            .documents
            .map { documento ->
                val idMascota = citas[documento.getString("id_cita") ?: ""]
                val mascota = mascotas[idMascota]
                Atencion(
                    id = documento.id,
                    fecha = texto(documento.getTimestamp("fecha_registro")?.toDate()),
                    diagnostico = documento.getString("diagnostico"),
                    tratamiento = documento.getString("tratamiento"),
                    recomendaciones = documento.getString("recomendaciones"),
                    mascota = mascota?.getString("nombre"),
                    fotoMascota = mascota?.getString("foto")
                )
            }
            .sortedByDescending { it.fecha }
    }

    // -----------------------------------------------------------------------
    // Panel de administracion (app movil)
    //
    // El administrador puede leer todas las colecciones: son consultas
    // completas, sin filtrar por usuario.
    // -----------------------------------------------------------------------

    /** Nombres, correos y fotos de todos los usuarios, por identificador. */
    private class Directorio(
        val nombres: Map<String, String>,
        val correos: Map<String, String>,
        val fotos: Map<String, String>
    )

    private suspend fun directorio(): Directorio {
        val documentos = db.collection(Coleccion.USUARIO).get().await().documents
        return Directorio(
            nombres = documentos.associateBy(
                { it.id },
                { it.getString("nombre") ?: it.getString("correo") ?: "-" }
            ),
            correos = documentos.associateBy({ it.id }, { it.getString("correo") ?: "-" }),
            fotos = documentos.associateBy({ it.id }, { it.getString("foto") ?: "" })
        )
    }

    suspend fun usuarios(): List<UsuarioAdmin> =
        db.collection(Coleccion.USUARIO).get().await().documents
            .map { documento ->
                UsuarioAdmin(
                    id = documento.id,
                    nombre = documento.getString("nombre") ?: "-",
                    correo = documento.getString("correo") ?: "-",
                    documento = documento.getString("numero_documento"),
                    tipo = documento.getString("tipo_usuario") ?: "-",
                    estado = documento.getString("estado") ?: "-",
                    foto = documento.getString("foto")
                )
            }
            .sortedBy { it.tipo + it.nombre.lowercase() }

    suspend fun proveedores(): List<Proveedor> {
        val datos = directorio()
        return db.collection("proveedor").get().await().documents
            .map { documento ->
                Proveedor(
                    id = documento.id,
                    nombre = datos.nombres[documento.id] ?: "-",
                    correo = datos.correos[documento.id] ?: "-",
                    tarjeta = documento.getString("tarjeta_profesional"),
                    especialidad = documento.getString("especialidad"),
                    estado = "ACTIVO",
                    foto = datos.fotos[documento.id]
                )
            }
            .sortedBy { it.nombre.lowercase() }
    }

    /** Todas las citas de la plataforma (para el panel del administrador). */
    suspend fun citasTodas(): List<Cita> {
        val datos = directorio()
        val servicios = db.collection(Coleccion.SERVICIO).get().await().documents
            .associateBy({ it.id }, { it.getString("nombre") ?: "" })
        val mascotas = db.collection(Coleccion.MASCOTA).get().await().documents
            .associateBy({ it.id }, { it })
        val rutas = db.collection(Coleccion.RUTA).get().await().documents
            .associateBy({ it.getString("id_cita") ?: "" }, { it })

        return db.collection(Coleccion.CITA).get().await().documents
            .map { documento ->
                val idProveedor = documento.getString("id_proveedor")
                val ruta = rutas[documento.id]
                val mascota = mascotas[documento.getString("id_mascota")]
                Cita(
                    id = documento.id,
                    fechaHora = texto(documento.getTimestamp("fecha_hora")?.toDate()),
                    estado = documento.getString("estado") ?: "",
                    mascota = mascota?.getString("nombre"),
                    fotoMascota = mascota?.getString("foto"),
                    servicio = servicios[documento.getString("id_servicio")],
                    responsable = idProveedor?.let { datos.nombres[it] },
                    propietario = datos.nombres[documento.getString("id_propietario")],
                    fotoResponsable = idProveedor?.let { datos.fotos[it] },
                    fotoPropietario = datos.fotos[documento.getString("id_propietario")],
                    destino = ruta?.getString("destino"),
                    origen = ruta?.getString("origen"),
                    latOrigen = ruta?.getDouble("lat_origen"),
                    lngOrigen = ruta?.getDouble("lng_origen"),
                    latDestino = ruta?.getDouble("lat"),
                    lngDestino = ruta?.getDouble("lng"),
                    distanciaKm = ruta?.getDouble("distancia_km"),
                    idRuta = ruta?.id,
                    idProveedor = idProveedor,
                    idPropietario = documento.getString("id_propietario"),
                    idServicio = documento.getString("id_servicio")
                )
            }
            .sortedByDescending { it.fechaHora }
    }

    suspend fun mascotasTodas(): List<MascotaAdmin> {
        val datos = directorio()
        return db.collection(Coleccion.MASCOTA).get().await().documents
            .map { documento ->
                val propietario = documento.getString("id_propietario")
                MascotaAdmin(
                    id = documento.id,
                    nombre = documento.getString("nombre") ?: "-",
                    especie = documento.getString("especie"),
                    raza = documento.getString("raza"),
                    fechaNacimiento = documento.getString("fecha_nacimiento"),
                    foto = documento.getString("foto"),
                    propietario = datos.nombres[propietario] ?: "-",
                    idPropietario = propietario,
                    fotoPropietario = datos.fotos[propietario]
                )
            }
            .sortedBy { it.nombre.lowercase() }
    }

    suspend fun suscripciones(): List<SuscripcionAdmin> {
        val datos = directorio()
        val planes = db.collection(Coleccion.PLAN).get().await().documents
            .associateBy({ it.id }, { it.getString("nombre") ?: "" })
        return db.collection(Coleccion.SUSCRIPCION).get().await().documents
            .map { documento ->
                val propietario = documento.getString("id_propietario")
                SuscripcionAdmin(
                    id = documento.id,
                    propietario = datos.nombres[propietario] ?: "-",
                    fotoPropietario = datos.fotos[propietario],
                    plan = planes[documento.getString("id_plan")],
                    inicio = texto(documento.getTimestamp("fecha_inicio")?.toDate()),
                    fin = texto(documento.getTimestamp("fecha_fin")?.toDate()),
                    estado = documento.getString("estado") ?: "-"
                )
            }
            .sortedByDescending { it.inicio }
    }

    suspend fun tratamientos(): List<TratamientoAdmin> {
        val datos = directorio()
        val citas = db.collection(Coleccion.CITA).get().await().documents
            .associateBy({ it.id }, { it })
        val mascotas = db.collection(Coleccion.MASCOTA).get().await().documents
            .associateBy({ it.id }, { it })
        return db.collection(Coleccion.ATENCION).get().await().documents
            .map { documento ->
                val cita = citas[documento.getString("id_cita")]
                val mascota = mascotas[cita?.getString("id_mascota")]
                TratamientoAdmin(
                    id = documento.id,
                    fecha = texto(documento.getTimestamp("fecha_registro")?.toDate()),
                    mascota = mascota?.getString("nombre"),
                    fotoMascota = mascota?.getString("foto"),
                    propietario = datos.nombres[cita?.getString("id_propietario")],
                    fotoPropietario = datos.fotos[cita?.getString("id_propietario")],
                    proveedor = datos.nombres[documento.getString("id_proveedor")],
                    idProveedor = documento.getString("id_proveedor"),
                    fotoProveedor = datos.fotos[documento.getString("id_proveedor")],
                    diagnostico = documento.getString("diagnostico"),
                    tratamiento = documento.getString("tratamiento"),
                    recomendaciones = documento.getString("recomendaciones")
                )
            }
            .sortedByDescending { it.fecha }
    }

    suspend fun pagos(): List<Pago> {
        val datos = directorio()
        return db.collection("pago").get().await().documents
            .map { documento ->
                val propietario = documento.getString("id_propietario")
                Pago(
                    id = documento.id,
                    propietario = datos.nombres[propietario],
                    fotoPropietario = datos.fotos[propietario],
                    monto = documento.numero("monto"),
                    estado = documento.getString("estado") ?: "-",
                    referencia = documento.getString("referencia_externa"),
                    fecha = texto(documento.getTimestamp("fecha_pago")?.toDate())
                )
            }
            .sortedByDescending { it.fecha }
    }

    /** Resumen para la pantalla principal del administrador. */
    suspend fun resumen(): ResumenAdmin {
        val citas = db.collection(Coleccion.CITA).get().await().documents
        val pagosDocs = db.collection("pago").get().await().documents
        return ResumenAdmin(
            citas = citas.size,
            citasPorEstado = citas
                .groupingBy { it.getString("estado") ?: "SIN ESTADO" }
                .eachCount(),
            usuarios = db.collection(Coleccion.USUARIO).get().await().size(),
            proveedores = db.collection("proveedor").get().await().size(),
            mascotas = db.collection(Coleccion.MASCOTA).get().await().size(),
            servicios = db.collection(Coleccion.SERVICIO).get().await().size(),
            planes = db.collection(Coleccion.PLAN).get().await().size(),
            suscripciones = db.collection(Coleccion.SUSCRIPCION).get().await().size(),
            tratamientos = db.collection(Coleccion.ATENCION).get().await().size(),
            pagos = pagosDocs.size,
            pagosCobrados = pagosDocs
                .filter { it.getString("estado") == "PAGADO" }
                .sumOf { it.numero("monto") },
            rutas = db.collection(Coleccion.RUTA).get().await().size(),
            eventos = db.collection(Coleccion.EVENTOS).get().await().size(),
            promociones = db.collection(Coleccion.PROMOCIONES).get().await().size()
        )
    }

    /**
     * Asigna (o quita) el veterinario de una cita y actualiza su estado.
     * Es la accion que hace el administrador desde la app movil: deja la cita,
     * la agenda del veterinario y el primer evento de la trazabilidad.
     */
    suspend fun asignarCita(
        idCita: String,
        idPropietario: String?,
        idProveedor: String?,
        estado: String,
        fechaHora: Date?,
        responsable: String
    ) {
        db.collection(Coleccion.CITA).document(idCita)
            .update(
                mapOf(
                    "id_proveedor" to idProveedor,
                    "estado" to estado
                )
            )
            .await()

        if (fechaHora != null) {
            val fin = Date(fechaHora.time + Horario.DURACION_CITA_MINUTOS * 60_000L)
            db.collection(Coleccion.AGENDA).document(idCita).set(
                mapOf(
                    "id_cita" to idCita,
                    "id_propietario" to idPropietario,
                    "id_proveedor" to idProveedor,
                    "inicio" to Timestamp(fechaHora),
                    "fin" to Timestamp(fin),
                    "estado" to estado
                ),
                SetOptions.merge()
            ).await()
        }

        db.collection(Coleccion.TRAZABILIDAD).add(
            mapOf(
                "id_cita" to idCita,
                "id_propietario" to idPropietario,
                "id_proveedor" to idProveedor,
                "id_usuario_responsable" to responsable,
                "estado_nuevo" to estado,
                "fecha_evento" to Timestamp(Date())
            )
        ).await()
    }

    // -----------------------------------------------------------------------
    // Guardar desde el panel de administracion (crear y editar)
    // -----------------------------------------------------------------------

    private suspend fun guardar(coleccion: String, id: String?, datos: Map<String, Any?>) {
        val referencia = if (id.isNullOrBlank()) {
            db.collection(coleccion).document()
        } else {
            db.collection(coleccion).document(id)
        }
        referencia.set(datos, SetOptions.merge()).await()
    }

    suspend fun guardarServicio(id: String?, nombre: String, precio: Double, duracion: Int) =
        guardar(
            Coleccion.SERVICIO, id,
            mapOf("nombre" to nombre, "precio_base" to precio, "duracion_minutos" to duracion)
        )

    suspend fun guardarPlan(
        id: String?,
        nombre: String,
        precio: Double,
        vigencia: Int,
        estado: String
    ) = guardar(
        Coleccion.PLAN, id,
        mapOf(
            "nombre" to nombre,
            "precio" to precio,
            "vigencia_dias" to vigencia,
            "estado" to estado
        )
    )

    suspend fun guardarUsuario(
        id: String,
        nombre: String,
        documento: String?,
        tipo: String,
        estado: String
    ) = guardar(
        Coleccion.USUARIO, id,
        mapOf(
            "nombre" to nombre,
            "numero_documento" to documento,
            "tipo_usuario" to tipo,
            "estado" to estado
        )
    )

    suspend fun guardarProveedor(id: String, tarjeta: String?, especialidad: String?) =
        guardar(
            "proveedor", id,
            mapOf(
                "id_usuario" to id,
                "tarjeta_profesional" to tarjeta,
                "especialidad" to especialidad
            )
        )

    suspend fun guardarMascotaAdmin(
        id: String?,
        nombre: String,
        especie: String?,
        raza: String?,
        nacimiento: String?,
        foto: String?,
        idPropietario: String
    ) = guardar(
        Coleccion.MASCOTA, id,
        mapOf(
            "nombre" to nombre,
            "especie" to especie,
            "raza" to raza,
            "fecha_nacimiento" to nacimiento,
            "foto" to foto,
            "id_propietario" to idPropietario
        )
    )

    suspend fun guardarEvento(
        id: String?,
        nombre: String,
        fecha: String?,
        texto: String?,
        imagen: String?
    ) = guardar(
        Coleccion.EVENTOS, id,
        mapOf(
            "nombre_evento" to nombre,
            "fecha_evento" to fecha,
            "texto_evento" to texto,
            "imagen_evento" to imagen
        )
    )

    suspend fun guardarPromocion(
        id: String?,
        nombre: String,
        descuento: String?,
        texto: String?,
        fecha: String?,
        imagen: String?
    ) = guardar(
        Coleccion.PROMOCIONES, id,
        mapOf(
            "nombre_promocion" to nombre,
            "descuento" to descuento,
            "texto_promocion" to texto,
            "fecha" to fecha,
            "imagen_promo" to imagen
        )
    )

    suspend fun guardarPago(
        id: String?,
        idPropietario: String,
        monto: Double,
        estado: String,
        referencia: String?
    ) = guardar(
        "pago", id,
        mapOf(
            "id_propietario" to idPropietario,
            "monto" to monto,
            "estado" to estado,
            "referencia_externa" to referencia,
            "fecha_pago" to if (estado == "PAGADO") Timestamp(Date()) else null
        )
    )

    suspend fun guardarSuscripcion(
        id: String?,
        idPropietario: String,
        idPlan: String,
        estado: String
    ) {
        val inicio = Date()
        val fin = Date(inicio.time + 30L * 24 * 60 * 60 * 1000)
        guardar(
            Coleccion.SUSCRIPCION, id,
            mapOf(
                "id_propietario" to idPropietario,
                "id_plan" to idPlan,
                "fecha_inicio" to Timestamp(inicio),
                "fecha_fin" to Timestamp(fin),
                "estado" to estado
            )
        )
    }

    suspend fun guardarTratamiento(
        idCita: String,
        idPropietario: String?,
        idProveedor: String?,
        diagnostico: String,
        tratamiento: String,
        recomendaciones: String?
    ) {
        // Si no llega el dueno se toma de la cita: así el tratamiento queda
        // siempre ligado a la persona correcta.
        val dueno = idPropietario
            ?: db.collection(Coleccion.CITA).document(idCita).get().await()
                .getString("id_propietario")

        guardar(
            Coleccion.ATENCION, idCita,
            mapOf(
                "id_cita" to idCita,
                "id_propietario" to dueno,
                "id_proveedor" to idProveedor,
                "diagnostico" to diagnostico,
                "tratamiento" to tratamiento,
                "recomendaciones" to recomendaciones,
                "fecha_registro" to Timestamp(Date())
            )
        )
        db.collection(Coleccion.CITA).document(idCita).update("estado", "FINALIZADA").await()
    }
}

/**
 * Lee un campo numerico que puede estar guardado como numero o como texto.
 * (En la consola de Firestore es facil que quede como texto: "120000".)
 */
private fun com.google.firebase.firestore.DocumentSnapshot.numero(campo: String): Double {
    val valor = get(campo) ?: return 0.0
    return when (valor) {
        is Number -> valor.toDouble()
        is String -> valor.trim().toDoubleOrNull()
            ?: valor.replace(".", "").replace(",", "").trim().toDoubleOrNull()
            ?: 0.0
        else -> 0.0
    }
}

private fun com.google.firebase.firestore.DocumentSnapshot.entero(campo: String): Int =
    numero(campo).toInt()

