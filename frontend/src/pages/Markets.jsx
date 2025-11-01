import { useState, useEffect } from 'react'
import { useSearchParams } from 'react-router-dom'
import { getActiveMarkets } from '../api/api'
import MarketCard from '../components/MarketCard'

export default function Markets() {
  const [markets, setMarkets] = useState([])
  const [loading, setLoading] = useState(true)
  const [searchParams] = useSearchParams()

  useEffect(() => {
    loadMarkets()
  }, [])

  // Scroll to specific market if URL has market parameter
  useEffect(() => {
    const marketId = searchParams.get('market')
    if (marketId && !loading && markets.length > 0) {
      // Small delay to ensure DOM is rendered
      setTimeout(() => {
        const element = document.getElementById(`market-${marketId}`)
        if (element) {
          element.scrollIntoView({ behavior: 'smooth', block: 'center' })
          // Highlight the card briefly
          element.classList.add('ring-4', 'ring-blue-500', 'ring-opacity-50', 'transition-all', 'duration-500')
          setTimeout(() => {
            element.classList.remove('ring-4', 'ring-blue-500', 'ring-opacity-50')
          }, 2000)
        }
      }, 300)
    }
  }, [loading, markets, searchParams])

  const loadMarkets = async () => {
    try {
      setLoading(true)
      const data = await getActiveMarkets()
      setMarkets(data)
    } catch (error) {
      console.error('Failed to load markets:', error)
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="space-y-4 sm:space-y-6">
      <div>
        <h1 className="text-2xl sm:text-3xl font-bold text-gray-900">Active Markets</h1>
        <p className="text-sm sm:text-base text-gray-600 mt-1 sm:mt-2">Browse and trade on active prediction markets</p>
      </div>

      {loading ? (
        <div className="text-center py-12">
          <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-600 mx-auto"></div>
        </div>
      ) : markets.length === 0 ? (
        <div className="bg-white rounded-lg shadow p-12 text-center">
          <p className="text-gray-500 text-lg">No active markets available</p>
        </div>
      ) : (
        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-3 sm:gap-4 md:gap-6">
          {markets.map(market => (
            <div id={`market-${market.id}`} key={market.id}>
              <MarketCard market={market} onUpdate={loadMarkets} />
            </div>
          ))}
        </div>
      )}
    </div>
  )
}

