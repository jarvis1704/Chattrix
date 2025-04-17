package com.biprangshu.chattrix

import android.app.Activity
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.biprangshu.chattrix.authentication.AuthViewModel
import com.biprangshu.chattrix.home.HomeScreen
import com.biprangshu.chattrix.onboarding.LoginScreen
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.common.api.ApiException

@Composable
fun ChattrixNavigation(modifier: Modifier = Modifier, authViewModel: AuthViewModel = viewModel(), navController: NavHostController) {

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


    NavHost(
        navController = navController,
        startDestination = ChattrixScreens.LOGIN_SCREEN
    ){
        composable(
            route = ChattrixScreens.LOGIN_SCREEN
        ) {
            LoginScreen(navController = navController)
        }

        composable (route = ChattrixScreens.HOME_SCREEN){
            HomeScreen()
        }
    }

}