import { useState } from 'react'
import { TrendingUp, TrendingDown, Clock, DollarSign } from 'lucide-react'
import { placeOrder } from '../api/api'
import { useAuth } from '../context/AuthContext'

export default function MarketCard({ market, onUpdate }) {
  const { currentUser } = useAuth()
  const connected = !!currentUser
  // For now, we'll use platform tokens (no external wallet needed)
  const publicKey = currentUser?.walletAddress || null
  const totalBalance = currentUser?.tokenBalance || 0
  const stakingInfo = { 
    stakedAmount: 0, // We'll get this from API later
    totalAvailable: totalBalance 
  }
  const [showBetModal, setShowBetModal] = useState(false)
  const [betSide, setBetSide] = useState(null)
  const [betAmount, setBetAmount] = useState('')
  const [loading, setLoading] = useState(false)

  const handlePlaceBet = async (side) => {
    if (!connected) {
      alert('Please sign in to place bets')
      return
    }
    setBetSide(side)
    setShowBetModal(true)
  }

  const submitBet = async () => {
    if (!currentUser || !betAmount || parseFloat(betAmount) <= 0 || !connected) {
      return
    }

    // Check if user has sufficient total balance
    if (parseFloat(betAmount) > totalBalance) {
      alert(`Insufficient balance. You have ${totalBalance.toFixed(2)} LLL available.`)
      return
    }

    setLoading(true)
    try {
      await placeOrder({
        userId: currentUser.id,
        walletAddress: publicKey || currentUser.username, // Use username as fallback if no wallet
        marketId: market.id,
        side: betSide,
        stakeAmount: parseFloat(betAmount)
      })
      
      setShowBetModal(false)
      setBetAmount('')
      alert('Bet placed successfully!')
      
      // Refresh data without full page reload
      if (onUpdate) onUpdate()
      
    } catch (error) {
      console.error('Order placement error:', error)
      alert('Failed to place bet: ' + (error.response?.data?.message || error.message || 'Unknown error'))
    } finally {
      setLoading(false)
    }
  }

  const odds = betSide === 'YES' ? market.yesOdds : market.noOdds
  const potentialPayout = betAmount ? (parseFloat(betAmount) * odds).toFixed(2) : '0.00'

  return (
    <>
      <div className="bg-white rounded-lg shadow hover:shadow-lg transition p-6">
        <div className="mb-4">
          <h3 className="text-lg font-semibold text-gray-900 mb-2">{market.title}</h3>
          <p className="text-sm text-gray-600 line-clamp-2">{market.description}</p>
        </div>

        <div className="space-y-3 mb-4">
          <div className="flex items-center justify-between text-sm">
            <span className="text-gray-600">Total Volume</span>
            <span className="font-semibold">{market.totalVolume.toFixed(2)} LLL</span>
          </div>
          <div className="flex items-center justify-between text-sm">
            <span className="text-gray-600">Expires</span>
            <span className="flex items-center space-x-1">
              <Clock size={14} />
              <span>{new Date(market.expiryDate).toLocaleDateString()}</span>
            </span>
          </div>
        </div>

        <div className="grid grid-cols-2 gap-3">
          <button
            onClick={() => handlePlaceBet('YES')}
            disabled={!connected}
            className={`flex items-center justify-center space-x-2 px-4 py-3 rounded-lg font-medium transition ${
              connected 
                ? 'bg-green-50 hover:bg-green-100 text-green-700' 
                : 'bg-gray-100 text-gray-400 cursor-not-allowed'
            }`}
          >
            <TrendingUp size={18} />
            <div className="text-left">
              <div className="text-xs">YES</div>
              <div className="font-bold">{market.yesOdds.toFixed(2)}x</div>
            </div>
          </button>
          
          <button
            onClick={() => handlePlaceBet('NO')}
            disabled={!connected}
            className={`flex items-center justify-center space-x-2 px-4 py-3 rounded-lg font-medium transition ${
              connected 
                ? 'bg-red-50 hover:bg-red-100 text-red-700' 
                : 'bg-gray-100 text-gray-400 cursor-not-allowed'
            }`}
          >
            <TrendingDown size={18} />
            <div className="text-left">
              <div className="text-xs">NO</div>
              <div className="font-bold">{market.noOdds.toFixed(2)}x</div>
            </div>
          </button>
        </div>
        
        {!connected && (
          <div className="mt-3 p-3 bg-yellow-50 border border-yellow-200 rounded-lg">
            <p className="text-sm text-yellow-800 text-center">
              🔗 Connect your wallet to place bets
            </p>
          </div>
        )}
      </div>

      {/* Bet Modal - Mobile Optimized */}
      {showBetModal && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center p-2 sm:p-4 z-50 overflow-y-auto">
          <div className="bg-white rounded-lg max-w-md w-full p-4 sm:p-6 my-4">
            <h3 className="text-lg sm:text-xl font-bold text-gray-900 mb-3 sm:mb-4">
              Place {betSide} Bet
            </h3>
            
            <div className="mb-3 sm:mb-4">
              <p className="text-xs sm:text-sm text-gray-600 mb-2 line-clamp-2">{market.title}</p>
              <div className={`p-2 sm:p-3 rounded-lg ${betSide === 'YES' ? 'bg-green-50' : 'bg-red-50'}`}>
                <p className="font-semibold text-sm sm:text-base">Odds: {odds.toFixed(2)}x</p>
              </div>
            </div>

            <div className="mb-3 sm:mb-4">
              <label className="block text-sm font-medium text-gray-700 mb-2">
                Bet Amount (LLL)
              </label>
              <input
                type="number"
                value={betAmount}
                onChange={(e) => setBetAmount(e.target.value)}
                step="0.01"
                min="0.01"
                max={totalBalance}
                placeholder="Enter amount"
                inputMode="decimal"
                className="w-full px-3 sm:px-4 py-3 text-base border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
              />
              <p className="text-xs text-gray-500 mt-1">
                Available Balance: {totalBalance.toFixed(2)} LLL
              </p>
            </div>

            <div className="bg-blue-50 border border-blue-200 rounded-lg p-3 sm:p-4 mb-3 sm:mb-4">
              <div className="flex items-center justify-between text-sm mb-2">
                <span className="text-gray-600">Stake:</span>
                <span className="font-semibold">{betAmount || '0.00'} LLL</span>
              </div>
              <div className="flex items-center justify-between text-sm sm:text-base">
                <span className="text-gray-600">Potential Payout:</span>
                <span className="font-bold text-blue-600">{potentialPayout} LLL</span>
              </div>
            </div>

            <div className="flex flex-col sm:flex-row gap-2 sm:gap-3">
              <button
                onClick={submitBet}
                disabled={loading || !betAmount || parseFloat(betAmount) <= 0}
                className="flex-1 bg-blue-600 text-white px-4 py-3 rounded-lg font-medium hover:bg-blue-700 disabled:bg-gray-400 text-base"
              >
                {loading ? 'Placing...' : 'Confirm Bet'}
              </button>
              <button
                onClick={() => {
                  setShowBetModal(false)
                  setBetAmount('')
                }}
                className="px-4 py-3 border border-gray-300 rounded-lg font-medium hover:bg-gray-50 text-base"
              >
                Cancel
              </button>
            </div>
          </div>
        </div>
      )}
    </>
  )
}

