package com.biprangshu.chattrix.onboarding

import android.app.Activity
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.biprangshu.chattrix.authentication.AuthViewModel
import com.biprangshu.chattrix.home.HomeScreen
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.common.api.ApiException

@Composable
fun OnBoardingNavigation(modifier: Modifier = Modifier, navController: NavHostController, authViewModel: AuthViewModel= hiltViewModel()) {

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

    //navigation for onboarding screens
    NavHost(
        navController = navController,
        startDestination = OnBoardingScreens.LOGIN_SCREEN
    ) {
        composable(
            route = OnBoardingScreens.LOGIN_SCREEN
        ) {
            LoginScreen(
                navController= navController,
                onSignInClick = {
                    launcher.launch(authViewModel.getSignInIntent())
                },
                onNavigateToHome = {
                    navController.navigate(OnBoardingScreens.HOME_SCREEN){
                        popUpTo(OnBoardingScreens.LOGIN_SCREEN){
                            inclusive = true
                        }
                    }
                }
            )
        }

        composable(
            route = OnBoardingScreens.LOGIN_EMAIL
        ) {
            LoginWithEmail(navController= navController)
        }
        composable(
            route = OnBoardingScreens.OTP_SCREEN
        ) {
            OtpScreen(navController= navController)
        }
        composable(
            route = OnBoardingScreens.HOME_SCREEN
        ) {
            HomeScreen(
                authViewModel = authViewModel,
                onSignOut = {
                    authViewModel.signOut()
                    navController.navigate(OnBoardingScreens.LOGIN_SCREEN) {
                        popUpTo(OnBoardingScreens.HOME_SCREEN) { inclusive = true }
                    }
                }

            )
        }
    }
}