package com.ethos.rutaecologica.data.local

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.ethos.rutaecologica.data.model.UserProgress
import com.ethos.rutaecologica.domain.GamificationLogic
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore by preferencesDataStore(name = "ruta_ecologica_prefs")

@Singleton
class UserPreferencesRepository @Inject constructor(
    private val context: Context
) {
    private object Keys {
        val USUARIO = stringPreferencesKey("usuario")
        val NIVEL = stringPreferencesKey("nivel")
        val ESTRELLAS = intPreferencesKey("estrellas")
        val INSIGNIAS = intPreferencesKey("insignias")
        val LUGARES_VISITADOS = stringPreferencesKey("lugares_visitados") // csv
        val ULTIMO_QR = stringPreferencesKey("ultimo_qr")
    }

    val progresoFlow: Flow<UserProgress> = context.dataStore.data.map { prefs ->
        UserProgress(
            usuario = prefs[Keys.USUARIO] ?: "Estudiante",
            nivel = prefs[Keys.NIVEL] ?: "Explorador",
            estrellas = prefs[Keys.ESTRELLAS] ?: 0,
            insignias = prefs[Keys.INSIGNIAS] ?: 1,
            lugaresVisitados = (prefs[Keys.LUGARES_VISITADOS] ?: "")
                .split(",").filter { it.isNotBlank() },
            ultimoQR = prefs[Keys.ULTIMO_QR] ?: ""
        )
    }

    suspend fun guardarUsuario(nombre: String) {
        context.dataStore.edit { it[Keys.USUARIO] = nombre }
    }

    suspend fun guardarUltimoQR(codigo: String) {
        context.dataStore.edit { it[Keys.ULTIMO_QR] = codigo }
    }

    /**
     * Equivalente al bloque `OtraPantallaCerrada` de App Inventor:
     * si el lugar es nuevo, suma estrellas, guarda la visita y recalcula nivel/insignias.
     * Devuelve true si el lugar era nuevo (para poder celebrar en la UI).
     */
    suspend fun registrarVisita(lugarId: String, estrellasGanadas: Int): Boolean {
        var esNuevo = false
        context.dataStore.edit { prefs ->
            val visitados = (prefs[Keys.LUGARES_VISITADOS] ?: "")
                .split(",").filter { it.isNotBlank() }.toMutableList()

            if (!visitados.contains(lugarId)) {
                esNuevo = true
                visitados.add(lugarId)
                prefs[Keys.LUGARES_VISITADOS] = visitados.joinToString(",")

                val nuevasEstrellas = (prefs[Keys.ESTRELLAS] ?: 0) + estrellasGanadas
                prefs[Keys.ESTRELLAS] = nuevasEstrellas

                val nivel = GamificationLogic.calcularNivel(nuevasEstrellas)
                prefs[Keys.NIVEL] = nivel.etiqueta
                prefs[Keys.INSIGNIAS] = nivel.numeroInsignias
            }
        }
        return esNuevo
    }

    suspend fun reiniciarProgreso() {
        context.dataStore.edit { it.clear() }
    }
}
