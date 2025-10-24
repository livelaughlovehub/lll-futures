import { useState, useEffect } from 'react'
import { getUserOrders } from '../api/api'
import { TrendingUp, TrendingDown, Clock, CheckCircle } from 'lucide-react'

export default function MyPositions({ currentUser }) {
  const [orders, setOrders] = useState([])
  const [loading, setLoading] = useState(true)
  const [filter, setFilter] = useState('all') // all, open, settled

  useEffect(() => {
    loadOrders()
  }, [currentUser])

  const loadOrders = async () => {
    if (!currentUser) return
    
    try {
      setLoading(true)
      const data = await getUserOrders(currentUser.id)
      setOrders(data)
    } catch (error) {
      console.error('Failed to load orders:', error)
    } finally {
      setLoading(false)
    }
  }

  const filteredOrders = orders.filter(order => {
    if (filter === 'open') return order.status === 'OPEN'
    if (filter === 'settled') return order.status === 'SETTLED'
    return true
  })

  const getStatusBadge = (status) => {
    const badges = {
      OPEN: { color: 'bg-blue-100 text-blue-800', icon: Clock },
      SETTLED: { color: 'bg-green-100 text-green-800', icon: CheckCircle },
      CANCELLED: { color: 'bg-gray-100 text-gray-800', icon: Clock },
    }
    const badge = badges[status] || badges.OPEN
    const Icon = badge.icon
    return (
      <span className={`inline-flex items-center space-x-1 px-2 py-1 rounded-full text-xs font-medium ${badge.color}`}>
        <Icon size={12} />
        <span>{status}</span>
      </span>
    )
  }

  return (
    <div className="space-y-4 sm:space-y-6">
      <div>
        <h1 className="text-2xl sm:text-3xl font-bold text-gray-900">My Positions</h1>
        <p className="text-sm sm:text-base text-gray-600 mt-1 sm:mt-2">Track your bets and winnings</p>
      </div>

      {/* Filter Tabs */}
      <div className="flex space-x-1 sm:space-x-2 border-b overflow-x-auto">
        {['all', 'open', 'settled'].map(f => (
          <button
            key={f}
            onClick={() => setFilter(f)}
            className={`px-3 sm:px-4 py-2 font-medium capitalize transition text-sm sm:text-base whitespace-nowrap ${
              filter === f
                ? 'border-b-2 border-blue-600 text-blue-600'
                : 'text-gray-600 hover:text-gray-900'
            }`}
          >
            {f} ({orders.filter(o => f === 'all' || o.status === f.toUpperCase()).length})
          </button>
        ))}
      </div>

      {loading ? (
        <div className="text-center py-12">
          <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-600 mx-auto"></div>
        </div>
      ) : filteredOrders.length === 0 ? (
        <div className="bg-white rounded-lg shadow p-12 text-center">
          <p className="text-gray-500 text-lg">No positions found</p>
        </div>
      ) : (
        <div className="space-y-3 sm:space-y-4">
          {filteredOrders.map(order => (
            <div key={order.id} className="bg-white rounded-lg shadow p-4 sm:p-6">
              <div className="flex items-start justify-between">
                <div className="flex-1">
                  <div className="flex flex-col sm:flex-row sm:items-center space-y-2 sm:space-y-0 sm:space-x-2 mb-3 sm:mb-2">
                    <h3 className="text-base sm:text-lg font-semibold text-gray-900 line-clamp-2">{order.marketTitle}</h3>
                    {getStatusBadge(order.status)}
                  </div>
                  <div className="grid grid-cols-2 md:grid-cols-4 gap-3 sm:gap-4 mt-3 sm:mt-4">
                    <div>
                      <p className="text-xs sm:text-sm text-gray-600">Position</p>
                      <div className="flex items-center space-x-1 mt-1">
                        {order.side === 'YES' ? (
                          <TrendingUp className="text-green-500" size={14} />
                        ) : (
                          <TrendingDown className="text-red-500" size={14} />
                        )}
                        <span className={`font-semibold text-sm sm:text-base ${order.side === 'YES' ? 'text-green-600' : 'text-red-600'}`}>
                          {order.side}
                        </span>
                      </div>
                    </div>
                    <div>
                      <p className="text-xs sm:text-sm text-gray-600">Stake</p>
                      <p className="font-semibold text-sm sm:text-base text-gray-900 mt-1">{order.stakeAmount.toFixed(0)} LLL</p>
                    </div>
                    <div>
                      <p className="text-xs sm:text-sm text-gray-600">Odds</p>
                      <p className="font-semibold text-sm sm:text-base text-gray-900 mt-1">{order.odds.toFixed(2)}x</p>
                    </div>
                    <div>
                      <p className="text-xs sm:text-sm text-gray-600">
                        {order.status === 'SETTLED' ? 'Payout' : 'Potential'}
                      </p>
                      <p className="font-semibold text-sm sm:text-base text-blue-600 mt-1">
                        {order.status === 'SETTLED' 
                          ? `${order.settledAmount.toFixed(0)} LLL`
                          : `${order.potentialPayout.toFixed(0)} LLL`
                        }
                      </p>
                    </div>
                  </div>
                  <div className="mt-3 sm:mt-4 text-xs sm:text-sm text-gray-500">
                    Placed on {new Date(order.createdAt).toLocaleDateString()}
                  </div>
                </div>
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  )
}

