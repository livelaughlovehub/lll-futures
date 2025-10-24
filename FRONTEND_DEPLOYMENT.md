# 🎯 Frontend Deployment Guide

## ✅ Backend is Live!
Your backend is running at: `https://lll-futures-backend.onrender.com`

## 🚀 Deploy Frontend on Render

### Step 1: Create Frontend Service
1. **Go to Render.com** → **"New +"** → **"Static Site"**
2. **Connect GitHub Repository**: `livelaughlovehub/lll-futures`
3. **Configure Settings:**
   - **Name**: `lll-futures-frontend`
   - **Branch**: `main`
   - **Root Directory**: `frontend`
   - **Build Command**: `npm install && npm run build`
   - **Publish Directory**: `dist`

### Step 2: Set Environment Variable
**In Render dashboard, add this environment variable:**
```
VITE_API_URL=https://lll-futures-backend.onrender.com/api
```

### Step 3: Deploy
- Click **"Create Static Site"**
- Wait for deployment (2-5 minutes)
- You'll get a URL like: `https://lll-futures-frontend.onrender.com`

## 🔧 Local Development

For local development, create a `.env.local` file in your `frontend/` directory:

```bash
# frontend/.env.local
VITE_API_URL=http://localhost:8080/api
```

## 🎯 How It Works

**Local Development:**
- Uses `http://localhost:8080/api` (your local backend)
- Frontend runs on `http://localhost:3000`

**Production:**
- Uses `https://lll-futures-backend.onrender.com/api` (your Render backend)
- Frontend runs on `https://lll-futures-frontend.onrender.com`

## ✅ Test Your Setup

**Local:**
1. Start backend: `cd backend && mvn spring-boot:run`
2. Start frontend: `cd frontend && npm run dev`
3. Visit: `http://localhost:3000`

**Production:**
1. Visit: `https://lll-futures-frontend.onrender.com`
2. Test wallet connection and trading

## 🎉 Success!

Once deployed, your full LL&L Futures platform will be live:
- **Frontend**: `https://lll-futures-frontend.onrender.com`
- **Backend**: `https://lll-futures-backend.onrender.com`

Users can connect wallets, stake LLL tokens, and trade futures markets!
