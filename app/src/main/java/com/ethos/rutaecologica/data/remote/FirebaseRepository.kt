package com.ethos.rutaecologica.data.remote

import android.util.Log
import com.ethos.rutaecologica.data.model.Lugar
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume

@Singleton
class FirebaseRepository @Inject constructor(
    private val database: FirebaseDatabase
) {
    companion object {
        private const val TAG = "FirebaseRepo"
    }

    /**
     * Diagnostico rapido: escucha en tiempo real (una sola vez) si el
     * dispositivo logra establecer conexion con Firebase. No devuelve
     * nada, solo escribe en Logcat con el tag TRACER_FIREBASE para
     * que sea facil de filtrar.
     */
    fun verificarConexion() {
        database.reference.child(".info/connected")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val conectado = snapshot.getValue(Boolean::class.java) ?: false
                    Log.i("TRACER_FIREBASE", "¿Conectado a Firebase?: $conectado")
                }
                override fun onCancelled(error: DatabaseError) {
                    Log.e("TRACER_FIREBASE", "Error verificando conexion: ${error.message}")
                }
            })
    }

    suspend fun obtenerLugar(id: String): Lugar? {
        val idUpper = id.uppercase().trim()
        val ruta = "RutaEcologica/Lugares/$idUpper"

        Log.d(TAG, "Buscando '$idUpper' en ruta fija: $ruta")

        val snapshot = consultar(ruta) ?: return null

        if (!snapshot.exists()) {
            Log.w(TAG, "El nodo '$ruta' respondio pero esta vacio (no existe ese ID)")
            return null
        }

        Log.i(TAG, "Encontrado: ${snapshot.value}")
        return mapSnapshot(snapshot, idUpper)
    }

    suspend fun obtenerTodosLosLugares(): List<Lugar> {
        val snapshot = consultar("RutaEcologica/Lugares") ?: return emptyList()
        if (!snapshot.exists()) return emptyList()

        return snapshot.children.mapNotNull { child ->
            val id = child.key ?: return@mapNotNull null
            mapSnapshot(child, id)
        }
    }

    private suspend fun consultar(ruta: String): DataSnapshot? = suspendCancellableCoroutine { cont ->
        try {
            database.reference.child(ruta).addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (cont.isActive) cont.resume(snapshot)
                }

                override fun onCancelled(error: DatabaseError) {
                    // Este log dice EXACTAMENTE por que fallo: permisos, red, url invalida, etc.
                    Log.e(TAG, "onCancelled en '$ruta' -> codigo=${error.code} mensaje=${error.message}")
                    if (cont.isActive) cont.resume(null)
                }
            })
        } catch (e: Exception) {
            Log.e(TAG, "Excepcion antes de consultar '$ruta'", e)
            if (cont.isActive) cont.resume(null)
        }
    }

    private fun safeStr(s: DataSnapshot, key: String, default: String = ""): String =
        s.child(key).value?.toString() ?: default

    private fun safeDouble(s: DataSnapshot, key: String): Double =
        s.child(key).value?.toString()?.toDoubleOrNull() ?: 0.0

    private fun safeInt(s: DataSnapshot, key: String): Int =
        s.child(key).value?.toString()?.toIntOrNull() ?: 0

    private fun mapSnapshot(s: DataSnapshot, id: String): Lugar {
        val nombreAlternativo = when {
            id.startsWith("MANG") -> "Manglar"
            id.startsWith("AVE") -> "Observatorio de Aves"
            else -> "Punto $id"
        }
        return Lugar(
            id = id,
            nombre = safeStr(s, "nombre", nombreAlternativo),
            nombreCientifico = safeStr(s, "nombreCientifico"),
            descripcion = safeStr(s, "descripcion"),
            categoria = safeStr(s, "categoria", "Ecosistema"),
            estrellas = safeInt(s, "estrellas"),
            audio = safeStr(s, "audio"),
            imagen = safeStr(s, "imagen"),
            video = safeStr(s, "video"),
            latitud = safeDouble(s, "latitud"),
            longitud = safeDouble(s, "longitud"),
            datoCurioso = safeStr(s, "datoCurioso"),
            estadoConservacion = safeStr(s, "estadoConservacion"),
            habitat = safeStr(s, "habitat"),
            modelo3D = safeStr(s, "modelo3D"),
            nivel = safeStr(s, "nivel")
        )
    }
}