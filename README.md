# Cryptopro - Crypto Analysis & Strategy Backtesting

Cryptopro is an Android application for analyzing cryptocurrency market data and backtesting trading strategies powered by local AI models.

## Features

- 📊 **Real-time Crypto Data**: Fetch live market data from Binance API
- 🤖 **Local AI Analysis**: AI-powered trading signal generation and price predictions
- 📈 **Strategy Backtesting**: Simulate trading strategies on historical data
- 💾 **Local Storage**: SQLite database for offline analysis
- 📱 **Modern UI**: Built with Jetpack Compose
- 🔔 **Technical Indicators**: SMA, RSI, MACD, and more

## Tech Stack

- **Language**: Kotlin
- **UI Framework**: Jetpack Compose
- **Database**: Room (SQLite)
- **API**: Retrofit 2 + OkHttp
- **Dependency Injection**: Hilt
- **ML**: TensorFlow Lite + Custom ML algorithms
- **Async**: Kotlin Coroutines
- **Charting**: MPAndroidChart

## Project Structure

```
app/src/main/kotlin/com/cryptopro/
├── data/
│   ├── local/
│   │   ├── database/   # Room database
│   │   ├── dao/        # Data Access Objects
│   │   └── entities/   # Database entities
│   └── remote/
│       ├── api/        # Retrofit API definitions
│       └── dto/        # Data Transfer Objects
├── domain/
│   ├── ml/             # AI/ML models
│   ├── repository/     # Repository interfaces
│   └── usecase/        # Business logic
├── ui/
│   ├── screens/        # Compose screens
│   └── theme/          # UI theming
├── di/                 # Dependency injection modules
└── MainActivity.kt
```

## Getting Started

### Prerequisites
- Android Studio Electric Eel or later
- Android SDK 24+
- Kotlin 1.9.0+

### Installation

1. Clone the repository
   ```bash
   git clone https://github.com/ahmadsijeh66/Cryptopro.git
   cd Cryptopro
   ```

2. Open in Android Studio

3. Build and run on emulator or device
   ```bash
   ./gradlew build
   ./gradlew installDebug
   ```

## API Integration

### Binance API
- Klines (candlestick data)
- Ticker prices
- Exchange information

## ML Models

The app uses local ML models for:
- **Trading Signal Analysis**: BUY/SELL/HOLD signals based on technical indicators
- **Price Prediction**: Next price movement prediction
- **Strategy Scoring**: Evaluate strategy effectiveness

## Backtesting Engine

Simulate trading strategies on historical data with metrics:
- Total Return %
- Win Rate
- Max Drawdown
- Number of Trades

## Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

## License

MIT License - see LICENSE file for details

## Support

For questions and support, please open an issue on GitHub.
