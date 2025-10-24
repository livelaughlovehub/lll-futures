import { useState, useEffect } from 'react'
import { TrendingUp, Users, DollarSign, Activity } from 'lucide-react'
import { getActiveMarkets, getUserOpenOrders } from '../api/api'
import MarketCard from '../components/MarketCard'

export default function Dashboard({ currentUser }) {
  const [markets, setMarkets] = useState([])
  const [orders, setOrders] = useState([])
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    loadData()
  }, [currentUser])

  const loadData = async () => {
    if (!currentUser) return
    
    try {
      setLoading(true)
      const [marketsData, ordersData] = await Promise.all([
        getActiveMarkets(),
        getUserOpenOrders(currentUser.id)
      ])
      setMarkets(marketsData.slice(0, 3)) // Show top 3
      setOrders(ordersData)
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
      <div>
        <h1 className="text-2xl sm:text-3xl font-bold text-gray-900">Welcome to LL&L Futures</h1>
        <p className="text-sm sm:text-base text-gray-600 mt-1 sm:mt-2">Trade prediction markets with LL&L Tokens</p>
      </div>

      {/* Stats Cards */}
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

      {/* Featured Markets */}
      <div>
        <div className="flex items-center justify-between mb-3 sm:mb-4">
          <h2 className="text-xl sm:text-2xl font-bold text-gray-900">Featured Markets</h2>
          <a href="/markets" className="text-sm sm:text-base text-blue-600 hover:text-blue-700 font-medium">
            View All →
          </a>
        </div>
        {loading ? (
          <div className="text-center py-12">
            <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-600 mx-auto"></div>
          </div>
        ) : (
          <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-3 sm:gap-4">
            {markets.map(market => (
              <MarketCard key={market.id} market={market} currentUser={currentUser} onUpdate={loadData} />
            ))}
          </div>
        )}
      </div>
    </div>
  )
}

