package com.biprangshu.chattrix

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.biprangshu.chattrix.onboarding.LoginScreen
import com.biprangshu.chattrix.onboarding.LoginWithEmail
import com.biprangshu.chattrix.onboarding.LoginWithPhone
import com.biprangshu.chattrix.onboarding.OtpScreen
import com.biprangshu.chattrix.ui.theme.ChattrixTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ChattrixTheme {
                LoginWithEmail()
            }
        }
    }
}


