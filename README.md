# LL&L Futures - Prediction Market Platform

![LL&L Futures](https://img.shields.io/badge/Version-1.1.0-blue)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.0-green)
![React](https://img.shields.io/badge/React-18.2.0-blue)
![Mobile](https://img.shields.io/badge/Mobile-Optimized-brightgreen)
![License](https://img.shields.io/badge/License-MIT-yellow)

A full-stack prediction market application powered by **LL&L Token (LLL)**. Users can create and trade on futures-style markets (bets) using a simulated token economy.

## 🎯 Overview

LL&L Futures is a prediction market platform where users can:
- Browse active markets and view odds
- Place YES/NO bets on future events
- Track positions and potential winnings
- Create custom markets
- Settle markets and distribute payouts (Admin)

This is an **MVP stage** application designed for local development and testing, with a clear roadmap to evolve into a blockchain-powered decentralized platform.

## 🏗️ Architecture

### Tech Stack

**Frontend:**
- React 18 with Vite
- Tailwind CSS for styling
- React Router for navigation
- Axios for API calls
- Lucide React for icons

**Backend:**
- Spring Boot 3.2.0
- Java 17
- Spring Data JPA
- H2 Persistent Database
- Lombok for boilerplate reduction

**Storage:**
- H2 Database (persistent file-based)
- Local folder structure for organization

### System Architecture

```
┌─────────────────────────────────────────────────────────┐
│                    React Frontend                       │
│  ┌──────────┬──────────┬──────────┬──────────────────┐ │
│  │Dashboard │ Markets  │Positions │ Wallet & Admin   │ │
│  └──────────┴──────────┴──────────┴──────────────────┘ │
└─────────────────────┬───────────────────────────────────┘
                      │ REST API (HTTP)
┌─────────────────────┴───────────────────────────────────┐
│              Spring Boot Backend (Port 8080)            │
│  ┌──────────────────────────────────────────────────┐  │
│  │         REST Controllers Layer                   │  │
│  │  UserController │ MarketController │ OrderController │
│  └──────────────────────────────────────────────────┘  │
│  ┌──────────────────────────────────────────────────┐  │
│  │         Service Layer (Business Logic)           │  │
│  │  UserService │ MarketService │ SettlementService  │  │
│  └──────────────────────────────────────────────────┘  │
│  ┌──────────────────────────────────────────────────┐  │
│  │         Repository Layer (Data Access)           │  │
│  │  UserRepo │ MarketRepo │ OrderRepo │ TxRepo      │  │
│  └──────────────────────────────────────────────────┘  │
│  ┌──────────────────────────────────────────────────┐  │
│  │              H2 Database (Persistent File)          │  │
│  │    Users │ Markets │ Orders │ Transactions       │  │
│  └──────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────┘
```

## 📊 Data Models

### Core Entities

**User**
- `id`: Long (Primary Key)
- `username`: String (Unique)
- `email`: String
- `tokenBalance`: Double (LLL tokens)
- `isAdmin`: Boolean
- Timestamps: createdAt, updatedAt

**Market**
- `id`: Long (Primary Key)
- `title`: String
- `description`: String
- `status`: Enum (ACTIVE, CLOSED, SETTLED, CANCELLED)
- `expiryDate`: LocalDateTime
- `yesOdds`: Double (payout multiplier for YES)
- `noOdds`: Double (payout multiplier for NO)
- `totalYesStake`: Double
- `totalNoStake`: Double
- `totalVolume`: Double
- `creator`: User (FK)
- `outcome`: Enum (YES, NO, VOID)
- Timestamps: createdAt, updatedAt, settledAt

**Order**
- `id`: Long (Primary Key)
- `user`: User (FK)
- `market`: Market (FK)
- `side`: Enum (YES, NO)
- `stakeAmount`: Double
- `odds`: Double (locked at order time)
- `potentialPayout`: Double
- `status`: Enum (OPEN, SETTLED, CANCELLED)
- `settledAmount`: Double
- Timestamps: createdAt, updatedAt, settledAt

**Transaction**
- `id`: Long (Primary Key)
- `user`: User (FK)
- `type`: Enum (DEPOSIT, WITHDRAWAL, BET_PLACED, BET_WON, BET_LOST, BET_REFUND)
- `amount`: Double
- `balanceBefore`: Double
- `balanceAfter`: Double
- `description`: String
- `relatedOrderId`: Long
- `relatedMarketId`: Long
- Timestamp: createdAt

## 🚀 Getting Started

### Prerequisites

- **Java 17+** (for backend)
- **Node.js 18+** and npm (for frontend)
- **Maven** (for building Spring Boot)
- Git

### Installation & Running

#### 1. Clone the Repository

```bash
git clone <repository-url>
cd MarketFutures
```

#### 2. Start the Backend

```bash
cd backend
mvn clean install
mvn spring-boot:run
```

The backend will start on **http://localhost:8080**

- H2 Console available at: **http://localhost:8080/h2-console**
  - JDBC URL: `jdbc:h2:file:./data/lll_futures_db`
  - Username: `sa`
  - Password: (leave empty)
  - **Note:** Database is now persistent - data survives restarts!

#### 3. Start the Frontend

```bash
cd frontend
npm install
npm run dev
```

The frontend will start on **http://localhost:3000**

### Default Users (Seeded on Startup)

| Username | Password | Admin | Balance |
|----------|----------|-------|---------|
| admin    | N/A      | ✅     | 10,000 LLL |
| alice    | N/A      | ❌     | 1,000 LLL |
| bob      | N/A      | ❌     | 1,000 LLL |
| charlie  | N/A      | ❌     | 1,000 LLL |

*Note: This MVP doesn't have authentication yet. Switch users via the navbar dropdown.*

## 📡 REST API Reference

### Base URL
```
http://localhost:8080/api
```

### Endpoints

#### **Users**
- `GET /users` - Get all users
- `GET /users/{id}` - Get user by ID
- `GET /users/username/{username}` - Get user by username
- `POST /users` - Create new user

#### **Markets**
- `GET /markets` - Get all markets
- `GET /markets/active` - Get active markets only
- `GET /markets/{id}` - Get market by ID
- `POST /markets` - Create new market
- `PUT /markets/{id}/close` - Close market (stop accepting bets)

#### **Orders**
- `GET /orders` - Get all orders
- `GET /orders/{id}` - Get order by ID
- `GET /orders/user/{userId}` - Get all orders for user
- `GET /orders/user/{userId}/open` - Get user's open orders
- `POST /orders` - Place new order/bet

#### **Settlement (Admin)**
- `POST /settlement/settle` - Settle a market and distribute payouts

### Example API Calls

**Create a Market:**
```json
POST /api/markets
{
  "title": "Will BTC reach $100K by Dec 2025?",
  "description": "Market resolves YES if Bitcoin trades at $100K+",
  "expiryDate": "2025-12-31T23:59:00",
  "yesOdds": 2.5,
  "noOdds": 1.5,
  "creatorId": 1
}
```

**Place a Bet:**
```json
POST /api/orders
{
  "userId": 2,
  "marketId": 1,
  "side": "YES",
  "stakeAmount": 100.0
}
```

**Settle a Market:**
```json
POST /api/settlement/settle
{
  "marketId": 1,
  "outcome": "YES"
}
```

## 💡 Features

### ✅ Current Features (MVP)

- **User Management**: Multiple user profiles with token balances
- **Market Creation**: Any user can create prediction markets
- **Order Placement**: Bet YES or NO on markets with custom amounts
- **Position Tracking**: View all open and settled positions
- **Token Wallet**: Track LLL token balance
- **Admin Panel**: Settle markets and distribute payouts
- **Real-time Updates**: Automatic balance and position updates
- **Responsive UI**: Beautiful Tailwind CSS interface

### 🔜 Future Enhancements (Roadmap)

**Phase 2: Enhanced Features**
- User authentication and authorization
- Market categories and filtering
- Order history with detailed analytics
- Social features (comments, likes)
- Real-time price updates and charts

**Phase 3: Blockchain Integration**
- Migrate LL&L Token to Solana blockchain (SPL token)
- Web3 wallet integration (Phantom, Solflare)
- Smart contract for trustless market settlement
- On-chain order book

**Phase 4: Production & Scale**
- Move from H2 to PostgreSQL
- Cloud deployment (AWS/GCP)
- S3/Cloud storage for user uploads
- CDN for static assets
- Redis caching layer
- WebSocket for real-time updates

**Phase 5: DeFi Features**
- Liquidity pools for markets
- Automated Market Maker (AMM) integration
- Token staking and rewards
- DAO governance for platform decisions

## 🎨 Frontend Structure

```
frontend/
├── src/
│   ├── api/
│   │   └── api.js              # API client & service functions
│   ├── components/
│   │   ├── Navbar.jsx          # Navigation bar with user selector
│   │   └── MarketCard.jsx      # Reusable market card with betting
│   ├── pages/
│   │   ├── Dashboard.jsx       # Overview with stats and featured markets
│   │   ├── Markets.jsx         # All active markets
│   │   ├── MyPositions.jsx     # User's bets and positions
│   │   ├── Wallet.jsx          # Token balance and info
│   │   ├── CreateMarket.jsx    # Form to create new markets
│   │   └── AdminPanel.jsx      # Admin tools for settlement
│   ├── App.jsx                 # Root component with routing
│   ├── main.jsx               # Entry point
│   └── index.css              # Tailwind imports
├── index.html
├── package.json
├── vite.config.js
└── tailwind.config.js
```

## 🔧 Backend Structure

```
backend/
├── src/main/java/com/lll/futures/
│   ├── model/                  # JPA entities
│   │   ├── User.java
│   │   ├── Market.java
│   │   ├── Order.java
│   │   └── Transaction.java
│   ├── repository/            # Spring Data JPA repositories
│   │   ├── UserRepository.java
│   │   ├── MarketRepository.java
│   │   ├── OrderRepository.java
│   │   └── TransactionRepository.java
│   ├── service/               # Business logic
│   │   ├── UserService.java
│   │   ├── MarketService.java
│   │   ├── OrderService.java
│   │   └── SettlementService.java
│   ├── controller/            # REST endpoints
│   │   ├── UserController.java
│   │   ├── MarketController.java
│   │   ├── OrderController.java
│   │   └── SettlementController.java
│   ├── dto/                   # Data Transfer Objects
│   ├── config/
│   │   └── DataSeeder.java    # Seed initial data
│   ├── exception/
│   │   └── GlobalExceptionHandler.java
│   └── FuturesApplication.java # Main application
├── src/main/resources/
│   └── application.properties  # Config
└── pom.xml
```

## 🧪 Testing

### Manual Testing

1. **Create a Market** (as admin or any user)
2. **Place Bets** on the market (switch to different users)
3. **View Positions** to see your active bets
4. **Settle Market** (as admin) to distribute payouts
5. **Check Wallet** to see updated balances

### API Testing with cURL

```bash
# Get all users
curl http://localhost:8080/api/users

# Get active markets
curl http://localhost:8080/api/markets/active

# Place a bet
curl -X POST http://localhost:8080/api/orders \
  -H "Content-Type: application/json" \
  -d '{"userId":2,"marketId":1,"side":"YES","stakeAmount":50}'
```

## 🤝 Contributing

This is an MVP project. Contributions welcome!

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit changes (`git commit -m 'Add amazing feature'`)
4. Push to branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## 📄 License

This project is licensed under the MIT License.

## 👥 Authors

Built with ❤️ for the future of decentralized prediction markets.

---

**Questions or Issues?**  
Open an issue on GitHub or reach out to the development team.

**Happy Trading! 🚀**

