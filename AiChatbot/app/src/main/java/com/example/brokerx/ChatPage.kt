package com.example.brokerx

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.brokerx.data.model.MessageModel
import com.example.brokerx.ui.theme.ColorModelMessage
import com.example.brokerx.ui.theme.ColorUserMessage
import com.example.brokerx.ui.theme.backgroundColor
import com.example.brokerx.viewmodels.ChatViewModel

@Composable
fun ChatPage (modifier: Modifier = Modifier, viewModel: ChatViewModel, navController : NavController) {
    Column (
        modifier = modifier.background(backgroundColor)
    ){
        AppHeader(navController = navController)
        MessageList(modifier = Modifier.weight(1f), messageList = viewModel.messageList)
        MessageInput(
            onMessageSend = {
                viewModel.sendMessage(it)
            }
        )
    }
}

@Composable
fun AppHeader (modifier: Modifier = Modifier, navController: NavController) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.primary)
            .statusBarsPadding()
    ) {
        Row (
            verticalAlignment = Alignment.CenterVertically
        ){
            Icon(
                modifier = Modifier
                    .size(30.dp)
                    .clickable{
                        navController.popBackStack()
                    },
                painter = painterResource(id = R.drawable.baseline_arrow_back_ios_new_24),
                contentDescription = "Back Icon",
                tint = Color.White
            )

            Text(
                modifier = Modifier.padding(16.dp),
                text = "BrokerX AI",
                color = Color.White,
                fontSize = 22.sp
            )
        }



    }
}

@Composable
fun MessageList (modifier: Modifier = Modifier, messageList : List<MessageModel>) {
    if (messageList.isEmpty()){
        Column (
            modifier = modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ){
            Icon(
                modifier = Modifier.size(60.dp),
                painter = painterResource(id = R.drawable.baseline_question_answer_24),
                contentDescription = "Icon",
                tint = Color.White)
            Text(text = "Ask Me Anything", fontSize = 22.sp, color = Color.White)
        }
    } else {
        LazyColumn (
            modifier = modifier,
            reverseLayout = true
        ){
            items(messageList.reversed()){
                MessageRow(messageModel = it)
            }
        }
    }


}

@Composable
fun MessageRow (messageModel: MessageModel) {
    val isModel = messageModel.role=="model"

    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                modifier = Modifier.align(
                    if(isModel) Alignment.BottomStart else Alignment.BottomEnd
                ).padding(
                    start = if(isModel) 8.dp else 70.dp,
                    end = if(isModel) 70.dp else 8.dp,
                    top = 8.dp,
                    bottom = 8.dp
                )
                    .clip(RoundedCornerShape(48f))
                    .background(if(isModel) ColorModelMessage else ColorUserMessage)
                    .padding(16.dp)
            ) {
                SelectionContainer {
                    Text(text = messageModel.message, fontWeight = FontWeight.W500, color = Color.White)
                }


            }

        }

    }
}

@Composable
fun MessageInput (onMessageSend : (String) -> Unit) {
    var message by rememberSaveable { mutableStateOf("") }

    Row (
        modifier = Modifier
            .padding(8.dp)
            .navigationBarsPadding(),
        verticalAlignment = Alignment.CenterVertically
    ){
        OutlinedTextField(
            modifier = Modifier.weight(1f).background(Color.White, shape = RoundedCornerShape(20.dp)),
            value = message,
            onValueChange = {
                message = it
            },
        )

        IconButton(onClick = {
            if (message.isNotEmpty()){
                onMessageSend(message)
                message = ""
            }
            }) {

            Icon(
                imageVector = Icons.Default.Send,
                contentDescription = "Send",
                tint = Color.White
            )
        }
    }
}