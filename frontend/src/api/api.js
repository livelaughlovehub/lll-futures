import axios from 'axios'

const API_BASE_URL = 'http://localhost:8080/api'

const api = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
})

// Users
export const getAllUsers = async () => {
  const response = await api.get('/users')
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

export default api


