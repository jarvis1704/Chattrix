package com.biprangshu.chattrix.onboarding

import android.widget.Toast
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.rememberLottieComposition
import com.biprangshu.chattrix.ChattrixScreens
import com.biprangshu.chattrix.R
import com.biprangshu.chattrix.authentication.AuthState
import com.biprangshu.chattrix.authentication.AuthViewModel
import com.biprangshu.chattrix.ui.theme.ChatTypography

@Composable
fun LoginWithEmail(modifier: Modifier = Modifier, navController: NavController, authViewModel: AuthViewModel= hiltViewModel()) {
    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.email_password))
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    val authState= authViewModel.authState.collectAsState()
    val hapticFeedback= LocalHapticFeedback.current

    when(authState.value){
        is AuthState.SignedIn -> {
            LaunchedEffect(key1 = authState.value) {
                navController.navigate(ChattrixScreens.HOME_SCREEN) {
                    popUpTo(OnBoardingScreens.LOGIN_SCREEN) { inclusive = true }
                }
            }
        }

        is AuthState.Loading -> {
            CircularProgressIndicator(modifier = Modifier.padding(8.dp))
        }

        is AuthState.Error ->{
            Toast.makeText(LocalContext.current, (authState.value as AuthState.Error).message, Toast.LENGTH_SHORT).show()
        }

        else -> Unit
    }

    var isFocused by remember { mutableStateOf(false) }


    val borderColor by animateColorAsState(
        targetValue = if (isFocused) Color(0xFF3F51B5) else Color.Gray,
        label = "BorderColorAnimation"
    )


    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Box(
            modifier = Modifier.fillMaxSize()
        ){
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
                modifier = Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding().padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {

                Card(
                    modifier = Modifier.fillMaxWidth().height(280.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                ) {
                    Box(
                        modifier = Modifier.fillMaxWidth().height(300.dp)
                    ) {
                        LottieAnimation(
                            composition = composition,
                            iterations = LottieConstants.IterateForever,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }

                Spacer(Modifier.height(24.dp))

                Card(
                    modifier = Modifier.fillMaxWidth().height(350.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(
                        containerColor= MaterialTheme.colorScheme.surface
                    )
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize().padding(16.dp),
                        verticalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Column {
                            Text("Enter your Email",
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                textAlign = TextAlign.Center,
                                style = ChatTypography.headlineLarge
                            )
                            Spacer(Modifier.height(12.dp))
                            OutlinedTextField(
                                value = email,
                                onValueChange = { email = it },
                                label = { Text("Your Email") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier.fillMaxWidth().onFocusChanged { isFocused = it.isFocused },
                                leadingIcon = {
                                    Icon(Icons.Default.Email, contentDescription = "Email icon")
                                },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = borderColor,
                                    unfocusedBorderColor = borderColor
                                )
                            )
                        }

                        Column() {
                            Text("Enter your Password",
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                textAlign = TextAlign.Center,
                                style = ChatTypography.headlineLarge
                            )
                            Spacer(Modifier.height(12.dp))
                            OutlinedTextField(
                                value = password,
                                onValueChange = { password = it },
                                label = { Text("Your Password") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                                visualTransformation = PasswordVisualTransformation(),
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier.fillMaxWidth().onFocusChanged { isFocused = it.isFocused },
                                leadingIcon = {
                                    Icon(Icons.Filled.Lock, contentDescription = "Lock icon")
                                },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = borderColor,
                                    unfocusedBorderColor = borderColor
                                )
                            )
                        }


                    }
                }



                Spacer(Modifier.height(24.dp))
                Box (
                    modifier = Modifier.fillMaxWidth().height(100.dp),
                    contentAlignment = Alignment.Center
                ){
                    // Sign Up Button
                    Button(
                        onClick = {
                            hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                            authViewModel.loginWithEmail(email, password)
                                  },
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
                        Text(
                            text = "Log In",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            style = ChatTypography.headlineLarge
                        )
                    }
                }
            }
        }

    }
}