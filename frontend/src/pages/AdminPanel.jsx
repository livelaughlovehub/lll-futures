import { useState, useEffect } from 'react'
import { getAllMarkets, settleMarket, getVault } from '../api/api'
import { Shield, CheckCircle, XCircle, Ban, Wallet } from 'lucide-react'

export default function AdminPanel({ currentUser }) {
  const [markets, setMarkets] = useState([])
  const [loading, setLoading] = useState(true)
  const [settlingMarketId, setSettlingMarketId] = useState(null)
  const [vaultInfo, setVaultInfo] = useState(null)
  const [vaultLoading, setVaultLoading] = useState(true)

  useEffect(() => {
    loadMarkets()
    loadVaultInfo()
  }, [])

  const loadVaultInfo = async () => {
    try {
      setVaultLoading(true)
      const data = await getVault()
      setVaultInfo(data)
    } catch (error) {
      console.error('Failed to load vault info:', error)
    } finally {
      setVaultLoading(false)
    }
  }

  const loadMarkets = async () => {
    try {
      setLoading(true)
      const data = await getAllMarkets()
      setMarkets(data)
    } catch (error) {
      console.error('Failed to load markets:', error)
    } finally {
      setLoading(false)
    }
  }

  const handleSettleMarket = async (marketId, outcome) => {
    if (!confirm(`Are you sure you want to settle this market as ${outcome}?`)) {
      return
    }

    try {
      setSettlingMarketId(marketId)
      await settleMarket({ marketId, outcome })
      await loadMarkets()
      alert('Market settled successfully!')
    } catch (error) {
      alert('Failed to settle market: ' + (error.response?.data?.message || error.message))
    } finally {
      setSettlingMarketId(null)
    }
  }

  if (!currentUser?.isAdmin) {
    return (
      <div className="max-w-2xl mx-auto">
        <div className="bg-red-50 border border-red-200 rounded-lg p-12 text-center">
          <Shield className="text-red-500 mx-auto mb-4" size={48} />
          <h2 className="text-2xl font-bold text-red-900 mb-2">Access Denied</h2>
          <p className="text-red-700">You need admin privileges to access this page.</p>
        </div>
      </div>
    )
  }

  return (
    <div className="space-y-6">
      <div className="flex items-center space-x-3">
        <Shield className="text-purple-600" size={32} />
        <div>
          <h1 className="text-3xl font-bold text-gray-900">Admin Panel</h1>
          <p className="text-gray-600">Manage and settle markets</p>
        </div>
      </div>

      {/* Vault Information Card */}
      <div className="bg-gradient-to-br from-blue-50 to-purple-50 rounded-xl shadow-lg p-6 border border-blue-200">
        <div className="flex items-center space-x-3 mb-4">
          <Wallet className="text-purple-600" size={24} />
          <h2 className="text-2xl font-bold text-gray-900">Vault Information</h2>
        </div>
        
        {vaultLoading ? (
          <div className="text-center py-4">
            <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-purple-600 mx-auto"></div>
          </div>
        ) : vaultInfo ? (
          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            <div className="bg-white rounded-lg p-4 shadow">
              <p className="text-sm text-gray-600 mb-1">Balance</p>
              <p className="text-2xl font-bold text-purple-600">
                {vaultInfo.balance?.toLocaleString(undefined, { maximumFractionDigits: 2 }) || '0'} LLL
              </p>
            </div>
            <div className="bg-white rounded-lg p-4 shadow">
              <p className="text-sm text-gray-600 mb-1">Status</p>
              <p className={`text-xl font-semibold ${vaultInfo.configured ? 'text-green-600' : 'text-red-600'}`}>
                {vaultInfo.configured ? '✓ Configured' : '✗ Not Configured'}
              </p>
            </div>
            <div className="bg-white rounded-lg p-4 shadow md:col-span-2">
              <p className="text-sm text-gray-600 mb-1">Public Key</p>
              <p className="text-xs font-mono text-gray-800 break-all">
                {vaultInfo.publicKey}
              </p>
            </div>
          </div>
        ) : (
          <div className="text-center py-4 text-red-600">
            Failed to load vault information
          </div>
        )}
      </div>

      {/* Markets Section */}
      <div className="mt-6">
        <h2 className="text-2xl font-bold text-gray-900 mb-4">Markets</h2>
      </div>

      {loading ? (
        <div className="text-center py-12">
          <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-purple-600 mx-auto"></div>
        </div>
      ) : (
        <div className="space-y-4">
          {markets.map(market => (
            <div key={market.id} className="bg-white rounded-lg shadow p-6">
              <div className="flex items-start justify-between">
                <div className="flex-1">
                  <div className="flex flex-wrap items-center gap-2 mb-2">
                    <h3 className="text-lg font-semibold text-gray-900">{market.title}</h3>
                    <span className={`px-2 py-1 rounded-full text-xs font-medium whitespace-nowrap ${
                      market.status === 'ACTIVE' ? 'bg-green-100 text-green-800' :
                      market.status === 'SETTLED' ? 'bg-gray-100 text-gray-800' :
                      'bg-yellow-100 text-yellow-800'
                    }`}>
                      {market.status}
                    </span>
                  </div>
                  
                  <div className="grid grid-cols-2 gap-3 sm:gap-4 mt-4">
                    <div>
                      <p className="text-sm text-gray-600">Total Volume</p>
                      <p className="font-semibold text-gray-900 mt-1">{market.totalVolume.toFixed(2)} LLL</p>
                    </div>
                    <div>
                      <p className="text-sm text-gray-600">YES Stakes</p>
                      <p className="font-semibold text-green-600 mt-1">{market.totalYesStake.toFixed(2)} LLL</p>
                    </div>
                    <div>
                      <p className="text-sm text-gray-600">NO Stakes</p>
                      <p className="font-semibold text-red-600 mt-1">{market.totalNoStake.toFixed(2)} LLL</p>
                    </div>
                    <div>
                      <p className="text-sm text-gray-600">Expiry</p>
                      <p className="font-semibold text-gray-900 mt-1">
                        {new Date(market.expiryDate).toLocaleDateString()}
                      </p>
                    </div>
                  </div>

                  {market.status === 'SETTLED' && (
                    <div className="mt-4 bg-gray-50 rounded-lg p-3">
                      <p className="text-sm text-gray-600">
                        Settled as <strong>{market.outcome}</strong> on{' '}
                        {new Date(market.settledAt).toLocaleString()}
                      </p>
                    </div>
                  )}

                  {(market.status === 'ACTIVE' || market.status === 'CLOSED') && (
                    <div className="mt-4 flex flex-wrap gap-2">
                      <button
                        onClick={() => handleSettleMarket(market.id, 'YES')}
                        disabled={settlingMarketId === market.id}
                        className="flex items-center space-x-1 px-4 py-2 bg-green-600 text-white rounded-lg hover:bg-green-700 disabled:bg-gray-400"
                      >
                        <CheckCircle size={16} />
                        <span>Settle YES</span>
                      </button>
                      <button
                        onClick={() => handleSettleMarket(market.id, 'NO')}
                        disabled={settlingMarketId === market.id}
                        className="flex items-center space-x-1 px-4 py-2 bg-red-600 text-white rounded-lg hover:bg-red-700 disabled:bg-gray-400"
                      >
                        <XCircle size={16} />
                        <span>Settle NO</span>
                      </button>
                      <button
                        onClick={() => handleSettleMarket(market.id, 'VOID')}
                        disabled={settlingMarketId === market.id}
                        className="flex items-center space-x-1 px-4 py-2 bg-gray-600 text-white rounded-lg hover:bg-gray-700 disabled:bg-gray-400"
                      >
                        <Ban size={16} />
                        <span>Void & Refund</span>
                      </button>
                    </div>
                  )}
                </div>
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  )
}


