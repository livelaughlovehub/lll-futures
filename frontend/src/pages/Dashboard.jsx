import { useState, useEffect } from 'react'
import { Link } from 'react-router-dom'
import { TrendingUp, Users, DollarSign, Activity } from 'lucide-react'
import { getActiveMarkets, getUserOpenOrders } from '../api/api'
import MarketCard from '../components/MarketCard'
import { useAuth } from '../context/AuthContext'

export default function Dashboard() {
  const [markets, setMarkets] = useState([])
  const [orders, setOrders] = useState([])
  const [loading, setLoading] = useState(true)
  const { currentUser } = useAuth()
  const connected = !!currentUser  // Convert to boolean for consistency

  useEffect(() => {
    loadData()
  }, [currentUser])

  const loadData = async () => {
    try {
      setLoading(true)
      
      // Always load markets (for guest viewing)
      const marketsData = await getActiveMarkets()
      setMarkets(marketsData.slice(0, 3)) // Show top 3
      
      // Only load orders if user is connected
      if (currentUser) {
        const ordersData = await getUserOpenOrders(currentUser.id)
        setOrders(ordersData)
      } else {
        setOrders([])
      }
    } catch (error) {
      console.error('Failed to load dashboard data:', error)
    } finally {
      setLoading(false)
    }
  }

  const totalStaked = orders.reduce((sum, order) => sum + order.stakeAmount, 0)
  const potentialWinnings = orders.reduce((sum, order) => sum + order.potentialPayout, 0)

  return (
    <div className="space-y-4 sm:space-y-6">
      {/* Wallet Connection Prompt */}
      {!connected && (
        <div className="bg-gradient-to-r from-blue-50 to-purple-50 border border-blue-200 rounded-lg p-6 text-center">
          <div className="flex items-center justify-center mb-4">
            <div className="bg-blue-100 rounded-full p-3">
              <TrendingUp className="text-blue-600" size={32} />
            </div>
          </div>
          <h2 className="text-xl font-semibold text-gray-900 mb-2">Join LL&L Futures</h2>
          <p className="text-gray-600 mb-4">
            Sign up to get 50 free LLL tokens, or sign in to your existing account
          </p>
          <div className="flex flex-col sm:flex-row gap-3 justify-center">
            <Link
              to="/signup"
              className="inline-flex items-center justify-center px-6 py-3 bg-gradient-to-r from-blue-600 to-purple-600 text-white rounded-lg hover:from-blue-700 hover:to-purple-700 transition-all duration-200 font-medium"
            >
              Sign Up - Get 50 LLL Tokens
            </Link>
            <Link
              to="/signin"
              className="inline-flex items-center justify-center px-6 py-3 border border-gray-300 text-gray-700 rounded-lg hover:bg-gray-50 transition-all duration-200 font-medium"
            >
              Sign In
            </Link>
          </div>
          <p className="text-sm text-gray-500 mt-3">
            Or connect your Solana wallet using the wallet button above
          </p>
        </div>
      )}

      {/* Always show markets - encourages engagement */}
      <div>
        <h1 className="text-2xl sm:text-3xl font-bold text-gray-900">
          {connected ? 'Welcome to LL&L Futures' : 'LL&L Futures Markets'}
        </h1>
        <p className="text-sm sm:text-base text-gray-600 mt-1 sm:mt-2">
          {connected ? 'Trade prediction markets with LL&L Tokens' : 'View active prediction markets - Connect wallet to trade'}
        </p>
      </div>

      {/* Stats Cards - Only show when connected */}
      {connected && (
        <>
          <div className="grid grid-cols-2 md:grid-cols-4 gap-3 sm:gap-4">
        <div className="bg-white p-3 sm:p-6 rounded-lg shadow">
          <div className="flex items-center justify-between">
            <div>
              <p className="text-xs sm:text-sm text-gray-600">Token Balance</p>
              <p className="text-lg sm:text-2xl font-bold text-gray-900 mt-1">
                {currentUser?.tokenBalance.toFixed(0) || '0'}
              </p>
            </div>
            <DollarSign className="text-yellow-500" size={32} />
          </div>
        </div>

        <div className="bg-white p-3 sm:p-6 rounded-lg shadow">
          <div className="flex items-center justify-between">
            <div>
              <p className="text-xs sm:text-sm text-gray-600">Open Positions</p>
              <p className="text-lg sm:text-2xl font-bold text-gray-900 mt-1">{orders.length}</p>
            </div>
            <Activity className="text-blue-500" size={32} />
          </div>
        </div>

        <div className="bg-white p-3 sm:p-6 rounded-lg shadow">
          <div className="flex items-center justify-between">
            <div>
              <p className="text-xs sm:text-sm text-gray-600">Total Staked</p>
              <p className="text-lg sm:text-2xl font-bold text-gray-900 mt-1">{totalStaked.toFixed(0)}</p>
            </div>
            <TrendingUp className="text-green-500" size={32} />
          </div>
        </div>

        <div className="bg-white p-3 sm:p-6 rounded-lg shadow">
          <div className="flex items-center justify-between">
            <div>
              <p className="text-xs sm:text-sm text-gray-600">Potential Win</p>
              <p className="text-lg sm:text-2xl font-bold text-gray-900 mt-1">{potentialWinnings.toFixed(0)}</p>
            </div>
            <Users className="text-purple-500" size={32} />
          </div>
        </div>
      </div>
        </>
      )}

      {/* Featured Markets - Always visible to encourage engagement */}
      <div>
        <div className="flex items-center justify-between mb-3 sm:mb-4">
          <h2 className="text-xl sm:text-2xl font-bold text-gray-900">Featured Markets</h2>
          <Link to="/markets" className="text-sm sm:text-base text-blue-600 hover:text-blue-700 font-medium">
            View All →
          </Link>
        </div>
        {loading ? (
          <div className="text-center py-12">
            <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-600 mx-auto"></div>
          </div>
        ) : (
          <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-3 sm:gap-4">
            {markets.map(market => (
              <MarketCard key={market.id} market={market} onUpdate={loadData} />
            ))}
          </div>
        )}
      </div>
    </div>
  )
}

