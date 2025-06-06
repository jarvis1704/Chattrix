package com.biprangshu.chattrix.home

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.biprangshu.chattrix.R
import com.biprangshu.chattrix.data.MessageModel
import com.biprangshu.chattrix.ui.theme.ChatTypography
import com.biprangshu.chattrix.viewmodel.ChatViewModel
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    modifier: Modifier = Modifier,
    navController: NavController,
    chatViewModel: ChatViewModel = hiltViewModel(),
    userId: String,
    userName: String
) {
    val messages by chatViewModel.messages.collectAsState()
    var messageText by remember { mutableStateOf("") }
    val currentUser = FirebaseAuth.getInstance().currentUser
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    val hapticFeedback = LocalHapticFeedback.current


    val sendButtonScale = remember { Animatable(1f) }
    val sendButtonRotation = remember { Animatable(0f) }

    LaunchedEffect(key1 = userId) {
        chatViewModel.loadMessage(userId)
    }


    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            coroutineScope.launch {
                listState.animateScrollToItem(messages.size - 1)
            }
        }
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize(),
        topBar = {
            TopAppBar(
                navController = navController,
                userName = userName
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPaddingValues ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPaddingValues).statusBarsPadding()
                .navigationBarsPadding(),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
            ) {

                if (messages.isEmpty()) {

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Card(
                            modifier = Modifier.fillMaxWidth(0.8f),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                            )
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center,
                                modifier = Modifier.padding(32.dp)
                            ) {
                                Icon(
                                    Icons.Default.Send,
                                    contentDescription = null,
                                    modifier = Modifier.size(48.dp),
                                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    "Start the conversation",
                                    style = ChatTypography.headlineMedium,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    "Send your first message to $userName",
                                    style = ChatTypography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                                )
                            }
                        }
                    }
                } else {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(vertical = 8.dp)
                    ) {
                        items(messages.size) { index ->
                            val message = messages[index]
                            val isFromMe = message.senderId == currentUser?.uid

                            MessageItem(
                                message = message,
                                isFromMe = isFromMe
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))


                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = messageText,
                            onValueChange = { messageText = it },
                            placeholder = {
                                Text(
                                    "Type a message...",
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                    style = ChatTypography.headlineMedium,
                                    fontSize = 14.sp
                                )
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(20.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                                focusedContainerColor = MaterialTheme.colorScheme.surface,
                                unfocusedContainerColor = MaterialTheme.colorScheme.surface
                            ),
                            maxLines = 4,
                            textStyle = ChatTypography.headlineMedium.copy(
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Normal
                            )
                        )

                        Spacer(Modifier.width(12.dp))


                        FloatingActionButton(
                            onClick = {
                                if (messageText.isNotBlank()) {
                                    hapticFeedback.performHapticFeedback(HapticFeedbackType.TextHandleMove)


                                    coroutineScope.launch {
                                        launch {
                                            sendButtonScale.animateTo(
                                                0.8f,
                                                animationSpec = tween(100)
                                            )
                                            sendButtonScale.animateTo(
                                                1f,
                                                animationSpec = spring(
                                                    dampingRatio = Spring.DampingRatioMediumBouncy,
                                                    stiffness = Spring.StiffnessHigh
                                                )
                                            )
                                        }
                                        launch {
                                            sendButtonRotation.animateTo(
                                                360f,
                                                animationSpec = tween(300)
                                            )
                                            sendButtonRotation.snapTo(0f)
                                        }
                                    }

                                    chatViewModel.sendMessage(userId, messageText)
                                    messageText = ""
                                }
                            },
                            modifier = Modifier
                                .size(48.dp)
                                .scale(sendButtonScale.value)
                                .graphicsLayer(rotationZ = sendButtonRotation.value),
                            elevation = FloatingActionButtonDefaults.elevation(
                                defaultElevation = 6.dp,
                                pressedElevation = 8.dp
                            ),
                            shape = CircleShape,
                            containerColor = if (messageText.isNotBlank())
                                MaterialTheme.colorScheme.primary
                            else
                                MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            Icon(
                                Icons.Default.Send,
                                contentDescription = "Send message",
                                tint = if (messageText.isNotBlank())
                                    MaterialTheme.colorScheme.onPrimary
                                else
                                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopAppBar(
    modifier: Modifier = Modifier,
    navController: NavController,
    userName: String
) {
    TopAppBar(
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {

                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .shadow(4.dp, CircleShape)
                        .background(MaterialTheme.colorScheme.primary, CircleShape)
                        .padding(2.dp)
                ) {
                    Image(
                        painter = painterResource(R.drawable.user),
                        contentDescription = "User image",
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surface),
                        contentScale = ContentScale.Crop,
                        colorFilter = androidx.compose.ui.graphics.ColorFilter.tint(
                            MaterialTheme.colorScheme.primary
                        )
                    )
                }

                Spacer(Modifier.width(12.dp))

                Column {
                    Text(
                        text = userName,
                        style = ChatTypography.headlineMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 18.sp
                    )
                }
            }
        },
        navigationIcon = {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(
                    Icons.Default.ArrowBack,
                    contentDescription = "Go back",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
        )
    )
}

@Composable
fun MessageItem(
    message: MessageModel,
    isFromMe: Boolean
) {
    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = if (isFromMe) Alignment.CenterEnd else Alignment.CenterStart
    ) {
        Card(
            shape = RoundedCornerShape(
                topStart = 16.dp,
                topEnd = 16.dp,
                bottomStart = if (isFromMe) 16.dp else 4.dp,
                bottomEnd = if (isFromMe) 4.dp else 16.dp
            ),
            colors = CardDefaults.cardColors(
                containerColor = if (isFromMe)
                    MaterialTheme.colorScheme.primary
                else
                    MaterialTheme.colorScheme.surfaceVariant
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.widthIn(max = 280.dp)
        ) {
            Column(
                modifier = Modifier.padding(12.dp)
            ) {
                Text(
                    text = message.message,
                    style = ChatTypography.bodyLarge,
                    color = if (isFromMe)
                        MaterialTheme.colorScheme.onPrimary
                    else
                        MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = formatTimestamp(message.timestamp),
                    style = ChatTypography.labelSmall,
                    color = if (isFromMe)
                        MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f)
                    else
                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    modifier = Modifier.align(Alignment.End)
                )
            }
        }
    }
}