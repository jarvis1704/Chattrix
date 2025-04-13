package com.biprangshu.chattrix.onboarding

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

@Composable
fun OnBoardingNavigation(modifier: Modifier = Modifier, navController: NavHostController) {

    //navigation for onboarding screens
    NavHost(
        navController = navController,
        startDestination = OnBoardingScreens.LOGIN_SCREEN
    ) {
        composable(
            route = OnBoardingScreens.LOGIN_SCREEN
        ) {
            LoginScreen()
        }

        composable(
            route = OnBoardingScreens.LOGIN_EMAIL
        ) {
            LoginWithEmail()
        }
        composable(
            route = OnBoardingScreens.OTP_SCREEN
        ) {
            OtpScreen()
        }
    }
}