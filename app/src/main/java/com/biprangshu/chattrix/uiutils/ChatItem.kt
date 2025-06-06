package com.biprangshu.chattrix.uiutils

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.biprangshu.chattrix.R
import com.biprangshu.chattrix.data.UserModel
import com.biprangshu.chattrix.ui.theme.ChatTypography

@Composable
fun ChatItem(
    userItem: UserModel,
    lastMessage: String? = null,
    onClick: () -> String,
    navController: NavController,
    isMessageRead: Boolean = true,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                navController.navigate(onClick())
            }
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {

        Box(
            modifier = Modifier
                .size(50.dp)
                .clip(CircleShape)
                .border(1.dp, MaterialTheme.colorScheme.primary, CircleShape)
        ) {
            Image(
                painter = painterResource(R.drawable.user),
                contentDescription = "Profile picture",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
            )
        }

        Spacer(modifier = Modifier.width(12.dp))


        Column(
            modifier = Modifier.weight(1f)
        ) {

            Text(
                text = userItem.userName ?: "Unknown User",
                style = ChatTypography.headlineMedium,
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(4.dp))


            Text(
                text = lastMessage ?: "Tap to start chatting",
                style = ChatTypography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                fontSize = 14.sp
            )
        }

        Spacer(Modifier.width(8.dp))


        if(!isMessageRead){
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .clip(CircleShape)
                    .background(color = MaterialTheme.colorScheme.primary),
            )
        }
    }
}