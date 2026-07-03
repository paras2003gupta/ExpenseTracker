# expenseIt 📱💸

A minimalist, high-performance personal expense tracker and group bill splitter for Android, built natively with **Kotlin** and **Jetpack Compose**.

---

## ✨ Features

- **🖤 Minimalist Theme**: Elegant black-and-white monochromatic user interface with dark mode and light mode preferences.
- **📊 Real-time Analytics**: Monochromatic spending graphs and a beautiful, high-contrast dashboard showing monthly limits, spent vs. limit progression, and details of transactions.
- **⚡ Quick Friend Creation**: Open the system contacts picker or type country codes dynamically when adding friends on-the-fly.
- **👥 Smart Group Splitting**: Create custom groups, add group expenses, and track split shares automatically.
- **🔄 Inter-tab Split Workflows**: Swipe-to-split personal expenses into group splits directly from the personal transactions list.
- **👉 Interactive Swipe Gestures**:
  - **Swipe Right** to instantly split any transaction.
  - **Swipe Left** to delete.

---

## 🛠️ Tech Stack & Architecture

- **UI Framework**: Jetpack Compose (Material 3)
- **Programming Language**: Kotlin
- **Architecture**: MVVM (Model-View-ViewModel)
- **Local Cache**: Room Database (SQLite backend)
- **Asynchronous Execution**: Kotlin Coroutines & Flow
- **Package Path**: `com.expenseit`

---

## 📂 Codebase Directory Structure

```
app/src/main/java/com/expenseit/
├── MainActivity.kt               # Entry activity with dynamic theme launcher
├── ExpenseItApplication.kt       # Application class initializing repositories
├── core/
│   ├── database/                 # Room database definitions & migrations
│   ├── model/                    # Kotlin Data Entities (Category, Transaction, Group, Friend)
│   └── util/                     # Date and Money formatting utilities
├── feature/
│   ├── tracker/                  # Personal Expense Tracker (Dashboard, Transactions list, limits)
│   └── splitter/                 # Group Bill Splitter (Groups list, Group details, Friends list)
├── navigation/                   # Compose Navigation graph routing
└── ui/
    └── theme/                    # Material 3 Color scheme & Typography
```

---

## 🚀 How to Run the App

1. **Prerequisites**:
   - Install **Android Studio (Ladybug or newer)**.
   - Connect your Android device via USB/Wi-Fi Debugging.
2. **Compile and Deploy**:
   - Run the app directly from Android Studio, or execute the Gradle task in your terminal:
     ```bash
     ./gradlew installDebug
     ```
3. **Launch**:
   - Start the main activity:
     ```bash
     adb shell am start -n com.expenseit/com.expenseit.MainActivity
     ```
