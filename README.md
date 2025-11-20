
# 🏥 Medicare – Smart Elder Care Assistance System

Medicare is an integrated smart healthcare solution designed to assist **senior citizens and their caregivers** in managing medication schedules, monitoring daily health, and ensuring safety through **IoT-based medicine dispensing and fall detection**.

Built using **Android (Kotlin)** with **Firebase backend**, the system enhances independent living for elderly individuals while providing real-time visibility and alerts to caregivers.

---

## 🌟 Key Features

### 📱 Medication Management
- Add, update, and track prescribed medicines  
- Scheduled reminders for medication  
- Caregivers notified if a dose is missed  

### 🤖 IoT-Enabled Medicine Dispenser
- Hardware-controlled automatic pill dispensing  
- Real-time data sync with the app  
- Cloud-based logs available for caregiver review  

### 🚨 Fall Detection System
- Detects accidental falls using hardware sensors  
- Sends immediate emergency alerts to caregivers  
- Can include location data for quicker response  

### 👴 Senior-Friendly UI
- Large, readable fonts  
- High-quality contrast  
- Simple and intuitive navigation  

### ☁ Cloud Integration
- Firebase-based secure data storage  
- Real-time synchronization across devices  
- Works for both patient and caregiver apps  

---

## 🧠 System Architecture

```

[User (Elder)]
↓
[Android App – Kotlin]
↓
[Firebase Authentication – Secure Login]
↓
[Firestore Database – Schedules, Logs, User Data]
↓
[IoT Module – Medicine Dispenser + Fall Detection]
↓
[Caregiver App – Remote Monitoring & Alerts]

```

---

## 🛠 Tech Stack

| Component             | Technology Used                     |
|----------------------|-------------------------------------|
| Mobile Application   | Android (Kotlin), XML UI            |
| Backend Services     | Firebase Firestore, Firebase Auth   |
| IoT Hardware         | Arduino / ESP32 + Sensors           |
| Notifications        | Firebase Cloud Messaging (FCM)       |
| Development Tools    | Android Studio, Postman, Firebase Console |

---

## 📂 Project Structure

```

Medicare/
├── app/
├── java/
│   ├── activities
│   ├── adapters
│   ├── models
│   └── services (FCM, monitoring, data sync)
├── res/
│   ├── layout/
│   └── drawable/
└── README.md

````

---

## 📸 Screenshots


| Screen | Preview |
|-------|---------|
![WhatsApp Image 2025-11-15 at 11 21 49 AM](https://github.com/user-attachments/assets/6434b25b-a60c-41e9-875a-7df027db0cf0)
![WhatsApp Image 2025-11-15 at 11 21 49 AM (1)](https://github.com/user-attachments/assets/59159756-40b0-4a95-a0d1-b132a538f7a5)
![WhatsApp Image 2025-11-15 at 11 21 53 AM](https://github.com/user-attachments/assets/556634f5-c36f-4973-84a1-eb2adb320d77)
![WhatsApp Image 2025-11-15 at 11 21 50 AM](https://github.com/user-attachments/assets/afd6d82e-8a96-4585-baa1-482ac5d6a2c5)
![WhatsApp Image 2025-11-15 at 11 21 50 AM (1)](https://github.com/user-attachments/assets/1113c3e7-d1e0-4e1b-ad70-5749032306a1)


---

## 🚀 Installation & Setup

### 1️⃣ Clone the Repository
```bash
git clone https://github.com/harshshah-2023/MedicalIotAndroidapp.git
cd medicare
````

### 2️⃣ Open in Android Studio

* Open the project folder
* Let Gradle download all dependencies

### 3️⃣ Configure Firebase

1. Create a Firebase project
2. Enable:

   * Firebase Authentication
   * Firestore Database
3. Download `google-services.json`
4. Place it inside:

```
app/src/
```

### 4️⃣ Build & Run the App

* Connect Android device or emulator
* Press **Run (▶)**

---

## 🔒 Data Privacy & Security

* Firebase Authentication ensures secure login
* Firestore securely stores medical data
* Database rules block unauthorized access
* Patient information confidentiality is prioritized

---

## 🧾 Use Cases

* Elderly individuals living independently
* Family caregivers monitoring remotely
* Hospitals with automated medicine assistance
* Home-care and aged care institutions

---

## 🌟 Future Enhancements

* Vital sign monitoring (BP, HR, SpO₂)
* Medicine stock prediction
* AI-based fall risk or health risk predictions
* Emergency contacts auto-dial with GPS

---

## 👥 Contributors

| Name            | Role                                      |
| --------------- | ----------------------------------------- |
| **Aditi Bhoir** | Lead Android Developer & System Architect |
| **Harsh Shah**  | IoT & Backend Integration Engineer        |

