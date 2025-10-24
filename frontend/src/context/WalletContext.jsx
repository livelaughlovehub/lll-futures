import React, { createContext, useContext, useState, useEffect } from 'react'
import { getTokenBalance, getStakingInfo } from '../api/solanaApi'

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
  const [loading, setLoading] = useState(false)

  // Mock wallet addresses for demo
  const mockWallets = [
    'DemoWallet1ABC123456789',
    'DemoWallet2DEF987654321',
    'DemoWallet3GHI456789123',
    'DemoWallet4JKL789123456'
  ]

  const connectWallet = async (walletAddress = null) => {
    setLoading(true)
    try {
      // Use provided address or select a random mock wallet
      const address = walletAddress || mockWallets[Math.floor(Math.random() * mockWallets.length)]
      
      setPublicKey(address)
      setConnected(true)
      
      // Load token data
      await loadTokenData(address)
      
    } catch (error) {
      console.error('Error connecting wallet:', error)
    } finally {
      setLoading(false)
    }
  }

  const disconnectWallet = () => {
    setConnected(false)
    setPublicKey(null)
    setTokenBalance(null)
    setStakingInfo(null)
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
