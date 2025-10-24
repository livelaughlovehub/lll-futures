# LL&L Futures - API Documentation

## Base URL
```
http://localhost:8080/api
```

## Response Format

All responses are in JSON format.

### Success Response
```json
{
  "id": 1,
  "field": "value",
  ...
}
```

### Error Response
```json
{
  "status": 400,
  "message": "Error message",
  "timestamp": "2025-10-20T12:00:00"
}
```

### Validation Error Response
```json
{
  "status": 400,
  "message": "Validation failed",
  "errors": {
    "fieldName": "Error message"
  },
  "timestamp": "2025-10-20T12:00:00"
}
```

---

## 👥 Users API

### Get All Users
```http
GET /users
```

**Response:**
```json
[
  {
    "id": 1,
    "username": "alice",
    "email": "alice@example.com",
    "tokenBalance": 1000.0,
    "isAdmin": false,
    "createdAt": "2025-10-20T10:00:00"
  }
]
```

### Get User by ID
```http
GET /users/{id}
```

**Parameters:**
- `id` (path) - User ID

**Response:**
```json
{
  "id": 1,
  "username": "alice",
  "email": "alice@example.com",
  "tokenBalance": 1000.0,
  "isAdmin": false,
  "createdAt": "2025-10-20T10:00:00"
}
```

### Get User by Username
```http
GET /users/username/{username}
```

**Parameters:**
- `username` (path) - Username

**Response:** Same as Get User by ID

### Create User
```http
POST /users
```

**Request Body:**
```json
{
  "username": "newuser",
  "email": "newuser@example.com",
  "initialBalance": 1000.0,
  "isAdmin": false
}
```

**Validations:**
- `username`: Required, must be unique
- `email`: Required, must be valid email format
- `initialBalance`: Optional, defaults to 1000.0
- `isAdmin`: Optional, defaults to false

**Response:** Created user object (status 201)

---

## 📊 Markets API

### Get All Markets
```http
GET /markets
```

**Response:**
```json
[
  {
    "id": 1,
    "title": "Will Bitcoin reach $100K by December 2025?",
    "description": "Market resolves YES if BTC trades at $100K+...",
    "status": "ACTIVE",
    "expiryDate": "2025-12-31T23:59:00",
    "yesOdds": 2.5,
    "noOdds": 1.5,
    "totalYesStake": 500.0,
    "totalNoStake": 300.0,
    "totalVolume": 800.0,
    "creatorId": 1,
    "creatorUsername": "admin",
    "outcome": null,
    "settledAt": null,
    "createdAt": "2025-10-20T10:00:00"
  }
]
```

### Get Active Markets Only
```http
GET /markets/active
```

**Response:** Array of markets with status "ACTIVE"

### Get Market by ID
```http
GET /markets/{id}
```

**Parameters:**
- `id` (path) - Market ID

**Response:** Single market object

### Create Market
```http
POST /markets
```

**Request Body:**
```json
{
  "title": "Will Ethereum pass $5K this year?",
  "description": "Market resolves YES if ETH reaches $5,000 or higher...",
  "expiryDate": "2025-12-31T23:59:00",
  "yesOdds": 3.0,
  "noOdds": 1.4,
  "creatorId": 1
}
```

**Validations:**
- `title`: Required, not blank
- `description`: Optional
- `expiryDate`: Required, must be future date
- `yesOdds`: Required, must be positive
- `noOdds`: Required, must be positive
- `creatorId`: Required, must exist

**Response:** Created market object (status 201)

### Close Market
```http
PUT /markets/{id}/close
```

**Parameters:**
- `id` (path) - Market ID

**Description:** Closes market, preventing new bets. Required before settlement.

**Response:** Updated market object

---

## 💰 Orders API

### Get All Orders
```http
GET /orders
```

**Response:**
```json
[
  {
    "id": 1,
    "userId": 2,
    "username": "alice",
    "marketId": 1,
    "marketTitle": "Will Bitcoin reach $100K by December 2025?",
    "side": "YES",
    "stakeAmount": 100.0,
    "odds": 2.5,
    "potentialPayout": 250.0,
    "status": "OPEN",
    "settledAmount": null,
    "settledAt": null,
    "createdAt": "2025-10-20T11:00:00"
  }
]
```

### Get Order by ID
```http
GET /orders/{id}
```

**Parameters:**
- `id` (path) - Order ID

**Response:** Single order object

### Get User's Orders
```http
GET /orders/user/{userId}
```

**Parameters:**
- `userId` (path) - User ID

**Response:** Array of orders for the user

### Get User's Open Orders
```http
GET /orders/user/{userId}/open
```

**Parameters:**
- `userId` (path) - User ID

**Response:** Array of open (unsettled) orders for the user

### Place Order (Bet)
```http
POST /orders
```

**Request Body:**
```json
{
  "userId": 2,
  "marketId": 1,
  "side": "YES",
  "stakeAmount": 50.0
}
```

**Validations:**
- `userId`: Required, must exist
- `marketId`: Required, must exist and be ACTIVE
- `side`: Required, must be "YES" or "NO"
- `stakeAmount`: Required, must be positive and <= user's balance

**Business Logic:**
1. Checks user has sufficient balance
2. Locks current odds for the order
3. Calculates potential payout (stake × odds)
4. Deducts tokens from user balance
5. Updates market volume
6. Creates transaction record

**Response:** Created order object (status 201)

---

## ⚖️ Settlement API

### Settle Market
```http
POST /settlement/settle
```

**Request Body:**
```json
{
  "marketId": 1,
  "outcome": "YES"
}
```

**Validations:**
- `marketId`: Required, must exist
- `outcome`: Required, must be "YES", "NO", or "VOID"

**Outcomes:**
- **YES**: All YES bets win, NO bets lose
- **NO**: All NO bets win, YES bets lose
- **VOID**: All bets refunded, nobody wins/loses

**Business Logic:**
1. Closes market (if not already closed)
2. Sets outcome and settlement timestamp
3. Iterates through all open orders:
   - Winners: Credit potentialPayout to balance
   - Losers: No payout (already deducted)
   - Void: Refund original stake
4. Marks all orders as SETTLED
5. Creates transaction records for all payouts

**Response:** Settled market object

**Note:** This is an admin operation. In production, add authorization.

---

## 📋 Data Types & Enums

### Market Status
- `ACTIVE` - Accepting bets
- `CLOSED` - No new bets, awaiting settlement
- `SETTLED` - Resolved, payouts distributed
- `CANCELLED` - Cancelled, all bets refunded

### Market Outcome
- `YES` - Event occurred
- `NO` - Event did not occur
- `VOID` - Invalid/cancelled market

### Order Side
- `YES` - Betting that event will occur
- `NO` - Betting that event will not occur

### Order Status
- `OPEN` - Active bet, awaiting settlement
- `SETTLED` - Market resolved, payout processed
- `CANCELLED` - Order cancelled

### Transaction Type
- `DEPOSIT` - Tokens added
- `WITHDRAWAL` - Tokens removed
- `BET_PLACED` - Tokens staked on bet
- `BET_WON` - Payout from winning bet
- `BET_LOST` - Loss recorded (no token change)
- `BET_REFUND` - Refund from voided market

---

## 🔄 Common Workflows

### Create & Bet on Market

1. **Create Market**
   ```
   POST /markets
   ```

2. **Users Place Bets**
   ```
   POST /orders (multiple calls with different users/sides)
   ```

3. **Close Market** (Optional, before settlement)
   ```
   PUT /markets/{id}/close
   ```

4. **Settle Market** (Admin)
   ```
   POST /settlement/settle
   ```

5. **Check Positions**
   ```
   GET /orders/user/{userId}
   ```

### Check Balance After Betting

1. **Before Bet**
   ```
   GET /users/{id}
   → tokenBalance: 1000.0
   ```

2. **Place Bet**
   ```
   POST /orders
   { "userId": 1, "marketId": 1, "side": "YES", "stakeAmount": 100 }
   ```

3. **After Bet**
   ```
   GET /users/{id}
   → tokenBalance: 900.0
   ```

4. **After Winning**
   ```
   POST /settlement/settle
   { "marketId": 1, "outcome": "YES" }
   
   GET /users/{id}
   → tokenBalance: 900.0 + 250.0 = 1150.0
   ```

---

## ⚠️ Error Codes

| Status | Description |
|--------|-------------|
| 200 | Success |
| 201 | Created |
| 400 | Bad Request (validation failed) |
| 404 | Not Found |
| 500 | Internal Server Error |

### Common Error Messages

**Insufficient Balance:**
```json
{
  "status": 400,
  "message": "Insufficient balance. Current: 50.0 LLL, Required: 100.0 LLL",
  "timestamp": "2025-10-20T12:00:00"
}
```

**Market Not Active:**
```json
{
  "status": 400,
  "message": "Market is not active",
  "timestamp": "2025-10-20T12:00:00"
}
```

**User Not Found:**
```json
{
  "status": 400,
  "message": "User not found with id: 999",
  "timestamp": "2025-10-20T12:00:00"
}
```

---

## 🧪 Testing with cURL

### Create User
```bash
curl -X POST http://localhost:8080/api/users \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "email": "test@example.com",
    "initialBalance": 2000.0
  }'
```

### Create Market
```bash
curl -X POST http://localhost:8080/api/markets \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Test Market",
    "description": "A test prediction market",
    "expiryDate": "2025-12-31T23:59:00",
    "yesOdds": 2.0,
    "noOdds": 2.0,
    "creatorId": 1
  }'
```

### Place Bet
```bash
curl -X POST http://localhost:8080/api/orders \
  -H "Content-Type: application/json" \
  -d '{
    "userId": 2,
    "marketId": 1,
    "side": "YES",
    "stakeAmount": 100.0
  }'
```

### Settle Market
```bash
curl -X POST http://localhost:8080/api/settlement/settle \
  -H "Content-Type: application/json" \
  -d '{
    "marketId": 1,
    "outcome": "YES"
  }'
```

### Get User Balance
```bash
curl http://localhost:8080/api/users/2
```

---

## 📚 Additional Resources

- [Main README](README.md) - Project overview and setup
- [Architecture Guide](ARCHITECTURE.md) - System design deep-dive
- [H2 Console](http://localhost:8080/h2-console) - Database browser

---

**Happy Building! 🚀**


