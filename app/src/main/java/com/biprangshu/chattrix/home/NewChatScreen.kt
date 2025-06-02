package com.biprangshu.chattrix.home

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.biprangshu.chattrix.ChattrixScreens
import com.biprangshu.chattrix.uiutils.ChatItem
import com.biprangshu.chattrix.viewmodel.MainActivityViewModel

@Composable
fun NewChatScreen(
    modifier: Modifier = Modifier,
    mainActivityViewModel: MainActivityViewModel = hiltViewModel(),
    navController: NavController
) {

    val allUsersList by mainActivityViewModel.allUserList.collectAsState()


    Surface(
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp).statusBarsPadding().navigationBarsPadding()
        ) {
            Text("New Chat")
            Spacer(Modifier.height(8.dp))
            Text("Select the contacts in Chattrix to chat with")
            Spacer(Modifier.height(16.dp))

            //users list in chattrix
            if(allUsersList.isEmpty()){
                Box(
                    contentAlignment = Alignment.Center,
                    modifier= Modifier.fillMaxWidth().weight(1f)
                ){
                    Text(text = "No users found, maybe touch some grass?", style = MaterialTheme.typography.bodyMedium)
                }
            }else{
                LazyColumn (
                    modifier = Modifier.weight(1f)
                ){
                    items(allUsersList.size){
                        index->
                        val user=allUsersList[index]
                        ChatItem(
                            userItem = user,
                            onClick = {"${ChattrixScreens.CHAT_SCREEN}/${user.userId}/${user.userName}"},
                            navController = navController
                        )
                    }
                }
            }

        }
    }
}