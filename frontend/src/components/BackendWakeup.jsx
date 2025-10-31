import { useState, useEffect } from 'react'
import { healthCheck } from '../api/api'

export default function BackendWakeup({ onBackendReady, children }) {
  const [isBackendReady, setIsBackendReady] = useState(false)
  const [attempts, setAttempts] = useState(0)
  const [error, setError] = useState(null)

  useEffect(() => {
    let intervalId
    let timeoutId

    const pingBackend = async () => {
      try {
        await healthCheck()
        setIsBackendReady(true)
        setError(null)
        if (onBackendReady) onBackendReady()
        clearInterval(intervalId)
      } catch (err) {
        setAttempts(prev => prev + 1)
        setError(err.message)
        console.log(`Backend ping attempt ${attempts + 1} failed:`, err.message)
      }
    }

    // Start pinging immediately
    pingBackend()

    // Continue pinging every 3 seconds if backend isn't ready
    if (!isBackendReady) {
      intervalId = setInterval(pingBackend, 3000)
      
      // Stop trying after 5 minutes (100 attempts)
      timeoutId = setTimeout(() => {
        clearInterval(intervalId)
        setError('Backend failed to respond after 5 minutes. Please check if the backend is running.')
      }, 300000)
    }

    return () => {
      clearInterval(intervalId)
      clearTimeout(timeoutId)
    }
  }, [isBackendReady, attempts, onBackendReady])

  if (isBackendReady) {
    return children
  }

  return (
    <div className="min-h-screen bg-gray-50 flex items-center justify-center">
      <div className="max-w-md w-full mx-auto p-6">
        <div className="text-center">
          <div className="animate-spin rounded-full h-16 w-16 border-b-4 border-blue-600 mx-auto mb-6"></div>
          
          <h1 className="text-2xl font-bold text-gray-900 mb-4">
            Starting LL&L Futures Platform
          </h1>
          
          <p className="text-gray-600 mb-6">
            Waking up the backend server... This may take up to 2 minutes on first load.
          </p>
          
          <div className="bg-white rounded-lg shadow p-4 mb-6">
            <div className="flex items-center justify-between mb-2">
              <span className="text-sm text-gray-600">Connection attempts:</span>
              <span className="text-sm font-medium text-blue-600">{attempts}</span>
            </div>
            
            <div className="w-full bg-gray-200 rounded-full h-2">
              <div 
                className="bg-blue-600 h-2 rounded-full transition-all duration-300"
                style={{ width: `${Math.min((attempts / 40) * 100, 100)}%` }}
              ></div>
            </div>
          </div>
          
          {error && attempts > 10 && (
            <div className="bg-yellow-50 border border-yellow-200 rounded-lg p-4 mb-4">
              <p className="text-sm text-yellow-800">
                <strong>Still trying...</strong> Backend services on can take 1-2 minutes to wake up.
              </p>
            </div>
          )}
          
          {attempts > 40 && (
            <div className="bg-red-50 border border-red-200 rounded-lg p-4">
              <p className="text-sm text-red-800">
                <strong>Taking longer than expected.</strong> Please ensure the backend is running.
              </p>
              {error && (
                <p className="text-xs text-red-600 mt-2">Error: {error}</p>
              )}
            </div>
          )}
        </div>
      </div>
    </div>
  )
}
