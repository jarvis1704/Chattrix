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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.biprangshu.chattrix.authentication.AuthState
import com.biprangshu.chattrix.authentication.AuthViewModel
import com.biprangshu.chattrix.authentication.UpdateState
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(
    modifier: Modifier = Modifier,
    navController: NavController,
    authViewModel: AuthViewModel = hiltViewModel()
) {
    val authState by authViewModel.authState.collectAsState()
    val updateState by authViewModel.updateState.collectAsState()
    val user = (authState as? AuthState.SignedIn)?.user

    var changedUserName by remember { mutableStateOf(user?.displayName ?: "Unknown User") }
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    // Handle update state changes
    LaunchedEffect(updateState) {
        when (updateState) {
            is UpdateState.Success -> {
                coroutineScope.launch {
                    snackbarHostState.showSnackbar("Profile updated successfully!")
                }
                authViewModel.resetUpdateState()
                // Optionally navigate back
                navController.popBackStack()
            }
            is UpdateState.Error -> {
                coroutineScope.launch {
                    snackbarHostState.showSnackbar("Error: ${(updateState as UpdateState.Error).message}")
                }
                authViewModel.resetUpdateState()
            }
            else -> {}
        }
    }

    LaunchedEffect(user?.displayName) {
        user?.displayName?.let { displayName ->
            changedUserName = displayName
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Edit your profile") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Arrow back")
                    }
                },
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            Text("Your username")
            Spacer(Modifier.height(8.dp))
            TextField(
                value = changedUserName,
                onValueChange = { changedUserName = it },
                keyboardOptions = KeyboardOptions.Default,
                enabled = updateState !is UpdateState.Loading
            )
            Spacer(Modifier.height(16.dp))
            Button(
                onClick = { authViewModel.updateUserName(changedUserName) },
                enabled = updateState !is UpdateState.Loading && changedUserName.isNotBlank()
            ) {
                if (updateState is UpdateState.Loading) {
                    CircularProgressIndicator(
                        modifier = Modifier.padding(end = 8.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
                Text("Confirm your profile change")
            }
        }
    }
}