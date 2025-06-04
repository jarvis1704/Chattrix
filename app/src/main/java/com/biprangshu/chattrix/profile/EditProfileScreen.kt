package com.biprangshu.chattrix.profile

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.biprangshu.chattrix.authentication.AuthState
import com.biprangshu.chattrix.authentication.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(modifier: Modifier = Modifier, navController: NavController, authViewModel: AuthViewModel = hiltViewModel()) {

    val authState by authViewModel.authState.collectAsState()
    val user = (authState as? AuthState.SignedIn)?.user

    var changedUserName by remember { mutableStateOf(user?.displayName?: "Unknown User") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Edit your profile") },
                navigationIcon = {
                    IconButton(onClick = {navController.popBackStack()}) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Arrow back")
                    }
                },
                )
        }
    ) {paddingValues->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Text("Your username")
            Spacer(Modifier.height(8.dp))
            TextField(
                value = changedUserName,
                onValueChange = {changedUserName=it},
                keyboardOptions = KeyboardOptions.Default
            )
            Spacer(Modifier.height(8.dp))
            Button(
                onClick = {authViewModel.updateUserName(changedUserName)}
            ) {
                Text("Confirm your profile change")
            }
        }
    }
}