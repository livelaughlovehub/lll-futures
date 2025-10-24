import { useState, useEffect } from 'react'
import { getUserById } from '../api/api'
import { Wallet as WalletIcon, TrendingUp, ArrowUpRight, ArrowDownRight } from 'lucide-react'

export default function Wallet({ currentUser }) {
  const [user, setUser] = useState(null)
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    loadUser()
  }, [currentUser])

  const loadUser = async () => {
    if (!currentUser) return
    
    try {
      setLoading(true)
      const data = await getUserById(currentUser.id)
      setUser(data)
    } catch (error) {
      console.error('Failed to load user:', error)
    } finally {
      setLoading(false)
    }
  }

  if (loading) {
    return (
      <div className="text-center py-12">
        <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-600 mx-auto"></div>
      </div>
    )
  }

  if (!user) {
    return <div>User not found</div>
  }

  return (
    <div className="space-y-4 sm:space-y-6">
      <div>
        <h1 className="text-2xl sm:text-3xl font-bold text-gray-900">Token Wallet</h1>
        <p className="text-sm sm:text-base text-gray-600 mt-1 sm:mt-2">Manage your LL&L Tokens</p>
      </div>

      {/* Balance Card */}
      <div className="bg-gradient-to-br from-blue-600 to-purple-600 rounded-lg shadow-xl p-4 sm:p-8 text-white">
        <div className="flex items-center justify-between mb-4 sm:mb-6">
          <div className="flex items-center space-x-2">
            <WalletIcon size={20} className="sm:w-6 sm:h-6" />
            <span className="text-base sm:text-lg font-medium">LL&L Token Balance</span>
          </div>
          <div className="bg-white/20 backdrop-blur-sm px-2 sm:px-3 py-1 rounded-full text-xs sm:text-sm">
            {user.username}
          </div>
        </div>
        <div>
          <p className="text-3xl sm:text-4xl font-bold mb-2">{user.tokenBalance.toFixed(2)} LLL</p>
          <p className="text-sm sm:text-base text-blue-100">Available for trading</p>
        </div>
      </div>

      {/* Stats Grid */}
      <div className="grid grid-cols-1 sm:grid-cols-3 gap-3 sm:gap-4">
        <div className="bg-white rounded-lg shadow p-4 sm:p-6">
          <div className="flex items-center justify-between">
            <div>
              <p className="text-xs sm:text-sm text-gray-600">Account Type</p>
              <p className="text-lg sm:text-xl font-bold text-gray-900 mt-1">
                {user.isAdmin ? 'Admin' : 'Standard'}
              </p>
            </div>
            <TrendingUp className="text-blue-500" size={28} />
          </div>
        </div>

        <div className="bg-white rounded-lg shadow p-4 sm:p-6">
          <div className="flex items-center justify-between">
            <div>
              <p className="text-xs sm:text-sm text-gray-600">Member Since</p>
              <p className="text-base sm:text-xl font-bold text-gray-900 mt-1">
                {new Date(user.createdAt).toLocaleDateString()}
              </p>
            </div>
            <ArrowUpRight className="text-green-500" size={28} />
          </div>
        </div>

        <div className="bg-white rounded-lg shadow p-4 sm:p-6">
          <div className="flex items-center justify-between">
            <div>
              <p className="text-xs sm:text-sm text-gray-600">Token Symbol</p>
              <p className="text-lg sm:text-xl font-bold text-gray-900 mt-1">LLL</p>
            </div>
            <ArrowDownRight className="text-purple-500" size={28} />
          </div>
        </div>
      </div>

      {/* Info Section */}
      <div className="bg-white rounded-lg shadow p-4 sm:p-6">
        <h2 className="text-lg sm:text-xl font-bold text-gray-900 mb-3 sm:mb-4">About LL&L Token</h2>
        <div className="space-y-2 sm:space-y-3 text-sm sm:text-base text-gray-600">
          <p>
            LL&L Token (LLL) is the native currency of the LL&L Futures platform. Use it to:
          </p>
          <ul className="list-disc list-inside space-y-1 sm:space-y-2 ml-2 sm:ml-4 text-sm sm:text-base">
            <li>Place bets on prediction markets</li>
            <li>Create new markets (requires tokens for market creation)</li>
            <li>Earn rewards from successful predictions</li>
          </ul>
          <div className="bg-blue-50 border border-blue-200 rounded-lg p-3 sm:p-4 mt-3 sm:mt-4">
            <p className="text-xs sm:text-sm text-blue-800 font-medium">
              🚀 Future Enhancement: LL&L Token will be migrated to Solana blockchain, 
              enabling real token ownership, Web3 wallet integration, and decentralized trading.
            </p>
          </div>
        </div>
      </div>
    </div>
  )
}

