# Project Structure

Complete file tree and explanation of the LL&L Futures codebase.

## 📁 Root Directory

```
MarketFutures/
├── backend/                    # Spring Boot backend
├── frontend/                   # React frontend
├── README.md                   # Main documentation
├── QUICKSTART.md              # Quick start guide
├── ARCHITECTURE.md            # System architecture
├── API_DOCUMENTATION.md       # API reference
├── PROJECT_STRUCTURE.md       # This file
├── start.sh                   # Startup script (macOS/Linux)
└── stop.sh                    # Stop script (macOS/Linux)
```

## 🔙 Backend Structure

```
backend/
├── src/
│   ├── main/
│   │   ├── java/com/lll/futures/
│   │   │   ├── FuturesApplication.java     # Main Spring Boot application
│   │   │   │
│   │   │   ├── model/                      # JPA Entity models
│   │   │   │   ├── User.java              # User entity (accounts & balances)
│   │   │   │   ├── Market.java            # Market entity (prediction markets)
│   │   │   │   ├── Order.java             # Order entity (bets/positions)
│   │   │   │   └── Transaction.java       # Transaction entity (audit trail)
│   │   │   │
│   │   │   ├── repository/                # Data access layer
│   │   │   │   ├── UserRepository.java
│   │   │   │   ├── MarketRepository.java
│   │   │   │   ├── OrderRepository.java
│   │   │   │   └── TransactionRepository.java
│   │   │   │
│   │   │   ├── service/                   # Business logic layer
│   │   │   │   ├── UserService.java       # User management & balance updates
│   │   │   │   ├── MarketService.java     # Market CRUD & volume tracking
│   │   │   │   ├── OrderService.java      # Order placement & validation
│   │   │   │   └── SettlementService.java # Market settlement & payouts
│   │   │   │
│   │   │   ├── controller/                # REST API endpoints
│   │   │   │   ├── UserController.java    # /api/users endpoints
│   │   │   │   ├── MarketController.java  # /api/markets endpoints
│   │   │   │   ├── OrderController.java   # /api/orders endpoints
│   │   │   │   └── SettlementController.java # /api/settlement endpoints
│   │   │   │
│   │   │   ├── dto/                       # Data Transfer Objects
│   │   │   │   ├── UserDTO.java
│   │   │   │   ├── CreateUserRequest.java
│   │   │   │   ├── MarketDTO.java
│   │   │   │   ├── CreateMarketRequest.java
│   │   │   │   ├── OrderDTO.java
│   │   │   │   ├── PlaceOrderRequest.java
│   │   │   │   └── SettleMarketRequest.java
│   │   │   │
│   │   │   ├── config/
│   │   │   │   └── DataSeeder.java        # Seeds initial users & markets
│   │   │   │
│   │   │   └── exception/
│   │   │       └── GlobalExceptionHandler.java # Centralized error handling
│   │   │
│   │   └── resources/
│   │       └── application.properties      # Application configuration
│   │
│   └── test/                              # Unit tests (to be added)
│
├── pom.xml                                # Maven dependencies
└── .gitignore
```

### Backend Key Files

**FuturesApplication.java**
- Main entry point
- Spring Boot auto-configuration

**Entities (model/)**
- JPA annotations (@Entity, @Table, @Column)
- Relationships (@ManyToOne, @OneToMany)
- Lifecycle hooks (@PrePersist, @PreUpdate)
- Lombok annotations (@Data, @Builder)

**Repositories (repository/)**
- Extend JpaRepository
- Custom query methods (findByUserId, etc.)
- No implementation needed (Spring Data JPA magic)

**Services (service/)**
- Business logic and validation
- Transaction management (@Transactional)
- Coordinate multiple repositories
- Token balance calculations

**Controllers (controller/)**
- REST endpoints (@RestController)
- Request mapping (@GetMapping, @PostMapping)
- Request validation (@Valid)
- CORS configuration

**DTOs (dto/)**
- Decouple API from internal models
- Validation annotations (@NotNull, @Email)
- Clean API contracts

**DataSeeder.java**
- Implements CommandLineRunner
- Runs on startup
- Creates 4 users and 6 markets

**application.properties**
- Database configuration (H2)
- Server port (8080)
- JPA settings
- CORS allowed origins

## 🎨 Frontend Structure

```
frontend/
├── src/
│   ├── api/
│   │   └── api.js                    # Axios API client & all API functions
│   │
│   ├── components/
│   │   ├── Navbar.jsx                # Top navigation with user selector
│   │   └── MarketCard.jsx            # Reusable market card with bet modal
│   │
│   ├── pages/
│   │   ├── Dashboard.jsx             # Home page with stats & featured markets
│   │   ├── Markets.jsx               # Browse all active markets
│   │   ├── MyPositions.jsx           # User's bets and positions
│   │   ├── Wallet.jsx                # Token balance and account info
│   │   ├── CreateMarket.jsx          # Form to create new markets
│   │   └── AdminPanel.jsx            # Admin tools to settle markets
│   │
│   ├── App.jsx                       # Root component with routing
│   ├── main.jsx                      # React entry point
│   └── index.css                     # Tailwind CSS imports
│
├── index.html                        # HTML template
├── package.json                      # npm dependencies & scripts
├── vite.config.js                    # Vite configuration
├── tailwind.config.js                # Tailwind CSS configuration
├── postcss.config.js                 # PostCSS configuration
└── .gitignore
```

### Frontend Key Files

**main.jsx**
- React 18 entry point
- Renders App component

**App.jsx**
- Router setup (React Router v6)
- User state management
- Routes definition

**api/api.js**
- Axios instance with base URL
- All API functions:
  - User APIs (getAllUsers, getUserById, createUser)
  - Market APIs (getAllMarkets, getActiveMarkets, createMarket)
  - Order APIs (getUserOrders, placeOrder)
  - Settlement APIs (settleMarket)

**components/Navbar.jsx**
- Navigation links
- User dropdown selector
- Balance display
- Responsive design

**components/MarketCard.jsx**
- Market information display
- YES/NO bet buttons
- Bet modal with amount input
- Payout calculation preview

**pages/Dashboard.jsx**
- Stats cards (balance, positions, staked, potential win)
- Featured markets (top 3)
- Overview of user's activity

**pages/Markets.jsx**
- Grid of all active markets
- MarketCard components
- Loading states

**pages/MyPositions.jsx**
- List of user's orders
- Filter tabs (all/open/settled)
- Position details (stake, odds, payout)
- Status badges

**pages/Wallet.jsx**
- Large balance card
- Account information
- Token info and roadmap

**pages/CreateMarket.jsx**
- Form with validation
- Date picker for expiry
- Odds input with preview
- Payout calculator

**pages/AdminPanel.jsx**
- Admin access check
- List all markets
- Settlement buttons (YES/NO/VOID)
- Volume statistics

**vite.config.js**
- Dev server on port 3000
- Proxy API calls to backend (port 8080)

**tailwind.config.js**
- Custom color scheme
- Content paths for purging

## 🗄️ Database Schema

```
┌─────────────────┐
│     USERS       │
├─────────────────┤
│ id (PK)         │
│ username        │
│ email           │
│ token_balance   │
│ is_admin        │
│ created_at      │
│ updated_at      │
└─────────────────┘
         │
         │ 1:N (creator)
         ▼
┌─────────────────┐
│    MARKETS      │
├─────────────────┤
│ id (PK)         │
│ title           │
│ description     │
│ status          │
│ expiry_date     │
│ yes_odds        │
│ no_odds         │
│ total_yes_stake │
│ total_no_stake  │
│ total_volume    │
│ creator_id (FK) │
│ outcome         │
│ settled_at      │
│ created_at      │
│ updated_at      │
└─────────────────┘
         │
         │ 1:N
         ▼
┌─────────────────┐
│     ORDERS      │
├─────────────────┤
│ id (PK)         │
│ user_id (FK)    │───┐
│ market_id (FK)  │   │
│ side            │   │
│ stake_amount    │   │
│ odds            │   │
│ potential_payout│   │
│ status          │   │
│ settled_amount  │   │
│ settled_at      │   │
│ created_at      │   │
│ updated_at      │   │
└─────────────────┘   │
                      │ N:1
                      ▼
            ┌─────────────────┐
            │  TRANSACTIONS   │
            ├─────────────────┤
            │ id (PK)         │
            │ user_id (FK)    │
            │ type            │
            │ amount          │
            │ balance_before  │
            │ balance_after   │
            │ description     │
            │ related_order_id│
            │ related_market_id│
            │ created_at      │
            └─────────────────┘
```

## 🎯 Code Flow Examples

### Example 1: User Places a Bet

```
Frontend (MarketCard.jsx)
    ↓ User clicks "YES" button
    ↓ Opens modal, enters amount
    ↓ Clicks "Confirm Bet"
    ↓
API Call (api.js)
    ↓ placeOrder({ userId, marketId, side, stakeAmount })
    ↓ POST /api/orders
    ↓
Backend Controller (OrderController.java)
    ↓ @PostMapping("/")
    ↓ Validates request (@Valid)
    ↓ Calls orderService.placeOrder()
    ↓
Service Layer (OrderService.java)
    ↓ Loads user & market from DB
    ↓ Validates market is ACTIVE
    ↓ Checks user balance
    ↓ Calculates potential payout
    ↓ Creates Order entity
    ↓ Saves order
    ↓ Updates user balance (via UserService)
    ↓ Updates market volume (via MarketService)
    ↓ Creates transaction record
    ↓ Returns OrderDTO
    ↓
Response
    ↓ JSON OrderDTO
    ↓ Frontend receives response
    ↓ Shows success message
    ↓ Reloads page (balance updated)
```

### Example 2: Admin Settles Market

```
Frontend (AdminPanel.jsx)
    ↓ Admin clicks "Settle YES"
    ↓ Confirms action
    ↓
API Call (api.js)
    ↓ settleMarket({ marketId, outcome: 'YES' })
    ↓ POST /api/settlement/settle
    ↓
Backend Controller (SettlementController.java)
    ↓ @PostMapping("/settle")
    ↓ Calls settlementService.settleMarket()
    ↓
Service Layer (SettlementService.java)
    ↓ Loads market
    ↓ Closes market (status → CLOSED)
    ↓ Sets outcome = YES
    ↓ Gets all open orders for market
    ↓ For each order:
    │   ├─ If order.side == YES: Winner
    │   │    └─ Credit potentialPayout
    │   └─ If order.side == NO: Loser
    │        └─ No payout
    ↓ Marks all orders as SETTLED
    ↓ Creates transaction records
    ↓ Saves market (status → SETTLED)
    ↓ Returns MarketDTO
    ↓
Response
    ↓ JSON MarketDTO
    ↓ Frontend receives response
    ↓ Shows success message
    ↓ Reloads market list
```

## 📦 Dependencies

### Backend (pom.xml)

- **spring-boot-starter-web** - REST API
- **spring-boot-starter-data-jpa** - Database access
- **spring-boot-starter-validation** - Input validation
- **h2** - In-memory database
- **lombok** - Boilerplate reduction
- **spring-boot-devtools** - Hot reload

### Frontend (package.json)

- **react** - UI library
- **react-dom** - React renderer
- **react-router-dom** - Routing
- **axios** - HTTP client
- **lucide-react** - Icons
- **tailwindcss** - CSS framework
- **vite** - Build tool

## 🚀 Build & Deploy

### Development

```bash
# Backend
cd backend && mvn spring-boot:run

# Frontend
cd frontend && npm run dev
```

### Production Build

```bash
# Backend JAR
cd backend
mvn clean package
java -jar target/futures-backend-1.0.0.jar

# Frontend static files
cd frontend
npm run build
# Serve the dist/ folder with any web server
```

## 📝 Code Conventions

### Backend

- **Package structure:** Feature-based (model, repository, service, controller)
- **Naming:** CamelCase for classes, camelCase for methods
- **Annotations:** Lombok for getters/setters, Spring for DI
- **Transactions:** Service layer methods are @Transactional
- **DTOs:** Separate DTOs for requests and responses

### Frontend

- **Components:** PascalCase file names (Dashboard.jsx)
- **Functions:** camelCase (placeOrder)
- **Hooks:** Use useState, useEffect
- **Props:** Destructure in function signature
- **Styling:** Tailwind utility classes

## 🔍 Where to Find Things

| What | Where |
|------|-------|
| Add new API endpoint | Backend controller + service |
| Add new page | Frontend src/pages/ |
| Change database schema | Backend model/ entities |
| Modify market logic | Backend MarketService.java |
| Update UI styling | Frontend components (Tailwind) |
| Add seed data | Backend DataSeeder.java |
| API configuration | Backend application.properties |
| Frontend config | Frontend vite.config.js |

---

This structure provides a solid foundation for growth. The modular design makes it easy to add features, refactor, and eventually migrate to microservices or blockchain integration.


