# Expense Tracker Android App

An Android application that automatically parses SMS messages from banks to track and categorize expenses.

## Features

### ✅ Completed (Backend)
- **SMS Parsing**: Automatically extracts transaction details from bank SMS messages
- **Smart Detection**: Supports multiple Indian banks with regex patterns for amounts, merchants, UPI IDs
- **Duplicate Prevention**: Intelligent duplicate transaction detection
- **Database**: Room database with proper indexing and relationships
- **Background Processing**: Automatic SMS monitoring and transaction import
- **Categories**: Pre-defined expense categories with customization support
- **Repository Pattern**: Clean architecture with use cases and dependency injection

### 🚧 In Progress (Frontend)
- **Transaction List**: View all parsed transactions
- **Categorization**: Simple UI to categorize uncategorized transactions  
- **Dashboard**: Basic spending overview and statistics
- **Navigation**: Simple navigation between screens

## Tech Stack

- **Language**: Kotlin
- **UI**: Jetpack Compose with Material Design 3
- **Architecture**: MVVM with Clean Architecture
- **Database**: Room (SQLite)
- **DI**: Hilt (Dagger)
- **Async**: Coroutines + Flow
- **Background**: WorkManager + JobIntentService

## Project Structure

```
app/src/main/java/com/expensetracker/
├── data/
│   ├── local/          # Room database, entities, DAOs
│   ├── repository/     # Repository implementations
│   ├── sms/           # SMS parsing and content providers
│   └── service/       # Background services
├── domain/
│   ├── model/         # Domain models
│   ├── usecase/       # Business logic use cases
│   └── util/          # Domain utilities
└── presentation/
    ├── transaction/   # Transaction list UI
    └── theme/         # Compose theme
```

## Current Status

**Backend**: 100% Complete ✅
- All SMS parsing logic implemented
- Database schema and operations ready
- Background processing working
- Use cases and repository pattern complete

**Frontend**: 25% Complete 🚧
- Basic transaction list screen created
- Test data integration working
- Ready for categorization and dashboard screens

## Development Setup

This project is designed to work with:
- Android Studio or Replit
- Minimum SDK: 24 (Android 7.0)
- Target SDK: 34 (Android 14)
- Kotlin 1.9.10
- Compose BOM 2023.10.01

## Next Steps

1. Complete categorization UI screen
2. Build minimal dashboard
3. Add basic navigation
4. Test end-to-end flow
5. Polish and optimize

## SMS Parsing Support

Currently supports transaction SMS from major Indian banks including:
- HDFC, SBI, ICICI, Axis, Kotak
- UPI payments (GPay, PhonePe, Paytm, etc.)
- Debit/Credit card transactions
- Net banking payments

## License

This project is for educational and personal use.