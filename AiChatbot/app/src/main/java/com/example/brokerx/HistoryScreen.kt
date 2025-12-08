package com.example.brokerx.screens

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.brokerx.NavigationBarSection
import com.example.brokerx.R
import com.example.brokerx.data.model.AssetOrder
import com.example.brokerx.viewmodels.HistoryViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    historyViewModel: HistoryViewModel,
    navController: NavController
) {
    val scrollState = rememberScrollState()
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    val backgroundColor = Color(0xFF0A0A0A)
    val surfaceColor = Color(0xFF141414)
    val primaryText = Color.White
    val secondaryText = Color(0xFF888888)
    val greenColor = Color(0xFF00D4AA)
    val redColor = Color(0xFFFF4444)

    if (isLandscape) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .background(backgroundColor)
        ) {
            NavigationBarSection(navController = navController)

            Column(
                modifier = Modifier
                    .weight(1f)
                    .statusBarsPadding()
                    .verticalScroll(scrollState)
                    .padding(16.dp) // spacing from edges
            ) {
                // Back button row
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            painter = painterResource(id = R.drawable.baseline_arrow_back_ios_new_24),
                            contentDescription = "Back",
                            tint = primaryText
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Trading History",
                        color = primaryText,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.W600
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Your content
                HistoryContent(
                    historyViewModel = historyViewModel,
                    navController = navController,
                    backgroundColor = backgroundColor,
                    surfaceColor = surfaceColor,
                    primaryText = primaryText,
                    secondaryText = secondaryText,
                    greenColor = greenColor,
                    redColor = redColor
                )
            }
        }
    } else {
        // PORTRAIT -> Bottom Nav
        Scaffold(
            containerColor = backgroundColor,
            bottomBar = { NavigationBarSection(navController = navController) },
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            "Trading History",
                            color = primaryText,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.W500
                        )
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = backgroundColor
                    ),
                    navigationIcon = {
                        IconButton(
                            onClick = { navController.popBackStack() }
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.baseline_arrow_back_ios_new_24),
                                contentDescription = "Back",
                                tint = primaryText
                            )
                        }
                    }
                )
            }
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .background(backgroundColor)
                    .padding(innerPadding)
                    .verticalScroll(scrollState)
            ) {
                HistoryContent(
                    historyViewModel = historyViewModel,
                    navController = navController,
                    backgroundColor = backgroundColor,
                    surfaceColor = surfaceColor,
                    primaryText = primaryText,
                    secondaryText = secondaryText,
                    greenColor = greenColor,
                    redColor = redColor
                )
            }
        }
    }
}

@Composable
private fun HistoryContent(
    historyViewModel: HistoryViewModel,
    navController: NavController,
    backgroundColor: Color,
    surfaceColor: Color,
    primaryText: Color,
    secondaryText: Color,
    greenColor: Color,
    redColor: Color
) {
    val orders by historyViewModel.orders.observeAsState(emptyList())
    val isLoading by historyViewModel.isLoading.observeAsState(true)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
            .padding(horizontal = 20.dp)
    ) {
        Spacer(modifier = Modifier.height(24.dp))

        // Header
        Text(
            text = "Trading History",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = primaryText
        )

        Text(
            text = "All completed transactions",
            fontSize = 16.sp,
            fontWeight = FontWeight.Normal,
            color = secondaryText
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Loading State
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 40.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    color = Color(0xFF1976D2),
                    strokeWidth = 2.dp
                )
            }
        }
        // Empty State
        else if (orders.isEmpty()) {
            EmptyHistoryState(
                surfaceColor = surfaceColor,
                primaryText = primaryText,
                secondaryText = secondaryText
            )
        }
        // Orders List
        else {
            // Summary Stats
            val totalOrders = orders.size
            val buyOrders = orders.count { it.type.equals("BUY", ignoreCase = true) }
            val sellOrders = orders.count { it.type.equals("SELL", ignoreCase = true) }
            val totalVolume = orders.sumOf { it.price * it.quantity }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                colors = CardDefaults.cardColors(containerColor = surfaceColor),
                elevation = CardDefaults.cardElevation(0.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    Text(
                        text = "Summary",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.W600,
                        color = primaryText
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        StatItem("Total Orders", totalOrders.toString(), primaryText, secondaryText)
                        StatItem("Buy Orders", buyOrders.toString(), greenColor, secondaryText)
                        StatItem("Sell Orders", sellOrders.toString(), redColor, secondaryText)
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    StatItem(
                        label = "Total Volume",
                        value = "$${String.format("%,.2f", totalVolume)}",
                        valueColor = primaryText,
                        labelColor = secondaryText
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Recent Transactions",
                fontSize = 18.sp,
                fontWeight = FontWeight.W600,
                color = primaryText,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            // Orders grouped by date
            val groupedOrders = orders
                .sortedByDescending { it.timestamp ?: 0L }
                .groupBy { order ->
                    val date = Date(order.timestamp ?: 0L)
                    SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(date)
                }

            groupedOrders.forEach { (date, dayOrders) ->
                Text(
                    text = date,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.W500,
                    color = secondaryText,
                    modifier = Modifier.padding(vertical = 8.dp)
                )

                dayOrders.forEach { order ->
                    OrderItem(
                        order = order,
                        surfaceColor = surfaceColor,
                        primaryText = primaryText,
                        secondaryText = secondaryText,
                        greenColor = greenColor,
                        redColor = redColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(100.dp))
        }
    }
}

@Composable
private fun StatItem(
    label: String,
    value: String,
    valueColor: Color,
    labelColor: Color
) {
    Column {
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = FontWeight.Normal,
            color = labelColor
        )
        Text(
            text = value,
            fontSize = 16.sp,
            fontWeight = FontWeight.W600,
            color = valueColor
        )
    }
}

@Composable
private fun OrderItem(
    order: AssetOrder,
    surfaceColor: Color,
    primaryText: Color,
    secondaryText: Color,
    greenColor: Color,
    redColor: Color
) {
    val isBuy = order.type.equals("BUY", ignoreCase = true)
    val orderColor = if (isBuy) greenColor else redColor
    val totalValue = order.price * order.quantity

    val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
    val time = timeFormat.format(Date(order.timestamp ?: 0L))

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = surfaceColor),
        elevation = CardDefaults.cardElevation(0.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Order Type Badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(orderColor.copy(alpha = 0.15f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = order.type.uppercase(),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.W600,
                        color = orderColor
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = order.symbol,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.W600,
                        color = primaryText
                    )
                    Text(
                        text = "${order.quantity} shares • $time",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Normal,
                        color = secondaryText
                    )
                }
            }

            Column(
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = "$${String.format("%.2f", order.price)}",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.W500,
                    color = secondaryText
                )
                Text(
                    text = "$${String.format("%,.2f", totalValue)}",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.W600,
                    color = primaryText
                )
            }
        }
    }
}

@Composable
private fun EmptyHistoryState(
    surfaceColor: Color,
    primaryText: Color,
    secondaryText: Color
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 60.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(80.dp)
                .background(surfaceColor, RoundedCornerShape(40.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "📊",
                fontSize = 32.sp
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "No Trading History",
            fontSize = 20.sp,
            fontWeight = FontWeight.W600,
            color = primaryText,
            textAlign = TextAlign.Center
        )

        Text(
            text = "Your completed transactions will appear here",
            fontSize = 14.sp,
            fontWeight = FontWeight.Normal,
            color = secondaryText,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 32.dp, vertical = 8.dp)
        )
    }
}