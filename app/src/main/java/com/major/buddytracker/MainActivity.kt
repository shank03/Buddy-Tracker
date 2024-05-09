package com.major.buddytracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.firebase.auth.FirebaseAuth
import com.major.buddytracker.screens.LoginScreen
import com.major.buddytracker.screens.MainScreen
import com.major.buddytracker.screens.QrCodeScreen
import com.major.buddytracker.screens.RegisterScreen
import com.major.buddytracker.ui.theme.BuddyTrackerTheme

object Screen {
    const val LOGIN = "LOGIN"
    const val MAIN = "MAIN"
    const val REGISTER = "REGISTER"
    const val QR_CODE = "QR_CODE"
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val localBackPressed = LocalOnBackPressedDispatcherOwner.current
            val navController = rememberNavController()

            BuddyTrackerTheme {
                NavHost(
                    modifier = Modifier
                        .fillMaxSize()
                        .imePadding(),
                    navController = navController,
                    startDestination = if (FirebaseAuth.getInstance().currentUser == null) {
                        Screen.LOGIN
                    } else {
                        Screen.MAIN
                    }
                ) {

                    composable(Screen.LOGIN) {
                        LoginScreen(onNavigateMain = {
                            navController.navigate(Screen.MAIN) {
                                popUpTo(Screen.LOGIN) { inclusive = true }
                            }
                        }, onNavigateRegister = {
                            navController.navigate(Screen.REGISTER)
                        })
                    }

                    composable(Screen.MAIN) {
                        MainScreen(onLogout = {
                            FirebaseAuth.getInstance().signOut()
                            navController.navigate(Screen.LOGIN) {
                                popUpTo(Screen.MAIN) { inclusive = true }
                            }
                        }, onQrCode = {
                            navController.navigate(Screen.QR_CODE)
                        })
                    }

                    composable(Screen.REGISTER) {
                        RegisterScreen(onBack = {
                            localBackPressed?.onBackPressedDispatcher?.onBackPressed()
                        })
                    }

                    composable(Screen.QR_CODE) {
                        QrCodeScreen(onBack = {
                            localBackPressed?.onBackPressedDispatcher?.onBackPressed()
                        })
                    }
                }
            }
        }
    }
}
