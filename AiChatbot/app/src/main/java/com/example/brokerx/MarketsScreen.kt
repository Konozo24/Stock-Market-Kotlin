import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.brokerx.NavigationBarSection
import com.example.brokerx.data.local.MarketEntity
import com.example.brokerx.data.model.AssetItem
import com.example.brokerx.utils.toAssetItem
import com.example.brokerx.viewmodels.CryptoInfoViewModel
import com.example.brokerx.viewmodels.CryptoViewModel


@Composable
fun MarketsScreen(
    cryptoViewModel: CryptoViewModel,
    cryptoInfoViewModel: CryptoInfoViewModel,
    navController: NavController
) {

    val cryptos by remember { derivedStateOf { cryptoViewModel.cryptos } }
    val scrollState = rememberScrollState()
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    // Saveable state for rotation
    var selectedTabIndex by rememberSaveable { mutableIntStateOf(0) }
    var query by rememberSaveable { mutableStateOf("") }

    val backgroundColor = Color(0xFF0A0A0A)

    LaunchedEffect(Unit) {
        cryptoViewModel.loadAllCryptosOnce()
    }

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

            ) {
                MarketsContentLazy(
                    cryptos = cryptos,
                    cryptoInfoViewModel = cryptoInfoViewModel,
                    navController = navController,
                    selectedTabIndex = selectedTabIndex,
                    onTabSelected = { selectedTabIndex = it },
                    query = query,
                    onQueryChange = { query = it },
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    } else {
        Scaffold(
            containerColor = backgroundColor,
            bottomBar = { NavigationBarSection(navController = navController) }
        ) { innerPadding ->
            MarketsContentLazy(
                cryptos = cryptos,
                cryptoInfoViewModel = cryptoInfoViewModel,
                navController = navController,
                selectedTabIndex = selectedTabIndex,
                onTabSelected = { selectedTabIndex = it },
                query = query,
                onQueryChange = { query = it },
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            )
        }
    }
}

@Composable
private fun MarketsContentLazy(
    cryptos: List<AssetItem>,
    cryptoInfoViewModel: CryptoInfoViewModel,
    navController: NavController,
    selectedTabIndex: Int,
    onTabSelected: (Int) -> Unit,
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundColor = Color(0xFF0A0A0A)
    val surfaceColor = Color(0xFF141414)
    val primaryText = Color.White
    val secondaryText = Color(0xFF888888)
    val greenColor = Color(0xFF00D4AA)
    val redColor = Color(0xFFFF4444)
    val accentColor = Color(0xFF1976D2)
    val tabs = listOf("Markets", "Gainers", "Losers")

    val filtered = when (selectedTabIndex) {
        0 -> cryptos.filter { it.symbol.contains(query, ignoreCase = true) }
        1 -> cryptos.filter { it.change > 0 && it.symbol.contains(query, ignoreCase = true) }
            .sortedByDescending { it.change }.take(50)
        2 -> cryptos.filter { it.change < 0 && it.symbol.contains(query, ignoreCase = true) }
            .sortedBy { it.change }.take(50)
        else -> cryptos
    }

    LazyColumn(
        modifier = modifier.background(backgroundColor),
        contentPadding = PaddingValues(bottom = 100.dp)
    ) {
        // Header
        item {
            Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 20.dp)) {
                Text(
                    text = "Markets",
                    fontSize = 40.sp,
                    fontWeight = FontWeight.Bold,
                    color = primaryText,
                    letterSpacing = (-0.5).sp
                )
                Text(
                    text = "Explore cryptocurrencies",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Normal,
                    color = secondaryText
                )
            }
        }

        // Search bar
        item {
            Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                SearchBar(
                    query = query,
                    onQueryChange = onQueryChange,
                    surfaceColor = surfaceColor,
                    primaryText = primaryText,
                    secondaryText = secondaryText
                )
                Spacer(modifier = Modifier.height(20.dp))
            }
        }

        // Tabs
        item {
            TabSelector(
                tabs = tabs,
                selectedIndex = selectedTabIndex,
                onTabSelected = onTabSelected,
                backgroundColor = backgroundColor,
                surfaceColor = surfaceColor,
                primaryText = primaryText,
                secondaryText = secondaryText,
                accentColor = accentColor
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Crypto list
        if (filtered.isEmpty()) {
            item {
                EmptyState(query = query, primaryText = primaryText, secondaryText = secondaryText)
            }
        } else {
            items(filtered) { crypto ->
                CryptoItem(
                    crypto = crypto,
                    onItemClick = {
                        cryptoInfoViewModel.loadCryptoDetail(crypto)
                        navController.navigate("CryptoInfo/${crypto.coinId}")
                    },
                    primaryText = primaryText,
                    secondaryText = secondaryText,
                    greenColor = greenColor,
                    redColor = redColor
                )

                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 20.dp),
                    color = Color(0xFF1A1A1A),
                    thickness = 1.dp
                )
            }
        }
    }
}


@Composable
private fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    surfaceColor: Color,
    primaryText: Color,
    secondaryText: Color
) {
    val keyboardController = LocalSoftwareKeyboardController.current

    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        placeholder = {
            Text(
                "Search cryptocurrencies...",
                color = secondaryText.copy(alpha = 0.6f)
            )
        },
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Search",
                tint = secondaryText
            )
        },
        trailingIcon = {
            if (query.isNotEmpty()) {
                IconButton(onClick = { onQueryChange("") }) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Clear",
                        tint = secondaryText
                    )
                }
            }
        },
        singleLine = true,
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        keyboardActions = KeyboardActions(
            onSearch = { keyboardController?.hide() }
        ),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Color(0xFF333333),
            unfocusedBorderColor = Color(0xFF222222),
            focusedTextColor = primaryText,
            unfocusedTextColor = primaryText,
            cursorColor = primaryText,
            focusedContainerColor = Color.Transparent,
            unfocusedContainerColor = Color.Transparent
        ),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
private fun TabSelector(
    tabs: List<String>,
    selectedIndex: Int,
    onTabSelected: (Int) -> Unit,
    backgroundColor: Color,
    surfaceColor: Color,
    primaryText: Color,
    secondaryText: Color,
    accentColor: Color
) {
    Column(
        modifier = Modifier.padding(horizontal = 20.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(surfaceColor)
                .padding(4.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            tabs.forEachIndexed { index, tab ->
                val isSelected = selectedIndex == index

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            if (isSelected) accentColor.copy(alpha = 0.2f)
                            else Color.Transparent
                        )
                        .clickable { onTabSelected(index) }
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = tab,
                        color = if (isSelected) accentColor else secondaryText,
                        fontWeight = if (isSelected) FontWeight.W500 else FontWeight.Normal,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun CryptoList(
    cryptos: List<AssetItem>,
    selectedTabIndex: Int,
    query: String,
    cryptoInfoViewModel: CryptoInfoViewModel,
    navController: NavController,
    primaryText: Color,
    secondaryText: Color,
    greenColor: Color,
    redColor: Color
) {


    val filtered = when (selectedTabIndex) {
        0 -> cryptos.filter { it.symbol.contains(query, ignoreCase = true) }
        1 -> cryptos.filter { it.change > 0 && it.symbol.contains(query, ignoreCase = true) }
            .sortedByDescending { it.change }.take(50)
        2 -> cryptos.filter { it.change < 0 && it.symbol.contains(query, ignoreCase = true) }
            .sortedBy { it.change }.take(50)
        else -> cryptos
    }

    if (filtered.isEmpty()) {
        EmptyState(query = query, primaryText = primaryText, secondaryText = secondaryText)
    } else {
        LazyColumn (
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 100.dp)
        ){
            item {
                // Header info
                Text(
                    text = "${filtered.size} cryptocurrencies",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Normal,
                    color = secondaryText,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
                    )
                }


                // List items
                items(filtered) { crypto ->
                    CryptoItem(
                        crypto = crypto,
                        onItemClick = {
                            cryptoInfoViewModel.loadCryptoDetail(crypto)
                            navController.navigate("CryptoInfo/${crypto.coinId}")
                        },
                        primaryText = primaryText,
                        secondaryText = secondaryText,
                        greenColor = greenColor,
                        redColor = redColor
                    )

                    // Divider after each item
                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 20.dp),
                        color = Color(0xFF1A1A1A),
                        thickness = 1.dp
                    )
                }


        }
    }
}

@Composable
private fun CryptoItem(
    crypto: AssetItem,
    onItemClick: () -> Unit,
    primaryText: Color,
    secondaryText: Color,
    greenColor: Color,
    redColor: Color
) {

    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onItemClick() }
                .padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Fixed-size logo container
            Box(
                modifier = Modifier
                    .width(40.dp)
                    .height(36.dp)
                    .clip(RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                if (!crypto.logoUrl.isNullOrEmpty()) {
                    AsyncImage(
                        model = crypto.logoUrl,
                        contentDescription = crypto.coinId,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }

            Spacer(modifier = Modifier.width(25.dp))
            // Left side - Crypto info
            Column (
                modifier = Modifier.weight(1f)
            ){
                Text(
                    text = crypto.symbol.uppercase(),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.W600,
                    color = primaryText
                )
                Text(
                    text = crypto.coinId.replaceFirstChar { it.uppercase() },
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Normal,
                    color = secondaryText
                )
            }

            // Right side - Price and change
            Column(
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = "$${String.format("%,.2f", crypto.price)}",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.W600,
                    color = primaryText
                )
                Text(
                    text = "${if (crypto.change >= 0) "+" else ""}${String.format("%.2f", crypto.change)}%",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Normal,
                    color = if (crypto.change >= 0) greenColor else redColor
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
private fun LoadingState() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 60.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Loading markets...",
            fontSize = 14.sp,
            color = Color(0xFF888888)
        )
    }
}

@Composable
private fun ErrorState(message: String, redColor: Color) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 60.dp, horizontal = 20.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = message,
            fontSize = 14.sp,
            color = redColor,
            fontWeight = FontWeight.Normal
        )
    }
}

@Composable
private fun EmptyState(
    query: String,
    primaryText: Color,
    secondaryText: Color
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 60.dp, horizontal = 20.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "No results found",
                fontSize = 18.sp,
                fontWeight = FontWeight.W500,
                color = primaryText
            )
            Text(
                text = if (query.isNotEmpty()) "Try searching for a different cryptocurrency"
                else "No cryptocurrencies available",
                fontSize = 14.sp,
                fontWeight = FontWeight.Normal,
                color = secondaryText
            )
        }
    }
}