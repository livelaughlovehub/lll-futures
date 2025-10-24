import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { createMarket } from '../api/api'
import { PlusCircle } from 'lucide-react'

export default function CreateMarket({ currentUser }) {
  const navigate = useNavigate()
  const [formData, setFormData] = useState({
    title: '',
    description: '',
    expiryDate: '',
    yesOdds: '2.0',
    noOdds: '2.0',
  })
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState(null)

  const handleChange = (e) => {
    setFormData({
      ...formData,
      [e.target.name]: e.target.value
    })
  }

  const handleSubmit = async (e) => {
    e.preventDefault()
    
    if (!currentUser) {
      setError('Please select a user first')
      return
    }

    setLoading(true)
    setError(null)

    try {
      const marketData = {
        title: formData.title,
        description: formData.description,
        expiryDate: new Date(formData.expiryDate).toISOString(),
        yesOdds: parseFloat(formData.yesOdds),
        noOdds: parseFloat(formData.noOdds),
        creatorId: currentUser.id
      }

      await createMarket(marketData)
      navigate('/markets')
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to create market')
    } finally {
      setLoading(false)
    }
  }

  // Get minimum date (tomorrow)
  const tomorrow = new Date()
  tomorrow.setDate(tomorrow.getDate() + 1)
  const minDate = tomorrow.toISOString().slice(0, 16)

  return (
    <div className="max-w-2xl mx-auto space-y-4 sm:space-y-6">
      <div>
        <h1 className="text-2xl sm:text-3xl font-bold text-gray-900">Create New Market</h1>
        <p className="text-sm sm:text-base text-gray-600 mt-1 sm:mt-2">Launch your own prediction market</p>
      </div>

      <div className="bg-white rounded-lg shadow p-4 sm:p-6">
        <form onSubmit={handleSubmit} className="space-y-4 sm:space-y-6">
          {error && (
            <div className="bg-red-50 border border-red-200 text-red-700 px-3 sm:px-4 py-2 sm:py-3 rounded text-sm sm:text-base">
              {error}
            </div>
          )}

          <div>
            <label className="block text-sm font-medium text-gray-700 mb-2">
              Market Title *
            </label>
            <input
              type="text"
              name="title"
              value={formData.title}
              onChange={handleChange}
              required
              placeholder="e.g., Will Bitcoin reach $100K by end of 2025?"
              className="w-full px-3 sm:px-4 py-2 sm:py-3 text-base border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
            />
          </div>

          <div>
            <label className="block text-sm font-medium text-gray-700 mb-2">
              Description
            </label>
            <textarea
              name="description"
              value={formData.description}
              onChange={handleChange}
              rows={4}
              placeholder="Provide details about how this market will be resolved..."
              className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
            />
          </div>

          <div>
            <label className="block text-sm font-medium text-gray-700 mb-2">
              Expiry Date & Time *
            </label>
            <input
              type="datetime-local"
              name="expiryDate"
              value={formData.expiryDate}
              onChange={handleChange}
              min={minDate}
              required
              className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
            />
          </div>

          <div className="grid grid-cols-2 gap-4">
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-2">
                YES Odds *
              </label>
              <input
                type="number"
                name="yesOdds"
                value={formData.yesOdds}
                onChange={handleChange}
                step="0.1"
                min="1.1"
                required
                className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
              />
              <p className="text-xs text-gray-500 mt-1">Payout multiplier for YES bets</p>
            </div>

            <div>
              <label className="block text-sm font-medium text-gray-700 mb-2">
                NO Odds *
              </label>
              <input
                type="number"
                name="noOdds"
                value={formData.noOdds}
                onChange={handleChange}
                step="0.1"
                min="1.1"
                required
                className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
              />
              <p className="text-xs text-gray-500 mt-1">Payout multiplier for NO bets</p>
            </div>
          </div>

          <div className="bg-blue-50 border border-blue-200 rounded-lg p-4">
            <h3 className="font-medium text-blue-900 mb-2">Market Preview</h3>
            <div className="text-sm text-blue-800 space-y-1">
              <p><strong>YES</strong> bet of 100 LLL pays: <strong>{(100 * parseFloat(formData.yesOdds || 2)).toFixed(2)} LLL</strong></p>
              <p><strong>NO</strong> bet of 100 LLL pays: <strong>{(100 * parseFloat(formData.noOdds || 2)).toFixed(2)} LLL</strong></p>
            </div>
          </div>

          <div className="flex flex-col sm:flex-row gap-3 sm:gap-4">
            <button
              type="submit"
              disabled={loading}
              className="flex-1 bg-blue-600 text-white px-6 py-3 rounded-lg font-medium hover:bg-blue-700 disabled:bg-gray-400 flex items-center justify-center space-x-2 text-base"
            >
              {loading ? (
                <div className="animate-spin rounded-full h-5 w-5 border-b-2 border-white"></div>
              ) : (
                <>
                  <PlusCircle size={20} />
                  <span>Create Market</span>
                </>
              )}
            </button>
            <button
              type="button"
              onClick={() => navigate('/markets')}
              className="px-6 py-3 border border-gray-300 rounded-lg font-medium hover:bg-gray-50 text-base"
            >
              Cancel
            </button>
          </div>
        </form>
      </div>
    </div>
  )
}

