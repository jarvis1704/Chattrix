package com.biprangshu.chattrix

import android.app.Activity
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.biprangshu.chattrix.authentication.AuthState
import com.biprangshu.chattrix.authentication.AuthViewModel
import com.biprangshu.chattrix.home.ChatScreen
import com.biprangshu.chattrix.home.HomeScreen
import com.biprangshu.chattrix.home.NewChatScreen
import com.biprangshu.chattrix.onboarding.LoginScreen
import com.biprangshu.chattrix.onboarding.LoginWithEmail
import com.biprangshu.chattrix.onboarding.OnBoardingScreens
import com.biprangshu.chattrix.onboarding.OtpScreen
import com.biprangshu.chattrix.onboarding.SignUpPage
import com.biprangshu.chattrix.profile.UserProfileScreen
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

    val authState by authViewModel.authState.collectAsState()

    // Check if we need to navigate to home when auth state changes to signed in
    LaunchedEffect(authState) {
        when (authState) {
            is AuthState.SignedIn -> {
                navController.navigate(ChattrixScreens.HOME_SCREEN) {
                    popUpTo(ChattrixScreens.LOGIN_SCREEN) {
                        inclusive = true
                    }
                }
            }
            else -> {}
        }
    }

    // Show loading while checking auth state
    if (authState is AuthState.Initial || authState is AuthState.Loading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    // Use a fixed start destination and handle navigation once NavHost is set up
    NavHost(
        navController = navController,
        startDestination = ChattrixScreens.LOGIN_SCREEN
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
                },
                navController = navController
            )
        }

        //SignUp screen
        composable(route= OnBoardingScreens.SIGNUP_SCREEN) {
            SignUpPage(
                navController = navController
            )
        }

        composable(route= ChattrixScreens.PROFILE_SCREEN) {
            UserProfileScreen(navController = navController)
        }

        // Chat screen with userId and userName
        composable(
            route = "${ChattrixScreens.CHAT_SCREEN}/{userId}/{userName}",
            arguments = listOf(
                navArgument("userId") { type = NavType.StringType },
                navArgument("userName") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val userId = backStackEntry.arguments?.getString("userId") ?: ""
            val userName = backStackEntry.arguments?.getString("userName") ?: ""
            ChatScreen(
                navController = navController,
                userId = userId,
                userName = userName
            )
        }

        //new chat screen
        composable(
            route = ChattrixScreens.NEW_CHAT_SCREEN
        ) {
            NewChatScreen(
                navController= navController
            )
        }
    }
}