# GymWidget 🏋️‍♂️

GymWidget is a modern Android application designed to track your workout progress with ease and visibility. It features a dedicated home screen widget that provides a quick glance at your training activity over the last week.

## Features

- **Workout Logging**: Track exercises, sets, weights, and dates.
- **Progress Visualization**: View your weight progress over the last month with dynamic charts.
- **Home Screen Widget**: Stay motivated with a widget showing your training days and muscle groups from the last week.
- **Smart Date Handling**: intuitive date labels (Today, Monday, 15/6) and a restricted date picker to prevent future logging.
- **Category Filtering**: Easily filter your exercise list by muscle group (Chest, Back, Legs, etc.).
- **Personal Records**: Automatic detection and highlighting of new personal records.
- **Estimated 1RM**: Get an estimated One-Rep Max calculation for your lifts based on your latest logs.

## Tech Stack

- **Language**: Kotlin
- **UI Framework**: Jetpack Compose
- **Widgets**: Jetpack Glance
- **Charts**: [Vico](https://github.com/PatrykAndPatrick/vico)
- **Data Storage**: JSON-based local storage
- **Architecture**: MVVM with ViewModel and State

## Getting Started

### Prerequisites

- Android Studio (latest version recommended)
- Android SDK 36 (Minimum SDK 36)

### Installation

1. Clone the repository:
   ```bash
   git clone https://github.com/talari-bgu/GymWidget.git
   ```
2. Open the project in Android Studio.
3. Sync the project with Gradle files.
4. Run the application on an emulator or a physical device.

## Usage

1. **Add Exercise**: Tap the "+ Add Exercise" button to log a new movement.
2. **View Progress**: Click the search icon on any exercise card to see its full history and a progress chart.
3. **Add Logs**: Inside an exercise's details, use the date picker and input fields to add new workout data.
4. **Home Widget**: Long-press on your home screen, select "Widgets", find "GymWidget", and add it to your screen to see your weekly summary.

## Screenshots

*(TBA)*

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.
