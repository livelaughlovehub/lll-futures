# 🚀 LL&L Futures - Render Deployment Guide

## 📋 Prerequisites
- GitHub repository: `livelaughlovehub/lll-futures`
- Render.com account
- All code committed and pushed to GitHub

## 🎯 Deployment Steps

### 1. Frontend Deployment (Static Site)

1. **Go to Render.com** → Sign in → Click "New +" → "Static Site"
2. **Connect GitHub Repository**: `livelaughlovehub/lll-futures`
3. **Configure Build Settings**:
   - **Name**: `lll-futures-frontend`
   - **Branch**: `main`
   - **Root Directory**: `frontend`
   - **Build Command**: `npm install && npm run build`
   - **Publish Directory**: `dist`
   - **Node Version**: `18`

4. **Environment Variables** (Add these in Render dashboard):
   ```
   VITE_API_URL=https://your-backend-url.onrender.com/api
   ```
   *Note: You'll update this after backend deployment*

5. **Click "Create Static Site"**
6. **Wait for deployment** (2-5 minutes)
7. **Note your frontend URL**: `https://lll-futures-frontend.onrender.com`

### 2. Backend Deployment (Web Service)

1. **Click "New +"** → "Web Service"
2. **Connect GitHub Repository**: `livelaughlovehub/lll-futures`
3. **Configure Build Settings**:
   - **Name**: `lll-futures-backend`
   - **Branch**: `main`
   - **Root Directory**: `backend`
   - **Runtime**: `Java`
   - **Build Command**: `./mvnw clean package -DskipTests`
   - **Start Command**: `java -jar target/futures-backend-1.0.0.jar`
   - **Java Version**: `17`

4. **Environment Variables** (Add these in Render dashboard):
   ```
   PORT=8080
   SPRING_PROFILES_ACTIVE=production
   ```

5. **Click "Create Web Service"**
6. **Wait for deployment** (5-10 minutes)
7. **Note your backend URL**: `https://lll-futures-backend.onrender.com`

### 3. Connect Frontend to Backend

1. **Update Frontend Environment Variable**:
   - Go to your frontend service in Render dashboard
   - Go to "Environment" tab
   - Update `VITE_API_URL` to: `https://lll-futures-backend.onrender.com/api`
   - Click "Save Changes"
   - Trigger a manual redeploy

2. **Update Backend CORS** (if needed):
   - Go to backend service → "Environment" tab
   - Add/update CORS origins if your frontend URL is different

### 4. Test Your Deployment

1. **Visit Frontend**: `https://lll-futures-frontend.onrender.com`
2. **Test Features**:
   - ✅ Wallet connection
   - ✅ Token staking
   - ✅ Market trading
   - ✅ Order placement

## 🔧 Troubleshooting

### Common Issues:
- **Cold Start**: Free tier apps sleep after 15 minutes of inactivity
- **CORS Errors**: Check backend CORS configuration
- **Build Failures**: Check Node/Java versions match requirements
- **API Connection**: Verify environment variables are set correctly

### Debug Steps:
1. Check Render build logs for errors
2. Verify environment variables are set
3. Test API endpoints directly: `https://your-backend.onrender.com/api/markets`
4. Check browser console for frontend errors

## 📊 Monitoring

- **Render Dashboard**: Monitor app health and logs
- **Build Logs**: Check for compilation errors
- **Runtime Logs**: Monitor application behavior
- **Metrics**: Track performance and usage

## 🎉 Success!

Once deployed, your LL&L Futures platform will be live at:
- **Frontend**: `https://lll-futures-frontend.onrender.com`
- **Backend**: `https://lll-futures-backend.onrender.com`

Users can now:
- Connect Solana wallets
- Stake LLL tokens
- Trade futures markets
- Earn rewards
