import React, { createContext, useContext, useState, useEffect } from 'react'
import { signInUser, getUserById } from '../api/api'

const AuthContext = createContext()

export const useAuth = () => {
  const context = useContext(AuthContext)
  if (!context) {
    throw new Error('useAuth must be used within an AuthProvider')
  }
  return context
}

export const AuthProvider = ({ children }) => {
  const [currentUser, setCurrentUser] = useState(null)
  const [loading, setLoading] = useState(false)

  // Load user from localStorage on mount
  useEffect(() => {
    const savedUser = localStorage.getItem('currentUser')
    if (savedUser) {
      const parsedUser = JSON.parse(savedUser)
      setCurrentUser(parsedUser)
    }
  }, [])

  const signIn = async (email, password) => {
    setLoading(true)
    try {
      const response = await signInUser({ email, password })
      // response now contains { token, user }
      setCurrentUser(response.user)
      localStorage.setItem('jwtToken', response.token)
      localStorage.setItem('currentUser', JSON.stringify(response.user))
      return response.user
    } catch (error) {
      console.error('Sign in error:', error)
      throw error
    } finally {
      setLoading(false)
    }
  }

  const signOut = () => {
    setCurrentUser(null)
    localStorage.removeItem('jwtToken')
    localStorage.removeItem('currentUser')
  }

  const updateUser = (user) => {
    setCurrentUser(user)
    localStorage.setItem('currentUser', JSON.stringify(user))
  }

  const setToken = async (token) => {
    localStorage.setItem('jwtToken', token)
    // Decode token to get user info (basic JWT decode)
    try {
      const payload = JSON.parse(atob(token.split('.')[1]))
      // Fetch user details using userId from token
      const user = await getUserById(payload.userId)
      setCurrentUser(user)
      localStorage.setItem('currentUser', JSON.stringify(user))
    } catch (e) {
      console.error('Error decoding token or fetching user:', e)
    }
  }

  const value = {
    currentUser,
    signIn,
    signOut,
    updateUser,
    setToken,
    loading
  }

  return (
    <AuthContext.Provider value={value}>
      {children}
    </AuthContext.Provider>
  )
}
