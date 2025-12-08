package com.example.brokerx.viewmodels


import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.brokerx.data.model.ChatCategory
import com.example.brokerx.data.model.MessageModel
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.GenerativeBackend
import com.google.firebase.ai.type.content
import kotlinx.coroutines.launch

class ChatViewModel : ViewModel() {

    val messageList by lazy {
        mutableStateListOf<MessageModel>()
    }

    val model = Firebase.ai(backend = GenerativeBackend.googleAI())
    .generativeModel("gemini-2.5-flash")



    fun sendMessage(question: String) {
        viewModelScope.launch {
            messageList.add(MessageModel(question, "user"))
            messageList.add(MessageModel("Typing...", "model"))

            val category = detectCategory(question)

            when (category) {
                ChatCategory.INVESTMENT -> {
                    try {
                        val chat = model.startChat(
                            history = messageList.map {
                                content(it.role) { text(it.message) }
                            }.toList()
                        )
                        val response = chat.sendMessage(question)
                        val reply = response.text ?: "No response"
                        updateLastMessage(reply)
                    } catch (e: Exception) {
                        updateLastMessage("⚠️ Error: ${e.message}")
                    }
                }
                ChatCategory.CUSTOMER_SUPPORT -> {
                    val reply = getSupportAnswer(question)
                    updateLastMessage(reply)
                }
            }
        }
    }

    private fun updateLastMessage(reply: String) {
        if (messageList.lastOrNull()?.message == "Typing...") {
            messageList.removeAt(messageList.lastIndex)
        }
        messageList.add(MessageModel(reply, "model"))
    }

    private fun detectCategory(message: String): ChatCategory {
        val msg = message.lowercase()

        val investmentKeywords = listOf("crypto", "bitcoin", "ethereum", "trading", "investment", "portfolio", "market")
        val supportKeywords = listOf("login", "password", "account", "deposit", "withdrawal", "help", "support")

        return when {
            investmentKeywords.any { msg.contains(it) } -> ChatCategory.INVESTMENT
            supportKeywords.any { msg.contains(it) } -> ChatCategory.CUSTOMER_SUPPORT
            else -> ChatCategory.CUSTOMER_SUPPORT // default to support
        }
    }

    private fun getSupportAnswer(question: String): String {
        val msg = question.lowercase()

        return when {
            msg.contains("hi") || msg.contains("hello") || msg.contains("hey") ->
                "👋 Hi there! How can I help you today?"
            msg.contains("login") || msg.contains("password") ->
                "🔑 If you forgot your password, tap *Login > Forgot Password* to reset it."
            msg.contains("deposit") ->
                "💰 To deposit funds, go to *Wallet > Deposit* and choose Bank Transfer or Card."
            msg.contains("withdraw") || msg.contains("payout") ->
                "🏦 To withdraw funds, go to *Wallet > Withdraw*. Withdrawals usually take 1–3 business days."
            msg.contains("account") ->
                "👤 You can update your account info under *Settings > Account Details*."
            msg.contains("support") || msg.contains("help") ->
                "📩 You can reach our support team at support@brokerx.com for further assistance."
            else ->
                "❓ I don’t have an answer for that. Please contact support@brokerx.com."
        }
    }

}