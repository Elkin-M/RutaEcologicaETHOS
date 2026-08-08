package com.ethos.rutaecologica.data.remote

import android.util.Log
import com.ethos.rutaecologica.data.model.Lugar
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume

@Singleton
class FirebaseRepository @Inject constructor(
    private val database: FirebaseDatabase
) {
    private val TAG = "TRACER_FIREBASE"

    suspend fun obtenerLugar(codigo: String): Lugar? = withContext(Dispatchers.IO) {
        val path = "RutaEcologica/Lugares/$codigo"
        val dbUrl = database.reference.toString()

        Log.d(TAG, "--------------------------------------------------")
        Log.d(TAG, "🚀 [INICIO] Solicitando lugar en Firebase")
        Log.d(TAG, "🌐 URL Base de Firebase: $dbUrl")
        Log.d(TAG, "📍 Ruta completa del nodo: $dbUrl/$path")
        Log.d(TAG, "🔑 Código buscado: '$codigo'")

        val resultado = withTimeoutOrNull(5000L) {
            suspendCancellableCoroutine { cont ->
                val ref = database.getReference(path)

                Log.d(TAG, "📡 Enviando listener singleValueEvent a: ${ref.path}")

                ref.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        Log.d(TAG, "📥 [RESPUESTA] ¡Llegó respuesta de Firebase!")
                        Log.d(TAG, "🔍 ¿El nodo existe en DB?: ${snapshot.exists()}")
                        Log.d(TAG, "📄 Key del Snapshot: ${snapshot.key}")
                        Log.d(TAG, "📦 Hijos dentro del nodo: ${snapshot.childrenCount}")

                        if (snapshot.exists()) {
                            Log.d(TAG, "📋 Valor crudo recuperado: ${snapshot.value}")
                            val lugar = mapSnapshotToLugar(codigo, snapshot)
                            Log.d(TAG, "✅ Objeto Lugar mapeado con éxito: $lugar")

                            if (cont.isActive) cont.resume(lugar)
                        } else {
                            Log.w(TAG, "⚠️ El nodo '$path' NO existe en la base de datos.")
                            if (cont.isActive) cont.resume(null)
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Log.e(TAG, "❌ [ERROR FIREBASE] Código: ${error.code} | Mensaje: ${error.message}")
                        Log.e(TAG, "❌ Detalles: ${error.details}")
                        if (cont.isActive) cont.resume(null)
                    }
                })

                cont.invokeOnCancellation {
                    Log.w(TAG, "🛑 Corrutina cancelada externamente.")
                }
            }
        }

        if (resultado == null) {
            Log.e(TAG, "⏱️ [TIMEOUT] Transcurrieron 5000ms sin respuesta de Firebase Realtime Database.")
        }

        Log.d(TAG, "--------------------------------------------------")
        return@withContext resultado
    }

    suspend fun obtenerTodosLosLugares(): List<Lugar> = withContext(Dispatchers.IO) {
        val path = "RutaEcologica/Lugares"
        val dbUrl = database.reference.toString()

        Log.d(TAG, "--------------------------------------------------")
        Log.d(TAG, "🚀 [INICIO] Solicitando TODOS los lugares en Firebase")
        Log.d(TAG, "📍 Ruta: $dbUrl/$path")

        val resultado = withTimeoutOrNull(5000L) {
            suspendCancellableCoroutine<List<Lugar>> { cont ->
                val ref = database.getReference(path)

                ref.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        Log.d(TAG, "📥 [RESPUESTA] Llegó lista de lugares")
                        val listaLugares = mutableListOf<Lugar>()

                        if (snapshot.exists()) {
                            for (child in snapshot.children) {
                                val id = child.key ?: continue
                                val lugar = mapSnapshotToLugar(id, child)
                                listaLugares.add(lugar)
                            }
                            Log.d(TAG, "✅ Total de lugares cargados: ${listaLugares.size}")
                        } else {
                            Log.w(TAG, "⚠️ El nodo '$path' NO existe en la base de datos.")
                        }

                        if (cont.isActive) cont.resume(listaLugares)
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Log.e(TAG, "❌ [ERROR FIREBASE] ${error.message}")
                        if (cont.isActive) cont.resume(emptyList())
                    }
                })
            }
        }

        if (resultado == null) {
            Log.e(TAG, "⏱️ [TIMEOUT] Se agotó el tiempo de espera para obtener la lista de lugares.")
        }

        Log.d(TAG, "--------------------------------------------------")
        return@withContext resultado ?: emptyList()
    }

    /**
     * Función de diagnóstico: escucha el estado de conexión de Firebase en tiempo real.
     * Úsala una sola vez al iniciar la app (por ejemplo en MainActivity.onCreate())
     * para saber si el dispositivo realmente logra establecer socket con Firebase.
     */
    fun verificarConexion() {
        val connectedRef = database.getReference(".info/connected")
        connectedRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val connected = snapshot.getValue(Boolean::class.java) ?: false
                Log.d(TAG, "🔌 ¿Conectado a Firebase?: $connected")
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "🔌 Error verificando conexión: ${error.message}")
            }
        })
    }

    private fun mapSnapshotToLugar(id: String, snapshot: DataSnapshot): Lugar {
        val nombre = snapshot.child("nombre").getValue(String::class.java) ?: ""
        val descripcion = snapshot.child("descripcion").getValue(String::class.java) ?: ""
        val categoria = snapshot.child("categoria").getValue(String::class.java) ?: ""
        val estrellas = toIntOrDefault(snapshot.child("estrellas").value, 0)
        val audio = snapshot.child("audio").getValue(String::class.java) ?: ""
        val imagen = snapshot.child("imagen").getValue(String::class.java) ?: ""
        val latitud = toDoubleOrDefault(snapshot.child("latitud").value, 0.0)
        val longitud = toDoubleOrDefault(snapshot.child("longitud").value, 0.0)

        Log.d(TAG, "   └─ [MAPEO] nombre: '$nombre', categoria: '$categoria', imagen: '$imagen'")

        return Lugar(
            id = id,
            nombre = nombre,
            descripcion = descripcion,
            categoria = categoria,
            estrellas = estrellas,
            audio = audio,
            imagen = imagen,
            latitud = latitud,
            longitud = longitud
        )
    }

    private fun toDoubleOrDefault(value: Any?, default: Double): Double {
        return when (value) {
            is Number -> value.toDouble()
            is String -> value.toDoubleOrNull() ?: default
            else -> default
        }
    }

    private fun toIntOrDefault(value: Any?, default: Int): Int {
        return when (value) {
            is Number -> value.toInt()
            is String -> value.toIntOrNull() ?: default
            else -> default
        }
    }
}