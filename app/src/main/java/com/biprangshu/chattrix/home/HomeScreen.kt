package com.biprangshu.chattrix.home

import android.graphics.ColorFilter
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.scrollable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SearchBar
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.biprangshu.chattrix.R
import com.biprangshu.chattrix.authentication.AuthState
import com.biprangshu.chattrix.authentication.AuthViewModel
import com.biprangshu.chattrix.data.UserModel
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
    onSignOut: () -> Unit
) {
    val authState by authViewModel.authState.collectAsState()
    val user = (authState as? AuthState.SignedIn)?.user
    val userList by mainActivityViewModel.userList.collectAsState()

    Surface(
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
//        Text(
//            text = "Welcome, ${user?.displayName}!",
//            style = MaterialTheme.typography.headlineMedium,
//            textAlign = TextAlign.Center
//        )
//
//        Spacer(modifier = Modifier.height(16.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
                    .padding(top = 30.dp)
            ){
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Chattrix", color = MaterialTheme.colorScheme.primary, modifier = Modifier.align(Alignment.CenterVertically))
                    Image(painter = painterResource(R.drawable.user),
                        contentDescription = "User image",
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .border(1.dp, MaterialTheme.colorScheme.primary, CircleShape),
                        contentScale = ContentScale.Crop,
                        colorFilter = androidx.compose.ui.graphics.ColorFilter.tint(MaterialTheme.colorScheme.primary)
                    )
                }
            }

            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                SearchBar(
                    query = "",
                    onQueryChange = {},
                    onSearch = {},
                    active = false,
                    onActiveChange = {},
                ) {


                //chats go here
                Text("Chats")
                Spacer(Modifier.height(16.dp))
                LazyColumn {
                    items(userList.size){
                        index->
                        val userItem=userList[index]
                        ChatItem(
                            userItem = userItem,
                            onClick = { TODO() }
                        )
                    }
                }
                Button(
                    onClick = onSignOut,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = MaterialTheme.colorScheme.onError
                    )
                ) {
                    Text("Sign Out")
                }
            }


        }
    }
}
}

@Composable
fun ChatItem(
    userItem: UserModel,
    onClick: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Profile picture
        Box(
            modifier = Modifier
                .size(50.dp)
                .clip(CircleShape)
                .border(1.dp, MaterialTheme.colorScheme.primary, CircleShape)
        ) {
//            if (userItem.profilePicUrl.isNullOrEmpty()) {
//                // Default profile image if no URL is available
//                Image(
//                    painter = painterResource(R.drawable.user),
//                    contentDescription = "Profile picture",
//                    modifier = Modifier.fillMaxSize(),
//                    contentScale = ContentScale.Crop,
//                    colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.primary)
//                )
//            } else {
//                // Load profile image from URL
//                AsyncImage(
//                    model = userItem.profilePicUrl,
//                    contentDescription = "Profile picture",
//                    modifier = Modifier.fillMaxSize(),
//                    contentScale = ContentScale.Crop
//                )
//            }
            Image(
                painter = painterResource(R.drawable.user),
                contentDescription = "Profile picture",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        // User details
        Column(
            modifier = Modifier.weight(1f)
        ) {
            // User name
            Text(
                text = userItem.userName ?: "Unknown User",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(4.dp))

//            // Last message (if exists in your UserModel)
//            userItem.?.let { lastMessage ->
//                Text(
//                    text = lastMessage,
//                    style = MaterialTheme.typography.bodyMedium,
//                    color = MaterialTheme.colorScheme.onSurfaceVariant,
//                    maxLines = 1,
//                    overflow = TextOverflow.Ellipsis
//                )
//            }
        }

//        // Timestamp (if exists in your UserModel)
//        userItem.lastMessageTime?.let { timestamp ->
//            Text(
//                text = formatTimestamp(timestamp),
//                style = MaterialTheme.typography.bodySmall,
//                color = MaterialTheme.colorScheme.onSurfaceVariant
//            )
//        }
    }

    // Divider
    Divider(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 62.dp),
        color = MaterialTheme.colorScheme.outlineVariant,
        thickness = 0.5.dp
    )
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


