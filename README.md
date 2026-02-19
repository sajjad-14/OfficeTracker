# 🏢 Office Tracker

**Stay Disciplined. Track Your Hours. Master Your Schedule.**

Office Tracker is a modern Android application designed to help you effortlessly track your office attendance, maintain discipline, and achieve your productivity goals. With automated geofencing and detailed analytics, it takes the hassle out of manual time sheets.

---

## ✨ Features

*   **📍 Automated Tracking**: Uses **Geofencing** to automatically check you in and out when you enter or leave your office.
*   **📊 Real-time Dashboard**: View your current session duration, daily progress, and monthly goals in a beautiful, animated interface.
*   **📈 Smart Analytics**: visualize your work habits with weekly bar charts and detailed statistics.
*   **🎯 Goal Setting**: Set custom daily and monthly targets to keep yourself motivated.
*   **👤 Personal Profile**: A personalized experience with a profile header and easy access to settings.
*   **🔔 Notifications**: Get reminders to start your day and notifications for automatic events.
*   **⚙️ Data Export**: Export your entire attendance history to CSV for your own records.

---

## 🚀 Getting Started

### Prerequisites

*   Android Device running Android 8.0 (Oreo) or higher.
*   Location permissions enabled (Always Allow is required for background tracking).

### Installation

1.  **Download APK**: Go to the [Releases](https://github.com/sajjad-14/OfficeTracker/releases) page and download the latest `app-debug.apk`.
2.  **Install**: Open the file on your Android device and install.
3.  **Onboarding**:
    *   Enter your Name.
    *   Take the quick App Tour.
    *   Set your Office Location by standing at your desk and tapping "Set Location".

---

## 🛠️ Tech Stack

*   **Language**: [Kotlin](https://kotlinlang.org/)
*   **UI Framework**: [Jetpack Compose](https://developer.android.com/jetpack/compose) (Material 3)
*   **Architecture**: MVVM (Model-View-ViewModel) + Clean Architecture principles.
*   **Dependency Injection**: [Dagger Hilt](https://dagger.dev/hilt/)
*   **Local Storage**:
    *   **Room Database**: For storing attendance sessions and stats.
    *   **DataStore Preferences**: For storing user settings and state.
*   **Background Processing**:
    *   **WorkManager**: For reliable background tasks and geofence checks.
    *   **Google Play Services Location**: For Geofencing API.

---

## 📸 Screenshots

| Dashboard | Analytics | Profile & Settings |
|:---:|:---:|:---:|
| *(Add Screenshot)* | *(Add Screenshot)* | *(Add Screenshot)* |

---

## 🤝 Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

1.  Fork the Project
2.  Create your Feature Branch (`git checkout -b feature/AmazingFeature`)
3.  Commit your Changes (`git commit -m 'Add some AmazingFeature'`)
4.  Push to the Branch (`git push origin feature/AmazingFeature`)
5.  Open a Pull Request

---

## 📄 License

This project is open source and available under the [MIT License](LICENSE).

---

Made with ❤️ by Sajjad
