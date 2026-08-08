# Ruta Ecológica ETHOS — Proyecto nativo Android

Migración completa de la app de MIT App Inventor a **Kotlin + Jetpack Compose (Material 3) + MVVM/Clean Architecture**.

## 1. Abrir el proyecto
1. Abre Android Studio (Koala o más reciente) → **Open** → selecciona la carpeta `RutaEcologicaETHOS`.
2. Deja que Gradle sincronice (puede pedirte actualizar el Gradle Wrapper; acepta la sugerencia del IDE).

## 2. Conectar Firebase (obligatorio)
1. Ve a [Firebase Console](https://console.firebase.google.com) → crea un proyecto (o usa uno existente).
2. Agrega una app Android con el `applicationId`: `com.ethos.rutaecologica`.
3. Descarga el archivo **`google-services.json`** y colócalo en `app/google-services.json`.
4. Activa **Realtime Database** (modo prueba para desarrollo) y **Storage** (para las imágenes/audios).

## 3. Cargar tus datos (el JSON que enviaste)
En `firebase-seed/firebase-seed.json` está tu `ruta-ecologica.json` original ya adaptado
a la estructura que espera la app (`RutaEcologica/Lugares/{id}` con `categoria`, `latitud`
y `longitud` añadidos). Para importarlo:

1. Firebase Console → Realtime Database → menú ⋮ → **Import JSON**.
2. Selecciona `firebase-seed/firebase-seed.json`.
3. Sube tus imágenes/audios reales a Firebase Storage y reemplaza las URLs
   `https://firebasestorage.googleapis.com/CAMBIA_ESTA_URL/...` por las URLs reales
   (clic derecho en el archivo subido → "Copiar URL de descarga").

Puedes agregar más lugares (ej. `MANG004`, senderos, flora local) siguiendo el mismo
esquema — la app los detecta automáticamente sin tocar código, tanto en el escáner
como en el Pasaporte.

## 4. Generar los códigos QR físicos
Cada QR del tótem debe contener **solo el ID** del lugar, por ejemplo:
```
MANG001
```
Puedes generarlos gratis en qr-code-generator.com o con la librería ZXing.

## 5. Ejecutar
Conecta un dispositivo físico o usa un emulador con cámara habilitada (los QR no
se detectan bien en emuladores sin cámara virtual configurada) y presiona ▶ Run.

---

## Qué incluye el proyecto

| Capa | Archivos |
|---|---|
| **UI (Compose)** | `LoginScreen`, `HomeScreen`, `ScannerScreen`, `ResultScreen`, `PassportScreen`, `ProfileScreen`, `QuestionScreen`, `InfoScreen` |
| **Navegación** | `NavGraph.kt` — grafo centralizado con Navigation Compose |
| **ViewModels** | `HomeViewModel`, `ScannerViewModel`, `ResultViewModel`, `PassportViewModel` |
| **Datos local** | `UserPreferencesRepository` (DataStore, reemplaza TinyDB) |
| **Datos remoto** | `FirebaseRepository` (Realtime Database) |
| **Dominio** | `GamificationLogic` (niveles/insignias, traducido 1:1 de tus bloques) |
| **DI** | Hilt (`AppModule`, `@HiltAndroidApp`, `@HiltViewModel`) |

### Identidad visual
Paleta ecológica (verdes de manglar, teal de agua, arena cálida, dorado para las
estrellas), encabezados con degradado, insignias circulares por nivel, barra de
progreso animada hacia el siguiente rango, y un Pasaporte tipo grilla de sellos
bloqueados/desbloqueados — todo en `ui/theme/` y `ui/common/EcoComponents.kt`.

### Lógica de gamificación (igual que en App Inventor)
- 0–19 ⭐ → Explorador (1 insignia)
- 20–49 ⭐ → Guardián (2 insignias)
- 50–99 ⭐ → Protector (3 insignias)
- 100+ ⭐ → Maestro ETHOS (4 insignias)

### Flujo de escaneo
`ScannerScreen` (CameraX + ML Kit) → detecta el ID → `FirebaseRepository` consulta
`RutaEcologica/Lugares/{id}` → si existe, navega a `ResultScreen` (imagen vía Coil,
audio vía Media3/ExoPlayer) → al pulsar "Continuar", `HomeViewModel.registrarVisita()`
valida que sea un lugar nuevo, suma estrellas y recalcula nivel — exactamente como el
bloque `OtraPantallaCerrada` original.

## Pendiente por decisión tuya
- Reemplazar las preguntas de `QuestionScreen` (están de ejemplo) por tu banco real de trivia.
- Si prefieres Firestore en vez de Realtime Database, el cambio se limita a `FirebaseRepository.kt`.
- Ícono de la app (`mipmap/ic_launcher`) — puedo generarlo si me compartes un logo o descripción.
