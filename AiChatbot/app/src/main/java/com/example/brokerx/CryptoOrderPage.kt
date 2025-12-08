import android.util.Log
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Divider
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.brokerx.data.model.AssetDetail
import com.example.brokerx.data.model.OrderType
import com.example.brokerx.viewmodels.CryptoViewModel
import com.example.brokerx.viewmodels.PortfolioViewModel
import com.example.brokerx.viewmodels.PortfolioViewModelFactory
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.firestore


import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import com.example.brokerx.R
import com.example.brokerx.data.model.AssetOrder
import com.example.brokerx.utils.NotificationHelper
import com.example.brokerx.utils.NotificationHelper.NotificationPermissionRequester

@Composable
fun CryptoOrderPage(
    cryptoDetail: AssetDetail,
    cryptoVM: CryptoViewModel,
    portfolioVM: PortfolioViewModel,
    cryptoOrderVM: CryptoOrderViewModel,
    onBackToWatchlist: () -> Unit // callback to navigate back
) {


    val wallet by portfolioVM.wallet.observeAsState()
    var quantityInput by rememberSaveable { mutableStateOf(0) }
    val portfolio by portfolioVM.portfolio.observeAsState(emptyList())

    var showConfirmDialog by rememberSaveable { mutableStateOf(false) }
    var showInvalidQuantityDialog by rememberSaveable { mutableStateOf(false) }
    var currentOrderType by rememberSaveable { mutableStateOf(OrderType.BUY) }
    var orderPlaced by rememberSaveable { mutableStateOf(false) }
    var insufficientFunds by rememberSaveable { mutableStateOf(false) }

    val context = LocalContext.current
    NotificationPermissionRequester()

    LaunchedEffect(cryptoVM.cryptos) {
        portfolioVM.trackLivePrices()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .statusBarsPadding()
    ) {

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                painter = painterResource(id = R.drawable.baseline_arrow_back_ios_new_24),
                contentDescription = "Back",
                tint = Color.White,
                modifier = Modifier.clickable{ onBackToWatchlist() })

            Text(
                text = "${cryptoDetail.name} (${cryptoDetail.symbol.uppercase()})",
                fontSize = 30.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            TextField(
                value = quantityInput.toString(),
                onValueChange = {
                    val intVal = it.toIntOrNull() ?: 0
                    quantityInput = intVal
                },
                label = { Text("Quantity", color = Color.LightGray) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.DarkGray,
                    unfocusedContainerColor = Color.DarkGray,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    cursorColor = Color.White,
                    focusedLabelColor = Color.LightGray,
                    unfocusedLabelColor = Color.Gray
                )
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Button(
                    onClick = {
                        if (quantityInput <= 0) {
                            showInvalidQuantityDialog = true
                        } else {
                            currentOrderType = OrderType.BUY
                            orderPlaced = false
                            insufficientFunds = false
                            showConfirmDialog = true
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)), // Green
                    modifier = Modifier.weight(1f)
                ) { Text("Buy") }

                Button(
                    onClick = {
                        if (quantityInput <= 0) {
                            showInvalidQuantityDialog = true
                        } else {
                            currentOrderType = OrderType.SELL
                            orderPlaced = false
                            insufficientFunds = false
                            showConfirmDialog = true
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF44336)), // Green
                    modifier = Modifier.weight(1f)
                ) { Text("Sell") }
            }

            wallet?.let {
                Text(
                    text = "Cash: \$${String.format("%.2f", it.cash)}",
                    fontSize = 25.sp,
                    color = Color(0xFF4CAF50)
                )
            }

            Text(
                text = "Portfolio",
                fontSize = 30.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.White
            )

            Divider(color = Color.Gray, thickness = 1.dp)


            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(portfolio) { stock ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "${stock.symbol.uppercase()} x${stock.quantity}",
                            color = Color.White,
                            fontSize = 25.sp
                        )
                        Text(
                            text = "\$${String.format("%.2f", stock.currentPrice)}",
                            color = if (stock.currentPrice >= stock.avgPurchasePrice) Color(0xFF4CAF50) else Color.Red,
                            fontSize = 25.sp
                        )
                    }
                }
            }
        }

        if (showInvalidQuantityDialog) {
            AlertDialog(
                onDismissRequest = { showInvalidQuantityDialog = false },
                title = { Text("Invalid Quantity") },
                text = { Text("Please enter a quantity greater than zero.", fontSize = 18.sp) },
                confirmButton = {
                    TextButton(onClick = { showInvalidQuantityDialog = false }) {
                        Text("OK", fontSize = 18.sp)
                    }
                }
            )
        }

        // --- Confirmation Dialog ---
        if (showConfirmDialog) {


            AlertDialog(
                onDismissRequest = {
                    showConfirmDialog = false
                    insufficientFunds = false
                    orderPlaced = false },
                title = {
                    Text(
                        text = if (!orderPlaced) "Confirm ${currentOrderType.name}" else "Order Confirmed"
                    )
                },
                text = {
                    if (!orderPlaced) {
                        if (insufficientFunds) {
                            Text(
                                "Insufficient ${if (currentOrderType == OrderType.BUY) "cash" else "crypto"} to place this order.",
                                fontSize = 18.sp,
                                color = Color.Red
                            )
                        } else {
                            Text(
                                "Are you sure you want to ${currentOrderType.name.lowercase()} $quantityInput ${cryptoDetail.symbol.uppercase()}?",
                                fontSize = 20.sp
                            )
                        }
                    } else {
                        Text(
                            "Your order has been successfully placed.",
                            fontSize = 20.sp
                        )
                    }
                },
                confirmButton = {
                    if (!orderPlaced) {
                        TextButton(onClick = {
                            val success = cryptoOrderVM.placeOrder(currentOrderType, quantityInput)
                            if (success) {
                                orderPlaced = true
                                //  Show notification
                                NotificationHelper.showTradeNotification(
                                    context,
                                    type = currentOrderType.name,
                                    symbol = cryptoDetail.symbol,
                                    quantity = quantityInput,
                                    price = cryptoDetail.price
                                )
                            } else {
                                insufficientFunds = true
                            }
                        }) {
                            Text("Yes", fontSize = 20.sp)
                        }
                    } else {
                        TextButton(onClick = {
                            showConfirmDialog = false
                            onBackToWatchlist()
                        }) {
                            Text("Back", fontSize = 20.sp)
                        }
                    }
                },
                dismissButton = {
                    if (!orderPlaced) {
                        TextButton(onClick = { showConfirmDialog = false }) {
                            Text("No", fontSize = 20.sp)
                        }
                    }
                }
            )
        }
    }
}

