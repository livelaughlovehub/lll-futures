import axios from 'axios'

// Use environment variable for API URL, fallback to localhost for development
const API_BASE_URL = import.meta.env.VITE_API_URL || 'http://localhost:8080/api'

const api = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
  timeout: 30000, // 30 seconds timeout for Render cold starts
})

// Add request interceptor to include JWT token
api.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('jwtToken')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  (error) => {
    return Promise.reject(error)
  }
)

// Add response interceptor to handle 401 (unauthorized)
api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      // Token expired or invalid - clear storage and redirect to login
      localStorage.removeItem('jwtToken')
      localStorage.removeItem('currentUser')
      // Only redirect if we're not already on the signin page
      if (window.location.pathname !== '/signin' && window.location.pathname !== '/signup') {
        window.location.href = '/signin'
      }
    }
    return Promise.reject(error)
  }
)

// Users
export const getAllUsers = async () => {
  const response = await api.get('/users')
  return response.data
}

export const signupUser = async (userData) => {
  const response = await api.post('/users/signup', userData)
  return response.data
}

export const signInUser = async (credentials) => {
  const response = await api.post('/users/signin', credentials)
  // Store JWT token
  if (response.data.token) {
    localStorage.setItem('jwtToken', response.data.token)
    localStorage.setItem('currentUser', JSON.stringify(response.data.user))
  }
  return response.data
}

export const getUserById = async (id) => {
  const response = await api.get(`/users/${id}`)
  return response.data
}

export const createUser = async (userData) => {
  const response = await api.post('/users', userData)
  return response.data
}

// Markets
export const getAllMarkets = async () => {
  const response = await api.get('/markets')
  return response.data
}

export const getActiveMarkets = async () => {
  const response = await api.get('/markets/active')
  return response.data
}

export const getMarketById = async (id) => {
  const response = await api.get(`/markets/${id}`)
  return response.data
}

export const createMarket = async (marketData) => {
  const response = await api.post('/markets', marketData)
  return response.data
}

export const closeMarket = async (id) => {
  const response = await api.put(`/markets/${id}/close`)
  return response.data
}

// Orders
export const getAllOrders = async () => {
  const response = await api.get('/orders')
  return response.data
}

export const getUserOrders = async (userId) => {
  const response = await api.get(`/orders/user/${userId}`)
  return response.data
}

export const getUserOpenOrders = async (userId) => {
  const response = await api.get(`/orders/user/${userId}/open`)
  return response.data
}

export const placeOrder = async (orderData) => {
  const response = await api.post('/orders', orderData)
  return response.data
}

// Settlement
export const settleMarket = async (settlementData) => {
  const response = await api.post('/settlement/settle', settlementData)
  return response.data
}

// File upload
export const uploadFile = async (file, folder = 'profiles') => {
  const formData = new FormData()
  formData.append('file', file)
  formData.append('folder', folder)
  
  const response = await api.post('/files/upload', formData, {
    headers: {
      'Content-Type': 'multipart/form-data',
    },
  })
  return response.data
}

// Profile update
export const updateUserProfile = async (userId, profileData) => {
  const response = await api.put(`/users/${userId}`, profileData)
  return response.data
}

// Health check endpoint
export const healthCheck = async () => {
  const response = await api.get('/health')
  return response.data
}

// Withdraw tokens to external wallet (e.g., Phantom)
export const withdrawToWallet = async (userId, phantomWallet, amount) => {
  const response = await api.post(`/users/${userId}/withdraw`, null, {
    params: {
      phantomWallet,
      amount
    }
  })
  return response.data
}

// Get deposit address for user
export const getDepositAddress = async (userId) => {
  const response = await api.get(`/users/${userId}/deposit-address`)
  return response.data
}

// Check for incoming deposits
export const checkDeposit = async (userId) => {
  const response = await api.post(`/users/${userId}/check-deposit`)
  return response.data
}

export default api


