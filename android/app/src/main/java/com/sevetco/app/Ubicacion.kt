package com.sevetco.app

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import androidx.core.content.ContextCompat
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

/**
 * Obtiene la ubicacion del dispositivo usando el servicio del sistema
 * (no requiere dependencias de Google Play).
 */
object Ubicacion {

    fun tienePermiso(context: Context): Boolean =
        ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) ==
            PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) ==
            PackageManager.PERMISSION_GRANTED

    /** Devuelve "latitud, longitud" o null si no se pudo obtener. */
    suspend fun obtenerTexto(context: Context): String? {
        val punto = obtener(context) ?: return null
        return String.format("%.6f, %.6f", punto.first, punto.second)
    }

    /** Distancia en kilometros entre dos puntos, con un decimal. */
    fun distanciaKm(lat1: Double, lng1: Double, lat2: Double, lng2: Double): Double {
        val resultado = FloatArray(1)
        Location.distanceBetween(lat1, lng1, lat2, lng2, resultado)
        return Math.round(resultado[0] / 100.0) / 10.0
    }

    suspend fun obtener(context: Context): Pair<Double, Double>? {
        if (!tienePermiso(context)) return null

        val gestor = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
            ?: return null

        // 1) Ultima ubicacion conocida: respuesta inmediata si existe.
        for (proveedor in listOf(LocationManager.GPS_PROVIDER, LocationManager.NETWORK_PROVIDER)) {
            try {
                val ultima = gestor.getLastKnownLocation(proveedor)
                if (ultima != null) return ultima.latitude to ultima.longitude
            } catch (e: SecurityException) {
                return null
            }
        }

        // 2) Si no hay ninguna, se pide una ubicacion nueva con un tiempo limite.
        return try {
            suspendCancellableCoroutine { continuacion ->
                val escucha = object : android.location.LocationListener {
                    override fun onLocationChanged(location: Location) {
                        gestor.removeUpdates(this)
                        if (continuacion.isActive) continuacion.resume(location.latitude to location.longitude)
                    }

                    override fun onProviderDisabled(proveedor: String) = Unit
                    override fun onProviderEnabled(proveedor: String) = Unit
                    @Deprecated("Obligatorio en versiones anteriores")
                    override fun onStatusChanged(proveedor: String?, estado: Int, extras: android.os.Bundle?) = Unit
                }

                gestor.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER, 0L, 0f, escucha, android.os.Looper.getMainLooper()
                )
                gestor.requestLocationUpdates(
                    LocationManager.NETWORK_PROVIDER, 0L, 0f, escucha, android.os.Looper.getMainLooper()
                )

                continuacion.invokeOnCancellation { gestor.removeUpdates(escucha) }
            }
        } catch (e: Exception) {
            null
        }
    }
}

