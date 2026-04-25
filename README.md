🚀 Modern Real-Time Messenger (with Offline Support)
A professional, full-stack messaging application built with Kotlin and Jetpack Compose. This app features a robust "Offline-First" approach, syncing real-time data from Firebase while caching it locally using Room Database for a seamless experience in any network condition.

✨ Key Features
Real-Time Messaging: Instant data synchronization using Firebase Realtime Database.

Offline Persistence: Integrated Room Database to cache messages and user profiles locally, enabling instant app loading and offline reading.

Media Sharing: Securely upload and share images, videos, and documents via Firebase Storage.

Instant Notifications: Integrated Firebase Cloud Messaging (FCM) for real-time push notifications.

Modern UI/UX: Built entirely with Jetpack Compose and Material 3, featuring smooth transitions and a clean design.

Reliable Background Tasks: Utilizes Android WorkManager to handle media uploads and data synchronization in the background.

MVVM Architecture: Strict adherence to the Model-View-ViewModel pattern for clean code and scalability.

🛠 Tech Stack
Language: Kotlin

UI Framework: Jetpack Compose

Architecture: MVVM (Model-View-ViewModel)

Local Database: Room Persistence Library (Offline Cache)

Background Processing: Android WorkManager

Backend Services (Firebase):

Realtime Database: Live chat synchronization.

Cloud Messaging (FCM): Push notifications.

Firebase Storage: File and media management.

Image Loading: Coil
## Installation

🚀 Installation & Setup
To get this project running on your local machine, follow these essential steps:

1. Clone the Repository
Bash
git clone https://github.com/codingindia87/Messanger.git
2. Firebase Configuration (Client-Side)
Go to the Firebase Console and create a new project.

Add an Android App to your Firebase project using your app's package name.

Download the google-services.json file.

Crucial: Paste the google-services.json file into the app/ directory of the project.

3. API Configuration (FCM Messaging)
To enable push notifications between devices, you must link your specific Firebase project ID in the code:

Open the file: app/src/main/java/.../core/api/SendMessageApi.kt (or your specific path).

Locate the Project ID placeholder.

Action: Paste your unique Firebase Project ID into the designated variable in SendMessageApi.kt. This ensures that the WorkManager can successfully route notifications through your Firebase project.

4. Enable Firebase Services
Ensure the following are enabled in your console:

Realtime Database: Set rules to allow read/write for authenticated users.

Firebase Storage: Set rules for media uploads.

Cloud Messaging: Ensure the FCM API is enabled in the Google Cloud Console.

5. Build & Run
Open the project in Android Studio.

Sync Gradle files.

Build and run the app on your physical device or emulator.

🤝 Contributing
Contributions are welcome! Please feel free to submit a Pull Request.

Developed with ❤️ by Sachin Vishwakarma
    
## 🔗 Links
[![portfolio](https://img.shields.io/badge/my_portfolio-000?style=for-the-badge&logo=ko-fi&logoColor=white)](https://coding-india.web.app/)
[![linkedin](https://img.shields.io/badge/linkedin-0A66C2?style=for-the-badge&logo=linkedin&logoColor=white)](https://www.linkedin.com/in/sachin-vishwakarma098/)
[![Github](https://img.shields.io/badge/twitter-1DA1F2?style=for-the-badge&logo=twitter&logoColor=white)](https://github.com/codingindia87)
