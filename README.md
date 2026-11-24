# SheLoans - Financial Empowerment for Women

SheLoans is an Android application designed to promote financial inclusion for women by providing access to credit, savings goals, and Chama (informal investment groups) management.

## Features

- **Loan Marketplace**: View curated loan products from various banks (Equity, KCB, Absa, etc.) specifically tailored for women.
- **Savings Goals**: Create and track personal or Chama-linked savings goals with progress visualization.
- **M-Pesa Integration**: Securely deposit and withdraw funds using M-Pesa STK Push.
- **Chama Management**: Join and manage investment groups to pool resources and grow wealth together.
- **Credit Scoring**: Improve your internal credit score by consistently saving and meeting financial goals.
- **AI-Powered Assistance**: Integrated with Groq/Llama for financial advice and support.

## App Screenshots

The screenshots below give a quick tour of the SheLoans Android app and its key experiences.

### Home dashboard

The dashboard gives members an at-a-glance view of their financial health score, savings, and active loans. It also provides shortcuts to Savings, Loans, Chama groups, the Financial Academy, and consumer support.

<img src="she-1.jpg.png" alt="SheLoans home dashboard" width="200">

### Financial Academy

The Financial Academy includes an AI financial advisor that users can ask about savings, interest rates, and growing their Chama.

<img src="she-2.jpg.png" alt="SheLoans Financial Academy AI advisor" width="200">

### Protection and Support

The Protection & Support page helps users report a financial issue and find relevant regulatory and consumer-support contacts, including the Central Bank of Kenya and the Consumer Federation of Kenya.

<img src="she-3.jpg.png" alt="SheLoans Protection and Support page" width="200">

### Notification permission

SheLoans can request permission to send notifications, so users can receive relevant app updates. The screenshot shows the Android system permission prompt.

<img src="she-4.jpg.png" alt="Android notification permission prompt for SheLoans" width="200">

## Technologies Used

- **Kotlin**: Primary programming language.
- **Firebase Realtime Database**: Real-time data storage for users, savings, and Chamas.
- **Firebase Authentication**: Secure user sign-in and management.
- **Retrofit**: For network requests and M-Pesa API integration.
- **Material Design**: For a modern and intuitive user interface.

## Getting Started

### Prerequisites
- Android Studio Ladybug or newer.
- A Firebase project (add your `google-services.json` to the `app/` directory).
- M-Pesa Daraja API credentials for the payment features.

### Setup
1. Clone the repository:
   ```bash
   git clone https://github.com/YOUR_USERNAME/SheLoans.git
   ```
2. Open the project in Android Studio.
3. Sync Gradle and run the app on an emulator or physical device.

## License
This project is licensed under the MIT License - see the LICENSE file for details.
