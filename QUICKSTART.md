# LL&L Futures - Quick Start Guide

Get up and running with LL&L Futures in 5 minutes!

## 📋 Prerequisites Checklist

- [ ] Java 17 or higher installed
- [ ] Node.js 18+ and npm installed
- [ ] Maven installed (or use mvnw wrapper)
- [ ] Terminal/Command Prompt access

## 🚀 Quick Start (5 Steps)

### Step 1: Clone or Navigate to Project

```bash
cd /Users/robertyoung/Documents/work/test/Gen2/MarketFutures
```

### Step 2: Start the Backend

Open a terminal and run:

```bash
cd backend
mvn spring-boot:run
```

Wait for the message: **"Started FuturesApplication in X seconds"**

✅ Backend is now running at `http://localhost:8080`

### Step 3: Start the Frontend

Open **another terminal** and run:

```bash
cd frontend
npm install
npm run dev
```

Wait for the message: **"Local: http://localhost:3000"**

✅ Frontend is now running at `http://localhost:3000`

### Step 4: Open in Browser

Navigate to: **http://localhost:3000**

You should see the LL&L Futures dashboard!

### Step 5: Start Trading

1. **Select a user** from the dropdown (default: alice)
2. **Browse markets** - Click "Markets" in the navigation
3. **Place a bet** - Click YES or NO, enter amount, confirm
4. **View your positions** - Click "My Positions"
5. **Check your wallet** - Click "Wallet" to see token balance

## 🎮 Try These Features

### As a Regular User (alice, bob, charlie)

1. **Browse Markets**
   - Go to "Markets" tab
   - See 6 pre-loaded prediction markets

2. **Place a Bet**
   - Click "YES" or "NO" on any market
   - Enter stake amount (you start with 1,000 LLL)
   - Confirm bet
   - Watch your balance update

3. **Check Positions**
   - Go to "My Positions"
   - See your active bets and potential payouts

4. **Create a Market**
   - Go to "Create Market"
   - Fill out the form
   - Submit to launch your own prediction market

### As an Admin User

1. **Switch to Admin**
   - Select "admin" from user dropdown (top right)

2. **Access Admin Panel**
   - Click "Admin" in navigation
   - See all markets with settlement options

3. **Settle a Market**
   - Find a market with bets
   - Click "Settle YES", "Settle NO", or "Void & Refund"
   - Confirm settlement
   - Payouts automatically distributed!

4. **Verify Payouts**
   - Switch back to a user who placed a winning bet
   - Check "Wallet" - balance should be updated
   - Check "My Positions" - bet should show as "SETTLED"

## 🧪 Test Scenario: Full Workflow

Try this complete workflow to see everything in action:

### Scenario: "Bitcoin $100K Bet"

1. **Switch to alice** (1,000 LLL balance)
2. **Go to Markets**, find "Will Bitcoin reach $100K by December 2025?"
3. **Click YES**, bet **100 LLL**, confirm
   - Your balance → 900 LLL
   - Potential payout → 250 LLL (100 × 2.5x odds)

4. **Switch to bob** (1,000 LLL balance)
5. **Same market**, click **NO**, bet **150 LLL**, confirm
   - Bob's balance → 850 LLL
   - Potential payout → 225 LLL (150 × 1.5x odds)

6. **Switch to admin**
7. **Go to Admin Panel**
8. **Find the Bitcoin market**
9. **Click "Settle YES"**, confirm
   - Alice wins!
   - Alice gets 250 LLL payout
   - Bob loses (gets nothing back)

10. **Switch back to alice**
11. **Check Wallet** → Balance now **1,150 LLL** (900 + 250)
12. **Check My Positions** → Bet shows "SETTLED", payout 250 LLL

13. **Switch to bob**
14. **Check Wallet** → Balance still **850 LLL** (lost the bet)
15. **Check My Positions** → Bet shows "SETTLED", payout 0 LLL

✨ **Congratulations!** You've completed a full prediction market cycle.

## 🛠️ Troubleshooting

### Backend Won't Start

**Problem:** Port 8080 already in use

**Solution:**
```bash
# Find and kill process on port 8080
# macOS/Linux:
lsof -ti:8080 | xargs kill -9

# Windows:
netstat -ano | findstr :8080
taskkill /PID <PID> /F
```

**Problem:** Maven not found

**Solution:** Use Maven wrapper instead:
```bash
./mvnw spring-boot:run    # macOS/Linux
mvnw.cmd spring-boot:run  # Windows
```

### Frontend Won't Start

**Problem:** Port 3000 already in use

**Solution:** Vite will automatically try port 3001, 3002, etc.

**Problem:** `npm install` fails

**Solution:**
```bash
# Clear npm cache
npm cache clean --force
rm -rf node_modules package-lock.json
npm install
```

### API Calls Failing

**Problem:** CORS errors in browser console

**Solution:** Backend is already configured for CORS. Make sure backend is running on port 8080.

**Problem:** "User not found" errors

**Solution:** Database resets on each backend restart. Users are auto-seeded. Refresh the frontend page.

## 📊 H2 Database Console (Optional)

Want to peek at the database?

1. **Ensure backend is running**
2. **Navigate to:** http://localhost:8080/h2-console
3. **Enter credentials:**
   - JDBC URL: `jdbc:h2:mem:lll_futures`
   - Username: `sa`
   - Password: (leave empty)
4. **Click "Connect"**
5. **Run queries:**
   ```sql
   SELECT * FROM USERS;
   SELECT * FROM MARKETS;
   SELECT * FROM ORDERS;
   SELECT * FROM TRANSACTIONS;
   ```

## 📱 What You'll See

### Dashboard
- Token balance card
- Open positions count
- Total staked amount
- Potential winnings
- Featured markets (top 3)

### Markets Page
- Grid of all active markets
- Each card shows:
  - Market title & description
  - Total volume
  - Expiry date
  - YES/NO odds
  - Bet buttons

### My Positions
- All your bets (open and settled)
- Filter tabs: All / Open / Settled
- Each position shows:
  - Market title
  - Your side (YES/NO)
  - Stake amount
  - Odds
  - Potential or actual payout
  - Status

### Wallet
- Large balance display
- Account type (Admin/Standard)
- Member since date
- Token info
- Future roadmap note

### Create Market
- Form to create new markets
- Fields:
  - Title
  - Description
  - Expiry date & time
  - YES odds
  - NO odds
- Live preview of payout calculations

### Admin Panel (Admin only)
- List of all markets
- Each market shows:
  - Volume statistics
  - YES/NO stakes breakdown
  - Settlement buttons
- One-click settlement

## 🎯 Next Steps

1. **Read the docs:**
   - [README.md](README.md) - Full documentation
   - [ARCHITECTURE.md](ARCHITECTURE.md) - System design
   - [API_DOCUMENTATION.md](API_DOCUMENTATION.md) - API reference

2. **Customize:**
   - Add your own markets
   - Modify odds and payouts
   - Create more users

3. **Develop:**
   - Add authentication
   - Implement real-time updates
   - Add charts and analytics
   - Integrate with Solana blockchain

## 💡 Pro Tips

- **Quick user switching:** Use the dropdown in the navbar to instantly switch between users
- **Test settlements:** Create a market, place bets from multiple users, then settle it
- **Watch the logs:** Backend logs show all operations (check your terminal)
- **Inspect network:** Open browser DevTools → Network to see API calls
- **Data resets:** Backend restarts clear all data and reseed the database

## 🐛 Known Limitations (MVP)

- No authentication/login system
- Data clears on backend restart (in-memory DB)
- Page refresh after betting to update balance
- No real-time updates
- No transaction history view yet
- No market search/filter
- Admin access not protected

These will be addressed in future versions!

## 🎉 You're All Set!

You now have a working prediction market platform. Happy trading! 🚀

**Questions?** Check the full [README](README.md) or explore the [API docs](API_DOCUMENTATION.md).

---

**Built with ❤️ using Spring Boot & React**

