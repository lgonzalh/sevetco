package com.sevetco.app

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.util.concurrent.TimeUnit

/**
 * Convierte coordenadas en una direccion legible (calle, barrio, ciudad).
 *
 * Usa el servicio abierto Nominatim de OpenStreetMap. No necesita clave de API.
 * Su politica de uso pide maximo una consulta por segundo y un identificador de
 * aplicacion, por eso solo se llama cuando el usuario elige un punto.
 */
object Geocodigo {

    private val cliente = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()

    suspend fun direccion(lat: Double, lng: Double): String? = withContext(Dispatchers.IO) {
        try {
            val url = "https://nominatim.openstreetmap.org/reverse" +
                "?format=jsonv2&zoom=18&addressdetails=1&accept-language=es" +
                "&lat=$lat&lon=$lng"

            val peticion = Request.Builder()
                .url(url)
                .header("User-Agent", "SEVETCO/1.0 (proyecto academico)")
                .build()

            val texto = cliente.newCall(peticion).execute().use { respuesta ->
                if (!respuesta.isSuccessful) return@withContext null
                respuesta.body?.string().orEmpty()
            }

            val objeto = JSONObject(texto)
            val datos = objeto.optJSONObject("address")

            val partes = listOfNotNull(
                datos?.optString("road")?.ifBlank { null },
                datos?.optString("house_number")?.ifBlank { null },
                datos?.optString("neighbourhood")?.ifBlank { null }
                    ?: datos?.optString("suburb")?.ifBlank { null },
                datos?.optString("city")?.ifBlank { null }
                    ?: datos?.optString("town")?.ifBlank { null }
                    ?: datos?.optString("municipality")?.ifBlank { null }
            )

            when {
                partes.isNotEmpty() -> partes.joinToString(", ")
                else -> objeto.optString("display_name").ifBlank { null }
            }
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Busca las coordenadas de una direccion escrita (geocodificacion directa).
     * Se usa cuando una ruta antigua no tiene las coordenadas del domicilio.
     */
    suspend fun buscar(direccion: String): Pair<Double, Double>? = withContext(Dispatchers.IO) {
        val texto = direccion.trim()
        if (texto.isBlank()) return@withContext null
        try {
            val url = "https://nominatim.openstreetmap.org/search" +
                "?format=jsonv2&limit=1&accept-language=es&q=" +
                java.net.URLEncoder.encode("$texto, Colombia", "UTF-8")

            val peticion = Request.Builder()
                .url(url)
                .header("User-Agent", "SEVETCO/1.0 (proyecto academico)")
                .build()

            val respuesta = cliente.newCall(peticion).execute().use { resultado ->
                if (!resultado.isSuccessful) return@withContext null
                resultado.body?.string().orEmpty()
            }

            val lista = org.json.JSONArray(respuesta)
            if (lista.length() == 0) return@withContext null
            val primero = lista.getJSONObject(0)
            primero.optString("lat").toDoubleOrNull()
                ?.let { lat -> primero.optString("lon").toDoubleOrNull()?.let { lng -> lat to lng } }
        } catch (e: Exception) {
            null
        }
    }
}

