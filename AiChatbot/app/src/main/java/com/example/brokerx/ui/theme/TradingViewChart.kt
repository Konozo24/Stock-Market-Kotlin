package com.example.brokerx.ui.theme

import android.webkit.WebView
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView


// ✅ Map your coinId to a proper TradingView symbol
fun mapToTradingViewSymbol(coinId: String): String? {
    return "BINANCE:${coinId}USDT"
}

@Composable
fun TradingViewChart(coinId: String, modifier: Modifier) {
    val symbol = mapToTradingViewSymbol(coinId)

    AndroidView(factory = { context ->
        WebView(context).apply {
            settings.javaScriptEnabled = true
            // Optional: improve performance
            settings.domStorageEnabled = true

            loadDataWithBaseURL(
                null,
                """
                <html>
                <head>
                    <script type="text/javascript" src="https://s3.tradingview.com/tv.js"></script>
                </head>
                <body>
                    <div id="tradingview_chart" style="width:100%; height:400px;"></div>
                    <script type="text/javascript">
                        new TradingView.widget({
                            "container_id": "tradingview_chart",
                            "autosize": true,
                            "symbol": "$symbol",
                            "interval": "1",
                            "timezone": "Etc/UTC",
                            "theme": "dark",
                            "style": "1",
                            "locale": "en",
                            "toolbar_bg": "#000000",
                            "enable_publishing": false,
                            "allow_symbol_change": true
                        });
                    </script>
                </body>
                </html>
                """.trimIndent(),
                "text/html",
                "UTF-8",
                null
            )
        }
    })
}