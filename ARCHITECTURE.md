# LL&L Futures - System Architecture

## Table of Contents
1. [Overview](#overview)
2. [System Design](#system-design)
3. [Backend Architecture](#backend-architecture)
4. [Frontend Architecture](#frontend-architecture)
5. [Data Flow](#data-flow)
6. [Security Considerations](#security-considerations)
7. [Scalability Plan](#scalability-plan)
8. [Future Architecture](#future-architecture)

---

## Overview

LL&L Futures is a **three-tier web application** following a clean, modular architecture:

1. **Presentation Layer** - React Frontend (SPA)
2. **Application Layer** - Spring Boot REST API
3. **Data Layer** - H2 In-Memory Database

### Design Principles

- **Separation of Concerns**: Clear boundaries between layers
- **RESTful Design**: Stateless HTTP API
- **Domain-Driven Design**: Rich domain models with business logic
- **Repository Pattern**: Abstract data access
- **DTO Pattern**: Decouple API contracts from domain models
- **Dependency Injection**: Loose coupling via Spring IoC

---

## System Design

### High-Level Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                    Browser (Client)                         │
│                                                             │
│  ┌───────────────────────────────────────────────────────┐ │
│  │         React SPA (Port 3000)                         │ │
│  │                                                       │ │
│  │  Components → Pages → API Client → Axios             │ │
│  └───────────────────────────────────────────────────────┘ │
└──────────────────────────┬──────────────────────────────────┘
                           │
                    HTTP REST API (JSON)
                           │
┌──────────────────────────┴──────────────────────────────────┐
│              Spring Boot Backend (Port 8080)                │
│                                                             │
│  ┌───────────────────────────────────────────────────────┐ │
│  │            Controllers (REST Endpoints)               │ │
│  │  @RestController + @RequestMapping                    │ │
│  │  - Validation (@Valid)                                │ │
│  │  - Exception Handling (@RestControllerAdvice)         │ │
│  └───────────────────────┬───────────────────────────────┘ │
│                          │                                  │
│  ┌───────────────────────┴───────────────────────────────┐ │
│  │              Service Layer                            │ │
│  │  - Business Logic                                     │ │
│  │  - Transaction Management (@Transactional)            │ │
│  │  - Domain Operations                                  │ │
│  └───────────────────────┬───────────────────────────────┘ │
│                          │                                  │
│  ┌───────────────────────┴───────────────────────────────┐ │
│  │            Repository Layer                           │ │
│  │  Spring Data JPA (JpaRepository)                      │ │
│  │  - Query Methods                                      │ │
│  │  - Custom Queries                                     │ │
│  └───────────────────────┬───────────────────────────────┘ │
│                          │                                  │
│  ┌───────────────────────┴───────────────────────────────┐ │
│  │              JPA/Hibernate                            │ │
│  │  - Entity Management                                  │ │
│  │  - ORM Mapping                                        │ │
│  └───────────────────────┬───────────────────────────────┘ │
│                          │                                  │
│  ┌───────────────────────┴───────────────────────────────┐ │
│  │            H2 Database (In-Memory)                    │ │
│  │  Tables: users, markets, orders, transactions         │ │
│  └───────────────────────────────────────────────────────┘ │
└─────────────────────────────────────────────────────────────┘
```

---

## Backend Architecture

### Layer Breakdown

#### 1. Controller Layer (`@RestController`)

**Responsibilities:**
- Handle HTTP requests/responses
- Input validation
- Route to appropriate services
- Error handling and HTTP status codes

**Example:**
```java
@RestController
@RequestMapping("/api/orders")
public class OrderController {
    private final OrderService orderService;
    
    @PostMapping
    public ResponseEntity<OrderDTO> placeOrder(@Valid @RequestBody PlaceOrderRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(orderService.placeOrder(request));
    }
}
```

#### 2. Service Layer (`@Service`)

**Responsibilities:**
- Business logic implementation
- Transaction management
- Coordinate between repositories
- Enforce business rules

**Key Services:**

**UserService**
- User CRUD operations
- Token balance management
- Validation

**MarketService**
- Market lifecycle management
- Volume tracking
- Status updates

**OrderService**
- Order placement
- Balance checking
- Transaction recording

**SettlementService**
- Market resolution
- Payout distribution
- Winner determination

#### 3. Repository Layer (`@Repository`)

**Responsibilities:**
- Data access abstraction
- Query execution
- Database operations

**Pattern:** Spring Data JPA with method name queries

```java
@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByUserId(Long userId);
    List<Order> findByMarketIdAndStatus(Long marketId, Order.OrderStatus status);
}
```

#### 4. Model/Entity Layer (`@Entity`)

**JPA Entities:**
- `User` - User accounts and balances
- `Market` - Prediction markets
- `Order` - Bets/positions
- `Transaction` - Audit trail of token movements

**Relationships:**
- User → Orders (One-to-Many)
- User → Markets (One-to-Many, as creator)
- Market → Orders (One-to-Many)
- User → Transactions (One-to-Many)

#### 5. DTO Layer

**Purpose:** Decouple API contracts from domain models

- `UserDTO`, `CreateUserRequest`
- `MarketDTO`, `CreateMarketRequest`
- `OrderDTO`, `PlaceOrderRequest`
- `SettleMarketRequest`

**Benefits:**
- Version API independently
- Hide internal structure
- Reduce over-fetching
- Enable API evolution

---

## Frontend Architecture

### React Component Hierarchy

```
App
├── Navbar (User selector, balance display)
├── Routes
│   ├── Dashboard (Stats + Featured markets)
│   ├── Markets (Browse all active markets)
│   │   └── MarketCard[] (Individual market cards)
│   ├── MyPositions (User's bets)
│   ├── Wallet (Token balance info)
│   ├── CreateMarket (Form)
│   └── AdminPanel (Settlement tools)
```

### State Management

**Current Approach:** Component-level state with `useState` and `useEffect`

**Data Flow:**
1. Component mounts → `useEffect` triggers
2. API call via `api.js` → Axios
3. Response → `setState`
4. Re-render with new data

**Future Enhancement:** Consider Context API or Zustand for global state

### API Client (`api.js`)

Centralized API service using Axios:

```javascript
const api = axios.create({
  baseURL: 'http://localhost:8080/api',
  headers: { 'Content-Type': 'application/json' }
})

export const placeOrder = async (orderData) => {
  const response = await api.post('/orders', orderData)
  return response.data
}
```

### Routing

React Router v6 for client-side navigation:
- `/` - Dashboard
- `/markets` - Markets
- `/positions` - My Positions
- `/wallet` - Wallet
- `/create` - Create Market
- `/admin` - Admin Panel (restricted)

---

## Data Flow

### Example: Placing a Bet

```
┌─────────────┐
│   User      │
│  Clicks     │
│  "Bet YES"  │
└──────┬──────┘
       │
       ▼
┌─────────────────┐
│  MarketCard     │
│  Component      │
│  - Opens modal  │
│  - User enters  │
│    amount       │
└────────┬────────┘
         │
         ▼
┌─────────────────────┐
│   API Call          │
│  placeOrder()       │
│  POST /api/orders   │
└──────────┬──────────┘
           │
           ▼
┌───────────────────────┐
│  OrderController      │
│  - Validate request   │
│  - Call service       │
└──────────┬────────────┘
           │
           ▼
┌───────────────────────┐
│  OrderService         │
│  - Check balance      │
│  - Calculate payout   │
│  - Create order       │
│  - Deduct tokens      │
│  - Update market      │
│  - Record transaction │
└──────────┬────────────┘
           │
           ▼
┌───────────────────────┐
│  Repositories         │
│  - Save order         │
│  - Update user        │
│  - Update market      │
│  - Save transaction   │
└──────────┬────────────┘
           │
           ▼
┌───────────────────────┐
│  H2 Database          │
│  - Persist all        │
└──────────┬────────────┘
           │
           ▼
┌───────────────────────┐
│  Response             │
│  OrderDTO → JSON      │
└──────────┬────────────┘
           │
           ▼
┌───────────────────────┐
│  Frontend             │
│  - Update UI          │
│  - Reload page        │
│    (refresh balance)  │
└───────────────────────┘
```

### Transaction Management

All financial operations are wrapped in `@Transactional` to ensure:
- Atomicity: All-or-nothing operations
- Consistency: Balance always correct
- Isolation: Concurrent bets don't interfere
- Durability: Changes persisted

---

## Security Considerations

### Current State (MVP)

⚠️ **Not production-ready**. No authentication/authorization implemented.

### Production Security Roadmap

**Authentication:**
- JWT-based auth
- OAuth2/OpenID Connect
- Session management

**Authorization:**
- Role-based access control (RBAC)
- Admin-only endpoints protected
- User can only access own data

**API Security:**
- CORS properly configured
- Rate limiting
- Input validation & sanitization
- SQL injection prevention (JPA handles this)
- XSS protection

**Data Security:**
- Password hashing (bcrypt)
- HTTPS only
- Secure headers
- Environment variables for secrets

---

## Scalability Plan

### Current Limitations

- Single server instance
- In-memory database (data lost on restart)
- No caching
- Synchronous processing
- No load balancing

### Scaling Strategy

#### Phase 1: Database
- Migrate H2 → PostgreSQL
- Connection pooling (HikariCP)
- Read replicas for queries

#### Phase 2: Caching
- Redis for:
  - User sessions
  - Market data
  - Leaderboards
- Cache invalidation strategy

#### Phase 3: Horizontal Scaling
- Stateless backend (multiple instances)
- Load balancer (Nginx/AWS ALB)
- Containerization (Docker)
- Orchestration (Kubernetes)

#### Phase 4: Async Processing
- Message queue (RabbitMQ/Kafka)
- Async settlement
- Event-driven architecture

#### Phase 5: Microservices
Split monolith into:
- User Service
- Market Service
- Order Service
- Settlement Service
- Notification Service

---

## Future Architecture

### Blockchain Integration

```
┌────────────────┐
│  React Web App │
└───────┬────────┘
        │
        ├───────────┐
        │           │
        ▼           ▼
┌──────────┐  ┌────────────┐
│  Web3    │  │ Spring Boot│
│  Wallet  │  │   Backend  │
│(Phantom) │  │            │
└────┬─────┘  └─────┬──────┘
     │              │
     ▼              ▼
┌─────────────────────┐
│   Solana Blockchain │
│                     │
│  - SPL Token (LLL)  │
│  - Smart Contracts  │
│  - On-chain Orders  │
└─────────────────────┘
```

### Decentralized Features

**Token (Solana SPL Token):**
- Real ownership
- Transferable
- Tradeable on DEXs

**Smart Contracts (Rust/Anchor):**
- Market creation
- Order placement
- Automated settlement
- Escrow/locking

**Benefits:**
- Trustless operation
- Censorship resistance
- Transparent settlement
- True token ownership

---

## Development Best Practices

### Backend
- Use DTOs for API contracts
- Write unit tests for services
- Use Lombok to reduce boilerplate
- Follow REST conventions
- Document APIs with Swagger/OpenAPI

### Frontend
- Component composition
- Extract reusable components
- Use custom hooks for logic
- Implement error boundaries
- Optimize re-renders

### Database
- Index foreign keys
- Avoid N+1 queries
- Use lazy loading wisely
- Monitor query performance

---

## Monitoring & Observability

### Future Implementation

**Logging:**
- Structured logging (JSON)
- Log aggregation (ELK/Splunk)
- Request tracing

**Metrics:**
- Prometheus + Grafana
- Application metrics
- Business metrics (bets/day, volume)

**Alerting:**
- Critical errors
- Performance degradation
- High latency

**APM:**
- New Relic / DataDog
- Distributed tracing

---

## Conclusion

This architecture provides a solid foundation for a prediction market platform. The modular design allows for gradual enhancement from MVP to production-scale blockchain-powered system.

**Key Takeaways:**
- Clean layer separation enables easy testing and maintenance
- RESTful API allows frontend flexibility
- DTO pattern decouples API from internal models
- Spring Boot's auto-configuration accelerates development
- Clear migration path to blockchain and microservices

For detailed API documentation, see [README.md](README.md).


