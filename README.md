# TrackerNow - Shipment Tracking App

A modern Android app for tracking shipments across multiple carriers with offline support.

## Features

- View all shipments with carrier, tracking number, and status
- Manual refresh with pull-to-refresh
- Offline support - cached data available without internet
- Favorite shipments for quick access
- Search by tracking number or carrier name
- Light/Dark/System theme support
- Shipment detail with full timeline history
- Real-time update simulation

## Tech Stack

- **UI**: Jetpack Compose, Material 3
- **Architecture**: Clean Architecture + MVVM
- **Database**: Room (SQLite)
- **Network**: Retrofit + Moshi (JSON parsing)
- **Async**: Kotlin Coroutines + Flow
- **DI**: Dagger Hilt
- **Preferences**: DataStore
- **Push Simulation**: Firebase Realtime Database

## Running the App

### Prerequisites
- Android Studio
- JDK 21
- Android SDK API 34+

### Setup Steps

1. **Clone the repository**
```bash
git clone https://github.com/yourusername/trackernow.git
cd trackernow