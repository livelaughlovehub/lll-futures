# Environment Setup Guide

## 🗄️ Database Configuration

### Local Development (H2)
- **Database**: H2 (File-based)
- **Location**: `./data/lll_futures_db`
- **Console**: Available at `http://localhost:8080/h2-console`
- **No setup required** - automatically created

### Production (Neon)
1. **Create Neon Account**: https://neon.tech
2. **Create Database**: Follow Neon setup wizard
3. **Get Connection String**: Copy from Neon dashboard
4. **Set Environment Variables**:
   ```bash
   NEON_DATABASE_URL=postgresql://username:password@hostname:port/database
   NEON_USERNAME=your_username
   NEON_PASSWORD=your_password
   ```

---

## 📁 Storage Configuration

### Local Development (File System)
- **Storage**: Local file system
- **Location**: `./uploads/`
- **No setup required** - automatically created

### Production (Cloudflare R2)
1. **Create Cloudflare Account**: https://cloudflare.com
2. **Enable R2**: Go to R2 Object Storage
3. **Create Bucket**: Choose a unique bucket name
4. **Generate API Token**: Create R2 token with read/write permissions
5. **Set Environment Variables**:
   ```bash
   R2_ENDPOINT=https://your-account-id.r2.cloudflarestorage.com
   R2_ACCESS_KEY=your_access_key
   R2_SECRET_KEY=your_secret_key
   R2_BUCKET=your_bucket_name
   ```

---

## 🚀 Deployment Setup

### Render Environment Variables

#### Backend Service:
```bash
# Database (Neon)
NEON_DATABASE_URL=postgresql://username:password@hostname:port/database
NEON_USERNAME=your_username
NEON_PASSWORD=your_password

# Storage (Cloudflare R2)
R2_ENDPOINT=https://your-account-id.r2.cloudflarestorage.com
R2_ACCESS_KEY=your_access_key
R2_SECRET_KEY=your_secret_key
R2_BUCKET=your_bucket_name

# Application
SPRING_PROFILES_ACTIVE=production
PORT=8080
STORAGE_TYPE=r2
```

#### Frontend Service:
```bash
# API Configuration
VITE_API_URL=https://lll-futures-backend.onrender.com/api
```

---

## 🔧 Local Development

### Start Backend (Local):
```bash
# Using Java 21
./start-backend-java21.sh

# Or manually
export SPRING_PROFILES_ACTIVE=local
cd backend
mvn spring-boot:run
```

### Start Frontend (Local):
```bash
cd frontend
npm install
npm run dev
```

---

## 📋 Environment Profiles

### Local Development:
- **Profile**: `local`
- **Database**: H2 (file-based)
- **Storage**: Local file system
- **H2 Console**: Enabled

### Production:
- **Profile**: `production`
- **Database**: Neon PostgreSQL
- **Storage**: Cloudflare R2
- **H2 Console**: Disabled

---

## 🔍 Testing Storage

### Upload File:
```bash
curl -X POST \
  http://localhost:8080/api/files/upload \
  -F "file=@test.jpg" \
  -F "folder=images"
```

### List Files:
```bash
curl http://localhost:8080/api/files/list?folder=images
```

### Delete File:
```bash
curl -X DELETE \
  "http://localhost:8080/api/files/delete?fileUrl=/uploads/images/test.jpg"
```

---

## 🎯 Benefits

### Local Development:
- ✅ **Fast setup** - No external services needed
- ✅ **Offline development** - Works without internet
- ✅ **Easy debugging** - Direct file access
- ✅ **H2 Console** - Visual database management

### Production:
- ✅ **Scalable database** - Neon PostgreSQL
- ✅ **Global CDN** - Cloudflare R2 storage
- ✅ **High availability** - Managed services
- ✅ **Automatic backups** - Built-in redundancy
