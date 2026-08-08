package com.ethos.rutaecologica.ui.navigation

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.ethos.rutaecologica.ui.screens.home.HomeScreen
import com.ethos.rutaecologica.ui.screens.home.HomeViewModel
import com.ethos.rutaecologica.ui.screens.info.InfoScreen
import com.ethos.rutaecologica.ui.screens.login.LoginScreen
import com.ethos.rutaecologica.ui.screens.passport.PassportScreen
import com.ethos.rutaecologica.ui.screens.profile.ProfileScreen
import com.ethos.rutaecologica.ui.screens.question.QuestionScreen
import com.ethos.rutaecologica.ui.screens.result.ResultScreen
import com.ethos.rutaecologica.ui.screens.scanner.ScannerScreen

object Rutas {
    const val LOGIN = "login"
    const val INICIO = "inicio"
    const val ESCANEAR = "escanear"
    const val RESULTADO = "resultado"
    const val PASAPORTE = "pasaporte"
    const val PERFIL = "perfil"
    const val PREGUNTA = "pregunta"
    const val INFO = "info"
}

@Composable
fun NavGraph(navController: NavHostController = rememberNavController()) {
    NavHost(navController = navController, startDestination = Rutas.LOGIN) {

        composable(Rutas.LOGIN) {
            LoginScreen(onIngresar = {
                navController.navigate(Rutas.INICIO) {
                    popUpTo(Rutas.LOGIN) { inclusive = true }
                }
            })
        }

        composable(Rutas.INICIO) { backStackEntry ->
            // ViewModel compartido a nivel de esta entrada para poder registrar visitas
            val homeViewModel: HomeViewModel = hiltViewModel(backStackEntry)
            HomeScreen(
                onIrAEscanear = { navController.navigate(Rutas.ESCANEAR) },
                onIrAPasaporte = { navController.navigate(Rutas.PASAPORTE) },
                onIrAProgreso = { navController.navigate(Rutas.PERFIL) },
                viewModel = homeViewModel
            )
        }

        composable(Rutas.ESCANEAR) {
            ScannerScreen(
                onLugarEncontrado = { navController.navigate(Rutas.RESULTADO) },
                onVolver = { navController.popBackStack() }
            )
        }

        composable(Rutas.RESULTADO) {
            // Home queda en el back stack: recuperamos su ViewModel para aplicar OtraPantallaCerrada
            val homeEntry = navController.getBackStackEntry(Rutas.INICIO)
            val homeViewModel: HomeViewModel = hiltViewModel(homeEntry)

            ResultScreen(
                onContinuar = { lugarId, estrellas ->
                    homeViewModel.registrarVisita(lugarId, estrellas)
                    navController.popBackStack(Rutas.INICIO, inclusive = false)
                },
                onVolver = { navController.popBackStack() }
            )
        }

        composable(Rutas.PASAPORTE) {
            PassportScreen(onVolver = { navController.popBackStack() })
        }

        composable(Rutas.PERFIL) {
            ProfileScreen(onVolver = { navController.popBackStack() })
        }

        composable(Rutas.PREGUNTA) {
            QuestionScreen(onVolver = { navController.popBackStack() })
        }

        composable(Rutas.INFO) {
            InfoScreen(onVolver = { navController.popBackStack() })
        }
    }
}
