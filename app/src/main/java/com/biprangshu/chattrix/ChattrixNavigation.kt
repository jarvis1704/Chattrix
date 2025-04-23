
package com.biprangshu.chattrix

import android.app.Activity
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.biprangshu.chattrix.authentication.AuthState
import com.biprangshu.chattrix.authentication.AuthViewModel
import com.biprangshu.chattrix.home.HomeScreen
import com.biprangshu.chattrix.onboarding.LoginScreen
import com.biprangshu.chattrix.onboarding.LoginWithEmail
import com.biprangshu.chattrix.onboarding.OnBoardingScreens
import com.biprangshu.chattrix.onboarding.OtpScreen
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.common.api.ApiException

@Composable
fun ChattrixNavigation(
    modifier: Modifier = Modifier,
    navController: NavHostController,
    authViewModel: AuthViewModel = hiltViewModel()
) {
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                val account = task.getResult(ApiException::class.java)
                account.idToken?.let { token ->
                    authViewModel.signInWithGoogle(token)
                }
            } catch (e: ApiException) {
                Log.w("GoogleSignIn", "Google sign in failed", e)
            }
        }
    }

    val authState = authViewModel.authState.collectAsState().value

    val startDestination = if (authState is AuthState.SignedIn) {
        ChattrixScreens.HOME_SCREEN
    } else {
        ChattrixScreens.LOGIN_SCREEN
    }

    NavHost(
        navController = navController,
        startDestination = startDestination
    ){
        // Login screen
        composable(route = ChattrixScreens.LOGIN_SCREEN) {
            LoginScreen(
                navController = navController,
                onSignInClick = {
                    launcher.launch(authViewModel.getSignInIntent())
                },
                onNavigateToHome = {
                    navController.navigate(ChattrixScreens.HOME_SCREEN) {
                        popUpTo(ChattrixScreens.LOGIN_SCREEN) {
                            inclusive = true
                        }
                    }
                }
            )
        }

        // Login with email
        composable(route = OnBoardingScreens.LOGIN_EMAIL) {
            LoginWithEmail(navController = navController)
        }

        // OTP verification screen
        composable(route = OnBoardingScreens.OTP_SCREEN) {
            OtpScreen(navController = navController)
        }

        // Home screen
        composable(route = ChattrixScreens.HOME_SCREEN) {
            HomeScreen(
                authViewModel = authViewModel,
                onSignOut = {
                    authViewModel.signOut()
                    navController.navigate(ChattrixScreens.LOGIN_SCREEN) {
                        popUpTo(ChattrixScreens.HOME_SCREEN) { inclusive = true }
                    }
                }
            )
        }
    }
}