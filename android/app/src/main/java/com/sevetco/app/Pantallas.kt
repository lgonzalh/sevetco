package com.sevetco.app

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Shapes
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Inbox
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.CardMembership
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.Vaccines
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.io.ByteArrayOutputStream
import java.io.File
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

// ---------------------------------------------------------------------------
// Tema
// ---------------------------------------------------------------------------

private val Azul = Color(0xFF1F3864)
private val AzulClaro = Color(0xFFEAEEF7)
private val Gris = Color(0xFF6B7280)
private val Fondo = Color(0xFFF4F6FA)
private val Verde = Color(0xFF1F7A4D)
private val Naranja = Color(0xFFA15C00)
private val Rojo = Color(0xFFB3261E)

/**
 * Esquinas de la aplicacion: un solo radio para tarjetas, campos, botones y
 * etiquetas, igual que en la pagina web (nada de botones tipo pastilla).
 */
private val FormaSevetco = RoundedCornerShape(12.dp)

@Composable
fun TemaSevetco(contenido: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = lightColorScheme(
            primary = Azul,
            secondary = Color(0xFF2F548E),
            background = Fondo,
            surface = Color.White
        ),
        // Esquinas rectas en todo el sistema: es el lenguaje de diseno que se
        // usa en la aplicacion (los botones lo fijan aparte, ver BotonBase).
        shapes = Shapes(
            extraSmall = FormaSevetco,
            small = FormaSevetco,
            medium = FormaSevetco,
            large = FormaSevetco,
            extraLarge = FormaSevetco
        ),
        content = contenido
    )
}

// ---------------------------------------------------------------------------
// Formatos
// ---------------------------------------------------------------------------

private fun limpiarIso(iso: String) = iso.replace(Regex("\\.\\d+"), "")

private fun fechaLegible(iso: String?): String {
    if (iso.isNullOrBlank()) return "-"
    return try {
        val entrada = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", Locale.US)
        val salida = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale("es", "CO"))
        salida.format(entrada.parse(limpiarIso(iso)) ?: return iso)
    } catch (e: Exception) {
        iso.take(16).replace("T", " ")
    }
}

private fun pesos(valor: Double): String =
    "$" + String.format(Locale("es", "CO"), "%,.0f", valor).replace(',', '.')

private fun colorEstado(estado: String): Color = when (estado) {
    "FINALIZADA", "COMPLETADA", "ACTIVO", "ACTIVA", "PAGADO" -> Verde
    "CANCELADA", "CANCELADO", "ANULADO", "INACTIVO", "VENCIDA" -> Rojo
    else -> Naranja
}

/** Edad en anios y meses a partir de la fecha de nacimiento. */
private fun edadDe(fechaNacimiento: String?): Pair<Int, Int>? {
    if (fechaNacimiento.isNullOrBlank()) return null
    return try {
        val nacimiento = SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(fechaNacimiento) ?: return null
        val hoy = Calendar.getInstance()
        val antes = Calendar.getInstance().apply { time = nacimiento }

        var meses = (hoy.get(Calendar.YEAR) - antes.get(Calendar.YEAR)) * 12 +
            (hoy.get(Calendar.MONTH) - antes.get(Calendar.MONTH))
        if (hoy.get(Calendar.DAY_OF_MONTH) < antes.get(Calendar.DAY_OF_MONTH)) meses -= 1
        if (meses < 0) meses = 0
        (meses / 12) to (meses % 12)
    } catch (e: Exception) {
        null
    }
}

/** Texto de la edad, por ejemplo "5 a\u00f1os y 3 meses". */
private fun textoEdad(fechaNacimiento: String?): String {
    val edad = edadDe(fechaNacimiento) ?: return "Sin definir"
    val (anios, meses) = edad
    val textoAnios = if (anios == 1) "1 a\u00f1o" else "$anios a\u00f1os"
    val textoMeses = if (meses == 1) "1 mes" else "$meses meses"
    return when {
        anios == 0 && meses == 0 -> "Menos de 1 mes"
        anios == 0 -> textoMeses
        meses == 0 -> textoAnios
        else -> "$textoAnios y $textoMeses"
    }
}

/** Fecha de nacimiento que corresponde a una edad en anios y meses. */
private fun fechaDesdeEdad(anios: Int, meses: Int): String {
    val fecha = Calendar.getInstance()
    fecha.add(Calendar.MONTH, -(anios.coerceIn(0, 16) * 12 + meses.coerceIn(0, 11)))
    return SimpleDateFormat("yyyy-MM-dd", Locale.US).format(fecha.time)
}

/** Reduce la foto de la camara y la guarda como texto para Firestore. */
private fun fotoATexto(bitmap: Bitmap): String {
    val lado = 320
    val escala = lado.toFloat() / maxOf(bitmap.width, bitmap.height)
    val ajustado = if (escala < 1f) {
        Bitmap.createScaledBitmap(
            bitmap,
            (bitmap.width * escala).toInt().coerceAtLeast(1),
            (bitmap.height * escala).toInt().coerceAtLeast(1),
            true
        )
    } else {
        bitmap
    }
    val salida = ByteArrayOutputStream()
    ajustado.compress(Bitmap.CompressFormat.JPEG, 65, salida)
    return "data:image/jpeg;base64," + Base64.encodeToString(salida.toByteArray(), Base64.NO_WRAP)
}

private fun textoAFoto(texto: String?): Bitmap? {
    if (texto.isNullOrBlank() || !texto.contains(",")) return null
    return try {
        val bytes = Base64.decode(texto.substringAfter(","), Base64.DEFAULT)
        BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
    } catch (e: Exception) {
        null
    }
}

// ---------------------------------------------------------------------------
// Aplicacion
// ---------------------------------------------------------------------------

@Composable
fun AppSevetco(
    datos: Datos,
    configurado: Boolean,
    conectado: Boolean,
    uid: String?,
    mensaje: String?,
    aviso: String?,
    onGoogle: () -> Unit,
    onCorreo: (String, String) -> Unit,
    onRegistrar: (String, String, String?) -> Unit,
    onRecuperar: (String) -> Unit,
    onSalir: () -> Unit
) {
    if (!conectado || uid == null) {
        PantallaLogin(
            configurado = configurado,
            mensaje = mensaje,
            aviso = aviso,
            onGoogle = onGoogle,
            onCorreo = onCorreo,
            onRegistrar = onRegistrar,
            onRecuperar = onRecuperar
        )
    } else {
        PantallaPrincipal(datos = datos, uid = uid, onSalir = onSalir)
    }
}

/**
 * Campo de contrasena con el ojito para verla u ocultarla.
 * Los iconos salen de assets/icons/shared (visibility y visibility_off).
 */
@Composable
private fun CampoClave(
    valor: String,
    onCambio: (String) -> Unit,
    etiqueta: String,
    modifier: Modifier = Modifier
) {
    var visible by remember { mutableStateOf(false) }
    OutlinedTextField(
        value = valor,
        onValueChange = onCambio,
        label = { Text(etiqueta) },
        singleLine = true,
        visualTransformation = if (visible) {
            VisualTransformation.None
        } else {
            PasswordVisualTransformation()
        },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
        trailingIcon = {
            IconButton(onClick = { visible = !visible }) {
                Icon(
                    painter = painterResource(
                        if (visible) R.drawable.ic_visibility_off else R.drawable.ic_visibility
                    ),
                    contentDescription = if (visible) "Ocultar contrasena" else "Mostrar contrasena",
                    tint = Gris
                )
            }
        },
        modifier = modifier
    )
}

@Composable
private fun PantallaLogin(
    configurado: Boolean,
    mensaje: String?,
    aviso: String?,
    onGoogle: () -> Unit,
    onCorreo: (String, String) -> Unit,
    onRegistrar: (String, String, String?) -> Unit,
    onRecuperar: (String) -> Unit
) {
    var pestana by remember { mutableStateOf(0) }
    var nombre by remember { mutableStateOf("") }
    var correo by remember { mutableStateOf("") }
    var clave by remember { mutableStateOf("") }

    // La columna se puede desplazar: cuando el teclado ocupa la pantalla, los
    // campos ya no se aplastan y el boton sigue siendo alcanzable.
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Azul)
            .verticalScroll(rememberScrollState())
            .padding(vertical = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Card(modifier = Modifier.padding(horizontal = 22.dp)) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Image(
                    painter = painterResource(R.drawable.logo_app),
                    contentDescription = null,
                    modifier = Modifier.size(72.dp)
                )
                Spacer(Modifier.height(8.dp))
                Text("SEVETCO", fontSize = 26.sp, fontWeight = FontWeight.Bold, color = Azul)
                Text("Servicios veterinarios a domicilio", fontSize = 13.sp, color = Gris)
                Spacer(Modifier.height(18.dp))

                TabRow(selectedTabIndex = pestana) {
                    Tab(selected = pestana == 0, onClick = { pestana = 0 }, text = { Text("Ingresar") })
                    Tab(selected = pestana == 1, onClick = { pestana = 1 }, text = { Text("Registrarse") })
                }

                Spacer(Modifier.height(14.dp))

                if (pestana == 1) {
                    OutlinedTextField(
                        value = nombre,
                        onValueChange = { nombre = it },
                        label = { Text("Nombre completo") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(8.dp))
                }

                OutlinedTextField(
                    value = correo,
                    onValueChange = { correo = it },
                        label = { Text("Correo electr\u00f3nico") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))
                CampoClave(
                    valor = clave,
                    onCambio = { clave = it },
                    etiqueta = "Contrase\u00f1a",
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(14.dp))
                TextButton(
                    onClick = { onRecuperar(correo) },
                    enabled = configurado
                ) {
                    Text(
                        "Olvid\u00e9 mi contrase\u00f1a",
                        fontSize = 13.sp,
                        color = Azul
                    )
                }
                Spacer(Modifier.height(4.dp))
                Button(
                    onClick = {
                        if (pestana == 1) onRegistrar(correo, clave, nombre)
                        else onCorreo(correo, clave)
                    },
                    enabled = configurado && correo.isNotBlank() && clave.isNotBlank(),
                    shape = FormaSevetco,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(if (pestana == 1) "Crear cuenta" else "Ingresar")
                }

                Spacer(Modifier.height(10.dp))
                HorizontalDivider()
                Spacer(Modifier.height(10.dp))

                OutlinedButton(
                    onClick = onGoogle,
                    enabled = configurado,
                    shape = FormaSevetco,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Continuar con Google")
                }

                if (!configurado) {
                    Spacer(Modifier.height(12.dp))
                    Text(
                        "Servicio no disponible por el momento.",
                        color = Rojo,
                        fontSize = 12.sp
                    )
                }
                if (!mensaje.isNullOrBlank()) {
                    Spacer(Modifier.height(12.dp))
                    Text(mensaje, color = Rojo, fontSize = 12.sp)
                }
                if (!aviso.isNullOrBlank()) {
                    Spacer(Modifier.height(12.dp))
                    Text(aviso, color = Verde, fontSize = 12.sp)
                }
            }
        }
    }
}

private enum class Seccion(val titulo: String) {
    INICIO("Inicio"),
    MASCOTAS("Mascotas"),
    SERVICIOS("Servicios"),
    PLANES("Planes"),
    CITAS("Citas"),
    CUENTA("Mi cuenta"),
    // Panel de administracion
    PANEL("Panel"),
    USUARIOS("Usuarios"),
    MAS("Mas opciones"),
    PROVEEDORES("Proveedores"),
    SUSCRIPCIONES("Suscripciones"),
    TRATAMIENTOS("Tratamientos"),
    PAGOS("Pagos"),
    RUTAS("Rutas"),
    EVENTOS("Eventos"),
    PROMOCIONES("Promociones")
}

@Composable
private fun PantallaPrincipal(datos: Datos, uid: String, onSalir: () -> Unit) {
    var seccion by remember { mutableStateOf(Seccion.INICIO) }
    var mostrarHistorial by remember { mutableStateOf(false) }
    var mostrarAgendar by remember { mutableStateOf(false) }
    var versionInicio by remember { mutableStateOf(0) }
    var mostrarAcerca by remember { mutableStateOf(false) }
    var tipoUsuario by remember { mutableStateOf<String?>(null) }
    var fotoUsuario by remember { mutableStateOf<String?>(null) }
    var perfilListo by remember { mutableStateOf(false) }

    LaunchedEffect(uid) {
        try {
            val autenticacion = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser
            datos.asegurarPerfil(
                uid,
                autenticacion?.email,
                autenticacion?.displayName,
                autenticacion?.photoUrl?.toString()
            )
            val perfil = datos.perfil(uid)
            tipoUsuario = perfil?.get("tipo_usuario") as? String
            fotoUsuario = perfil?.get("foto") as? String
        } catch (e: Exception) {
            // Si falla, las pantallas muestran su propio mensaje de error.
        }
        perfilListo = true
    }

    val esProveedor = tipoUsuario == "PROVEEDOR"
    val esAdmin = tipoUsuario == "ADMIN"

    // Cada rol ve su propia barra: el dueno su app, el veterinario sus citas y
    // el administrador el panel completo de la plataforma.
    val secciones = when {
        esAdmin -> listOf(Seccion.PANEL, Seccion.CITAS, Seccion.USUARIOS, Seccion.MAS)
        esProveedor -> listOf(
            Seccion.INICIO, Seccion.CITAS, Seccion.TRATAMIENTOS, Seccion.CUENTA
        )
        else -> listOf(
            Seccion.INICIO, Seccion.MASCOTAS, Seccion.SERVICIOS,
            Seccion.PLANES, Seccion.CITAS, Seccion.CUENTA
        )
    }

    // Los modulos que se abren desde "Mas opciones": la barra inferior no
    // alcanza para mostrarlos todos.
    val modulosAdmin = listOf(
        Seccion.PROVEEDORES, Seccion.MASCOTAS, Seccion.SUSCRIPCIONES,
        Seccion.TRATAMIENTOS, Seccion.PAGOS, Seccion.RUTAS,
        Seccion.EVENTOS, Seccion.PROMOCIONES,
        Seccion.SERVICIOS, Seccion.PLANES
    )
    val enModuloAdmin = esAdmin && seccion !in listOf(Seccion.PANEL, Seccion.CITAS, Seccion.USUARIOS)

    // Al saber el rol se abre la primera pantalla que le corresponde (los
    // modulos que se abren desde "Mas opciones" no se tocan).
    LaunchedEffect(tipoUsuario) {
        if (tipoUsuario != null && seccion !in secciones && seccion !in modulosAdmin) {
            seccion = secciones.first()
        }
    }

    // RF-17: al abrir la app, el veterinario marca su ubicacion. Con ese punto
    // queda el origen de la ruta de cada cita asignada y la distancia hasta el
    // domicilio, que es lo que ve el dueno de la mascota.
    val contexto = LocalContext.current
    LaunchedEffect(esProveedor) {
        if (!esProveedor) return@LaunchedEffect
        try {
            val punto = Ubicacion.obtener(contexto) ?: return@LaunchedEffect
            val direccion = Geocodigo.direccion(punto.first, punto.second)
            val salida = direccion
                ?: String.format(Locale.US, "%.6f, %.6f", punto.first, punto.second)

            datos.citasAsignadas(uid).forEach { cita ->
                val idRuta = cita.idRuta ?: return@forEach

                // Citas antiguas pueden no tener las coordenadas del domicilio:
                // se buscan a partir de la direccion escrita.
                val destino = when {
                    cita.latDestino != null && cita.lngDestino != null ->
                        cita.latDestino to cita.lngDestino
                    !cita.destino.isNullOrBlank() -> Geocodigo.buscar(cita.destino)
                    else -> null
                }
                val latDestino = destino?.first
                val lngDestino = destino?.second

                val distancia = if (latDestino != null && lngDestino != null) {
                    Ubicacion.distanciaKm(punto.first, punto.second, latDestino, lngDestino)
                } else {
                    0.0
                }
                datos.marcarOrigenRuta(
                    idRuta = idRuta,
                    origen = salida,
                    lat = punto.first,
                    lng = punto.second,
                    distanciaKm = distancia,
                    latDestino = latDestino,
                    lngDestino = lngDestino
                )
            }
        } catch (e: Exception) {
            // Sin permiso de ubicacion o sin datos: la app sigue funcionando.
        }
    }

    if (mostrarAcerca) {
        PantallaAcerca(onVolver = { mostrarAcerca = false })
        return
    }

    // Mientras se lee el rol del usuario se muestra una espera: asi nunca
    // aparece por un instante el menu de otro tipo de cuenta.
    if (!perfilListo) {
        Box(Modifier.fillMaxSize().background(Fondo), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    // Por seguridad la foto se pide una sola vez: mientras la ficha no tenga
    // foto, no se puede usar la aplicacion.
    if (perfilListo && fotoUsuario.isNullOrBlank()) {
        DialogoFotoSeguridad(
            datos = datos,
            uid = uid,
            onListo = { fotoUsuario = it },
            onSalir = onSalir
        )
    }

    Scaffold(
        topBar = {
            Encabezado(
                titulo = seccion.titulo,
                onAcerca = { mostrarAcerca = true },
                onSalir = onSalir
            )
        },
        bottomBar = {
            NavigationBar {
                secciones.forEach { item ->
                    val activo = if (item == Seccion.MAS) enModuloAdmin else seccion == item
                    NavigationBarItem(
                        selected = activo,
                        onClick = { seccion = item },
                        icon = {
                            Icon(
                                painter = painterResource(iconoDe(item)),
                                contentDescription = item.titulo,
                                modifier = Modifier.size(22.dp)
                            )
                        },
                        label = {
                            Text(
                                item.titulo,
                                fontSize = 9.sp,
                                maxLines = 1,
                                softWrap = false
                            )
                        }
                    )
                }
            }
        }
    ) { relleno ->
        Box(modifier = Modifier.padding(relleno)) {
            when (seccion) {
                Seccion.INICIO -> PantallaInicio(
                    datos = datos,
                    uid = uid,
                    version = versionInicio,
                    mostrarAcciones = !esProveedor,
                    foto = fotoUsuario,
                    onVerHistorial = { mostrarHistorial = true },
                    onAgendar = { mostrarAgendar = true }
                )
                Seccion.MASCOTAS -> if (esAdmin) {
                    PantallaMascotasAdmin(datos)
                } else {
                    PantallaMascotas(datos, uid)
                }
                Seccion.SERVICIOS -> if (esAdmin) PantallaServiciosAdmin(datos) else PantallaServicios(datos)
                Seccion.PLANES -> if (esAdmin) PantallaPlanesAdmin(datos) else PantallaPlanes(datos, uid)
                Seccion.CITAS -> if (esAdmin) {
                    PantallaCitasAdmin(datos, uid)
                } else {
                    PantallaCitas(datos, uid, esProveedor)
                }
                Seccion.CUENTA -> PantallaCuenta(datos, uid) { fotoUsuario = it }
                // Panel de administracion
                Seccion.PANEL -> PantallaPanelAdmin(datos, uid)
                Seccion.USUARIOS -> PantallaUsuariosAdmin(datos)
                Seccion.MAS -> PantallaMasOpciones(modulosAdmin) { seccion = it }
                Seccion.PROVEEDORES -> PantallaProveedoresAdmin(datos)
                Seccion.SUSCRIPCIONES -> PantallaSuscripcionesAdmin(datos)
                Seccion.TRATAMIENTOS -> if (esProveedor) {
                    PantallaTratamientosProveedor(datos, uid)
                } else {
                    PantallaTratamientosAdmin(datos)
                }
                Seccion.PAGOS -> PantallaPagosAdmin(datos)
                Seccion.RUTAS -> PantallaRutasAdmin(datos)
                Seccion.EVENTOS -> PantallaEventosAdmin(datos)
                Seccion.PROMOCIONES -> PantallaPromocionesAdmin(datos)
            }
        }
    }

    if (mostrarHistorial) PantallaHistorial(datos, uid) { mostrarHistorial = false }
    if (mostrarAgendar) {
        PantallaAgendar(datos, uid) {
            mostrarAgendar = false
            versionInicio++
        }
    }
}

// ---------------------------------------------------------------------------
// Panel de administracion (app movil)
// ---------------------------------------------------------------------------

/** Convierte la fecha guardada en la cita a Date (para la agenda). */
private fun fechaDeTexto(iso: String?): Date? = try {
    if (iso.isNullOrBlank()) {
        null
    } else {
        SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", Locale.US).parse(limpiarIso(iso))
    }
} catch (e: Exception) {
    null
}

/**
 * Ficha de un tratamiento: la mascota a la izquierda y, junto al nombre de la
 * persona, su foto (el veterinario que atendio o el dueno, segun la pantalla).
 */
@Composable
private fun TarjetaTratamiento(
    tratamiento: TratamientoAdmin,
    persona: String?,
    fotoPersona: String?,
    etiquetaPersona: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = BorderStroke(1.dp, Color(0xFFE4E9F2))
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            FotoMascota(tratamiento.fotoMascota, 48)
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(tratamiento.mascota ?: "-", fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                Text("Diagnostico: ${tratamiento.diagnostico ?: "-"}", color = Gris, fontSize = 13.sp)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (!fotoPersona.isNullOrBlank()) {
                        AvatarUsuario(fotoPersona, 28)
                        Spacer(Modifier.width(8.dp))
                    }
                    Text(
                        "$etiquetaPersona: ${persona ?: "-"}",
                        color = Gris,
                        fontSize = 13.sp
                    )
                }
                Text(fechaLegible(tratamiento.fecha), color = Color(0xFF9AA3B2), fontSize = 12.sp)
            }
        }
    }
}

/** Carga listas de opciones para los formularios (propietarios, planes, etc.). */
@Composable
private fun recordarOpciones(
    cargar: suspend () -> List<Pair<String, String>>
): List<Pair<String, String>> {
    var opciones by remember { mutableStateOf<List<Pair<String, String>>>(emptyList()) }
    LaunchedEffect(Unit) {
        opciones = try {
            cargar()
        } catch (e: Exception) {
            emptyList()
        }
    }
    return opciones
}

/**
 * Pantalla de un modulo del panel: lista los registros, abre el formulario
 * para crear uno nuevo y para editar el que se toque.
 */
@Composable
private fun <T> PantallaEditable(
    clave: String,
    cargar: suspend () -> List<T>,
    mensajeVacio: String,
    textoNuevo: String? = null,
    tituloFormulario: (T?) -> String,
    campos: (T?) -> List<CampoFormulario>,
    alGuardar: suspend (T?, Map<String, String>) -> Unit,
    fila: @Composable (T, () -> Unit) -> Unit
) {
    var recarga by remember { mutableStateOf(0) }
    var abierto by remember { mutableStateOf(false) }
    var seleccionado by remember { mutableStateOf<T?>(null) }

    PantallaLista(
        clave = "$clave-$recarga",
        cargar = cargar,
        mensajeVacio = mensajeVacio,
        textoNuevo = textoNuevo,
        alNuevo = {
            seleccionado = null
            abierto = true
        },
        fila = { elemento ->
            fila(elemento) {
                seleccionado = elemento
                abierto = true
            }
        }
    )

    if (abierto) {
        DialogoFormulario(
            titulo = tituloFormulario(seleccionado),
            campos = campos(seleccionado),
            onCerrar = {
                abierto = false
                seleccionado = null
            },
            alGuardar = { valores ->
                alGuardar(seleccionado, valores)
                recarga++
            }
        )
    }
}

/** Tarjeta con un numero grande y su etiqueta. */
@Composable
private fun TarjetaNumero(
    valor: String,
    etiqueta: String,
    icono: Int,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = BorderStroke(1.dp, Color(0xFFE4E9F2))
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(14.dp)) {
            Box(
                modifier = Modifier.size(34.dp).background(AzulClaro),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(icono),
                    contentDescription = null,
                    tint = Azul,
                    modifier = Modifier.size(19.dp)
                )
            }
            Spacer(Modifier.height(8.dp))
            Text(valor, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Azul)
            Text(etiqueta, fontSize = 12.sp, color = Gris)
        }
    }
}

/** Etiqueta de estado con fondo suave del color del estado. */
@Composable
private fun ChipEstado(estado: String) {
    val color = colorEstado(estado)
    Box(
        modifier = Modifier
            .clip(FormaSevetco)
            .background(color.copy(alpha = 0.12f))
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(
            estado,
            color = color,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1
        )
    }
}

/** Mensaje centrado con icono para las listas vacias. */
@Composable
private fun EstadoVacio(mensaje: String, icono: ImageVector = Icons.Filled.Inbox) {
    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            icono,
            contentDescription = null,
            tint = Color(0xFFC3CBDA),
            modifier = Modifier.size(56.dp)
        )
        Spacer(Modifier.height(12.dp))
        Text(mensaje, color = Gris, fontSize = 14.sp, textAlign = TextAlign.Center)
    }
}

/** Titulo de seccion con una barra de color a la izquierda. */
@Composable
private fun TituloSeccion(texto: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .width(4.dp)
                .height(18.dp)
                .background(Azul)
        )
        Spacer(Modifier.width(8.dp))
        Text(texto, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Azul)
    }
}

private enum class TipoCampo { TEXTO, NUMERO, DECIMAL, LISTA, FOTO }

private data class CampoFormulario(
    val clave: String,
    val etiqueta: String,
    val valor: String = "",
    val tipo: TipoCampo = TipoCampo.TEXTO,
    val opciones: List<Pair<String, String>> = emptyList(),
    val requerido: Boolean = false
)

/**
 * Formulario generico para crear y editar: se le pasa la lista de campos y
 * devuelve los valores escritos. Evita repetir el mismo codigo en cada modulo.
 */
@Composable
private fun DialogoFormulario(
    titulo: String,
    campos: List<CampoFormulario>,
    onCerrar: () -> Unit,
    alGuardar: suspend (Map<String, String>) -> Unit
) {
    val valores = remember { mutableStateMapOf<String, String>() }
    var guardando by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    val alcance = rememberCoroutineScope()

    LaunchedEffect(Unit) { campos.forEach { valores[it.clave] = it.valor } }

    val camara = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicturePreview()
    ) { capturada ->
        val clave = campos.firstOrNull { it.tipo == TipoCampo.FOTO }?.clave
        if (capturada != null && clave != null) {
            valores[clave] = fotoATexto(capturada)
        }
    }

    AlertDialog(
        onDismissRequest = onCerrar,
        title = { Text(titulo) },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                campos.forEach { campo ->
                    when (campo.tipo) {
                        TipoCampo.FOTO -> {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                FotoMascota(valores[campo.clave], 48)
                                Spacer(Modifier.width(12.dp))
                                OutlinedButton(
                                    onClick = { camara.launch(null) },
                                    shape = FormaSevetco
                                ) {
                                    Text(
                                        if (valores[campo.clave].isNullOrBlank()) "Tomar foto"
                                        else "Cambiar foto"
                                    )
                                }
                            }
                        }

                        TipoCampo.LISTA -> {
                            Text(campo.etiqueta, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                            Spacer(Modifier.height(6.dp))
                            campo.opciones.forEach { (valor, texto) ->
                                OpcionSeleccion(
                                    texto = texto,
                                    activo = valores[campo.clave] == valor
                                ) { valores[campo.clave] = valor }
                            }
                            Spacer(Modifier.height(8.dp))
                        }

                        else -> OutlinedTextField(
                            value = valores[campo.clave].orEmpty(),
                            onValueChange = { valores[campo.clave] = it },
                            label = { Text(campo.etiqueta) },
                            singleLine = true,
                            shape = FormaSevetco,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = when (campo.tipo) {
                                    TipoCampo.NUMERO -> KeyboardType.Number
                                    TipoCampo.DECIMAL -> KeyboardType.Decimal
                                    else -> KeyboardType.Text
                                }
                            ),
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                        )
                    }
                }

                if (!error.isNullOrBlank()) {
                    Spacer(Modifier.height(8.dp))
                    Text(error ?: "", color = Rojo, fontSize = 13.sp)
                }
            }
        },
        confirmButton = {
            TextButton(
                enabled = !guardando,
                onClick = {
                    val faltante = campos.firstOrNull {
                        it.requerido && valores[it.clave].isNullOrBlank()
                    }
                    if (faltante != null) {
                        error = "Completa el campo: ${faltante.etiqueta}"
                    } else {
                        guardando = true
                        error = null
                        alcance.launch {
                            try {
                                alGuardar(valores.toMap())
                                onCerrar()
                            } catch (e: Exception) {
                                error = e.message
                            }
                            guardando = false
                        }
                    }
                }
            ) { Text("Guardar") }
        },
        dismissButton = { TextButton(onClick = onCerrar) { Text("Cancelar") } }
    )
}

/** Fila de lista con foto, titulo, detalle y estado. */
@Composable
private fun FilaDato(
    titulo: String,
    detalle: String? = null,
    extra: String? = null,
    estado: String? = null,
    foto: String? = null,
    fotoPersona: Boolean = false,
    icono: Int? = null,
    onClick: (() -> Unit)? = null
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = BorderStroke(1.dp, Color(0xFFE4E9F2))
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (icono != null) {
                Box(
                    modifier = Modifier.size(48.dp).background(AzulClaro),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(icono),
                        contentDescription = null,
                        tint = Azul,
                        modifier = Modifier.size(24.dp)
                    )
                }
            } else if (fotoPersona) {
                AvatarUsuario(foto, 48)
            } else {
                FotoMascota(foto, 48)
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(titulo, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                if (!detalle.isNullOrBlank()) {
                    Text(detalle, color = Gris, fontSize = 13.sp)
                }
                if (!extra.isNullOrBlank()) {
                    Text(extra, color = Color(0xFF9AA3B2), fontSize = 12.sp)
                }
            }
            if (!estado.isNullOrBlank()) {
                Spacer(Modifier.width(10.dp))
                ChipEstado(estado)
            }
        }
    }
}

/** Lista con su estado de carga, error y mensaje cuando esta vacia. */
@Composable
private fun ListaDatos(
    cargando: Boolean,
    error: String?,
    vacia: Boolean,
    mensajeVacio: String,
    contenido: @Composable () -> Unit
) {
    when {
        cargando -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        error != null -> Box(
            Modifier.fillMaxSize().padding(24.dp),
            contentAlignment = Alignment.Center
        ) { Text(error, color = Rojo, fontSize = 14.sp) }
        vacia -> EstadoVacio(mensajeVacio)
        else -> contenido()
    }
}

/** Carga una lista y la dibuja con el mismo formato en todos los modulos. */
@Composable
private fun <T> PantallaLista(
    clave: Any,
    cargar: suspend () -> List<T>,
    mensajeVacio: String,
    textoNuevo: String? = null,
    alNuevo: (() -> Unit)? = null,
    fila: @Composable (T) -> Unit
) {
    var items by remember { mutableStateOf<List<T>>(emptyList()) }
    var cargando by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(clave) {
        cargando = true
        error = null
        try {
            items = cargar()
        } catch (e: Exception) {
            error = e.message
        }
        cargando = false
    }

    ListaDatos(cargando, error, items.isEmpty(), mensajeVacio) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            if (textoNuevo != null && alNuevo != null) {
                item {
                    Button(
                        onClick = alNuevo,
                        shape = FormaSevetco,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_add_circle),
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(textoNuevo)
                    }
                }
            }
            items(items) { elemento -> fila(elemento) }
        }
    }
}

/** Resumen general de la plataforma. */
@Composable
private fun PantallaPanelAdmin(datos: Datos, uid: String) {
    var resumen by remember { mutableStateOf<ResumenAdmin?>(null) }
    var cargando by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(uid) {
        try {
            resumen = datos.resumen()
        } catch (e: Exception) {
            error = e.message
        }
        cargando = false
    }

    if (cargando) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        return
    }

    val datosResumen = resumen
    if (datosResumen == null) {
        Box(Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
            Text(error ?: "No se pudo cargar el panel.", color = Rojo, fontSize = 14.sp)
        }
        return
    }

    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp)
    ) {
        TituloSeccion("Resumen de la plataforma")
        Spacer(Modifier.height(12.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            TarjetaNumero(datosResumen.citas.toString(), "Citas", R.drawable.ic_calendar_month, Modifier.weight(1f))
            TarjetaNumero(datosResumen.mascotas.toString(), "Mascotas", R.drawable.ic_pets, Modifier.weight(1f))
        }
        Spacer(Modifier.height(10.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            TarjetaNumero(datosResumen.usuarios.toString(), "Usuarios", R.drawable.ic_person, Modifier.weight(1f))
            TarjetaNumero(datosResumen.proveedores.toString(), "Proveedores", R.drawable.ic_medical_services, Modifier.weight(1f))
        }
        Spacer(Modifier.height(10.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            TarjetaNumero(datosResumen.suscripciones.toString(), "Suscripciones", R.drawable.ic_card_membership, Modifier.weight(1f))
            TarjetaNumero(datosResumen.tratamientos.toString(), "Tratamientos", R.drawable.ic_vaccines, Modifier.weight(1f))
        }
        Spacer(Modifier.height(10.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            TarjetaNumero(datosResumen.pagos.toString(), "Pagos", R.drawable.ic_payments, Modifier.weight(1f))
            TarjetaNumero(pesos(datosResumen.pagosCobrados), "Cobrado", R.drawable.ic_price_check, Modifier.weight(1f))
        }
        Spacer(Modifier.height(10.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            TarjetaNumero(datosResumen.servicios.toString(), "Servicios", R.drawable.ic_medical_services, Modifier.weight(1f))
            TarjetaNumero(datosResumen.planes.toString(), "Planes", R.drawable.ic_storefront, Modifier.weight(1f))
        }
        Spacer(Modifier.height(10.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            TarjetaNumero(datosResumen.rutas.toString(), "Rutas", R.drawable.ic_location_on, Modifier.weight(1f))
            TarjetaNumero(datosResumen.eventos.toString(), "Eventos", R.drawable.ic_event, Modifier.weight(1f))
        }
        Spacer(Modifier.height(10.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            TarjetaNumero(datosResumen.promociones.toString(), "Promociones", R.drawable.ic_price_check, Modifier.weight(1f))
            Spacer(Modifier.weight(1f))
        }

        Spacer(Modifier.height(20.dp))
        TituloSeccion("Citas por estado")
        Spacer(Modifier.height(10.dp))
        datosResumen.citasPorEstado.forEach { (estado, cantidad) ->
            Card(
                modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                border = BorderStroke(1.dp, Color(0xFFE4E9F2))
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(modifier = Modifier.weight(1f)) { ChipEstado(estado) }
                    Text(cantidad.toString(), fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Azul)
                }
            }
        }
    }
}

/** Menu con los modulos que no caben en la barra inferior. */
@Composable
private fun PantallaMasOpciones(modulos: List<Seccion>, onElegir: (Seccion) -> Unit) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            TituloSeccion("Modulos de administracion")
        }
        items(modulos) { modulo ->
            Card(
                modifier = Modifier.fillMaxWidth().clickable { onElegir(modulo) },
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                border = BorderStroke(1.dp, Color(0xFFE4E9F2))
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .background(AzulClaro),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(iconoDe(modulo)),
                            contentDescription = null,
                            tint = Azul,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(Modifier.width(12.dp))
                    Text(
                        modulo.titulo,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.weight(1f)
                    )
                    Icon(Icons.Filled.ArrowForward, contentDescription = null, tint = Gris)
                }
            }
        }
    }
}

@Composable
private fun PantallaUsuariosAdmin(datos: Datos) = PantallaEditable(
    clave = "usuarios",
    cargar = { datos.usuarios() },
    mensajeVacio = "No hay usuarios registrados.",
    tituloFormulario = { "Editar usuario" },
    campos = { usuario ->
        listOf(
            CampoFormulario("nombre", "Nombre completo", usuario?.nombre ?: "", requerido = true),
            CampoFormulario("documento", "Numero de documento", usuario?.documento ?: ""),
            CampoFormulario(
                "tipo", "Tipo de usuario", usuario?.tipo ?: "PROPIETARIO", TipoCampo.LISTA,
                listOf(
                    "PROPIETARIO" to "Propietario",
                    "PROVEEDOR" to "Proveedor",
                    "ADMIN" to "Administrador"
                )
            ),
            CampoFormulario(
                "estado", "Estado", usuario?.estado ?: "ACTIVO", TipoCampo.LISTA,
                listOf("ACTIVO" to "Activo", "INACTIVO" to "Inactivo")
            )
        )
    },
    alGuardar = { usuario, valores ->
        usuario ?: return@PantallaEditable
        datos.guardarUsuario(
            id = usuario.id,
            nombre = valores["nombre"].orEmpty(),
            documento = valores["documento"],
            tipo = valores["tipo"] ?: usuario.tipo,
            estado = valores["estado"] ?: usuario.estado
        )
    },
    fila = { usuario, editar ->
        FilaDato(
            titulo = usuario.nombre,
            detalle = usuario.correo,
            extra = "Documento: ${usuario.documento ?: "-"}",
            estado = usuario.estado,
            foto = usuario.foto,
            fotoPersona = true,
            onClick = editar
        )
    }
)

@Composable
private fun PantallaProveedoresAdmin(datos: Datos) = PantallaEditable(
    clave = "proveedores",
    cargar = { datos.proveedores() },
    mensajeVacio = "No hay proveedores registrados.",
    tituloFormulario = { proveedor -> "Editar ${proveedor?.nombre ?: "proveedor"}" },
    campos = { proveedor ->
        listOf(
            CampoFormulario("tarjeta", "Tarjeta profesional", proveedor?.tarjeta ?: ""),
            CampoFormulario("especialidad", "Especialidad", proveedor?.especialidad ?: "")
        )
    },
    alGuardar = { proveedor, valores ->
        proveedor ?: return@PantallaEditable
        datos.guardarProveedor(
            id = proveedor.id,
            tarjeta = valores["tarjeta"],
            especialidad = valores["especialidad"]
        )
    },
    fila = { proveedor, editar ->
        FilaDato(
            titulo = proveedor.nombre,
            detalle = proveedor.correo,
            extra = "${proveedor.especialidad ?: "-"}  |  T.P. ${proveedor.tarjeta ?: "-"}",
            foto = proveedor.foto,
            fotoPersona = true,
            onClick = editar
        )
    }
)

@Composable
private fun PantallaMascotasAdmin(datos: Datos) {
    val duenos = recordarOpciones {
        datos.usuarios().filter { it.tipo == "PROPIETARIO" }.map { it.id to it.nombre }
    }

    PantallaEditable(
        clave = "mascotas-admin",
        cargar = { datos.mascotasTodas() },
        mensajeVacio = "No hay mascotas registradas.",
        textoNuevo = "Nueva mascota",
        tituloFormulario = { mascota -> if (mascota == null) "Nueva mascota" else "Editar ${mascota.nombre}" },
        campos = { mascota ->
            listOf(
                CampoFormulario("nombre", "Nombre", mascota?.nombre ?: "", requerido = true),
                CampoFormulario("especie", "Especie (perro, gato...)", mascota?.especie ?: ""),
                CampoFormulario("raza", "Raza", mascota?.raza ?: ""),
                CampoFormulario("nacimiento", "Nacimiento (aaaa-mm-dd)", mascota?.fechaNacimiento ?: ""),
                CampoFormulario("foto", "Foto de la mascota", mascota?.foto ?: "", TipoCampo.FOTO),
                CampoFormulario("dueno", "Propietario", mascota?.idPropietario ?: "", TipoCampo.LISTA, duenos, requerido = true)
            )
        },
        alGuardar = { mascota, valores ->
            datos.guardarMascotaAdmin(
                id = mascota?.id,
                nombre = valores["nombre"].orEmpty(),
                especie = valores["especie"],
                raza = valores["raza"],
                nacimiento = valores["nacimiento"],
                foto = valores["foto"],
                idPropietario = valores["dueno"].orEmpty()
            )
        },
        fila = { mascota, editar ->
            FilaDato(
                titulo = mascota.nombre,
                detalle = listOfNotNull(mascota.especie, mascota.raza).joinToString(" - "),
                extra = "Dueno: ${mascota.propietario ?: "-"}",
                foto = mascota.foto,
                onClick = editar
            )
        }
    )
}

@Composable
private fun PantallaSuscripcionesAdmin(datos: Datos) {
    val duenos = recordarOpciones {
        datos.usuarios().filter { it.tipo == "PROPIETARIO" }.map { it.id to it.nombre }
    }
    val planes = recordarOpciones { datos.planes().map { it.id to it.nombre } }

    PantallaEditable(
        clave = "suscripciones-admin",
        cargar = { datos.suscripciones() },
        mensajeVacio = "No hay suscripciones registradas.",
        textoNuevo = "Nueva suscripcion",
        tituloFormulario = { if (it == null) "Nueva suscripcion" else "Editar suscripcion" },
        campos = { suscripcion ->
            listOf(
                CampoFormulario("dueno", "Propietario", "", TipoCampo.LISTA, duenos, requerido = true),
                CampoFormulario("plan", "Plan", "", TipoCampo.LISTA, planes, requerido = true),
                CampoFormulario(
                    "estado", "Estado", suscripcion?.estado ?: "ACTIVA", TipoCampo.LISTA,
                    listOf("ACTIVA" to "Activa", "VENCIDA" to "Vencida", "CANCELADA" to "Cancelada")
                )
            )
        },
        alGuardar = { _, valores ->
            datos.guardarSuscripcion(
                id = null,
                idPropietario = valores["dueno"].orEmpty(),
                idPlan = valores["plan"].orEmpty(),
                estado = valores["estado"] ?: "ACTIVA"
            )
        },
        fila = { suscripcion, editar ->
            FilaDato(
                titulo = suscripcion.propietario ?: "-",
                detalle = "Plan: ${suscripcion.plan ?: "-"}",
                extra = "Hasta: ${fechaLegible(suscripcion.fin)}",
                estado = suscripcion.estado,
                foto = suscripcion.fotoPropietario,
                fotoPersona = true,
                onClick = editar
            )
        }
    )
}

@Composable
private fun PantallaTratamientosAdmin(datos: Datos) {
    // El tratamiento lo registra unicamente el veterinario que atendio la cita.
    // El administrador solo consulta el historial.
    PantallaEditable(
        clave = "tratamientos-admin",
        cargar = { datos.tratamientos() },
        mensajeVacio = "Todavia no hay tratamientos registrados.",
        tituloFormulario = { "Tratamiento" },
        campos = { emptyList() },
        alGuardar = { _, _ -> },
        fila = { tratamiento, _ ->
            TarjetaTratamiento(
                tratamiento = tratamiento,
                persona = tratamiento.proveedor,
                fotoPersona = tratamiento.fotoProveedor,
                etiquetaPersona = "Veterinario"
            )
        }
    )
}

/** El veterinario registra el tratamiento de las citas que tiene asignadas. */
@Composable
private fun PantallaTratamientosProveedor(datos: Datos, uid: String) {
    val citas = recordarOpciones {
        datos.citasAsignadas(uid)
            .filter { it.estado == "ASIGNADA" || it.estado == "EN_PROCESO" || it.estado == "FINALIZADA" }
            .map { it.id to "${fechaLegible(it.fechaHora)} - ${it.mascota ?: ""}" }
    }
    var recarga by remember { mutableStateOf(0) }
    var abierto by remember { mutableStateOf(false) }

    PantallaLista(
        clave = "tratamientos-proveedor-$recarga",
        cargar = { datos.tratamientos().filter { it.idProveedor == uid } },
        mensajeVacio = "Todavia no has registrado tratamientos.",
        textoNuevo = "Registrar tratamiento",
        alNuevo = { abierto = true },
        fila = { tratamiento ->
            TarjetaTratamiento(
                tratamiento = tratamiento,
                persona = tratamiento.propietario,
                fotoPersona = tratamiento.fotoPropietario,
                etiquetaPersona = "Dueno"
            )
        }
    )

    if (abierto) {
        DialogoFormulario(
            titulo = "Registrar tratamiento",
            campos = listOf(
                CampoFormulario("cita", "Cita", "", TipoCampo.LISTA, citas, requerido = true),
                CampoFormulario("diagnostico", "Diagnostico", "", requerido = true),
                CampoFormulario("tratamiento", "Tratamiento", "", requerido = true),
                CampoFormulario("recomendaciones", "Recomendaciones", "")
            ),
            onCerrar = { abierto = false },
            alGuardar = { valores ->
                datos.guardarTratamiento(
                    idCita = valores["cita"].orEmpty(),
                    idPropietario = null,
                    idProveedor = uid,
                    diagnostico = valores["diagnostico"].orEmpty(),
                    tratamiento = valores["tratamiento"].orEmpty(),
                    recomendaciones = valores["recomendaciones"]
                )
                recarga++
            }
        )
    }
}

@Composable
private fun PantallaPagosAdmin(datos: Datos) {
    val duenos = recordarOpciones {
        datos.usuarios().filter { it.tipo == "PROPIETARIO" }.map { it.id to it.nombre }
    }

    PantallaEditable(
        clave = "pagos-admin",
        cargar = { datos.pagos() },
        mensajeVacio = "No hay pagos registrados.",
        textoNuevo = "Registrar pago",
        tituloFormulario = { if (it == null) "Registrar pago" else "Editar pago" },
        campos = { pago ->
            listOf(
                CampoFormulario("dueno", "Propietario", "", TipoCampo.LISTA, duenos, requerido = true),
                CampoFormulario(
                    "monto", "Monto (COP)",
                    pago?.monto?.let { String.format(Locale.US, "%.0f", it) } ?: "",
                    TipoCampo.NUMERO, requerido = true
                ),
                CampoFormulario(
                    "estado", "Estado", pago?.estado ?: "PENDIENTE", TipoCampo.LISTA,
                    listOf("PENDIENTE" to "Pendiente", "PAGADO" to "Pagado", "ANULADO" to "Anulado")
                ),
                CampoFormulario("referencia", "Referencia externa", pago?.referencia ?: "")
            )
        },
        alGuardar = { pago, valores ->
            datos.guardarPago(
                id = pago?.id,
                idPropietario = valores["dueno"].orEmpty(),
                monto = valores["monto"]?.toDoubleOrNull() ?: 0.0,
                estado = valores["estado"] ?: "PENDIENTE",
                referencia = valores["referencia"]
            )
        },
        fila = { pago, editar ->
            FilaDato(
                titulo = pesos(pago.monto),
                detalle = "Propietario: ${pago.propietario ?: "-"}",
                extra = "Referencia: ${pago.referencia ?: "-"}",
                estado = pago.estado,
                foto = pago.fotoPropietario,
                fotoPersona = true,
                onClick = editar
            )
        }
    )
}

@Composable
private fun PantallaServiciosAdmin(datos: Datos) = PantallaEditable(
    clave = "servicios-admin",
    cargar = { datos.servicios() },
    mensajeVacio = "No hay servicios registrados.",
    textoNuevo = "Nuevo servicio",
    tituloFormulario = { servicio -> if (servicio == null) "Nuevo servicio" else "Editar ${servicio.nombre}" },
    campos = { servicio ->
        listOf(
            CampoFormulario("nombre", "Nombre del servicio", servicio?.nombre ?: "", requerido = true),
            CampoFormulario(
                "precio", "Precio (COP)",
                servicio?.precioBase?.let { String.format(Locale.US, "%.0f", it) } ?: "",
                TipoCampo.NUMERO, requerido = true
            ),
            CampoFormulario(
                "duracion", "Duracion (minutos)",
                servicio?.duracionMinutos?.toString() ?: "60",
                TipoCampo.NUMERO, requerido = true
            )
        )
    },
    alGuardar = { servicio, valores ->
        datos.guardarServicio(
            id = servicio?.id,
            nombre = valores["nombre"].orEmpty(),
            precio = valores["precio"]?.toDoubleOrNull() ?: 0.0,
            duracion = valores["duracion"]?.toIntOrNull() ?: 60
        )
    },
    fila = { servicio, editar ->
        FilaDato(
            titulo = servicio.nombre,
            detalle = "${pesos(servicio.precioBase)}  |  ${servicio.duracionMinutos} min",
            icono = R.drawable.ic_medical_services,
            onClick = editar
        )
    }
)

@Composable
private fun PantallaPlanesAdmin(datos: Datos) = PantallaEditable(
    clave = "planes-admin",
    cargar = { datos.planes() },
    mensajeVacio = "No hay planes registrados.",
    textoNuevo = "Nuevo plan",
    tituloFormulario = { plan -> if (plan == null) "Nuevo plan" else "Editar ${plan.nombre}" },
    campos = { plan ->
        listOf(
            CampoFormulario("nombre", "Nombre del plan", plan?.nombre ?: "", requerido = true),
            CampoFormulario(
                "precio", "Precio (COP)",
                plan?.precio?.let { String.format(Locale.US, "%.0f", it) } ?: "",
                TipoCampo.NUMERO, requerido = true
            ),
            CampoFormulario(
                "vigencia", "Vigencia (dias)",
                plan?.vigenciaDias?.toString() ?: "30",
                TipoCampo.NUMERO, requerido = true
            ),
            CampoFormulario(
                "estado", "Estado", plan?.estado ?: "ACTIVO", TipoCampo.LISTA,
                listOf("ACTIVO" to "Activo", "INACTIVO" to "Inactivo")
            )
        )
    },
    alGuardar = { plan, valores ->
        datos.guardarPlan(
            id = plan?.id,
            nombre = valores["nombre"].orEmpty(),
            precio = valores["precio"]?.toDoubleOrNull() ?: 0.0,
            vigencia = valores["vigencia"]?.toIntOrNull() ?: 30,
            estado = valores["estado"] ?: "ACTIVO"
        )
    },
    fila = { plan, editar ->
        FilaDato(
            titulo = plan.nombre,
            detalle = "${pesos(plan.precio)}  |  ${plan.vigenciaDias} dias",
            estado = plan.estado,
            icono = R.drawable.ic_storefront,
            onClick = editar
        )
    }
)

@Composable
private fun PantallaRutasAdmin(datos: Datos) {
    var porVer by remember { mutableStateOf<Cita?>(null) }

    PantallaLista(
        clave = "rutas-admin",
        cargar = { datos.citasTodas().filter { it.idRuta != null } },
        mensajeVacio = "No hay rutas registradas."
    ) { cita ->
        Card(modifier = Modifier.fillMaxWidth().clickable { porVer = cita }) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                FotoMascota(cita.fotoMascota, 48)
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(cita.mascota ?: "-", fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                    Text("Sale de: ${cita.origen ?: "sin marcar"}", color = Gris, fontSize = 13.sp)
                    Text("Domicilio: ${cita.destino ?: "-"}", color = Gris, fontSize = 13.sp)
                }
                Column(horizontalAlignment = Alignment.End) {
                    if (cita.distanciaKm != null) {
                        Text("${cita.distanciaKm} km", color = Azul, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    }
                    Text("Ver ruta", color = Azul, fontSize = 12.sp)
                }
            }
        }
    }

    porVer?.let { cita -> MapaRuta(cita) { porVer = null } }
}

@Composable
private fun PantallaEventosAdmin(datos: Datos) = PantallaEditable(
    clave = "eventos-admin",
    cargar = { datos.eventos() },
    mensajeVacio = "No hay eventos registrados.",
    textoNuevo = "Nuevo evento",
    tituloFormulario = { evento -> if (evento == null) "Nuevo evento" else "Editar ${evento.nombre}" },
    campos = { evento ->
        listOf(
            CampoFormulario("nombre", "Nombre del evento", evento?.nombre ?: "", requerido = true),
            CampoFormulario("fecha", "Fecha (por ejemplo 31/10/2026)", evento?.fecha ?: ""),
            CampoFormulario("inicio", "Hora de inicio (9:00 a.m.)", evento?.horaInicio ?: ""),
            CampoFormulario("fin", "Hora de fin (12:00 p.m.)", evento?.horaFin ?: ""),
            CampoFormulario("texto", "Texto de la tarjeta", evento?.texto ?: ""),
            CampoFormulario("imagen", "Imagen de la tarjeta", evento?.imagen ?: "", TipoCampo.FOTO)
        )
    },
    alGuardar = { evento, valores ->
        datos.guardarEvento(
            id = evento?.id,
            nombre = valores["nombre"].orEmpty(),
            fecha = valores["fecha"],
            texto = valores["texto"],
            imagen = valores["imagen"]
        )
    },
    fila = { evento, editar ->
        FilaDato(
            titulo = evento.nombre,
            detalle = listOfNotNull(evento.fecha, evento.horaInicio, evento.horaFin)
                .joinToString("  "),
            extra = evento.texto,
            foto = evento.imagen,
            icono = if (evento.imagen.isNullOrBlank()) R.drawable.ic_event else null,
            onClick = editar
        )
    }
)

@Composable
private fun PantallaPromocionesAdmin(datos: Datos) = PantallaEditable(
    clave = "promociones-admin",
    cargar = { datos.promociones() },
    mensajeVacio = "No hay promociones registradas.",
    textoNuevo = "Nueva promocion",
    tituloFormulario = { promo -> if (promo == null) "Nueva promocion" else "Editar ${promo.nombre}" },
    campos = { promo ->
        listOf(
            CampoFormulario("nombre", "Nombre de la promocion", promo?.nombre ?: "", requerido = true),
            CampoFormulario("descuento", "Descuento (10% de descuento)", promo?.descuento ?: ""),
            CampoFormulario("fecha", "Fecha (por ejemplo 31/10/2026)", promo?.fecha ?: ""),
            CampoFormulario("texto", "Texto de la tarjeta", promo?.texto ?: ""),
            CampoFormulario("imagen", "Imagen de la tarjeta", promo?.imagen ?: "", TipoCampo.FOTO)
        )
    },
    alGuardar = { promo, valores ->
        datos.guardarPromocion(
            id = promo?.id,
            nombre = valores["nombre"].orEmpty(),
            descuento = valores["descuento"],
            texto = valores["texto"],
            fecha = valores["fecha"],
            imagen = valores["imagen"]
        )
    },
    fila = { promo, editar ->
        FilaDato(
            titulo = promo.nombre,
            detalle = listOfNotNull(promo.fecha, promo.descuento).joinToString("  "),
            extra = promo.texto,
            foto = promo.imagen,
            icono = if (promo.imagen.isNullOrBlank()) R.drawable.ic_price_check else null,
            onClick = editar
        )
    }
)

/** Asigna el veterinario y cambia el estado de una cita. */
@Composable
private fun DialogoAsignarCita(
    datos: Datos,
    uid: String,
    cita: Cita,
    proveedores: List<Proveedor>,
    onCerrar: () -> Unit,
    onHecho: () -> Unit
) {
    var idProveedor by remember { mutableStateOf(cita.idProveedor) }
    var estado by remember { mutableStateOf(cita.estado) }
    var guardando by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    val alcance = rememberCoroutineScope()
    val estados = listOf("PENDIENTE", "ASIGNADA", "EN_PROCESO", "FINALIZADA", "CANCELADA")

    AlertDialog(
        onDismissRequest = onCerrar,
        title = { Text("Cita de ${cita.mascota ?: ""}") },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                Text(fechaLegible(cita.fechaHora), color = Gris, fontSize = 13.sp)
                Text("Dueno: ${cita.propietario ?: "-"}", color = Gris, fontSize = 13.sp)
                Spacer(Modifier.height(14.dp))

                Text("Veterinario responsable", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                Spacer(Modifier.height(6.dp))
                OpcionSeleccion(
                    texto = "Sin asignar",
                    activo = idProveedor == null
                ) { idProveedor = null }
                proveedores.forEach { proveedor ->
                    OpcionSeleccion(
                        texto = proveedor.nombre,
                        activo = idProveedor == proveedor.id,
                        foto = proveedor.foto,
                        detalle = proveedor.especialidad
                    ) { idProveedor = proveedor.id }
                }

                Spacer(Modifier.height(14.dp))
                Text("Estado de la cita", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                Spacer(Modifier.height(6.dp))
                estados.forEach { opcion ->
                    OpcionSeleccion(texto = opcion, activo = estado == opcion) { estado = opcion }
                }

                if (!error.isNullOrBlank()) {
                    Spacer(Modifier.height(8.dp))
                    Text(error ?: "", color = Rojo, fontSize = 13.sp)
                }
            }
        },
        confirmButton = {
            TextButton(
                enabled = !guardando,
                onClick = {
                    guardando = true
                    error = null
                    alcance.launch {
                        try {
                            datos.asignarCita(
                                idCita = cita.id,
                                idPropietario = cita.idPropietario,
                                idProveedor = idProveedor,
                                estado = estado,
                                fechaHora = fechaDeTexto(cita.fechaHora),
                                responsable = uid
                            )
                            onHecho()
                        } catch (e: Exception) {
                            error = e.message
                        }
                        guardando = false
                    }
                }
            ) { Text("Guardar") }
        },
        dismissButton = { TextButton(onClick = onCerrar) { Text("Cancelar") } }
    )
}

@Composable
private fun PantallaCitasAdmin(datos: Datos, uid: String) {
    var citas by remember { mutableStateOf<List<Cita>>(emptyList()) }
    var proveedores by remember { mutableStateOf<List<Proveedor>>(emptyList()) }
    var cargando by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var recarga by remember { mutableStateOf(0) }
    var porAsignar by remember { mutableStateOf<Cita?>(null) }
    var porTrazar by remember { mutableStateOf<Cita?>(null) }

    LaunchedEffect(recarga) {
        cargando = true
        error = null
        try {
            citas = datos.citasTodas()
            proveedores = datos.proveedores()
        } catch (e: Exception) {
            error = e.message
        }
        cargando = false
    }

    ListaDatos(cargando, error, citas.isEmpty(), "No hay citas registradas.") {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(citas) { cita ->
                Column {
                    FilaDato(
                        titulo = cita.mascota ?: "-",
                        detalle = "${fechaLegible(cita.fechaHora)}  |  ${cita.servicio ?: "-"}",
                        extra = "Dueno: ${cita.propietario ?: "-"}  |  Veterinario: ${cita.responsable ?: "sin asignar"}",
                        estado = cita.estado,
                        foto = cita.fotoMascota,
                        onClick = { porAsignar = cita }
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        TextButton(onClick = { porAsignar = cita }) {
                            Text("Asignar", color = Azul, fontSize = 13.sp)
                        }
                        TextButton(onClick = { porTrazar = cita }) {
                            Text("Trazabilidad", color = Azul, fontSize = 13.sp)
                        }
                    }
                }
            }
        }
    }

    porAsignar?.let { cita ->
        DialogoAsignarCita(
            datos = datos,
            uid = uid,
            cita = cita,
            proveedores = proveedores,
            onCerrar = { porAsignar = null },
            onHecho = {
                porAsignar = null
                recarga++
            }
        )
    }

    porTrazar?.let { cita ->
        DialogoTrazabilidad(datos = datos, uid = uid, cita = cita) { porTrazar = null }
    }
}

/**
 * Icono de cada seccion: son los mismos iconos que se usan en la pagina web
 * (assets/icons) para que las dos aplicaciones se vean iguales.
 */
private fun iconoDe(seccion: Seccion): Int = when (seccion) {
    Seccion.INICIO -> R.drawable.ic_home
    Seccion.MASCOTAS -> R.drawable.ic_pets
    Seccion.SERVICIOS -> R.drawable.ic_medical_services
    Seccion.PLANES -> R.drawable.ic_storefront
    Seccion.CITAS -> R.drawable.ic_calendar_month
    Seccion.CUENTA -> R.drawable.ic_account_circle
    Seccion.PANEL -> R.drawable.ic_dashboard
    Seccion.USUARIOS -> R.drawable.ic_person
    Seccion.MAS -> R.drawable.ic_menu
    Seccion.PROVEEDORES -> R.drawable.ic_badge
    Seccion.SUSCRIPCIONES -> R.drawable.ic_card_membership
    Seccion.TRATAMIENTOS -> R.drawable.ic_vaccines
    Seccion.PAGOS -> R.drawable.ic_payments
    Seccion.RUTAS -> R.drawable.ic_location_on
    Seccion.EVENTOS -> R.drawable.ic_event
    Seccion.PROMOCIONES -> R.drawable.ic_price_check
}

@Composable
private fun Encabezado(titulo: String, onAcerca: () -> Unit, onSalir: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().background(Azul).padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = painterResource(R.drawable.logo_app),
            contentDescription = null,
            colorFilter = androidx.compose.ui.graphics.ColorFilter.tint(Color.White),
            modifier = Modifier.size(28.dp)
        )
        Spacer(Modifier.width(10.dp))
        Text(
            "SEVETCO - $titulo",
            color = Color.White,
            fontSize = 17.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.weight(1f)
        )
        IconButton(onClick = onAcerca) {
            Icon(Icons.Filled.Info, contentDescription = "Acerca de", tint = Color.White)
        }
        IconButton(onClick = onSalir) {
            Icon(Icons.Filled.Logout, contentDescription = "Cerrar sesion", tint = Color.White)
        }
    }
}

@Composable
private fun PantallaInicio(
    datos: Datos,
    uid: String,
    version: Int,
    mostrarAcciones: Boolean = true,
    foto: String? = null,
    onVerHistorial: () -> Unit,
    onAgendar: () -> Unit
) {
    var eventos by remember { mutableStateOf<List<Evento>>(emptyList()) }
    var promociones by remember { mutableStateOf<List<Promocion>>(emptyList()) }
    var cargando by remember { mutableStateOf(true) }
    var nombre by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(version) {
        eventos = try { datos.eventos() } catch (e: Exception) { emptyList() }
        promociones = try { datos.promociones() } catch (e: Exception) { emptyList() }
        // El nombre se lee de la base de datos, igual que en el panel web:
        // si el administrador lo corrige alli, la app lo refleja.
        val perfil = try { datos.perfil(uid) } catch (e: Exception) { null }
        nombre = perfil?.get("nombre") as? String
        cargando = false
    }

    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            AvatarUsuario(foto, 48)
            Spacer(Modifier.width(14.dp))
            Column {
                Text(
                    "Hola, ${nombre?.takeIf { it.isNotBlank() } ?: "bienvenido"}",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Azul
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    "Gestiona tus mascotas y tus servicios a domicilio.",
                    color = Gris,
                    fontSize = 14.sp
                )
            }
        }
        Spacer(Modifier.height(18.dp))

        if (mostrarAcciones) {
            Button(
                onClick = onAgendar,
                shape = FormaSevetco,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Filled.Add, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Agendar cita")
            }
            Spacer(Modifier.height(8.dp))
            OutlinedButton(
                onClick = onVerHistorial,
                shape = FormaSevetco,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Filled.History, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Historial de tratamientos")
            }
            Spacer(Modifier.height(24.dp))
        }

        if (cargando) {
            CircularProgressIndicator()
        } else {
            SeccionEventos(eventos)
            Spacer(Modifier.height(16.dp))
            SeccionPromociones(promociones)
        }
    }

}

/**
 * Pantalla "Mi cuenta": cambiar el correo de acceso y la contrasena.
 *
 * El correo de acceso vive en Firebase Authentication, no en la base de datos:
 * por eso se cambia aqui, y asi el enlace de restablecer contrasena llega a la
 * direccion correcta.
 */
@Composable
private fun PantallaCuenta(datos: Datos, uid: String, onFotoCambiada: (String) -> Unit = {}) {
    var correo by remember { mutableStateOf<String?>(null) }
    var nombre by remember { mutableStateOf<String?>(null) }
    var tipo by remember { mutableStateOf<String?>(null) }
    var foto by remember { mutableStateOf<String?>(null) }
    var nuevoCorreo by remember { mutableStateOf("") }
    var actual by remember { mutableStateOf("") }
    var nueva by remember { mutableStateOf("") }
    var repetir by remember { mutableStateOf("") }
    var guardando by remember { mutableStateOf(false) }
    var errorCorreo by remember { mutableStateOf<String?>(null) }
    var avisoCorreo by remember { mutableStateOf<String?>(null) }
    var guardandoFoto by remember { mutableStateOf(false) }
    var errorFoto by remember { mutableStateOf<String?>(null) }
    var errorClave by remember { mutableStateOf<String?>(null) }
    var avisoClave by remember { mutableStateOf<String?>(null) }
    val alcance = rememberCoroutineScope()
    val tieneClave = remember { datos.tieneClave() }

    // La foto de seguridad siempre se toma con la camara del dispositivo.
    val camara = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicturePreview()
    ) { capturada ->
        if (capturada != null) {
            val tomada = fotoATexto(capturada)
            guardandoFoto = true
            errorFoto = null
            alcance.launch {
                try {
                    datos.guardarFotoUsuario(uid, tomada)
                    foto = tomada
                    onFotoCambiada(tomada)
                } catch (e: Exception) {
                    errorFoto = e.message
                }
                guardandoFoto = false
            }
        }
    }

    LaunchedEffect(uid) {
        val perfil = try { datos.perfil(uid) } catch (e: Exception) { null }
        nombre = perfil?.get("nombre") as? String
        tipo = perfil?.get("tipo_usuario") as? String
        foto = perfil?.get("foto") as? String

        // El correo que manda es el de la ficha del usuario en la base de
        // datos: asi lo que el administrador deje alli es lo que se ve aqui.
        correo = perfil?.get("correo") as? String
        if (correo.isNullOrBlank()) {
            val cuenta = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser
            correo = cuenta?.email
            if (correo.isNullOrBlank()) {
                try {
                    cuenta?.reload()?.await()
                    correo = cuenta?.email
                } catch (e: Exception) {
                    // sin conexion
                }
            }
        }
        nuevoCorreo = correo.orEmpty()
    }

    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp)
    ) {
        // ------------------------- Quien es -------------------------
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // La foto con el boton de la camara encima: al tocarlo se
                    // toma una foto nueva con el dispositivo.
                    Box {
                        AvatarUsuario(foto, 48)
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .size(21.dp)
                                .clip(CircleShape)
                                .background(Azul)
                                .clickable(enabled = !guardandoFoto) {
                                    camara.launch(null)
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Filled.PhotoCamera,
                                contentDescription = "Cambiar foto",
                                tint = Color.White,
                                modifier = Modifier.size(13.dp)
                            )
                        }
                    }
                    Spacer(Modifier.width(14.dp))
                    Column {
                        Text(
                            nombre ?: "-",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 17.sp
                        )
                        Text(correo ?: "", color = Gris, fontSize = 13.sp)
                        if (!tipo.isNullOrBlank()) {
                            Text(
                                tipo ?: "",
                                color = Azul,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
                if (!errorFoto.isNullOrBlank()) {
                    Spacer(Modifier.height(8.dp))
                    Text(errorFoto ?: "", color = Rojo, fontSize = 13.sp)
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        // --------------------- Correo de la cuenta ---------------------
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                Text(
                    "Correo de la cuenta",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp,
                    color = Azul
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    "Es el correo con el que inicias sesion.",
                    color = Gris,
                    fontSize = 12.sp
                )
                Spacer(Modifier.height(6.dp))
                OutlinedTextField(
                    value = nuevoCorreo,
                    onValueChange = { nuevoCorreo = it },
                    label = { Text("Correo electronico") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    modifier = Modifier.fillMaxWidth()
                )
                if (!errorCorreo.isNullOrBlank()) {
                    Spacer(Modifier.height(6.dp))
                    Text(errorCorreo ?: "", color = Rojo, fontSize = 13.sp)
                }
                if (!avisoCorreo.isNullOrBlank()) {
                    Spacer(Modifier.height(6.dp))
                    Text(avisoCorreo ?: "", color = Verde, fontSize = 13.sp)
                }
                Spacer(Modifier.height(10.dp))
                Button(
                    enabled = !guardando && nuevoCorreo.isNotBlank() && nuevoCorreo != correo,
                    shape = FormaSevetco,
                    onClick = {
                        guardando = true
                        errorCorreo = null
                        avisoCorreo = null
                        alcance.launch {
                            try {
                                datos.cambiarCorreo(uid, nuevoCorreo.trim())
                                avisoCorreo = "Te enviamos un correo a " +
                                    nuevoCorreo.trim() +
                                    " para confirmar el cambio. Abre ese enlace y " +
                                    "quedara aplicado."
                                nuevoCorreo = correo.orEmpty()
                            } catch (e: Exception) {
                                errorCorreo = when {
                                    e.message?.contains("recent", true) == true ->
                                        "Por seguridad, cierra sesion y vuelve a entrar antes de cambiarlo."
                                    e.message?.contains("already in use", true) == true ->
                                        "Ese correo ya tiene una cuenta."
                                    e.message?.contains("badly formatted", true) == true ->
                                        "El correo no es valido."
                                    e.message?.contains("not allowed", true) == true ->
                                        "Firebase no permitio el cambio. Intenta de nuevo."
                                    else -> e.message
                                }
                            }
                            guardando = false
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) { Text("Guardar correo") }
            }
        }

        Spacer(Modifier.height(16.dp))

        // ------------------------ Contrasena ------------------------
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                Text(
                    "Cambiar contrase\u00f1a",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp,
                    color = Azul
                )
                Spacer(Modifier.height(6.dp))

                if (!tieneClave) {
                    Text(
                        "Tu cuenta entra con Google, por eso no tiene contrase\u00f1a que cambiar.",
                        color = Gris,
                        fontSize = 13.sp
                    )
                } else {
                    CampoClave(
                        valor = actual,
                        onCambio = { actual = it },
                        etiqueta = "Contrase\u00f1a actual",
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(8.dp))
                    CampoClave(
                        valor = nueva,
                        onCambio = { nueva = it },
                        etiqueta = "Nueva contrase\u00f1a",
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(8.dp))
                    CampoClave(
                        valor = repetir,
                        onCambio = { repetir = it },
                        etiqueta = "Repetir la nueva contrase\u00f1a",
                        modifier = Modifier.fillMaxWidth()
                    )

                    if (!errorClave.isNullOrBlank()) {
                        Spacer(Modifier.height(6.dp))
                        Text(errorClave ?: "", color = Rojo, fontSize = 13.sp)
                    }
                    if (!avisoClave.isNullOrBlank()) {
                        Spacer(Modifier.height(6.dp))
                        Text(avisoClave ?: "", color = Verde, fontSize = 13.sp)
                    }

                    Spacer(Modifier.height(10.dp))
                    Button(
                        enabled = !guardando && actual.isNotBlank()
                            && nueva.length >= 6 && nueva == repetir,
                        shape = FormaSevetco,
                        onClick = {
                            guardando = true
                            errorClave = null
                            avisoClave = null
                            alcance.launch {
                                try {
                                    datos.cambiarClave(correo, actual, nueva)
                                    actual = ""
                                    nueva = ""
                                    repetir = ""
                                    avisoClave = "Contrase\u00f1a actualizada."
                                } catch (e: Exception) {
                                    errorClave = when {
                                        e.message?.contains("password is invalid", true) == true ->
                                            "La contrase\u00f1a actual no es correcta."
                                        e.message?.contains("recent", true) == true ->
                                            "Por seguridad, cierra sesion y vuelve a entrar antes de cambiarla."
                                        e.message?.contains("at least 6", true) == true ->
                                            "La nueva contrase\u00f1a debe tener al menos 6 caracteres."
                                        else -> e.message
                                    }
                                }
                                guardando = false
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) { Text("Guardar contrase\u00f1a") }
                }
            }
        }

        Spacer(Modifier.height(24.dp))
    }
}
// ---------------------------------------------------------------------------
// Eventos y promociones (contenido de la pantalla de Inicio)
// ---------------------------------------------------------------------------

@Composable
private fun SeccionEventos(eventos: List<Evento>) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Filled.Event, contentDescription = null, tint = Azul)
            Spacer(Modifier.width(10.dp))
            Text(
                "Eventos",
                fontWeight = FontWeight.SemiBold,
                color = Azul,
                fontSize = 16.sp
            )
        }
        Spacer(Modifier.height(10.dp))

        if (eventos.isEmpty()) {
            Card(modifier = Modifier.fillMaxWidth()) {
                Text(
                    "Todav\u00eda no hay eventos publicados.",
                    color = Gris,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(16.dp)
                )
            }
        } else {
            eventos.forEach { evento ->
                TarjetaContenido(
                    nombre = evento.nombre,
                    fecha = evento.fecha,
                    horaInicio = evento.horaInicio,
                    horaFin = evento.horaFin,
                    texto = evento.texto,
                    imagen = evento.imagen
                )
                Spacer(Modifier.height(14.dp))
            }
        }
    }
}

@Composable
private fun SeccionPromociones(promociones: List<Promocion>) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Filled.LocalOffer, contentDescription = null, tint = Azul)
            Spacer(Modifier.width(10.dp))
            Text(
                "Promociones",
                fontWeight = FontWeight.SemiBold,
                color = Azul,
                fontSize = 16.sp
            )
        }
        Spacer(Modifier.height(10.dp))

        if (promociones.isEmpty()) {
            Card(modifier = Modifier.fillMaxWidth()) {
                Text(
                    "Todav\u00eda no hay promociones publicadas.",
                    color = Gris,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(16.dp)
                )
            }
        } else {
            promociones.forEach { promocion ->
                TarjetaContenido(
                    nombre = promocion.nombre,
                    fecha = promocion.fecha,
                    horaInicio = promocion.horaInicio,
                    horaFin = promocion.horaFin,
                    texto = promocion.texto,
                    descuento = promocion.descuento,
                    imagen = promocion.imagen
                )
                Spacer(Modifier.height(14.dp))
            }
        }
    }
}

@Composable
private fun TarjetaContenido(
    nombre: String,
    fecha: String?,
    horaInicio: String?,
    horaFin: String?,
    texto: String?,
    imagen: String?,
    descuento: String? = null
) {
    val forma = FormaSevetco

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 210.dp)
            .clip(forma)
            .background(Azul)
    ) {
        // La imagen llena toda la tarjeta.
        //
        // Cuando la sube el administrador desde la web llega como texto
        // (data:image/jpeg;base64,...) y se dibuja aqui. Si en cambio es una
        // direccion de internet, la carga Coil.
        when {
            imagen.isNullOrBlank() -> Unit
            imagen.startsWith("data:image") -> {
                val mapa = remember(imagen) { textoAFoto(imagen)?.asImageBitmap() }
                if (mapa != null) {
                    Image(
                        bitmap = mapa,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.matchParentSize()
                    )
                }
            }
            else -> coil.compose.AsyncImage(
                model = imagen,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.matchParentSize()
            )
        }

        // Velo oscuro en la parte baja para que el texto se lea.
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(
                    Brush.verticalGradient(
                        0.30f to Color.Transparent,
                        1f to Color(0xE6000000)
                    )
                )
        )

        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                nombre,
                color = Color.White,
                fontSize = 19.sp,
                fontWeight = FontWeight.Bold
            )

            val horario = listOfNotNull(horaInicio, horaFin)
                .joinToString(" - ")
                .ifBlank { null }
            val cuando = listOfNotNull(fecha, horario).joinToString("   ")
            if (cuando.isNotBlank()) {
                Spacer(Modifier.height(4.dp))
                Text(cuando, color = Color(0xFFEDEDED), fontSize = 13.sp)
            }

            if (!texto.isNullOrBlank()) {
                Spacer(Modifier.height(8.dp))
                Text(texto, color = Color.White, fontSize = 14.sp)
            }

            if (!descuento.isNullOrBlank()) {
                Spacer(Modifier.height(10.dp))
                Box(
                    modifier = Modifier
                        .clip(FormaSevetco)
                        .background(Color(0xFF2E7D53))
                        .padding(horizontal = 12.dp, vertical = 5.dp)
                ) {
                    Text(
                        "$descuento% de descuento",
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@Composable
private fun PantallaMascotas(datos: Datos, uid: String) {
    var mascotas by remember { mutableStateOf<List<Mascota>>(emptyList()) }
    var cargando by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var recarga by remember { mutableStateOf(0) }
    var idEnEdicion by rememberSaveable { mutableStateOf<String?>(null) }
    var mostrarFormulario by rememberSaveable { mutableStateOf(false) }
    val alcance = rememberCoroutineScope()
    val enEdicion = mascotas.firstOrNull { it.id == idEnEdicion }

    LaunchedEffect(recarga) {
        cargando = true
        error = null
        try {
            mascotas = datos.mascotas(uid)
        } catch (e: Exception) {
            error = e.message
        }
        cargando = false
    }

    Box(modifier = Modifier.fillMaxSize()) {
        when {
            cargando -> CircularProgressIndicator(Modifier.align(Alignment.Center))
            error != null -> Text(
                error ?: "",
                color = Rojo,
                modifier = Modifier.align(Alignment.Center).padding(20.dp)
            )
            mascotas.isEmpty() -> Text(
                "Todav\u00eda no tienes mascotas registradas.",
                color = Gris,
                modifier = Modifier.align(Alignment.Center).padding(20.dp)
            )
            else -> LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(mascotas) { mascota ->
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            FotoMascota(mascota.foto)
                            Spacer(Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(mascota.nombre, fontWeight = FontWeight.SemiBold)
                                val detalle = listOfNotNull(
                                    mascota.especie,
                                    mascota.raza,
                                    mascota.fechaNacimiento?.let { "Edad: ${textoEdad(it)}" }
                                ).joinToString(" - ")
                                if (detalle.isNotBlank()) {
                                    Text(detalle, color = Gris, fontSize = 13.sp)
                                }
                            }
                            IconButton(onClick = {
                                idEnEdicion = mascota.id
                                mostrarFormulario = true
                            }) {
                                Icon(Icons.Filled.Edit, contentDescription = "Editar", tint = Azul)
                            }
                            IconButton(onClick = {
                                alcance.launch {
                                    try {
                                        datos.eliminarMascota(mascota.id)
                                        recarga++
                                    } catch (e: Exception) {
                                        error = e.message
                                    }
                                }
                            }) {
                                Icon(Icons.Filled.Delete, contentDescription = "Eliminar", tint = Rojo)
                            }
                        }
                    }
                }
            }
        }

        Button(
            onClick = {
                idEnEdicion = null
                mostrarFormulario = true
            },
            shape = FormaSevetco,
            modifier = Modifier.align(Alignment.BottomEnd).padding(20.dp)
        ) {
            Icon(Icons.Filled.Add, contentDescription = null)
            Spacer(Modifier.width(6.dp))
            Text("Nueva")
        }
    }

    if (mostrarFormulario) {
        FormularioMascota(
            mascota = enEdicion,
            errorExterno = error,
            onCancelar = { mostrarFormulario = false },
            onGuardar = { nombre, especie, raza, nacimiento, foto ->
                alcance.launch {
                    try {
                        datos.guardarMascota(uid, enEdicion?.id, nombre, especie, raza, nacimiento, foto)
                        error = null
                        mostrarFormulario = false
                        recarga++
                    } catch (e: Exception) {
                        error = e.message
                    }
                }
            }
        )
    }
}

@Composable
private fun FormularioMascota(
    mascota: Mascota?,
    errorExterno: String?,
    onCancelar: () -> Unit,
    onGuardar: (String, String?, String?, String?, String?) -> Unit
) {
    // rememberSaveable: si el sistema cierra la app mientras esta abierta la
    // camara, los datos escritos no se pierden al volver.
    var nombre by rememberSaveable { mutableStateOf(mascota?.nombre.orEmpty()) }
    var especie by rememberSaveable { mutableStateOf(mascota?.especie.orEmpty()) }
    var raza by rememberSaveable { mutableStateOf(mascota?.raza.orEmpty()) }
    var nacimiento by rememberSaveable { mutableStateOf(mascota?.fechaNacimiento.orEmpty()) }
    var foto by rememberSaveable { mutableStateOf(mascota?.foto) }
    var mostrarEdad by rememberSaveable { mutableStateOf(false) }
    val contexto = LocalContext.current

    // TakePicturePreview abre la camara del dispositivo. No hay acceso a la
    // galeria, por lo que la foto siempre se toma en el momento.
    val camara = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicturePreview()
    ) { capturada ->
        if (capturada != null) foto = fotoATexto(capturada)
    }

    AlertDialog(
        onDismissRequest = onCancelar,
        title = { Text(if (mascota == null) "Nueva mascota" else "Editar mascota") },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    FotoMascota(foto, 48)
                    Spacer(Modifier.width(12.dp))
                    OutlinedButton(
                        onClick = { camara.launch(null) },
                        shape = FormaSevetco
                    ) {
                        Icon(Icons.Filled.Add, contentDescription = null)
                        Spacer(Modifier.width(6.dp))
                        Text(if (foto == null) "Tomar foto" else "Cambiar foto")
                    }
                }
                Spacer(Modifier.height(12.dp))

                OutlinedTextField(
                    value = nombre,
                    onValueChange = { nombre = it },
                    label = { Text("Nombre") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = especie,
                    onValueChange = { especie = it },
                    label = { Text("Especie (perro, gato...)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = raza,
                    onValueChange = { raza = it },
                    label = { Text("Raza") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = if (nacimiento.isBlank()) "" else textoEdad(nacimiento),
                    onValueChange = { },
                    label = { Text("Edad") },
                    readOnly = true,
                    modifier = Modifier.fillMaxWidth()
                )
                TextButton(onClick = { mostrarEdad = true }) { Text("Elegir edad") }

                if (!errorExterno.isNullOrBlank()) {
                    Spacer(Modifier.height(8.dp))
                    Text(errorExterno, color = Rojo, fontSize = 12.sp)
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                if (nombre.isNotBlank()) {
                    onGuardar(
                        nombre.trim(),
                        especie.ifBlank { null },
                        raza.ifBlank { null },
                        nacimiento.ifBlank { null },
                        foto
                    )
                }
            }) { Text("Guardar") }
        },
        dismissButton = { TextButton(onClick = onCancelar) { Text("Cancelar") } }
    )

    if (mostrarEdad) {
        DialogoEdad(
            nacimiento = nacimiento,
            onCancelar = { mostrarEdad = false },
            onAceptar = { fecha ->
                nacimiento = fecha
                mostrarEdad = false
            }
        )
    }
}

/** Selector de edad: de 0 a 16 anios y de 0 a 11 meses. */
@Composable
private fun DialogoEdad(
    nacimiento: String?,
    onCancelar: () -> Unit,
    onAceptar: (String) -> Unit
) {
    val edadActual = edadDe(nacimiento) ?: (0 to 0)
    var anios by rememberSaveable { mutableStateOf(edadActual.first.toString()) }
    var meses by rememberSaveable { mutableStateOf(edadActual.second.toString()) }

    val aniosNumero = anios.toIntOrNull()?.coerceIn(0, 16) ?: 0
    val mesesNumero = meses.toIntOrNull()?.coerceIn(0, 11) ?: 0
    val fechaResultante = fechaDesdeEdad(aniosNumero, mesesNumero)

    AlertDialog(
        onDismissRequest = onCancelar,
        title = { Text("Edad de la mascota") },
        text = {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = anios,
                        onValueChange = { texto ->
                            anios = texto.filter { it.isDigit() }.take(2)
                        },
                        label = { Text("A\u00f1os (0 a 16)") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.width(150.dp)
                    )
                    Spacer(Modifier.width(12.dp))
                    OutlinedTextField(
                        value = meses,
                        onValueChange = { texto ->
                            meses = texto.filter { it.isDigit() }.take(2)
                        },
                        label = { Text("Meses (0 a 11)") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.width(150.dp)
                    )
                }
                Spacer(Modifier.height(12.dp))
                Text(
                    "Nace aproximadamente el " +
                        SimpleDateFormat("dd/MM/yyyy", Locale("es", "CO"))
                            .format(SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(fechaResultante)!!),
                    color = Gris,
                    fontSize = 13.sp
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { onAceptar(fechaResultante) }) { Text("Aceptar") }
        },
        dismissButton = { TextButton(onClick = onCancelar) { Text("Cancelar") } }
    )
}

/**
 * Foto del usuario. Si la cuenta entro con Google se usa la foto de esa
 * cuenta (queda guardada en la ficha); si no tiene, se muestra el logo.
 */
@Composable
private fun AvatarUsuario(foto: String?, tamano: Int = 48) {
    val medida = Modifier.size(tamano.dp)
    val esBase64 = !foto.isNullOrBlank() && foto.startsWith("data:image")
    val dibujo = remember(foto) {
        if (esBase64) textoAFoto(foto)?.asImageBitmap() else null
    }

    when {
        dibujo != null -> Image(
            bitmap = dibujo,
            contentDescription = "Foto del usuario",
            contentScale = ContentScale.Crop,
            modifier = medida.clip(CircleShape)
        )

        esBase64 -> Image(
            painter = painterResource(R.drawable.logo_app),
            contentDescription = null,
            modifier = medida
        )

        !foto.isNullOrBlank() -> coil.compose.AsyncImage(
            model = foto,
            contentDescription = "Foto del usuario",
            contentScale = ContentScale.Crop,
            modifier = medida.clip(CircleShape)
        )

        else -> Image(
            painter = painterResource(R.drawable.logo_app),
            contentDescription = null,
            modifier = medida
        )
    }
}

/**
 * Foto de seguridad del usuario: se pide al entrar. La foto se toma con la
 * camara del dispositivo (no hay acceso a la galeria) y queda guardada en la
 * ficha del usuario.
 */
@Composable
private fun DialogoFotoSeguridad(
    datos: Datos,
    uid: String,
    onListo: (String) -> Unit,
    onSalir: () -> Unit
) {
    val alcance = rememberCoroutineScope()
    var foto by remember { mutableStateOf<String?>(null) }
    var guardando by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    val camara = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicturePreview()
    ) { capturada ->
        if (capturada != null) foto = fotoATexto(capturada)
    }

    AlertDialog(
        onDismissRequest = { },
        properties = DialogProperties(
            dismissOnBackPress = false,
            dismissOnClickOutside = false
        ),
        title = { Text("Foto de seguridad") },
        text = {
            Column {
                Text("Toma una foto con la c\u00e1mara para continuar.", fontSize = 14.sp)
                Spacer(Modifier.height(14.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    AvatarUsuario(foto, 48)
                    Spacer(Modifier.width(12.dp))
                    OutlinedButton(
                        onClick = { camara.launch(null) },
                        enabled = !guardando,
                        shape = FormaSevetco
                    ) {
                        Icon(Icons.Filled.PhotoCamera, contentDescription = null)
                        Spacer(Modifier.width(6.dp))
                        Text(if (foto == null) "Tomar foto" else "Repetir")
                    }
                }
                if (!error.isNullOrBlank()) {
                    Spacer(Modifier.height(8.dp))
                    Text(error ?: "", color = Rojo, fontSize = 13.sp)
                }
            }
        },
        confirmButton = {
            TextButton(
                enabled = foto != null && !guardando,
                onClick = {
                    val tomada = foto ?: return@TextButton
                    guardando = true
                    error = null
                    alcance.launch {
                        try {
                            datos.guardarFotoUsuario(uid, tomada)
                            onListo(tomada)
                        } catch (e: Exception) {
                            error = e.message
                        }
                        guardando = false
                    }
                }
            ) { Text("Guardar") }
        },
        dismissButton = {
            TextButton(onClick = onSalir) { Text("Cerrar sesi\u00f3n", color = Rojo) }
        }
    )
}

@Composable
private fun FotoMascota(foto: String?, tamano: Int = 48) {
    val imagen = remember(foto) { textoAFoto(foto)?.asImageBitmap() }
    if (imagen != null) {
        Image(
            bitmap = imagen,
            contentDescription = "Foto de la mascota",
            contentScale = ContentScale.Crop,
            modifier = Modifier.size(tamano.dp).clip(CircleShape)
        )
    } else {
        Box(
            modifier = Modifier
                .size(tamano.dp)
                .clip(CircleShape)
                .background(AzulClaro),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Filled.Pets, contentDescription = null, tint = Azul)
        }
    }
}

@Composable
private fun PantallaServicios(datos: Datos) {
    var servicios by remember { mutableStateOf<List<Servicio>>(emptyList()) }
    var cargando by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        try {
            servicios = datos.servicios()
        } catch (e: Exception) {
            error = e.message
        }
        cargando = false
    }

    if (cargando) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        return
    }

    if (error != null) {
        Box(Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
            Text(error ?: "", color = Rojo, fontSize = 14.sp)
        }
        return
    }

    if (servicios.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Todav\u00eda no hay servicios cargados.", color = Gris)
        }
        return
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(servicios) { servicio ->
            Card(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Filled.MedicalServices, contentDescription = null, tint = Azul)
                    Spacer(Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(servicio.nombre, fontWeight = FontWeight.SemiBold)
                        Text(
                            "${pesos(servicio.precioBase)} - ${servicio.duracionMinutos} min",
                            color = Gris,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PantallaPlanes(datos: Datos, uid: String) {
    var planes by remember { mutableStateOf<List<Plan>>(emptyList()) }
    var cargando by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var seleccionado by remember { mutableStateOf<Plan?>(null) }
    var incluidos by remember { mutableStateOf<List<Pair<String, Int>>>(emptyList()) }
    var suscripciones by remember { mutableStateOf<List<Suscripcion>>(emptyList()) }
    var recarga by remember { mutableStateOf(0) }
    var porAdquirir by remember { mutableStateOf<Plan?>(null) }
    val alcance = rememberCoroutineScope()

    LaunchedEffect(recarga) {
        try {
            planes = datos.planes()
            suscripciones = datos.suscripciones(uid)
        } catch (e: Exception) {
            error = e.message
        }
        cargando = false
    }

    LaunchedEffect(seleccionado) {
        incluidos = seleccionado?.let {
            try { datos.serviciosDePlan(it.id) } catch (e: Exception) { emptyList() }
        } ?: emptyList()
    }

    if (cargando) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        return
    }

    if (error != null) {
        Box(Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
            Text(error ?: "", color = Rojo, fontSize = 14.sp)
        }
        return
    }

    if (planes.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Todav\u00eda no hay planes cargados.", color = Gris)
        }
        return
    }

    val planActivo = suscripciones
        .firstOrNull { it.estado == "ACTIVA" }
        ?.let { suscripcion -> planes.firstOrNull { it.id == suscripcion.idPlan } }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        if (planActivo != null) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Azul)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Filled.Storefront,
                            contentDescription = null,
                            tint = Color.White
                        )
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text(
                                "Tu plan activo",
                                color = Color(0xFFC9D4EA),
                                fontSize = 12.sp
                            )
                            Text(
                                planActivo.nombre,
                                color = Color.White,
                                fontSize = 17.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }

        items(planes) { plan ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = if (seleccionado?.id == plan.id) AzulClaro else Color.White
                )
            ) {
                Column(modifier = Modifier.fillMaxWidth().padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Storefront, contentDescription = null, tint = Azul)
                        Spacer(Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(plan.nombre, fontWeight = FontWeight.SemiBold)
                            Text(
                                "${pesos(plan.precio)} - ${plan.vigenciaDias} dias",
                                color = Gris,
                                fontSize = 13.sp
                            )
                        }
                        TextButton(onClick = {
                            seleccionado = if (seleccionado?.id == plan.id) null else plan
                        }) { Text(if (seleccionado?.id == plan.id) "Ocultar" else "Ver servicios") }
                    }

                    if (seleccionado?.id == plan.id) {
                        Spacer(Modifier.height(8.dp))
                        if (incluidos.isEmpty()) {
                            Text("Este plan no tiene servicios asociados.", color = Gris, fontSize = 13.sp)
                        } else {
                            incluidos.forEach { (nombre, cantidad) ->
                                Text("• $nombre (x$cantidad)", fontSize = 14.sp)
                            }
                        }
                    }

                    Spacer(Modifier.height(4.dp))
                    if (planActivo?.id == plan.id) {
                        Text(
                            "Plan activo",
                            color = Verde,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    } else {
                        TextButton(
                            onClick = { porAdquirir = plan },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Adquirir este plan", color = Azul, fontSize = 14.sp)
                        }
                    }
                }
            }
        }
    }

    porAdquirir?.let { plan ->
        AlertDialog(
            onDismissRequest = { porAdquirir = null },
            title = { Text("Adquirir plan") },
            text = {
                Text(
                    "Vas a adquirir el ${plan.nombre} por ${pesos(plan.precio)}, " +
                        "con vigencia de ${plan.vigenciaDias} dias."
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    alcance.launch {
                        try {
                            datos.adquirirPlan(uid, plan)
                            porAdquirir = null
                            recarga++
                        } catch (e: Exception) {
                            error = e.message
                            porAdquirir = null
                        }
                    }
                }) { Text("Adquirir") }
            },
            dismissButton = {
                TextButton(onClick = { porAdquirir = null }) { Text("Cancelar") }
            }
        )
    }
}

@Composable
private fun PantallaCitas(datos: Datos, uid: String, esProveedor: Boolean = false) {
    var citas by remember { mutableStateOf<List<Cita>>(emptyList()) }
    var cargando by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var recarga by remember { mutableStateOf(0) }
    var porCancelar by remember { mutableStateOf<Cita?>(null) }
    var porReagendar by remember { mutableStateOf<Cita?>(null) }
    var porTrazar by remember { mutableStateOf<Cita?>(null) }
    var porVerRuta by remember { mutableStateOf<Cita?>(null) }
    val alcance = rememberCoroutineScope()

    LaunchedEffect(recarga, esProveedor) {
        error = null
        cargando = true
        try {
            citas = if (esProveedor) datos.citasAsignadas(uid) else datos.citas(uid)
        } catch (e: Exception) {
            error = e.message
        }
        cargando = false
    }

    if (cargando) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        return
    }

    if (error != null) {
        Box(Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
            Text(error ?: "", color = Rojo, fontSize = 14.sp)
        }
        return
    }

    if (citas.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(
                if (esProveedor) "Todav\u00eda no tienes servicios asignados."
                else "Todav\u00eda no tienes citas registradas.",
                color = Gris
            )
        }
        return
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(citas) { cita ->
            Column {
                TarjetaCita(cita, esProveedor)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    TextButton(onClick = { porTrazar = cita }) {
                        Text("Trazabilidad", color = Azul, fontSize = 13.sp)
                    }
                    // RF-17: la ruta aparece cuando la cita tiene desplazamiento.
                    if (cita.idRuta != null) {
                        TextButton(onClick = { porVerRuta = cita }) {
                            Text("Ver ruta", color = Azul, fontSize = 13.sp)
                        }
                    }
                    // Se puede reagendar o cancelar mientras la cita no haya
                    // terminado ni este ya cancelada.
                    if (!esProveedor && cita.estado != "FINALIZADA" && cita.estado != "CANCELADA") {
                        TextButton(onClick = { porReagendar = cita }) {
                            Text("Reagendar", color = Azul, fontSize = 13.sp)
                        }
                        TextButton(onClick = { porCancelar = cita }) {
                            Text("Cancelar", color = Rojo, fontSize = 13.sp)
                        }
                    }
                }
            }
        }
    }

    porReagendar?.let { cita ->
        DialogoReagendar(
            datos = datos,
            cita = cita,
            onCerrar = { porReagendar = null },
            onHecho = {
                porReagendar = null
                recarga++
            }
        )
    }

    porTrazar?.let { cita ->
        DialogoTrazabilidad(datos = datos, uid = uid, cita = cita) { porTrazar = null }
    }

    porVerRuta?.let { cita ->
        MapaRuta(cita) { porVerRuta = null }
    }

    porCancelar?.let { cita ->
        AlertDialog(
            onDismissRequest = { porCancelar = null },
            title = { Text("Cancelar cita") },
            text = {
                Text("Se cancelar\u00e1 la cita del ${fechaLegible(cita.fechaHora)}. Esta acci\u00f3n no se puede deshacer.")
            },
            confirmButton = {
                TextButton(onClick = {
                    alcance.launch {
                        try {
                            datos.cancelarCita(uid, cita.id, null)
                            porCancelar = null
                            recarga++
                        } catch (e: Exception) {
                            error = e.message
                            porCancelar = null
                        }
                    }
                }) { Text("S\u00ed, cancelar", color = Rojo) }
            },
            dismissButton = {
                TextButton(onClick = { porCancelar = null }) { Text("Volver") }
            }
        )
    }
}

/**
 * Reagendar muestra solo las horas libres del veterinario asignado
 * (horario de atencion de 8:00 a 18:00 y sin choques con otras citas).
 */
@Composable
private fun DialogoReagendar(
    datos: Datos,
    cita: Cita,
    onCerrar: () -> Unit,
    onHecho: () -> Unit
) {
    val contexto = LocalContext.current
    val alcance = rememberCoroutineScope()

    var dia by remember { mutableStateOf(Calendar.getInstance().time) }
    var libres by remember { mutableStateOf<List<Int>>(emptyList()) }
    var horaElegida by remember { mutableStateOf<Int?>(null) }
    var cargando by remember { mutableStateOf(true) }
    var guardando by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(dia.key()) {
        cargando = true
        horaElegida = null
        error = null
        try {
            libres = datos.horasLibres(cita.idProveedor, dia)
        } catch (e: Exception) {
            error = e.message
        }
        cargando = false
    }

    AlertDialog(
        onDismissRequest = onCerrar,
        title = { Text("Reagendar cita") },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    FotoMascota(cita.fotoMascota, 48)
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text(
                            cita.mascota ?: "",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 15.sp
                        )
                        Text(cita.servicio ?: "", color = Gris, fontSize = 13.sp)
                    }
                }
                Spacer(Modifier.height(12.dp))

                Text("Fecha", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        SimpleDateFormat("dd/MM/yyyy", Locale("es", "CO")).format(dia),
                        modifier = Modifier.weight(1f),
                        fontSize = 15.sp
                    )
                    TextButton(onClick = {
                        val base = Calendar.getInstance().apply { time = dia }
                        DatePickerDialog(
                            contexto,
                            { _, a, m, d ->
                                dia = Calendar.getInstance().apply {
                                    time = dia
                                    set(Calendar.YEAR, a)
                                    set(Calendar.MONTH, m)
                                    set(Calendar.DAY_OF_MONTH, d)
                                }.time
                            },
                            base.get(Calendar.YEAR),
                            base.get(Calendar.MONTH),
                            base.get(Calendar.DAY_OF_MONTH)
                        ).show()
                    }) { Text("Elegir fecha") }
                }

                Spacer(Modifier.height(8.dp))
                Text("Hora disponible", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                Spacer(Modifier.height(6.dp))

                if (cargando) {
                    CircularProgressIndicator(modifier = Modifier.size(28.dp))
                } else {
                    (Horario.HORA_INICIO until Horario.HORA_FIN).toList()
                        .chunked(4)
                        .forEach { fila ->
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                fila.forEach { hora ->
                                    val libre = libres.contains(hora)
                                    val elegida = horaElegida == hora
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(38.dp)
                                            .clip(FormaSevetco)
                                            .background(
                                                when {
                                                    elegida -> Azul
                                                    libre -> Color.White
                                                    else -> Color(0xFFEDEFF3)
                                                }
                                            )
                                            .border(
                                                1.dp,
                                                when {
                                                    elegida -> Azul
                                                    libre -> Color(0xFFDFE3EA)
                                                    else -> Color(0xFFEDEFF3)
                                                },
                                                FormaSevetco
                                            )
                                            .clickable(enabled = libre) { horaElegida = hora },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            "$hora:00",
                                            color = when {
                                                elegida -> Color.White
                                                libre -> Color(0xFF1A1A1A)
                                                else -> Color(0xFFB0B6C0)
                                            },
                                            fontSize = 14.sp
                                        )
                                    }
                                }
                                repeat(4 - fila.size) {
                                    Spacer(Modifier.weight(1f))
                                }
                            }
                        }
                }

                if (!error.isNullOrBlank()) {
                    Spacer(Modifier.height(8.dp))
                    Text(error ?: "", color = Rojo, fontSize = 13.sp)
                }
            }
        },
        confirmButton = {
            TextButton(
                enabled = !guardando && horaElegida != null,
                onClick = {
                    val hora = horaElegida ?: return@TextButton
                    guardando = true
                    error = null
                    val nueva = Calendar.getInstance().apply {
                        time = dia
                        set(Calendar.HOUR_OF_DAY, hora)
                        set(Calendar.MINUTE, 0)
                        set(Calendar.SECOND, 0)
                        set(Calendar.MILLISECOND, 0)
                    }.time
                    alcance.launch {
                        try {
                            datos.reagendarCita(
                                uid = com.google.firebase.auth.FirebaseAuth
                                    .getInstance().currentUser?.uid.orEmpty(),
                                idCita = cita.id,
                                idProveedor = cita.idProveedor,
                                nuevaFecha = nueva,
                                estadoActual = cita.estado
                            )
                            onHecho()
                        } catch (e: Exception) {
                            error = e.message
                        }
                        guardando = false
                    }
                }
            ) { Text("Reagendar") }
        },
        dismissButton = { TextButton(onClick = onCerrar) { Text("Cancelar") } }
    )
}

private fun Date.key(): Long = time / 86_400_000L

/** RF-15: el propietario consulta la trazabilidad de su cita. */
@Composable
private fun DialogoTrazabilidad(
    datos: Datos,
    uid: String,
    cita: Cita,
    onCerrar: () -> Unit
) {
    var eventos by remember { mutableStateOf<List<EventoCita>>(emptyList()) }
    var cargando by remember { mutableStateOf(true) }

    LaunchedEffect(cita.id) {
        eventos = try { datos.trazabilidad(uid, cita.id) } catch (e: Exception) { emptyList() }
        cargando = false
    }

    AlertDialog(
        onDismissRequest = onCerrar,
        title = { Text("Trazabilidad de la cita") },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    FotoMascota(cita.fotoMascota, 48)
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text(
                            cita.mascota ?: "",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 15.sp
                        )
                        Text(fechaLegible(cita.fechaHora), color = Gris, fontSize = 13.sp)
                    }
                }
                Spacer(Modifier.height(14.dp))

                when {
                    cargando -> CircularProgressIndicator(modifier = Modifier.size(28.dp))
                    eventos.isEmpty() -> Text(
                        "Todav\u00eda no hay eventos registrados.",
                        color = Gris,
                        fontSize = 14.sp
                    )
                    else -> eventos.forEachIndexed { indice, evento ->
                        Row {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Box(
                                    modifier = Modifier
                                        .size(12.dp)
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(if (indice == eventos.lastIndex) Azul else Color(0xFF9AA6BC))
                                )
                                if (indice != eventos.lastIndex) {
                                    Box(
                                        modifier = Modifier
                                            .width(2.dp)
                                            .height(34.dp)
                                            .background(Color(0xFFD7DEEA))
                                    )
                                }
                            }
                            Spacer(Modifier.width(12.dp))
                            Column(modifier = Modifier.padding(bottom = 8.dp)) {
                                Text(
                                    evento.estado,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Azul,
                                    fontSize = 14.sp
                                )
                                Text(
                                    fechaLegible(evento.fecha),
                                    color = Gris,
                                    fontSize = 12.sp
                                )
                                if (!evento.responsable.isNullOrBlank()) {
                                    Text(evento.responsable ?: "", color = Gris, fontSize = 12.sp)
                                }
                                if (!evento.nota.isNullOrBlank()) {
                                    Text(
                                        evento.nota ?: "",
                                        color = Azul,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = onCerrar) { Text("Cerrar") } }
    )
}

@Composable
private fun TarjetaCita(cita: Cita, esProveedor: Boolean = false) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.fillMaxWidth().padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                FotoMascota(cita.fotoMascota, 48)
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        cita.mascota ?: "-",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp
                    )
                    Text(fechaLegible(cita.fechaHora), color = Gris, fontSize = 13.sp)
                }
                Text(
                    cita.estado,
                    color = colorEstado(cita.estado),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
            Spacer(Modifier.height(8.dp))
            Text("Servicio: ${cita.servicio ?: "-"}", fontSize = 14.sp)
            val fotoPersona = if (esProveedor) cita.fotoPropietario else cita.fotoResponsable
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (!fotoPersona.isNullOrBlank()) {
                    AvatarUsuario(fotoPersona, 48)
                    Spacer(Modifier.width(8.dp))
                }
                Text(
                    if (esProveedor) {
                        "Due\u00f1o: ${cita.propietario ?: "-"}"
                    } else {
                        "Responsable: ${cita.responsable ?: "Por asignar"}"
                    },
                    fontSize = 14.sp
                )
            }
            if (!cita.destino.isNullOrBlank()) {
                Spacer(Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Filled.LocationOn,
                        contentDescription = null,
                        tint = Gris,
                        modifier = Modifier.height(16.dp).width(16.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(cita.destino, color = Gris, fontSize = 13.sp)
                }
            }
        }
    }
}

@Composable
private fun PantallaHistorial(datos: Datos, uid: String, onCerrar: () -> Unit) {
    var atenciones by remember { mutableStateOf<List<Atencion>>(emptyList()) }
    var cargando by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        atenciones = try { datos.atenciones(uid) } catch (e: Exception) { emptyList() }
        cargando = false
    }

    AlertDialog(
        onDismissRequest = onCerrar,
        title = { Text("Historial de tratamientos") },
        text = {
            when {
                cargando -> CircularProgressIndicator()
                atenciones.isEmpty() -> Text("Todav\u00eda no hay tratamientos registrados.", color = Gris)
                else -> Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    atenciones.forEach { atencion ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            FotoMascota(atencion.fotoMascota, 48)
                            Spacer(Modifier.width(10.dp))
                            Column {
                                Text(
                                    atencion.mascota ?: "-",
                                    fontWeight = FontWeight.SemiBold,
                                    color = Azul,
                                    fontSize = 15.sp
                                )
                                Text(
                                    fechaLegible(atencion.fecha),
                                    color = Gris,
                                    fontSize = 12.sp
                                )
                            }
                        }
                        Spacer(Modifier.height(6.dp))
                        Text("Diagn\u00f3stico: ${atencion.diagnostico ?: "-"}", fontSize = 14.sp)
                        Text("Tratamiento: ${atencion.tratamiento ?: "-"}", fontSize = 14.sp)
                        if (!atencion.recomendaciones.isNullOrBlank()) {
                            Text("Recomendaciones: ${atencion.recomendaciones}", fontSize = 14.sp)
                        }
                        Spacer(Modifier.height(12.dp))
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = onCerrar) { Text("Cerrar") } }
    )
}

@Composable
private fun PantallaAgendar(datos: Datos, uid: String, onCerrar: () -> Unit) {
    val contexto = LocalContext.current
    val alcance = rememberCoroutineScope()

    var mascotas by remember { mutableStateOf<List<Mascota>>(emptyList()) }
    var servicios by remember { mutableStateOf<List<Servicio>>(emptyList()) }
    var cargando by remember { mutableStateOf(true) }

    var mascotaElegida by remember { mutableStateOf<Mascota?>(null) }
    var servicioElegido by remember { mutableStateOf<Servicio?>(null) }

    val ahora = remember { Calendar.getInstance() }
    var anio by remember { mutableStateOf(ahora.get(Calendar.YEAR)) }
    var mes by remember { mutableStateOf(ahora.get(Calendar.MONTH)) }
    var dia by remember { mutableStateOf(ahora.get(Calendar.DAY_OF_MONTH)) }
    var hora by remember { mutableStateOf(hora12(ahora).toString()) }
    var minuto by remember { mutableStateOf(String.format(Locale.US, "%02d", ahora.get(Calendar.MINUTE))) }
    var esPm by remember { mutableStateOf(ahora.get(Calendar.HOUR_OF_DAY) >= 12) }

    var direccion by remember { mutableStateOf("") }
    var gps by remember { mutableStateOf<Pair<Double, Double>?>(null) }
    var mostrarMapa by remember { mutableStateOf(false) }
    var guardando by remember { mutableStateOf(false) }
    var mensaje by remember { mutableStateOf<String?>(null) }

    // Guarda el punto elegido y muestra la direccion legible (calle, barrio...).
    val aplicarUbicacion: (Pair<Double, Double>, String?) -> Unit = { punto, texto ->
        gps = punto
        direccion = texto ?: String.format(Locale.US, "%.6f, %.6f", punto.first, punto.second)
        mensaje = null
    }

    LaunchedEffect(Unit) {
        try {
            mascotas = datos.mascotas(uid)
            servicios = datos.servicios()
            // Con una sola opcion la eleccion es automatica.
            if (mascotas.size == 1) mascotaElegida = mascotas.first()
            if (servicios.size == 1) servicioElegido = servicios.first()
        } catch (e: Exception) {
            mensaje = e.message
        }
        cargando = false
    }

    // Formulario por pasos: encabezado, cuerpo desplazable y pie con acciones.
    Dialog(
        onDismissRequest = onCerrar,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Column(modifier = Modifier.fillMaxSize().background(Fondo)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Azul)
                    .padding(horizontal = 6.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onCerrar) {
                    Icon(Icons.Filled.Close, contentDescription = "Cerrar", tint = Color.White)
                }
                Text(
                    "Agendar cita",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f)
                )
            }

            if (cargando) {
                Box(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) { CircularProgressIndicator() }
            } else {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (mascotas.isEmpty()) {
                        Text(
                            "Primero registra una mascota en la secci\u00f3n Mascotas.",
                            color = Rojo,
                            fontSize = 14.sp
                        )
                    }

                    PasoFormulario("1", "Mascota") {
                        mascotas.forEach { mascota ->
                            OpcionSeleccion(
                                texto = mascota.nombre,
                                activo = mascotaElegida?.id == mascota.id,
                                foto = mascota.foto
                            ) { mascotaElegida = mascota }
                        }
                    }

                    PasoFormulario("2", "Servicio") {
                        servicios.forEach { servicio ->
                            OpcionSeleccion(
                                texto = servicio.nombre,
                                detalle = pesos(servicio.precioBase),
                                activo = servicioElegido?.id == servicio.id
                            ) { servicioElegido = servicio }
                        }
                    }

                    PasoFormulario("3", "Fecha y hora") {
                        OutlinedButton(
                            onClick = {
                                DatePickerDialog(
                                    contexto,
                                    { _, a, m, d ->
                                        anio = a
                                        mes = m
                                        dia = d
                                    },
                                    anio,
                                    mes,
                                    dia
                                ).show()
                            },
                            shape = FormaSevetco,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                Icons.Filled.CalendarMonth,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                String.format(Locale.US, "%02d/%02d/%d", dia, mes + 1, anio),
                                fontSize = 15.sp
                            )
                        }
                        Spacer(Modifier.height(12.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            OutlinedTextField(
                                value = hora,
                                onValueChange = { texto -> hora = texto.filter { it.isDigit() }.take(2) },
                                label = { Text("Hora") },
                                singleLine = true,
                                shape = FormaSevetco,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.width(80.dp)
                            )
                            Text(
                                ":",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = Azul,
                                modifier = Modifier.padding(horizontal = 8.dp)
                            )
                            OutlinedTextField(
                                value = minuto,
                                onValueChange = { texto -> minuto = texto.filter { it.isDigit() }.take(2) },
                                label = { Text("Min") },
                                singleLine = true,
                                shape = FormaSevetco,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.width(80.dp)
                            )
                            Spacer(Modifier.weight(1f))
                            SelectorAmPm(esPm) { esPm = it }
                        }
                    }

                    PasoFormulario("4", "Direcci\u00f3n del servicio") {
                        OutlinedTextField(
                            value = direccion,
                            onValueChange = { direccion = it },
                            label = { Text("Direcci\u00f3n del domicilio") },
                            shape = FormaSevetco,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(Modifier.height(12.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                        OutlinedButton(
                            onClick = {
                                if (!Ubicacion.tienePermiso(contexto)) {
                                    mensaje = "Concede el permiso de ubicaci\u00f3n a SEVETCO."
                                } else {
                                    alcance.launch {
                                        val punto = Ubicacion.obtener(contexto)
                                        if (punto == null) {
                                            mensaje = "No se pudo obtener la ubicaci\u00f3n."
                                        } else {
                                            val texto = Geocodigo.direccion(punto.first, punto.second)
                                            aplicarUbicacion(punto, texto)
                                        }
                                    }
                                }
                            },
                            shape = FormaSevetco,
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                Icons.Filled.MyLocation,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(
                                "Mi ubicaci\u00f3n",
                                fontSize = 12.sp,
                                maxLines = 1,
                                softWrap = false
                            )
                        }
                        OutlinedButton(
                            onClick = { mostrarMapa = true },
                            shape = FormaSevetco,
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                Icons.Filled.LocationOn,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(Modifier.width(6.dp))
                            Text("Ver mapa", fontSize = 12.sp, maxLines = 1, softWrap = false)
                        }
                        }
                    }

                    if (!mensaje.isNullOrBlank()) {
                        Text(mensaje ?: "", color = Rojo, fontSize = 13.sp)
                    }
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onCerrar,
                    shape = FormaSevetco,
                    modifier = Modifier.weight(1f)
                ) { Text("Cancelar") }

                Button(
                    enabled = !guardando && mascotaElegida != null && servicioElegido != null,
                    shape = FormaSevetco,
                    onClick = {
                    guardando = true
                    mensaje = null
                    val fecha = Calendar.getInstance().apply {
                        set(
                            anio,
                            mes,
                            dia,
                            hora24(hora, esPm),
                            minuto.toIntOrNull()?.coerceIn(0, 59) ?: 0,
                            0
                        )
                        set(Calendar.MILLISECOND, 0)
                    }
                    alcance.launch {
                        try {
                            datos.crearCita(
                                uid = uid,
                                idMascota = mascotaElegida!!.id,
                                idServicio = servicioElegido!!.id,
                                fecha = fecha.time,
                                destino = direccion.ifBlank { null },
                                lat = gps?.first,
                                lng = gps?.second
                            )
                            onCerrar()
                        } catch (e: Exception) {
                            mensaje = e.message
                        }
                        guardando = false
                    }
                    },
                    modifier = Modifier.weight(1f)
                ) { Text(if (guardando) "Agendando..." else "Agendar") }
            }
        }
    }

    if (mostrarMapa) {
        MapaUbicacion(
            inicial = gps,
            onAceptar = { punto, texto ->
                aplicarUbicacion(punto, texto)
                mostrarMapa = false
            },
            onCerrar = { mostrarMapa = false }
        )
    }
}

/**
 * RF-17: ruta del servicio. El primer punto es donde esta el veterinario y el
 * segundo el domicilio de la mascota; se unen con la linea del recorrido.
 */
@Composable
private fun MapaRuta(cita: Cita, onCerrar: () -> Unit) {
    val latOrigen = cita.latOrigen
    val lngOrigen = cita.lngOrigen
    val latDestino = cita.latDestino
    val lngDestino = cita.lngDestino
    val vistaMapa = remember { arrayOfNulls<MapView>(1) }

    DisposableEffect(Unit) {
        onDispose {
            vistaMapa[0]?.let { vista ->
                vista.onPause()
                vista.onDestroy()
            }
        }
    }

    Dialog(
        onDismissRequest = onCerrar,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Column(modifier = Modifier.fillMaxSize().background(Color.White)) {
            Row(
                modifier = Modifier.fillMaxWidth().background(Azul).padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Filled.LocationOn, contentDescription = null, tint = Color.White)
                Spacer(Modifier.width(10.dp))
                Text(
                    "Ruta del servicio",
                    color = Color.White,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f)
                )
                TextButton(onClick = onCerrar) { Text("Cerrar", color = Color.White) }
            }

            Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                AndroidView(
                    modifier = Modifier.fillMaxSize(),
                    factory = { contexto ->
                        MapView(contexto).apply {
                            vistaMapa[0] = this
                            onCreate(null)
                            onResume()

                            getMapAsync { mapa ->
                                val salida = if (latOrigen != null && lngOrigen != null) {
                                    LatLng(latOrigen, lngOrigen)
                                } else {
                                    null
                                }
                                val llegada = if (latDestino != null && lngDestino != null) {
                                    LatLng(latDestino, lngDestino)
                                } else {
                                    null
                                }

                                salida?.let {
                                    mapa.addMarker(
                                        MarkerOptions().position(it).title("Veterinario")
                                    )
                                }
                                llegada?.let {
                                    mapa.addMarker(
                                        MarkerOptions().position(it).title("Domicilio")
                                    )
                                }

                                if (salida != null && llegada != null) {
                                    mapa.addPolyline(
                                        PolylineOptions()
                                            .add(salida, llegada)
                                            .width(10f)
                                            .color(android.graphics.Color.parseColor("#1F3864"))
                                    )
                                    val limites = LatLngBounds.builder()
                                        .include(salida)
                                        .include(llegada)
                                        .build()
                                    mapa.moveCamera(
                                        CameraUpdateFactory.newLatLngBounds(limites, 130)
                                    )
                                } else {
                                    val unico = salida ?: llegada ?: LatLng(4.6097, -74.0817)
                                    mapa.moveCamera(
                                        CameraUpdateFactory.newLatLngZoom(unico, 15f)
                                    )
                                }
                            }
                        }
                    }
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (!cita.fotoResponsable.isNullOrBlank()) {
                        AvatarUsuario(cita.fotoResponsable, 48)
                        Spacer(Modifier.width(8.dp))
                    }
                    Text(
                        "Veterinario: ${cita.responsable ?: "Por asignar"}",
                        fontSize = 13.sp
                    )
                }
                if (!cita.origen.isNullOrBlank()) {
                    Text("Sale de: ${cita.origen}", fontSize = 13.sp, color = Gris)
                } else {
                    Text(
                        "El veterinario todav\u00eda no marc\u00f3 su ubicaci\u00f3n.",
                        fontSize = 13.sp,
                        color = Naranja
                    )
                }
                if (!cita.destino.isNullOrBlank()) {
                    Text("Domicilio: ${cita.destino}", fontSize = 13.sp, color = Gris)
                }
                if (cita.distanciaKm != null) {
                    Text(
                        "Distancia: ${cita.distanciaKm} km",
                        fontSize = 13.sp,
                        color = Azul,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

/**
 * Mapa interactivo (OpenStreetMap) con un pin que se puede arrastrar.
 * Devuelve las coordenadas del punto elegido.
 */
@Composable
private fun MapaUbicacion(
    inicial: Pair<Double, Double>?,
    onAceptar: (Pair<Double, Double>, String?) -> Unit,
    onCerrar: () -> Unit
) {
    val puntoInicial = inicial ?: (4.6097 to -74.0817)
    var elegido by remember { mutableStateOf(puntoInicial) }
    var direccion by remember { mutableStateOf<String?>(null) }
    var buscando by remember { mutableStateOf(false) }
    var mapa by remember { mutableStateOf<GoogleMap?>(null) }
    var marcador by remember { mutableStateOf<Marker?>(null) }
    val alcance = rememberCoroutineScope()
    val vistaMapa = remember { arrayOfNulls<MapView>(1) }

    DisposableEffect(Unit) {
        onDispose {
            vistaMapa[0]?.let { vista ->
                vista.onPause()
                vista.onDestroy()
            }
        }
    }

    // Cada vez que el pin cambia se busca la direccion de ese punto.
    val moverPin: (Pair<Double, Double>) -> Unit = { punto ->
        elegido = punto
        buscando = true
        alcance.launch {
            direccion = Geocodigo.direccion(punto.first, punto.second)
            buscando = false
        }
    }

    Dialog(
        onDismissRequest = onCerrar,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Column(modifier = Modifier.fillMaxSize().background(Color.White)) {
            Row(
                modifier = Modifier.fillMaxWidth().background(Azul).padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Filled.LocationOn, contentDescription = null, tint = Color.White)
                Spacer(Modifier.width(10.dp))
                Text(
                    "Ubicaci\u00f3n del servicio",
                    color = Color.White,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f)
                )
                TextButton(onClick = onCerrar) { Text("Cerrar", color = Color.White) }
            }

            Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                AndroidView(
                    modifier = Modifier.fillMaxSize(),
                    factory = { contexto ->
                        MapView(contexto).apply {
                            vistaMapa[0] = this
                            onCreate(null)
                            onResume()

                            getMapAsync { googleMap ->
                                mapa = googleMap
                                val inicio = LatLng(puntoInicial.first, puntoInicial.second)

                                googleMap.moveCamera(
                                    CameraUpdateFactory.newLatLngZoom(inicio, 16f)
                                )

                                marcador = googleMap.addMarker(
                                    MarkerOptions()
                                        .position(inicio)
                                        .draggable(true)
                                        .title("Ubicaci\u00f3n del servicio")
                                )

                                // Arrastrar el pin o tocar el mapa mueve el punto.
                                googleMap.setOnMarkerDragListener(
                                    object : GoogleMap.OnMarkerDragListener {
                                        override fun onMarkerDragStart(punto: Marker) = Unit
                                        override fun onMarkerDrag(punto: Marker) = Unit
                                        override fun onMarkerDragEnd(punto: Marker) {
                                            moverPin(
                                                punto.position.latitude to
                                                    punto.position.longitude
                                            )
                                        }
                                    }
                                )

                                googleMap.setOnMapClickListener { ubicacion ->
                                    marcador?.position = ubicacion
                                    moverPin(ubicacion.latitude to ubicacion.longitude)
                                }
                            }
                        }
                    }
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Filled.LocationOn,
                        contentDescription = null,
                        tint = Azul,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        when {
                            buscando -> "Buscando la direcci\u00f3n..."
                            !direccion.isNullOrBlank() -> direccion.orEmpty()
                            else -> "Sin ubicaci\u00f3n seleccionada"
                        },
                        color = if (direccion.isNullOrBlank()) Gris else Azul,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Spacer(Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedButton(
                        onClick = {
                            elegido?.let { punto ->
                                val ubicacion = LatLng(punto.first, punto.second)
                                marcador?.position = ubicacion
                                mapa?.animateCamera(CameraUpdateFactory.newLatLng(ubicacion))
                            }
                        },
                        shape = FormaSevetco,
                        modifier = Modifier.weight(1f)
                    ) { Text("Centrar pin") }
                    Button(
                        onClick = { elegido?.let { onAceptar(it, direccion) } },
                        enabled = elegido != null,
                        shape = FormaSevetco,
                        modifier = Modifier.weight(1f)
                    ) { Text("Usar este punto") }
                }
            }
        }
    }
}

private fun hora12(calendario: Calendar): Int {
    val valor = calendario.get(Calendar.HOUR_OF_DAY) % 12
    return if (valor == 0) 12 else valor
}

private fun hora24(hora12: String, pm: Boolean): Int {
    val valor = (hora12.toIntOrNull() ?: 12).coerceIn(1, 12) % 12
    return if (pm) valor + 12 else valor
}

@Composable
private fun OpcionSeleccion(
    texto: String,
    activo: Boolean,
    foto: String? = null,
    detalle: String? = null,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp)
            .clip(FormaSevetco)
            .background(if (activo) AzulClaro else Color.White)
            .border(
                if (activo) 2.dp else 1.dp,
                if (activo) Azul else Color(0xFFDFE3EA),
                FormaSevetco
            )
            .clickable { onClick() }
            .padding(horizontal = 8.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = activo,
            onClick = onClick,
            colors = RadioButtonDefaults.colors(selectedColor = Azul, unselectedColor = Gris)
        )
        if (foto != null) {
            FotoMascota(foto, 48)
            Spacer(Modifier.width(10.dp))
        }
        Column {
            Text(
                texto,
                color = if (activo) Azul else Color(0xFF1A1A1A),
                fontSize = 14.sp,
                fontWeight = if (activo) FontWeight.SemiBold else FontWeight.Normal
            )
            if (!detalle.isNullOrBlank()) {
                Text(detalle, color = Gris, fontSize = 13.sp)
            }
        }
    }
}

/** Paso del formulario: numero, titulo y contenido dentro de una tarjeta. */
@Composable
private fun PasoFormulario(
    numero: String,
    titulo: String,
    contenido: @Composable ColumnScope.() -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.fillMaxWidth().padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier.size(22.dp).clip(CircleShape).background(Azul),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        numero,
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(Modifier.width(8.dp))
                Text(titulo, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = Azul)
            }
            Spacer(Modifier.height(12.dp))
            contenido()
        }
    }
}

/** Selector AM / PM en un solo bloque. */
@Composable
private fun SelectorAmPm(esPm: Boolean, onCambio: (Boolean) -> Unit) {
    Row(
        modifier = Modifier
            .clip(FormaSevetco)
            .border(1.dp, Color(0xFFDFE3EA), FormaSevetco)
            .background(Color.White)
            .padding(2.dp)
    ) {
        listOf("AM" to false, "PM" to true).forEach { (texto, valor) ->
            val activo = esPm == valor
            Box(
                modifier = Modifier
                    .width(46.dp)
                    .height(34.dp)
                    .background(if (activo) Azul else Color.Transparent)
                    .clickable { onCambio(valor) },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    texto,
                    color = if (activo) Color.White else Gris,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

