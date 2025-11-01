import { useEffect, useState } from 'react'
import { useNavigate, useSearchParams } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'

export default function OAuthCallback() {
  const [searchParams] = useSearchParams()
  const navigate = useNavigate()
  const { setToken } = useAuth()
  const [error, setError] = useState('')

  useEffect(() => {
    const token = searchParams.get('token')
    
    if (token) {
      // Store the token
      setToken(token)
      
      // Redirect to profile
      setTimeout(() => {
        navigate('/profile')
      }, 1500)
    } else {
      setError('Authentication failed. Please try again.')
      setTimeout(() => {
        navigate('/signin')
      }, 2000)
    }
  }, [searchParams, navigate, setToken])

  return (
    <div className="min-h-screen bg-gradient-to-br from-blue-50 to-purple-50 flex items-center justify-center p-4">
      <div className="max-w-md w-full bg-white rounded-xl shadow-lg p-8 text-center">
        {error ? (
          <>
            <div className="text-red-600 text-6xl mb-4">✕</div>
            <h2 className="text-2xl font-bold text-gray-900 mb-2">Authentication Failed</h2>
            <p className="text-gray-600">{error}</p>
          </>
        ) : (
          <>
            <div className="animate-spin rounded-full h-16 w-16 border-b-2 border-blue-600 mx-auto mb-4"></div>
            <h2 className="text-2xl font-bold text-gray-900 mb-2">Signing you in...</h2>
            <p className="text-gray-600">Redirecting to your profile</p>
          </>
        )}
      </div>
    </div>
  )
}

