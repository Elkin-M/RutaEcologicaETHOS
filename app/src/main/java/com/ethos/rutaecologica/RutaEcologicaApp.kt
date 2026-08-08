package com.ethos.rutaecologica

import android.app.Application
import android.util.Log
import com.google.firebase.FirebaseApp
import com.google.firebase.database.FirebaseDatabase
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class RutaEcologicaApp : Application() {

    // Hilt puede inyectar campos directamente en la clase Application
    // gracias a @HiltAndroidApp. Se resuelve apenas Application.onCreate() corre.
    @Inject
    lateinit var firebaseDatabase: FirebaseDatabase

    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)

        // 🔥 "Calentamos" la conexión con Firebase Realtime Database apenas
        // arranca la app, para que el handshake/autenticación con los
        // servidores de Google ya esté en curso (o listo) antes de que
        // el usuario escanee el primer QR. Esto evita el timeout de 5s
        // que veíamos en los logs, causado por el handshake inicial (GSC)
        // tardando ~4s en la primera consulta.
        try {
            firebaseDatabase.goOnline()
            firebaseDatabase.reference.keepSynced(true)
            Log.d("TRACER_APP", "🔥 Firebase warm-up iniciado: goOnline() + keepSynced(true)")
        } catch (e: Exception) {
            Log.e("TRACER_APP", "❌ Error al calentar conexión de Firebase: ${e.message}")
        }
    }
}