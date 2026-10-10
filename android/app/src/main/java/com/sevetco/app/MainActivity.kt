package com.sevetco.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.OAuthProvider

class MainActivity : ComponentActivity() {

    private val datos = Datos()

    private var disponible = false
    private var conectado by mutableStateOf(false)
    private var uid by mutableStateOf<String?>(null)
    private var mensaje by mutableStateOf<String?>(null)
    private var aviso by mutableStateOf<String?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        disponible = Config.disponible(this)
        if (disponible) {
            FirebaseAuth.getInstance().addAuthStateListener { autenticacion ->
                val usuario = autenticacion.currentUser
                uid = usuario?.uid
                conectado = usuario != null
            }
        }

        setContent {
            TemaSevetco {
                AppSevetco(
                    datos = datos,
                    configurado = disponible,
                    conectado = conectado,
                    uid = uid,
                    mensaje = mensaje,
                    aviso = aviso,
                    onGoogle = ::entrarConGoogle,
                    onCorreo = ::entrarConCorreo,
                    onRegistrar = ::registrarConCorreo,
                    onRecuperar = ::recuperarClave,
                    onSalir = {
                        FirebaseAuth.getInstance().signOut()
                        mensaje = null
                    }
                )
            }
        }
    }

    // -----------------------------------------------------------------------
    // Acceso con Google (Firebase Authentication)
    // -----------------------------------------------------------------------

    private fun entrarConGoogle() {
        aviso = null
        val proveedor = OAuthProvider.newBuilder("google.com")
            .setScopes(listOf("email", "profile"))
            .build()

        FirebaseAuth.getInstance()
            .startActivityForSignInWithProvider(this, proveedor)
            .addOnSuccessListener { mensaje = null }
            .addOnFailureListener { error -> mensaje = traducirGoogle(error.message) }
    }

    // -----------------------------------------------------------------------
    // Acceso con correo y contrasena
    // -----------------------------------------------------------------------

    private fun entrarConCorreo(correo: String, clave: String) {
        aviso = null
        FirebaseAuth.getInstance()
            .signInWithEmailAndPassword(correo, clave)
            .addOnSuccessListener { mensaje = null }
            .addOnFailureListener { error -> mensaje = traducir(error.message) }
    }

    private fun registrarConCorreo(correo: String, clave: String, nombre: String?) {
        aviso = null
        FirebaseAuth.getInstance()
            .createUserWithEmailAndPassword(correo, clave)
            .addOnSuccessListener { resultado ->
                nombre?.takeIf { it.isNotBlank() }?.let { valor ->
                    val perfil = com.google.firebase.auth.UserProfileChangeRequest.Builder()
                        .setDisplayName(valor)
                        .build()
                    resultado.user?.updateProfile(perfil)
                }
                mensaje = null
            }
            .addOnFailureListener { error -> mensaje = traducir(error.message) }
    }

    /** Envia el correo de Firebase para restablecer la contrasena. */
    private fun recuperarClave(correo: String) {
        if (correo.isBlank()) {
            mensaje = "Escribe primero tu correo electr\u00f3nico."
            return
        }
        mensaje = null
        FirebaseAuth.getInstance()
            .sendPasswordResetEmail(correo.trim())
            .addOnSuccessListener {
                aviso = "Te enviamos un correo para restablecer la contrase\u00f1a."
            }
            .addOnFailureListener { error ->
                aviso = null
                mensaje = traducir(error.message)
            }
    }

    /** Mensajes claros cuando falla el acceso con Google. */
    private fun traducirGoogle(texto: String?): String {
        val valor = texto ?: return "No se pudo completar el acceso con Google."
        return when {
            valor.contains("canceled", true) || valor.contains("cancelled", true) ->
                "Se cerro la ventana de Google antes de terminar. Intenta de nuevo."
            valor.contains("network", true) -> "Revisa la conexion a internet."
            valor.contains("10:", true) || valor.contains("DEVELOPER_ERROR", true) ->
                "La firma de la aplicacion no coincide con la registrada en Firebase."
            else -> valor
        }
    }

    private fun traducir(texto: String?): String {
        val valor = texto ?: return "No fue posible completar la operacion."
        return when {
            valor.contains("no user record", true) ||
                valor.contains("user not found", true) -> "No existe una cuenta con ese correo."
            valor.contains("password is invalid", true) ||
                valor.contains("credential is incorrect", true) -> "Correo o contrase\u00f1a incorrectos."
            valor.contains("already in use", true) -> "Ese correo ya tiene una cuenta."
            valor.contains("at least 6 characters", true) -> "La contrase\u00f1a debe tener al menos 6 caracteres."
            valor.contains("CONFIGURATION_NOT_FOUND", true) ||
                valor.contains("configuration not found", true) ->
                "El servicio de acceso no esta disponible."
            valor.contains("operation-not-allowed", true) ->
                "El acceso con correo no esta disponible."
            valor.contains("network", true) -> "Revisa la conexion a internet."
            else -> valor
        }
    }
}

