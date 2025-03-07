# 📈 Real-Time Stock Trading Engine (Multi-Threaded)

## 🚀 Overview
This project implements a **real-time stock trading engine** that **matches buy and sell orders** efficiently in a **multi-threaded environment**. It ensures:
- **Thread safety** using `ReentrantLock`
- **Order matching** based on price and time priority
- **Efficient order book management** using `LinkedList`
- **Multi-threaded trading simulation** with random orders

## 📑 Features
✅ Supports **Buy & Sell Orders**  
✅ Implements **Order Matching** (Highest Buy meets Lowest Sell)  
✅ Maintains **Order Book** efficiently using **LinkedLists**  
✅ Uses **Multi-threading** to simulate multiple traders  
✅ Ensures **Thread-Safety** with **`ReentrantLock`**  
✅ Matches **Orders in Real-Time**  

---

## 🛠️ Installation & Setup
### **1️⃣ Clone the Repository**
```bash
git clone https://github.com/your-repo/stock-trading-system.git
cd stock-trading-system

##Compile Java Code
javac StockTradingSystem.java

## Run the trading engine
java StockTradingSystem


