package com.example.brokerx

import PortfolioStock
import android.content.res.Configuration
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.brokerx.viewmodels.PortfolioViewModel

@Composable
fun PortfolioScreen(portfolioViewModel: PortfolioViewModel, navController: NavController) {
    val scrollState = rememberScrollState()
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    val backgroundColor = Color(0xFF0A0A0A)

    if (isLandscape) {
        // LANDSCAPE -> Side Nav
        Row(
            modifier = Modifier
                .fillMaxSize()
                .background(backgroundColor)
        ) {
            NavigationBarSection(navController = navController) // left sidebar

            Column(
                modifier = Modifier
                    .weight(1f)
                    .statusBarsPadding()
                    .verticalScroll(scrollState)
            ) {
                PortfolioContent(portfolioViewModel = portfolioViewModel, navController = navController)
            }
        }
    } else {
        // PORTRAIT -> Bottom Nav
        Scaffold(
            containerColor = backgroundColor,
            bottomBar = { NavigationBarSection(navController = navController) }
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .background(backgroundColor)
                    .padding(innerPadding)
                    .verticalScroll(scrollState)
            ) {
                PortfolioContent(portfolioViewModel = portfolioViewModel, navController = navController)
            }
        }
    }
}

@Composable
private fun PortfolioContent(portfolioViewModel: PortfolioViewModel, navController: NavController) {
    val portfolio by portfolioViewModel.portfolio.observeAsState(emptyList())
    val wallet by portfolioViewModel.wallet.observeAsState()
    val totalValue by portfolioViewModel.totalValue.observeAsState(0.0)
    val totalPL by portfolioViewModel.totalPL.observeAsState(0.0)
    val unrealizedPL by portfolioViewModel.unrealizedPL.observeAsState(0.0)
    val bankAccount by portfolioViewModel.bankAccount.observeAsState()
    val unrealizedPLPercent by portfolioViewModel.unrealizedPLPercent.observeAsState(0.0)
    val context = LocalContext.current
    var showToast by rememberSaveable { mutableStateOf(false) }

    val backgroundColor = Color(0xFF0A0A0A)
    val surfaceColor = Color(0xFF141414)
    val primaryText = Color.White
    val secondaryText = Color(0xFF888888)
    val greenColor = Color(0xFF00D4AA)
    val redColor = Color(0xFFFF4444)
    val accentColor = Color(0xFF1976D2)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor),
        verticalArrangement = Arrangement.spacedBy(0.dp)
    ) {
        // Header Section
        Column(
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 24.dp)
        ) {
            // Header row with Portfolio + History button
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Portfolio",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = primaryText
                )

                ActionButton(
                    text = "History",
                    modifier = Modifier.width(100.dp),
                    backgroundColor = accentColor,
                    textColor = Color.White,
                    onClick = { navController.navigate("HistoryScreen") }
                )
            }
            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Total Value",
                fontSize = 20.sp,
                fontWeight = FontWeight.Normal,
                color = secondaryText,
                letterSpacing = 0.2.sp
            )

            Text(
                text = "$${String.format("%,.2f", totalValue)}",
                fontSize = 28.sp,
                fontWeight = FontWeight.W600,
                color = primaryText,
                letterSpacing = (-0.3).sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${if (totalPL >= 0) "+" else ""}$${String.format("%.2f", totalPL)}",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.W500,
                    color = if (totalPL >= 0) greenColor else redColor
                )

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = "(${if (unrealizedPLPercent >= 0) "+" else ""}${String.format("%.2f", unrealizedPLPercent)}%)",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Normal,
                    color = if (totalPL >= 0) greenColor else redColor
                )

                Spacer(modifier = Modifier.weight(1f))

                Text(
                    text = "Today",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Normal,
                    color = secondaryText
                )
            }
        }

        // Cash Balance Section
        wallet?.let { walletData ->
            Column(
                modifier = Modifier
                    .padding(horizontal = 20.dp)
                    .fillMaxWidth()
            ) {
                HorizontalDivider(
                    color = Color(0xFF222222),
                    thickness = 1.dp
                )

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "Buying Power",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Normal,
                            color = secondaryText
                        )
                        Text(
                            text = "$${String.format("%,.2f", walletData.cash)}",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.W500,
                            color = primaryText
                        )
                    }

                    Column(
                        horizontalAlignment = Alignment.End
                    ) {
                        Text(
                            text = "Available",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Normal,
                            color = secondaryText
                        )
                        Text(
                            text = "$${String.format("%,.2f", walletData.cash)}",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.W500,
                            color = primaryText
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }

        // Action Buttons
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ActionButton(
                text = "Deposit",
                modifier = Modifier.weight(1f),
                backgroundColor = accentColor,
                textColor = Color.White,
                onClick = {
                    if (bankAccount != null) {
                        navController.navigate("DepositScreen")
                    } else {
                        showToast = true
                    }
                }
            )


            ActionButton(
                text = "Transfer",
                modifier = Modifier.weight(1f),
                backgroundColor = surfaceColor,
                textColor = primaryText,
                onClick = {
                    if (bankAccount != null) {
                        navController.navigate("WithdrawScreen")
                    } else {
                        showToast = true
                    }
                }
            )

            LaunchedEffect(showToast) {
                if (showToast) {
                    Toast.makeText(context, "Please add a bank account first", Toast.LENGTH_SHORT).show()
                    showToast = false
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Holdings Header
        Column(
            modifier = Modifier.padding(horizontal = 20.dp)
        ) {
            Text(
                text = "Holdings",
                fontSize = 20.sp,
                fontWeight = FontWeight.W500,
                color = primaryText
            )

            Text(
                text = "${portfolio.size} positions",
                fontSize = 16.sp,
                fontWeight = FontWeight.Normal,
                color = secondaryText
            )

            Spacer(modifier = Modifier.height(16.dp))
        }

        // Holdings List
        if (portfolio.isEmpty()) {
            EmptyHoldingsState()
        } else {
            portfolio.forEach { stock ->
                HoldingItem(
                    stock = stock,
                    greenColor = greenColor,
                    redColor = redColor,
                    primaryText = primaryText,
                    secondaryText = secondaryText
                )
            }
        }

        // Bottom padding
        Spacer(modifier = Modifier.height(100.dp))
    }
}

@Composable
private fun ActionButton(
    text: String,
    modifier: Modifier = Modifier,
    backgroundColor: Color,
    textColor: Color,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .height(48.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(backgroundColor)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            fontSize = 16.sp,
            fontWeight = FontWeight.W500,
            color = textColor
        )
    }
}

@Composable
private fun HoldingItem(
    stock: PortfolioStock, // ✅ real model
    greenColor: Color,
    redColor: Color,
    primaryText: Color,
    secondaryText: Color
) {
    val plToday = (stock.currentPrice - stock.avgPurchasePrice) * stock.quantity
    val plPercent = if (stock.avgPurchasePrice > 0)
        (plToday / (stock.avgPurchasePrice * stock.quantity)) * 100 else 0.0
    val totalValue = stock.currentPrice * stock.quantity

    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { /* TODO: Navigate to stock details */ }
                .padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left side - Stock info
            Column {
                Text(
                    text = stock.symbol,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.W600,
                    color = primaryText
                )
                Text(
                    text = "${stock.quantity} shares",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Normal,
                    color = secondaryText
                )
            }

            // Right side - Values
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "$${String.format("%,.2f", totalValue)}",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.W600,
                    color = primaryText
                )

                Text(
                    text = "${if (plToday >= 0) "+" else ""}$${String.format("%.2f", plToday)} " +
                            "(${if (plPercent >= 0) "+" else ""}${String.format("%.1f", plPercent)}%)",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Normal,
                    color = if (plToday >= 0) greenColor else redColor
                )
            }
        }

        HorizontalDivider(
            modifier = Modifier.padding(horizontal = 20.dp),
            color = Color(0xFF1A1A1A),
            thickness = 1.dp
        )
    }
}

@Composable
private fun EmptyHoldingsState() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .background(
                    Color(0xFF1A1A1A),
                    RoundedCornerShape(32.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "📊",
                fontSize = 24.sp
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "No Holdings Yet",
            fontSize = 18.sp,
            fontWeight = FontWeight.W500,
            color = Color.White
        )

        Text(
            text = "Start investing to see your portfolio here",
            fontSize = 14.sp,
            fontWeight = FontWeight.Normal,
            color = Color(0xFF888888)
        )
    }
}