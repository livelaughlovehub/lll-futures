import React from 'react'
import { Coins, TrendingUp, Info } from 'lucide-react'

export default function TokenBalanceDisplay({ userBalance = 0, stakedAmount = 0 }) {
  const totalBalance = userBalance + stakedAmount
  
  return (
    <div className="bg-white rounded-lg shadow p-4">
      <h3 className="text-lg font-semibold mb-3 flex items-center">
        <Coins className="mr-2 text-blue-600" size={20} />
        Your LLL Tokens
      </h3>
      
      <div className="space-y-3">
        {/* Total Available for Trading */}
        <div className="bg-green-50 border border-green-200 rounded-lg p-3">
          <div className="flex justify-between items-center">
            <span className="text-green-700 font-medium">Available for Trading:</span>
            <span className="font-bold text-green-800 text-lg">{totalBalance.toFixed(2)} LLL</span>
          </div>
        </div>
        
        {/* Balance Breakdown */}
        <div className="text-sm text-gray-600 space-y-2">
          <div className="flex justify-between">
            <span>• Regular Balance:</span>
            <span className="font-medium">{userBalance.toFixed(2)} LLL</span>
          </div>
          <div className="flex justify-between">
            <span>• Staked (earning 10% APY):</span>
            <span className="font-medium text-blue-600">{stakedAmount.toFixed(2)} LLL</span>
          </div>
        </div>
      </div>
      
      {/* Info Box */}
      <div className="mt-4 p-3 bg-blue-50 rounded-lg">
        <div className="flex items-start">
          <Info className="text-blue-600 mr-2 mt-0.5 flex-shrink-0" size={16} />
          <div className="text-sm text-blue-700">
            <p className="font-medium mb-1">How it works:</p>
            <ul className="space-y-1 text-xs">
              <li>• Both balances can be used for trading</li>
              <li>• Staked tokens earn 10% APY rewards</li>
              <li>• System uses staked tokens first (they earn rewards anyway)</li>
              <li>• Winnings go to regular balance for flexibility</li>
            </ul>
          </div>
        </div>
      </div>
    </div>
  )
}
