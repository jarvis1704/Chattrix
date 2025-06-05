package com.biprangshu.chattrix.onboarding

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.rememberLottieComposition
import com.biprangshu.chattrix.R
import com.biprangshu.chattrix.authentication.AuthState
import com.biprangshu.chattrix.authentication.AuthViewModel
import kotlinx.coroutines.delay

@Composable
fun LoginScreen(
    modifier: Modifier = Modifier,
    navController: NavController,
    authViewModel: AuthViewModel = hiltViewModel(),
    onSignInClick: () -> Unit,
    onNavigateToHome: () -> Unit
) {
    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.chatanimation_1))
    val authState by authViewModel.authState.collectAsState()

    var isVisible by remember { mutableStateOf(false) }
    val hapticFeedback= LocalHapticFeedback.current

    LaunchedEffect(Unit) {
        delay(300)
        isVisible = true
    }

    LaunchedEffect(authState) {
        if (authState is AuthState.SignedIn) {
            onNavigateToHome()
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            // Gradient background
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.05f),
                                MaterialTheme.colorScheme.background,
                                MaterialTheme.colorScheme.secondary.copy(alpha = 0.03f)
                            )
                        )
                    )
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .navigationBarsPadding()
                    .padding(16.dp),
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                AnimatedVisibility(
                    visible = isVisible,
                    enter = slideInVertically(
                        initialOffsetY = { -it },
                        animationSpec = tween(800)
                    ) + fadeIn(animationSpec = tween(800))
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Animation container
                        Card(
                            modifier = Modifier.fillMaxWidth().height(280.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                            shape = RoundedCornerShape(24.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surface
                            )
                        ) {
                            Box(
                                modifier = Modifier.fillMaxSize().background(
                                    Brush.verticalGradient(
                                        colors = listOf(
                                            MaterialTheme.colorScheme.primary.copy(alpha = 0.05f),
                                            MaterialTheme.colorScheme.background,
                                            MaterialTheme.colorScheme.secondary.copy(alpha = 0.03f)
                                        )
                                    )
                                ),
                                contentAlignment = Alignment.Center
                            ) {
                                LottieAnimation(
                                    composition = composition,
                                    iterations = LottieConstants.IterateForever,
                                    modifier = Modifier.size(350.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(32.dp))

                        // Welcome text
                        Text(
                            text = "Welcome to",
                            fontSize = 20.sp,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                            textAlign = TextAlign.Center
                        )

                        Text(
                            text = "Chattrix",
                            fontSize = 36.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Connect, chat, and collaborate with ease",
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                            textAlign = TextAlign.Center,
                            lineHeight = 22.sp
                        )
                    }
                }


                AnimatedVisibility(
                    visible = isVisible,
                    enter = slideInVertically(
                        initialOffsetY = { it },
                        animationSpec = tween(800, delayMillis = 200)
                    ) + fadeIn(animationSpec = tween(800, delayMillis = 200))
                ) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            // Google Sign In Button
                            Button(
                                onClick = onSignInClick,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(56.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary
                                ),
                                elevation = ButtonDefaults.buttonElevation(
                                    defaultElevation = 2.dp,
                                    pressedElevation = 4.dp
                                )
                            ) {
                                Row(
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Image(
                                        painter = painterResource(R.drawable.googlelogo),
                                        contentDescription = "Google logo",
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(
                                        text = "Continue with Google",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }

                            // Loading indicator
                            AnimatedVisibility(
                                visible = authState is AuthState.Loading,
                                enter = fadeIn(),
                                exit = fadeOut()
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.padding(vertical = 16.dp),
                                    strokeWidth = 3.dp
                                )
                            }

                            // Error message
                            AnimatedVisibility(
                                visible = authState is AuthState.Error,
                                enter = fadeIn(),
                                exit = fadeOut()
                            ) {
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 12.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.errorContainer
                                    ),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(
                                        text = (authState as? AuthState.Error)?.message ?: "",
                                        color = MaterialTheme.colorScheme.onErrorContainer,
                                        modifier = Modifier.padding(12.dp),
                                        fontSize = 14.sp,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }

                            // Divider with "OR"
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 20.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                HorizontalDivider(
                                    modifier = Modifier.weight(1f),
                                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                                )
                                Text(
                                    text = "OR",
                                    modifier = Modifier.padding(horizontal = 16.dp),
                                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium
                                )
                                HorizontalDivider(
                                    modifier = Modifier.weight(1f),
                                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                                )
                            }

                            // Email Sign In Button
                            OutlinedButton(
                                onClick = {
                                    hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                                    navController.navigate(route = OnBoardingScreens.LOGIN_EMAIL)
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(56.dp),
                                shape = RoundedCornerShape(12.dp),
                                border = ButtonDefaults.outlinedButtonBorder.copy(
                                    width = 1.5.dp
                                )
                            ) {
                                Row(
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Image(
                                        painter = painterResource(R.drawable.maillogo),
                                        contentDescription = "Email logo",
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(
                                        text = "Continue with Email",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Sign Up Button
                            OutlinedButton(
                                onClick = {
                                    hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                                    navController.navigate(route = OnBoardingScreens.SIGNUP_SCREEN)
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = MaterialTheme.colorScheme.primary
                                )
                            ) {
                                Text(
                                    text = "Create New Account",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}