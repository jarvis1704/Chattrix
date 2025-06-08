package com.biprangshu.chattrix

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
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
import com.biprangshu.chattrix.profile.EditProfileScreen
import com.biprangshu.chattrix.profile.UserProfileScreen

object AnimationConstants {
    const val DURATION_SHORT = 200
    const val DURATION_MEDIUM = 300
    const val DURATION_LONG = 400
    const val SCALE_INITIAL = 0.95f
    const val OFFSET_FULL = 1000
    const val OFFSET_PARTIAL = 300
}

object NavigationAnimations {

    fun slideInFromRight(): EnterTransition = slideInHorizontally(
        initialOffsetX = { it },
        animationSpec = tween(
            durationMillis = AnimationConstants.DURATION_MEDIUM,
            easing = FastOutSlowInEasing
        )
    ) + fadeIn(
        animationSpec = tween(
            durationMillis = AnimationConstants.DURATION_SHORT,
            delayMillis = 0,
            easing = LinearOutSlowInEasing
        )
    )

    fun slideOutToLeft(): ExitTransition = slideOutHorizontally(
        targetOffsetX = { -it },
        animationSpec = tween(
            durationMillis = AnimationConstants.DURATION_MEDIUM,
            easing = FastOutSlowInEasing
        )
    ) + fadeOut(
        animationSpec = tween(
            durationMillis = AnimationConstants.DURATION_SHORT,
            delayMillis = AnimationConstants.DURATION_SHORT,
            easing = LinearOutSlowInEasing
        )
    )

    fun slideInFromLeft(): EnterTransition = slideInHorizontally(
        initialOffsetX = { -it },
        animationSpec = tween(
            durationMillis = AnimationConstants.DURATION_MEDIUM,
            easing = FastOutSlowInEasing
        )
    ) + fadeIn(
        animationSpec = tween(
            durationMillis = AnimationConstants.DURATION_SHORT,
            delayMillis = 0,
            easing = LinearOutSlowInEasing
        )
    )

    fun slideOutToRight(): ExitTransition = slideOutHorizontally(
        targetOffsetX = { it },
        animationSpec = tween(
            durationMillis = AnimationConstants.DURATION_MEDIUM,
            easing = FastOutSlowInEasing
        )
    ) + fadeOut(
        animationSpec = tween(
            durationMillis = AnimationConstants.DURATION_SHORT,
            delayMillis = AnimationConstants.DURATION_SHORT,
            easing = LinearOutSlowInEasing
        )
    )

    fun scaleSlideInFromBottom(): EnterTransition = slideInVertically(
        initialOffsetY = { AnimationConstants.OFFSET_PARTIAL },
        animationSpec = tween(
            durationMillis = AnimationConstants.DURATION_MEDIUM,
            easing = FastOutSlowInEasing
        )
    ) + scaleIn(
        initialScale = AnimationConstants.SCALE_INITIAL,
        animationSpec = tween(
            durationMillis = AnimationConstants.DURATION_MEDIUM,
            easing = FastOutSlowInEasing
        )
    ) + fadeIn(
        animationSpec = tween(
            durationMillis = AnimationConstants.DURATION_SHORT,
            delayMillis = 0,
            easing = LinearOutSlowInEasing
        )
    )

    fun scaleSlideOutToTop(): ExitTransition = slideOutVertically(
        targetOffsetY = { -AnimationConstants.OFFSET_PARTIAL },
        animationSpec = tween(
            durationMillis = AnimationConstants.DURATION_MEDIUM,
            easing = FastOutSlowInEasing
        )
    ) + scaleOut(
        targetScale = AnimationConstants.SCALE_INITIAL,
        animationSpec = tween(
            durationMillis = AnimationConstants.DURATION_MEDIUM,
            easing = FastOutSlowInEasing
        )
    ) + fadeOut(
        animationSpec = tween(
            durationMillis = AnimationConstants.DURATION_SHORT,
            delayMillis = AnimationConstants.DURATION_SHORT,
            easing = LinearOutSlowInEasing
        )
    )

    fun modalSlideInFromBottom(): EnterTransition = slideInVertically(
        initialOffsetY = { it },
        animationSpec = tween(
            durationMillis = AnimationConstants.DURATION_LONG,
            easing = FastOutSlowInEasing
        )
    ) + fadeIn(
        animationSpec = tween(
            durationMillis = AnimationConstants.DURATION_SHORT,
            delayMillis = 0,
            easing = LinearOutSlowInEasing
        )
    )

    fun modalSlideOutToBottom(): ExitTransition = slideOutVertically(
        targetOffsetY = { it },
        animationSpec = tween(
            durationMillis = AnimationConstants.DURATION_LONG,
            easing = FastOutSlowInEasing
        )
    ) + fadeOut(
        animationSpec = tween(
            durationMillis = AnimationConstants.DURATION_SHORT,
            delayMillis = AnimationConstants.DURATION_MEDIUM,
            easing = LinearOutSlowInEasing
        )
    )

    fun gentleFadeIn(): EnterTransition = fadeIn(
        animationSpec = tween(
            durationMillis = AnimationConstants.DURATION_MEDIUM,
            delayMillis = 0,
            easing = LinearOutSlowInEasing
        )
    ) + scaleIn(
        initialScale = 0.98f,
        animationSpec = tween(
            durationMillis = AnimationConstants.DURATION_MEDIUM,
            easing = FastOutSlowInEasing
        )
    )

    fun gentleFadeOut(): ExitTransition = fadeOut(
        animationSpec = tween(
            durationMillis = AnimationConstants.DURATION_SHORT,
            delayMillis = AnimationConstants.DURATION_SHORT,
            easing = FastOutSlowInEasing
        )
    ) + scaleOut(
        targetScale = 0.98f,
        animationSpec = tween(
            durationMillis = AnimationConstants.DURATION_SHORT,
            easing = FastOutSlowInEasing
        )
    )
}

@Composable
fun ChattrixNavigation(
    modifier: Modifier = Modifier,
    navController: NavHostController,
    authViewModel: AuthViewModel = hiltViewModel(),
) {
    val context = LocalContext.current
    val authState by authViewModel.authState.collectAsState()

    LaunchedEffect(authState) {
        when (authState) {
            is AuthState.SignedIn -> {
                navController.navigate(ChattrixScreens.HOME_SCREEN) {
                    popUpTo(ChattrixScreens.LOGIN_SCREEN) {
                        inclusive = true
                    }
                }
            }
            else -> {  }
        }
    }

    //to be experimented with after version 1 of Chattix
//    if (authState is AuthState.Initial || authState is AuthState.Loading) {
//        Box(
//            modifier = Modifier
//                .fillMaxSize()
//                .background(MaterialTheme.colorScheme.background),
//            contentAlignment = Alignment.Center
//        ) {
//            CircularProgressIndicator(
//                color = MaterialTheme.colorScheme.primary
//            )
//        }
//        return
//    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        NavHost(
            navController = navController,
            startDestination = ChattrixScreens.LOGIN_SCREEN,
            modifier = Modifier.fillMaxSize()
        ) {

            // ─── Login screen ───
            composable(
                route = ChattrixScreens.LOGIN_SCREEN,
                enterTransition = { NavigationAnimations.gentleFadeIn() },
                exitTransition = { NavigationAnimations.gentleFadeOut() }
            ) {
                LoginScreen(
                    navController = navController,
                    onSignInClick = {
                        authViewModel.signInWithGoogle(context)
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

            // ─── Login with email ───
            composable(
                route = OnBoardingScreens.LOGIN_EMAIL,
                enterTransition = { NavigationAnimations.slideInFromRight() },
                exitTransition = { NavigationAnimations.slideOutToLeft() },
                popEnterTransition = { NavigationAnimations.slideInFromLeft() },
                popExitTransition = { NavigationAnimations.slideOutToRight() }
            ) {
                LoginWithEmail(navController = navController)
            }

            // ─── OTP verification ───
            composable(
                route = OnBoardingScreens.OTP_SCREEN,
                enterTransition = { NavigationAnimations.slideInFromRight() },
                exitTransition = { NavigationAnimations.slideOutToLeft() },
                popEnterTransition = { NavigationAnimations.slideInFromLeft() },
                popExitTransition = { NavigationAnimations.slideOutToRight() }
            ) {
                OtpScreen(navController = navController)
            }

            // ─── SignUp ───
            composable(
                route = OnBoardingScreens.SIGNUP_SCREEN,
                enterTransition = { NavigationAnimations.slideInFromRight() },
                exitTransition = { NavigationAnimations.slideOutToLeft() },
                popEnterTransition = { NavigationAnimations.slideInFromLeft() },
                popExitTransition = { NavigationAnimations.slideOutToRight() }
            ) {
                SignUpPage(navController = navController)
            }

            // ─── Home ───
            composable(
                route = ChattrixScreens.HOME_SCREEN,
                enterTransition = { NavigationAnimations.scaleSlideInFromBottom() },
                exitTransition = { NavigationAnimations.slideOutToLeft() },
                popEnterTransition = { NavigationAnimations.scaleSlideInFromBottom() },
                popExitTransition = { NavigationAnimations.scaleSlideOutToTop() }
            ) {
                HomeScreen(
                    authViewModel = authViewModel,
                    navController = navController,
                )
            }

            // ─── Profile ───
            composable(
                route = ChattrixScreens.PROFILE_SCREEN,
                enterTransition = { NavigationAnimations.modalSlideInFromBottom() },
                exitTransition = { NavigationAnimations.modalSlideOutToBottom() },
                popEnterTransition = { NavigationAnimations.modalSlideInFromBottom() },
                popExitTransition = { NavigationAnimations.modalSlideOutToBottom() }
            ) {
                UserProfileScreen(navController = navController)
            }

            // ─── Edit Profile ───
            composable(
                route = ChattrixScreens.EDIT_PROFILE_SCREEN,
                enterTransition = { NavigationAnimations.slideInFromRight() },
                exitTransition = { NavigationAnimations.slideOutToLeft() },
                popEnterTransition = { NavigationAnimations.slideInFromLeft() },
                popExitTransition = { NavigationAnimations.slideOutToRight() }
            ) {
                EditProfileScreen(navController = navController)
            }

            // ─── Chat ───
            composable(
                route = "${ChattrixScreens.CHAT_SCREEN}/{userId}/{userName}",
                arguments = listOf(
                    navArgument("userId") { type = NavType.StringType },
                    navArgument("userName") { type = NavType.StringType }
                ),
                enterTransition = { NavigationAnimations.slideInFromRight() },
                exitTransition = { NavigationAnimations.slideOutToLeft() },
                popEnterTransition = { NavigationAnimations.slideInFromLeft() },
                popExitTransition = { NavigationAnimations.slideOutToRight() }
            ) { backStackEntry ->
                val userId = backStackEntry.arguments?.getString("userId") ?: ""
                val userName = backStackEntry.arguments?.getString("userName") ?: ""
                ChatScreen(
                    navController = navController,
                    userId = userId,
                    userName = userName
                )
            }

            // ─── New chat ───
            composable(
                route = ChattrixScreens.NEW_CHAT_SCREEN,
                enterTransition = { NavigationAnimations.modalSlideInFromBottom() },
                exitTransition = { NavigationAnimations.modalSlideOutToBottom() },
                popEnterTransition = { NavigationAnimations.modalSlideInFromBottom() },
                popExitTransition = { NavigationAnimations.modalSlideOutToBottom() }
            ) {
                NewChatScreen(navController = navController)
            }
        }
    }
}