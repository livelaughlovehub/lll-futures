import React, { useState } from 'react'
import { TrendingUp, TrendingDown, RefreshCw, AlertCircle } from 'lucide-react'
import { useWallet } from '../context/WalletContext'
import { stakeTokens, unstakeTokens } from '../api/solanaApi'

export default function StakingPanel() {
  const { connected, publicKey, tokenBalance, stakingInfo, refreshTokenData } = useWallet()
  const [stakeAmount, setStakeAmount] = useState('')
  const [unstakeAmount, setUnstakeAmount] = useState('')
  const [loading, setLoading] = useState(false)
  const [message, setMessage] = useState('')

  const handleStake = async () => {
    if (!connected || !publicKey) return
    
    setLoading(true)
    setMessage('')
    
    try {
      const amount = parseFloat(stakeAmount)
      if (amount <= 0) {
        throw new Error('Amount must be positive')
      }
      
      if (tokenBalance?.lllBalance < amount) {
        throw new Error('Insufficient LLL balance')
      }
      
      await stakeTokens(publicKey, amount)
      setStakeAmount('')
      setMessage(`Successfully staked ${amount} LLL tokens!`)
      
      // Refresh token data
      await refreshTokenData()
      
    } catch (error) {
      setMessage(`Error: ${error.message}`)
    } finally {
      setLoading(false)
    }
  }

  const handleUnstake = async () => {
    if (!connected || !publicKey) return
    
    setLoading(true)
    setMessage('')
    
    try {
      const amount = parseFloat(unstakeAmount)
      if (amount <= 0) {
        throw new Error('Amount must be positive')
      }
      
      if (stakingInfo?.stakedAmount < amount) {
        throw new Error('Insufficient staked amount')
      }
      
      await unstakeTokens(publicKey, amount)
      setUnstakeAmount('')
      setMessage(`Successfully unstaked ${amount} LLL tokens!`)
      
      // Refresh token data
      await refreshTokenData()
      
    } catch (error) {
      setMessage(`Error: ${error.message}`)
    } finally {
      setLoading(false)
    }
  }

  if (!connected) {
    return (
      <div className="bg-white rounded-lg shadow-lg p-6">
        <h3 className="text-lg font-bold mb-4 flex items-center">
          <TrendingUp className="mr-2 text-green-600" size={20} />
          Stake LLL Tokens
        </h3>
        <div className="text-center py-8">
          <AlertCircle className="mx-auto text-gray-400 mb-4" size={48} />
          <p className="text-gray-600 mb-4">Connect your wallet to start staking LLL tokens</p>
          <p className="text-sm text-gray-500">Optional: Earn 10% APY by staking your tokens</p>
        </div>
      </div>
    )
  }

  const estimatedRewards = stakingInfo?.stakedAmount ? stakingInfo.stakedAmount * 0.10 : 0

  return (
    <div className="bg-white rounded-lg shadow-lg p-6">
      <h3 className="text-lg font-bold mb-4 flex items-center">
        <TrendingUp className="mr-2 text-green-600" size={20} />
        Stake LLL Tokens
      </h3>
      
      {/* Staking Stats */}
      <div className="grid grid-cols-2 gap-4 mb-6">
        <div className="bg-green-50 border border-green-200 rounded-lg p-4">
          <div className="text-sm text-green-600 font-medium">Staked Amount</div>
          <div className="text-xl font-bold text-green-700">
            {stakingInfo?.stakedAmount?.toFixed(2) || '0.00'} LLL
          </div>
        </div>
        <div className="bg-blue-50 border border-blue-200 rounded-lg p-4">
          <div className="text-sm text-blue-600 font-medium">Estimated Rewards</div>
          <div className="text-xl font-bold text-blue-700">
            {estimatedRewards.toFixed(2)} LLL
          </div>
        </div>
      </div>

      {/* APY Info */}
      <div className="bg-blue-50 border border-blue-200 rounded-lg p-4 mb-6">
        <div className="flex items-center">
          <TrendingUp className="text-blue-600 mr-2" size={16} />
          <span className="text-sm text-blue-800">
            <strong>Optional:</strong> Stake tokens to earn 10% APY. 
            You can trade with unstaked tokens too!
          </span>
        </div>
      </div>

      {/* Message Display */}
      {message && (
        <div className={`mb-4 p-3 rounded-lg ${
          message.includes('Error') 
            ? 'bg-red-50 border border-red-200 text-red-800' 
            : 'bg-green-50 border border-green-200 text-green-800'
        }`}>
          {message}
        </div>
      )}

      {/* Stake Form */}
      <div className="space-y-4">
        <div>
          <label className="block text-sm font-medium text-gray-700 mb-2">
            Stake Amount (LLL)
          </label>
          <div className="flex space-x-2">
            <input
              type="number"
              value={stakeAmount}
              onChange={(e) => setStakeAmount(e.target.value)}
              className="flex-1 px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
              placeholder="Enter amount to stake"
              min="0.01"
              step="0.01"
            />
            <button
              onClick={handleStake}
              disabled={loading || !stakeAmount || parseFloat(stakeAmount) <= 0}
              className="px-4 py-2 bg-green-600 text-white rounded-lg hover:bg-green-700 disabled:bg-gray-400 flex items-center space-x-2"
            >
              <TrendingUp size={16} />
              <span>{loading ? 'Staking...' : 'Stake'}</span>
            </button>
          </div>
          <p className="text-xs text-gray-500 mt-1">
            Available: {tokenBalance?.lllBalance?.toFixed(2) || '0.00'} LLL
          </p>
        </div>
        
        <div>
          <label className="block text-sm font-medium text-gray-700 mb-2">
            Unstake Amount (LLL)
          </label>
          <div className="flex space-x-2">
            <input
              type="number"
              value={unstakeAmount}
              onChange={(e) => setUnstakeAmount(e.target.value)}
              className="flex-1 px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
              placeholder="Enter amount to unstake"
              min="0.01"
              step="0.01"
            />
            <button
              onClick={handleUnstake}
              disabled={loading || !unstakeAmount || parseFloat(unstakeAmount) <= 0}
              className="px-4 py-2 bg-red-600 text-white rounded-lg hover:bg-red-700 disabled:bg-gray-400 flex items-center space-x-2"
            >
              <TrendingDown size={16} />
              <span>{loading ? 'Unstaking...' : 'Unstake'}</span>
            </button>
          </div>
          <p className="text-xs text-gray-500 mt-1">
            Staked: {stakingInfo?.stakedAmount?.toFixed(2) || '0.00'} LLL
          </p>
        </div>
      </div>

      {/* Refresh Button */}
      <div className="mt-4 pt-4 border-t border-gray-200">
        <button
          onClick={refreshTokenData}
          className="flex items-center space-x-2 text-gray-600 hover:text-gray-800 transition"
        >
          <RefreshCw size={16} />
          <span className="text-sm">Refresh Data</span>
        </button>
      </div>
    </div>
  )
}
