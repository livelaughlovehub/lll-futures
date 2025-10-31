import React, { createContext, useContext, useState, useEffect } from 'react'
import { getTokenBalance, getStakingInfo } from '../api/solanaApi'
import { getAllUsers } from '../api/api'

const WalletContext = createContext()

export const useWallet = () => {
  const context = useContext(WalletContext)
  if (!context) {
    throw new Error('useWallet must be used within a WalletProvider')
  }
  return context
}

export const WalletProvider = ({ children }) => {
  const [connected, setConnected] = useState(false)
  const [publicKey, setPublicKey] = useState(null)
  const [tokenBalance, setTokenBalance] = useState(null)
  const [stakingInfo, setStakingInfo] = useState(null)
  const [currentUser, setCurrentUser] = useState(null)
  const [loading, setLoading] = useState(false)

  // Real wallet addresses for testing
  const mockWallets = [
    'DmpJsyNToL3i9cKoCZtT88nYLABdKNvfy2X8bpxDYZehs', // Admin wallet (100 LLL)
    '5M38wf2Uruu9cKoCZtT88nYLABdKNvfy2X8bpxDYZehs'  // User wallet (100 LLL)
  ]

  const connectWallet = async (walletAddress = null) => {
    setLoading(true)
    try {
      // Use provided address or select a random mock wallet
      const address = walletAddress || mockWallets[Math.floor(Math.random() * mockWallets.length)]
      
      setPublicKey(address)
      setConnected(true)
      
      // Find or create user for this wallet
      await findOrCreateUser(address)
      
      // Load token data
      await loadTokenData(address)
      
    } catch (error) {
      console.error('Error connecting wallet:', error)
    } finally {
      setLoading(false)
    }
  }

  const findOrCreateUser = async (walletAddress) => {
    try {
      // Get all users and find one with this wallet address
      const users = await getAllUsers()
      const existingUser = users.find(user => user.walletAddress === walletAddress)
      
      if (existingUser) {
        setCurrentUser(existingUser)
        console.log('Found existing user:', existingUser.username)
      } else {
        // Create a new user for this wallet
        const newUser = {
          id: Date.now(), // Temporary ID
          username: `Wallet_${walletAddress.slice(0, 8)}`,
          walletAddress: walletAddress,
          tokenBalance: 0, // Will be updated by loadTokenData
          isAdmin: false
        }
        setCurrentUser(newUser)
        console.log('Created new user for wallet:', walletAddress)
      }
    } catch (error) {
      console.error('Error finding/creating user:', error)
    }
  }

  const disconnectWallet = () => {
    setConnected(false)
    setPublicKey(null)
    setTokenBalance(null)
    setStakingInfo(null)
    setCurrentUser(null)
  }

  const loadTokenData = async (walletAddress) => {
    try {
      const [balance, staking] = await Promise.all([
        getTokenBalance(walletAddress),
        getStakingInfo(walletAddress)
      ])
      
      setTokenBalance(balance)
      setStakingInfo(staking)
    } catch (error) {
      console.error('Error loading token data:', error)
    }
  }

  const refreshTokenData = async () => {
    if (publicKey) {
      await loadTokenData(publicKey)
    }
  }

  const value = {
    connected,
    publicKey,
    tokenBalance,
    stakingInfo,
    currentUser,
    loading,
    connectWallet,
    disconnectWallet,
    refreshTokenData,
    mockWallets
  }

  return (
    <WalletContext.Provider value={value}>
      {children}
    </WalletContext.Provider>
  )
}
