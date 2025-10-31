import React from 'react'
import { TrendingUp, Wallet, RefreshCw } from 'lucide-react'
import { useWallet } from '../context/WalletContext'
import StakingPanel from '../components/StakingPanel'

export default function StakingPage() {
  const { connected, publicKey, tokenBalance, stakingInfo, refreshTokenData } = useWallet()

  return (
    <div className="max-w-4xl mx-auto">
      <div className="mb-8">
        <h1 className="text-3xl font-bold text-gray-900 mb-2 flex items-center">
          <TrendingUp className="mr-3 text-green-600" size={32} />
          Staking Dashboard
        </h1>
        <p className="text-gray-600">
          Optional: Stake your LLL tokens to earn 10% APY. You can trade without staking!
        </p>
      </div>

      {!connected ? (
        <div className="bg-white rounded-lg shadow-lg p-8 text-center">
          <Wallet className="mx-auto text-gray-400 mb-4" size={64} />
          <h2 className="text-xl font-semibold text-gray-900 mb-2">Connect Your Wallet</h2>
          <p className="text-gray-600 mb-6">
            Connect your wallet to start staking LLL tokens and earning rewards
          </p>
          <div className="bg-blue-50 border border-blue-200 rounded-lg p-4 max-w-md mx-auto">
            <h3 className="font-semibold text-blue-900 mb-2">How Staking Works</h3>
            <ul className="text-sm text-blue-800 text-left space-y-1">
              <li>• Optional: Stake LLL tokens to earn 10% APY</li>
              <li>• Trade immediately with unstaked tokens</li>
              <li>• Earn additional rewards for trading activity</li>
              <li>• Unstake anytime to reclaim your tokens</li>
            </ul>
          </div>
        </div>
      ) : (
        <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
          {/* Staking Panel */}
          <div className="lg:col-span-1">
            <StakingPanel />
          </div>

          {/* Portfolio Overview */}
          <div className="lg:col-span-1">
            <div className="bg-white rounded-lg shadow-lg p-6">
              <h3 className="text-lg font-bold mb-4 flex items-center">
                <RefreshCw className="mr-2 text-blue-600" size={20} />
                Portfolio Overview
              </h3>
              
              <div className="space-y-4">
                <div className="bg-gray-50 rounded-lg p-4">
                  <div className="text-sm text-gray-600">Wallet Address</div>
                  <div className="font-mono text-sm text-gray-900 break-all">
                    {publicKey}
                  </div>
                </div>

                <div className="grid grid-cols-2 gap-4">
                  <div className="bg-green-50 border border-green-200 rounded-lg p-4">
                    <div className="text-sm text-green-600 font-medium">Total LLL Balance</div>
                    <div className="text-xl font-bold text-green-700">
                      {tokenBalance?.lllBalance?.toFixed(2) || '0.00'} LLL
                    </div>
                  </div>
                  <div className="bg-blue-50 border border-blue-200 rounded-lg p-4">
                    <div className="text-sm text-blue-600 font-medium">Staked Amount</div>
                    <div className="text-xl font-bold text-blue-700">
                      {stakingInfo?.stakedAmount?.toFixed(2) || '0.00'} LLL
                    </div>
                  </div>
                </div>

                <div className="grid grid-cols-2 gap-4">
                  <div className="bg-yellow-50 border border-yellow-200 rounded-lg p-4">
                    <div className="text-sm text-yellow-600 font-medium">Total Earned</div>
                    <div className="text-xl font-bold text-yellow-700">
                      {tokenBalance?.totalEarned?.toFixed(2) || '0.00'} LLL
                    </div>
                  </div>
                  <div className="bg-purple-50 border border-purple-200 rounded-lg p-4">
                    <div className="text-sm text-purple-600 font-medium">APY</div>
                    <div className="text-xl font-bold text-purple-700">10%</div>
                  </div>
                </div>

                <div className="bg-gradient-to-r from-green-50 to-blue-50 border border-green-200 rounded-lg p-4">
                  <div className="text-sm text-gray-600 font-medium">Estimated Annual Rewards</div>
                  <div className="text-lg font-bold text-gray-900">
                    {stakingInfo?.stakedAmount ? (stakingInfo.stakedAmount * 0.10).toFixed(2) : '0.00'} LLL
                  </div>
                </div>
              </div>

              <div className="mt-6 pt-4 border-t border-gray-200">
                <button
                  onClick={refreshTokenData}
                  className="w-full flex items-center justify-center space-x-2 px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition"
                >
                  <RefreshCw size={16} />
                  <span>Refresh Data</span>
                </button>
              </div>
            </div>
          </div>
        </div>
      )}
    </div>
  )
}
