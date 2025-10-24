// API service for LLL token operations
import axios from 'axios'

// Use environment variable for API URL, fallback to localhost for development
const API_BASE_URL = import.meta.env.VITE_API_URL || 'http://localhost:8080/api'

// Token Balance API
export const getTokenBalance = async (walletAddress) => {
  try {
    const response = await axios.get(`${API_BASE_URL}/lll/balance/${walletAddress}`)
    return response.data
  } catch (error) {
    console.error('Error fetching token balance:', error)
    throw error
  }
}

// Staking API
export const stakeTokens = async (walletAddress, amount) => {
  try {
    const response = await axios.post(`${API_BASE_URL}/lll/stake`, {
      walletAddress,
      amount
    })
    return response.data
  } catch (error) {
    console.error('Error staking tokens:', error)
    throw error
  }
}

export const unstakeTokens = async (walletAddress, amount) => {
  try {
    const response = await axios.post(`${API_BASE_URL}/lll/unstake`, {
      walletAddress,
      amount
    })
    return response.data
  } catch (error) {
    console.error('Error unstaking tokens:', error)
    throw error
  }
}

// Rewards API
export const claimReward = async (walletAddress, amount, rewardType) => {
  try {
    const response = await axios.post(`${API_BASE_URL}/lll/rewards/claim`, {
      walletAddress,
      amount,
      rewardType
    })
    return response.data
  } catch (error) {
    console.error('Error claiming reward:', error)
    throw error
  }
}

export const processDailyLogin = async (walletAddress) => {
  try {
    const response = await axios.post(`${API_BASE_URL}/lll/rewards/daily-login`, {
      walletAddress
    })
    return response.data
  } catch (error) {
    console.error('Error processing daily login:', error)
    throw error
  }
}

// Staking Info API
export const getStakingInfo = async (walletAddress) => {
  try {
    const response = await axios.get(`${API_BASE_URL}/lll/staking/${walletAddress}`)
    return response.data
  } catch (error) {
    console.error('Error fetching staking info:', error)
    throw error
  }
}
