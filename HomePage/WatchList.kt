package com.example.aichatbot.ui.theme

import android.view.Menu
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Button
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.aichatbot.R

@Composable
fun WatchList (navController: NavController) {
    WatchListScreen(navController)
}

@Composable
fun WatchListScreen (navController : NavController) {
    Scaffold (
        containerColor = Color(0xFF000000), //Black
        bottomBar = {
            NavigationBarSection()
        }
    ){
        innerPadding -> Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .background(Color(0xFF000000))
            .padding(innerPadding) // bottom nav padding
    ){
        MenuHeader(navController)
        AssetsSection()

    }
    }

}


@Composable
fun MenuHeader (navController: NavController) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF000000)) //Black Color
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("BrokerX", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color.White)
        Icon(modifier = Modifier
            .size(30.dp)
            .clickable {
                navController.navigate("AiChatBot")
            },
            painter = painterResource(id = R.drawable.baseline_message_24),
            contentDescription = "AI Chatbot",
            tint = Color.White
        )
    }
}

@Composable
fun AssetsSection (modifier: Modifier = Modifier) {

    var isVisible by remember { mutableStateOf(true) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text("Total Assets (USD)", fontSize = 14.sp, color = Color.Gray)
            Spacer(modifier = Modifier.height(2.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = if (isVisible) "200.12" else "****"
                    , fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Color.White)
                Spacer(Modifier.width(8.dp))

                Icon(modifier = Modifier
                    .size(30.dp)
                    .clickable {
                        isVisible = !isVisible
                    },
                    painter = painterResource(id = R.drawable.baseline_remove_red_eye_24),
                    contentDescription = "Show/Hide",
                    tint = Color.White
                )
            }
        }
        Column(horizontalAlignment = Alignment.End) {
            Text("Today's P/L", fontSize = 14.sp, color = Color.Gray)
            Spacer(modifier = Modifier.height(2.dp))
            Text("+2.38%", color = Color(0xFF2E7D32), fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }
    }

    Divider(
        color = Color.White,
        thickness = 2.dp,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 10.dp)
    )
}

@Composable
fun StockSection (modifier: Modifier = Modifier) {
    
}

@Composable
fun NavigationBarSection (modifier: Modifier = Modifier) {

    Divider(color = Color.Gray, thickness = 1.dp)

    Row (
        modifier = Modifier
        .fillMaxWidth()
        .background(Color.Black)
        .navigationBarsPadding()
        .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ){
        NavBarItem(R.drawable.baseline_home_filled_24, "Home")
        NavBarItem(R.drawable.baseline_bar_chart_24, "Market")
        NavBarItem(R.drawable.baseline_account_balance_wallet_24, "Wallet")
        NavBarItem(R.drawable.baseline_account_circle_24, "Account")
    }
}

@Composable
fun NavBarItem(icon: Int, label: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            painter = painterResource(id = icon),
            contentDescription = label,
            modifier = Modifier.size(24.dp),
            tint = Color.White
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            color = Color.White,
            fontSize = 12.sp
        )
    }
}
