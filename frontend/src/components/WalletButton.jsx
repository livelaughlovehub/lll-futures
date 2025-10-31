import React, { useState } from 'react'
import { Wallet, LogOut, RefreshCw } from 'lucide-react'
import { useWallet } from '../context/WalletContext'

export default function WalletButton() {
  const { 
    connected, 
    publicKey, 
    tokenBalance, 
    stakingInfo, 
    loading, 
    connectWallet, 
    disconnectWallet, 
    refreshTokenData,
    mockWallets 
  } = useWallet()
  
  const [showWalletSelector, setShowWalletSelector] = useState(false)
  const [customWallet, setCustomWallet] = useState('')

  const handleConnect = () => {
    setShowWalletSelector(true)
  }

  const handleSelectWallet = (walletAddress) => {
    connectWallet(walletAddress)
    setShowWalletSelector(false)
  }

  const handleRefresh = async () => {
    await refreshTokenData()
  }

  if (!connected) {
    return (
      <div className="relative">
        <button
          onClick={handleConnect}
          disabled={loading}
          className="bg-gradient-to-r from-purple-600 to-blue-600 text-white px-4 py-2 rounded-lg font-medium hover:from-purple-700 hover:to-blue-700 disabled:opacity-50 flex items-center space-x-2"
        >
          <Wallet size={20} />
          <span>{loading ? 'Connecting...' : 'Connect Wallet'}</span>
        </button>

        {showWalletSelector && (
          <div className="absolute top-full right-0 mt-2 w-80 bg-white rounded-lg shadow-lg border border-gray-200 z-50">
            <div className="p-4">
              <h3 className="text-lg font-semibold text-gray-900 mb-3">Connect Wallet</h3>
              
              {/* Manual Wallet Input */}
              <div className="mb-4">
                <label className="block text-sm font-medium text-gray-700 mb-2">
                  Enter Your Wallet Address
                </label>
                <input
                  type="text"
                  placeholder="DmpJsyNToL3i9cKoCZtT88nYLABdKNvfy2X8bpxDYZehs"
                  value={customWallet}
                  onChange={(e) => setCustomWallet(e.target.value)}
                  className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 text-sm"
                />
                <button
                  onClick={() => {
                    if (customWallet.trim()) {
                      handleSelectWallet(customWallet.trim())
                    }
                  }}
                  disabled={!customWallet.trim()}
                  className="w-full mt-2 px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 disabled:opacity-50 disabled:cursor-not-allowed text-sm font-medium"
                >
                  Connect Custom Wallet
                </button>
              </div>
              
              <div className="border-t pt-3">
                <p className="text-sm text-gray-600 mb-2">Or use demo wallets:</p>
                <div className="space-y-2">
                  {/* Admin Wallet */}
                  <button
                    onClick={() => handleSelectWallet(mockWallets[0])}
                    className="w-full text-left p-3 rounded-lg border border-gray-200 hover:bg-gray-50 transition"
                  >
                    <div className="font-medium text-sm text-gray-900">Admin Wallet</div>
                    <div className="text-xs text-gray-500 font-mono">
                      {mockWallets[0].slice(0, 8)}...{mockWallets[0].slice(-8)} (100 LLL)
                    </div>
                  </button>
                  
                  {/* User Wallet */}
                  <button
                    onClick={() => handleSelectWallet(mockWallets[1])}
                    className="w-full text-left p-3 rounded-lg border border-gray-200 hover:bg-gray-50 transition"
                  >
                    <div className="font-medium text-sm text-gray-900">User Wallet</div>
                    <div className="text-xs text-gray-500 font-mono">
                      {mockWallets[1].slice(0, 8)}...{mockWallets[1].slice(-8)} (100 LLL)
                    </div>
                  </button>
                </div>
              </div>
              
              <button
                onClick={() => {
                  setShowWalletSelector(false)
                  setCustomWallet('')
                }}
                className="w-full mt-3 px-4 py-2 text-gray-600 hover:text-gray-800 transition text-sm"
              >
                Cancel
              </button>
            </div>
          </div>
        )}
      </div>
    )
  }

  return (
    <div className="flex items-center space-x-2">
      {/* Compact Token Balance Display */}
      <div className="bg-gradient-to-r from-yellow-400 to-yellow-500 text-white px-2 py-1 rounded-lg text-xs">
        <div className="flex items-center space-x-2">
          <span>{tokenBalance?.lllBalance?.toFixed(0) || '0'} LLL</span>
          <span className="opacity-75">|</span>
          <span>{stakingInfo?.stakedAmount?.toFixed(0) || '0'} Staked</span>
        </div>
      </div>

      {/* Compact Wallet Address */}
      <div className="text-xs text-gray-600 font-mono">
        {publicKey?.slice(0, 4)}...{publicKey?.slice(-4)}
      </div>

      {/* Action Buttons - Smaller */}
      <div className="flex items-center space-x-1">
        <button
          onClick={handleRefresh}
          className="p-1 text-gray-600 hover:text-gray-800 hover:bg-gray-100 rounded transition"
          title="Refresh"
        >
          <RefreshCw size={14} />
        </button>
        <button
          onClick={disconnectWallet}
          className="p-1 text-gray-600 hover:text-red-600 hover:bg-red-50 rounded transition"
          title="Disconnect"
        >
          <LogOut size={14} />
        </button>
      </div>
    </div>
  )
}
