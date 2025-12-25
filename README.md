# BrokerX

A modern, feature-rich cryptocurrency and stock trading Android application built with Kotlin and Jetpack Compose. BrokerX provides a comprehensive platform for managing your crypto portfolio, tracking markets, and making informed trading decisions with AI-powered assistance.

## Features

### Authentication & User Management
- User registration and login with Firebase Authentication
- Secure password recovery
- KYC (Know Your Customer) verification system
- Personal profile management

### Trading & Portfolio Management
- **Markets Screen**: Real-time cryptocurrency market data
- **Portfolio Management**: Track your investments and holdings
- **Watchlist**: Monitor your favorite cryptocurrencies
- **Order Execution**: Buy and sell crypto assets seamlessly
- **Transaction History**: Complete record of all your trades

### Banking Operations
- **Deposit Funds**: Add money to your trading account
- **Withdraw Funds**: Securely withdraw your earnings
- **Bank Account Management**: Link and manage bank accounts

### AI-Powered Features
- **AI Chatbot**: Get trading insights and assistance powered by Google AI
- **Smart Trading Suggestions**: AI-driven market analysis

### Market Information
- Real-time crypto prices and market data
- Detailed cryptocurrency information pages
- Price charts and market trends
- Market statistics and analytics

### Account Management
- Personal details configuration
- Notification settings
- Privacy and security settings
- Terms & conditions
- Settings customization

## Tech Stack

### Core Technologies
- **Language**: Kotlin
- **UI Framework**: Jetpack Compose
- **Minimum SDK**: 31 (Android 12)
- **Target SDK**: 36

### Architecture & Libraries
- **Architecture Pattern**: MVVM (Model-View-ViewModel)
- **Navigation**: Jetpack Navigation Compose
- **Dependency Injection**: ViewModel Factory pattern
- **Async Operations**: Kotlin Coroutines & LiveData

### Backend & Data
- **Remote Database**: Firebase Firestore
- **Authentication**: Firebase Auth
- **Local Database**: Room Database
- **Data Persistence**: DataStore Preferences
- **API Integration**: Retrofit with Gson converter

### AI & Analytics
- **AI Integration**: Google AI Edge (AI Core)
- **Firebase Analytics**: User behavior tracking
- **Firebase AI**: ML-powered features

### UI & Media
- **Image Loading**: Coil
- **Material Design**: Material 3 components
- **Adaptive UI**: Edge-to-edge display support
  
## Getting Started

### Prerequisites
- Android Studio Hedgehog or later
- JDK 11 or higher
- Android SDK 31+
- Firebase account with project setup

### Installation

1. **Clone the repository**
   ```bash
   git clone https://github.com/Konozo24/Stock-Market-Kotlin.git
   cd Stock-Market-Kotlin
   ```

2. **Set up Firebase**
   - Create a new Firebase project at [Firebase Console](https://console.firebase.google.com/)
   - Add an Android app to your Firebase project
   - Download `google-services.json`
   - Place it in the `AiChatbot/app/` directory

3. **Configure Firebase Services**
   - Enable Firebase Authentication (Email/Password)
   - Enable Firebase Firestore Database
   - Enable Firebase Analytics
   - Set up appropriate security rules

4. **Open in Android Studio**
   - Open Android Studio
   - Select "Open an existing project"
   - Navigate to the cloned directory
   - Wait for Gradle sync to complete

5. **Build and Run**
   ```bash
   ./gradlew build
   ```
   - Connect an Android device or start an emulator
   - Click "Run" in Android Studio

## Project Structure

```
AiChatbot/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/example/brokerx/
│   │   │   │   ├── MainActivity.kt          # Entry point
│   │   │   │   ├── StockAppNavigation.kt    # Navigation graph
│   │   │   │   ├── Account/                  # Account management screens
│   │   │   │   ├── authentication/           # Auth screens
│   │   │   │   ├── data/                     # Data layer
│   │   │   │   │   ├── api/                  # API services
│   │   │   │   │   ├── local/                # Room database
│   │   │   │   │   └── model/                # Data models
│   │   │   │   ├── ui/                       # UI components
│   │   │   │   ├── viewmodels/               # ViewModels
│   │   │   │   └── utils/                    # Utility classes
│   │   │   └── res/                          # Resources
│   │   └── test/                             # Unit tests
│   ├── build.gradle.kts                       # App-level Gradle config
│   └── google-services.json                   # Firebase config
├── gradle/                                     # Gradle wrapper
├── build.gradle.kts                           # Project-level Gradle
└── settings.gradle.kts                        # Gradle settings
```

## Key Components

### ViewModels
- **AuthViewModel**: Handles user authentication
- **CryptoViewModel**: Manages cryptocurrency data
- **CryptoInfoViewModel**: Detailed crypto information
- **ChatViewModel**: AI chatbot interactions
- **PortfolioViewModel**: User portfolio management
- **HistoryViewModel**: Transaction history
- **PersonalDetailsViewModel**: User profile data

### Main Screens
- **SplashRouter**: Initial app routing
- **LoginPage/SignUpPage**: Authentication screens
- **MarketsScreen**: Browse all available cryptocurrencies
- **PortfolioScreen**: View your holdings
- **WatchList**: Track favorite assets
- **CryptoInfoPage**: Detailed asset information
- **CryptoOrderPage**: Execute trades
- **ChatPage**: AI-powered assistant
- **HistoryScreen**: Transaction records
- **DepositScreen/WithdrawScreen**: Fund management

## Security Features

- Firebase Authentication for secure user login
- KYC verification system
- Secure data storage with Room Database
- Encrypted preferences with DataStore
- ProGuard configuration for release builds

## API Integration

The app integrates with cryptocurrency market APIs using Retrofit to fetch:
- Real-time price data
- Market statistics
- Historical price charts
- Asset information

## Author

**Konozo24**
- GitHub: [@Konozo24](https://github.com/Konozo24)

## Acknowledgments

- Firebase for backend services
- Google AI for chatbot capabilities
- Jetpack Compose team for the amazing UI toolkit
- The Kotlin community
