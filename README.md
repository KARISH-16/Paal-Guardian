# Paal-Guardian


**PAAL GUARDIAN** is a smart IoT-based milk chilling and monitoring system designed to improve milk storage, quality monitoring, and temperature management. The system combines an **ESP32-powered Smart Milk Chilling Can** with a **Flutter mobile application**, enabling users to monitor milk conditions in real time and receive important alerts through a connected mobile device.

## 🎯 Purpose

The primary goal of PAAL GUARDIAN is to provide an affordable and intelligent solution for maintaining milk quality during storage and transportation. By continuously monitoring temperature and storage conditions, the system helps reduce milk spoilage and provides users with timely information about the condition of the stored milk.

## ✨ Key Features

* 🌡️ **Real-Time Temperature Monitoring** – Continuously monitors the temperature of stored milk.
* ❄️ **Smart Milk Chilling** – Supports temperature-controlled milk storage using an IoT-enabled chilling system.
* 📱 **Flutter Mobile Application** – Provides an intuitive interface for monitoring and managing the Smart Can.
* 🔵 **Bluetooth Low Energy (BLE)** – Enables communication between the ESP32 Smart Can and the mobile application.
* 🚨 **Smart Alerts** – Notifies users when temperature or storage conditions exceed predefined limits.
* 📊 **Milk Condition Monitoring** – Displays important monitoring data through the mobile application.
* 🔐 **Secure User Authentication** – Uses Supabase Authentication for secure account management.
* 💾 **Local Data Storage** – Uses SQLite for storing relevant application data locally.
* 🤖 **Machine Learning Integration** – Provides scope for intelligent milk-quality analysis and predictive monitoring.
* ⚡ **ESP32-Based IoT System** – Connects sensors and the chilling mechanism with the mobile application.

## 🛠️ Technology Stack

### Mobile Application

* **Flutter**
* **Dart**
* **Material UI**

### IoT Hardware

* **ESP32**
* **Temperature Sensors**
* **BLE (Bluetooth Low Energy)**
* **Smart Chilling Can**

### Database & Authentication

* **SQLite** – Local application data storage
* **Supabase** – Authentication and cloud backend services

### Intelligence

* **Machine Learning** – For future milk-quality prediction and intelligent monitoring

## 🔄 System Overview

```text
Milk
  ↓
Smart Chilling Can
  ↓
Sensors → ESP32
           ↓
          BLE
           ↓
    Flutter Mobile App
           ↓
 Monitoring + Alerts
           ↓
 SQLite / Supabase
```

PAAL GUARDIAN aims to bridge **IoT, mobile technology, and intelligent monitoring** to create a practical and scalable solution for safer and more efficient milk storage.
