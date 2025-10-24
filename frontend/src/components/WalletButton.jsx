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
              <h3 className="text-lg font-semibold text-gray-900 mb-3">Select Demo Wallet</h3>
              <div className="space-y-2">
                {mockWallets.map((wallet, index) => (
                  <button
                    key={wallet}
                    onClick={() => handleSelectWallet(wallet)}
                    className="w-full text-left p-3 rounded-lg border border-gray-200 hover:bg-gray-50 transition"
                  >
                    <div className="font-medium text-sm text-gray-900">
                      Demo Wallet {index + 1}
                    </div>
                    <div className="text-xs text-gray-500 font-mono">
                      {wallet.slice(0, 8)}...{wallet.slice(-8)}
                    </div>
                  </button>
                ))}
              </div>
              <button
                onClick={() => setShowWalletSelector(false)}
                className="w-full mt-3 px-4 py-2 text-gray-600 hover:text-gray-800 transition"
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
    <div className="flex items-center space-x-3">
      {/* Token Balance Display */}
      <div className="bg-gradient-to-r from-yellow-400 to-yellow-500 text-white px-3 py-2 rounded-lg">
        <div className="flex items-center space-x-4">
          <div>
            <div className="text-xs">LLL Balance</div>
            <div className="font-bold text-sm">
              {tokenBalance?.lllBalance?.toFixed(2) || '0.00'} LLL
            </div>
          </div>
          <div>
            <div className="text-xs">Staked</div>
            <div className="font-bold text-sm">
              {stakingInfo?.stakedAmount?.toFixed(2) || '0.00'} LLL
            </div>
          </div>
        </div>
      </div>

      {/* Wallet Info */}
      <div className="text-sm text-gray-600">
        <div className="font-mono">
          {publicKey?.slice(0, 8)}...{publicKey?.slice(-8)}
        </div>
      </div>

      {/* Refresh Button */}
      <button
        onClick={handleRefresh}
        className="p-2 text-gray-600 hover:text-gray-800 hover:bg-gray-100 rounded-lg transition"
        title="Refresh token data"
      >
        <RefreshCw size={16} />
      </button>

      {/* Disconnect Button */}
      <button
        onClick={disconnectWallet}
        className="p-2 text-gray-600 hover:text-red-600 hover:bg-red-50 rounded-lg transition"
        title="Disconnect wallet"
      >
        <LogOut size={16} />
      </button>
    </div>
  )
}
