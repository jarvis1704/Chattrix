package com.biprangshu.chattrix.home

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SearchBar
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.biprangshu.chattrix.ChattrixScreens
import com.biprangshu.chattrix.R
import com.biprangshu.chattrix.authentication.AuthState
import com.biprangshu.chattrix.authentication.AuthViewModel
import com.biprangshu.chattrix.data.UserModel
import com.biprangshu.chattrix.uiutils.ChatItem
import com.biprangshu.chattrix.viewmodel.MainActivityViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    authViewModel: AuthViewModel = hiltViewModel(),
    mainActivityViewModel: MainActivityViewModel = hiltViewModel(),
    navController: NavController,
    onSignOut: () -> Unit
) {
    val authState by authViewModel.authState.collectAsState()
    val user = (authState as? AuthState.SignedIn)?.user
    val userChatInfoList by mainActivityViewModel.userList.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    var isSearchActive by remember { mutableStateOf(false) }

    Log.d("HomeScreen", "Recomposing. currentUserList size: ${userChatInfoList.size}, Content: ${userChatInfoList.joinToString { it.userModel.userName?: "N/A" }}")

    Surface(
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // App Header
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 32.dp)
            ) {
                Text(
                    "Chattrix",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.primary
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
//                    // User display name if available
//                    user?.displayName?.let {
//                        Text(
//                            text = it,
//                            style = MaterialTheme.typography.bodyMedium,
//                            maxLines = 1,
//                            overflow = TextOverflow.Ellipsis,
//                            modifier = Modifier.padding(end = 8.dp)
//                        )
//                    }

                    // Profile image
                    Image(
                        painter = painterResource(R.drawable.user),
                        contentDescription = "User image",
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .border(1.dp, MaterialTheme.colorScheme.primary, CircleShape).clickable(onClick = {
                                navController.navigate(route = ChattrixScreens.PROFILE_SCREEN)
                            }),
                        contentScale = ContentScale.Crop,
                        colorFilter = androidx.compose.ui.graphics.ColorFilter.tint(MaterialTheme.colorScheme.primary)
                    )
                }
            }

            // Search Bar
            SearchBar(
                query = searchQuery,
                onQueryChange = { searchQuery = it },
                onSearch = { isSearchActive = false },
                active = isSearchActive,
                onActiveChange = { isSearchActive = it },
                placeholder = { Text("Search chats...") },
                modifier = Modifier.fillMaxWidth()
            ) {
                // Search results would go here
                Text("No results found", modifier = Modifier.padding(16.dp))
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Chat Header
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            ) {
                Text(
                    "Chats",
                    style = MaterialTheme.typography.titleMedium
                )

            }

            // Chat List
            if (userChatInfoList.isEmpty()) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    Text(
                        "No users found",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f)
                ) {
                    items(userChatInfoList.size) { index ->
                        val chatInfo = userChatInfoList[index]
                        ChatItem(
                            userItem = chatInfo.userModel,
                            onClick = { "${ChattrixScreens.CHAT_SCREEN}/${chatInfo.userModel.userId}/${chatInfo.userModel.userName}" },
                            navController = navController,
                            lastMessage = chatInfo.lastMessageText
                        )
                    }
                }
            }

            FloatingActionButton(
                onClick = {navController.navigate(ChattrixScreens.NEW_CHAT_SCREEN)},
                modifier = Modifier.offset(x=150.dp, y= (-70).dp),
                elevation = FloatingActionButtonDefaults.elevation(
                    defaultElevation = 15.dp
                )
            ) {
                Icon(imageVector = Icons.Filled.Add, contentDescription = "Add button")
            }

//            // Sign Out Button
//            TextButton(
//                onClick = onSignOut,
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .padding(top = 16.dp)
//            ) {
//                Text("Sign Out")
//            }
        }
    }
}


// Helper function to format timestamp
fun formatTimestamp(timestamp: Long): String {
    val now = Calendar.getInstance()
    val messageTime = Calendar.getInstance().apply { timeInMillis = timestamp }

    return when {
        // Today
        now.get(Calendar.DAY_OF_YEAR) == messageTime.get(Calendar.DAY_OF_YEAR) &&
                now.get(Calendar.YEAR) == messageTime.get(Calendar.YEAR) -> {
            val formatter = SimpleDateFormat("HH:mm", Locale.getDefault())
            formatter.format(Date(timestamp))
        }
        // Yesterday
        now.get(Calendar.DAY_OF_YEAR) - messageTime.get(Calendar.DAY_OF_YEAR) == 1 &&
                now.get(Calendar.YEAR) == messageTime.get(Calendar.YEAR) -> {
            "Yesterday"
        }
        // This week
        now.get(Calendar.WEEK_OF_YEAR) == messageTime.get(Calendar.WEEK_OF_YEAR) &&
                now.get(Calendar.YEAR) == messageTime.get(Calendar.YEAR) -> {
            val formatter = SimpleDateFormat("EEEE", Locale.getDefault())
            formatter.format(Date(timestamp))
        }
        // Other
        else -> {
            val formatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            formatter.format(Date(timestamp))
        }
    }
}