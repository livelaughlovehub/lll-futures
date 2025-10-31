import { Link, useLocation } from 'react-router-dom'
import { Home, TrendingUp, FileText, Wallet, PlusCircle, Settings, Menu, X, Lock, User, ChevronDown, LogOut } from 'lucide-react'
import { useState, useEffect, useRef } from 'react'
import WalletButton from './WalletButton'
import { useAuth } from '../context/AuthContext'

export default function Navbar() {
  const location = useLocation()
  const [mobileMenuOpen, setMobileMenuOpen] = useState(false)
  const [userMenuOpen, setUserMenuOpen] = useState(false)
  const { currentUser, signOut } = useAuth()
  const userMenuRef = useRef(null)

  const isActive = (path) => location.pathname === path

  // Helper function to get image URL
  const getImageUrl = (profilePicturePath) => {
    if (!profilePicturePath) return ''
    
    // If it's already a full URL (http/https), return as is
    if (profilePicturePath.startsWith('http://') || profilePicturePath.startsWith('https://')) {
      return profilePicturePath
    }
    
    // If it's an uploaded file path like "/uploads/profiles/filename.jpg"
    // Convert it to use the file serving endpoint
    if (profilePicturePath.startsWith('/uploads/')) {
      const apiBase = import.meta.env.VITE_API_URL || 'http://localhost:8080/api'
      // Replace /uploads/ with /files/serve/uploads/
      const servedPath = profilePicturePath.replace('/uploads/', '/files/serve/uploads/')
      return `${apiBase}${servedPath}`
    }
    
    // Return as is for relative paths
    return profilePicturePath
  }

  // Close user menu when clicking outside
  useEffect(() => {
    const handleClickOutside = (event) => {
      if (userMenuRef.current && !userMenuRef.current.contains(event.target)) {
        setUserMenuOpen(false)
      }
    }

    document.addEventListener('mousedown', handleClickOutside)
    return () => {
      document.removeEventListener('mousedown', handleClickOutside)
    }
  }, [])

  const navItems = [
    { path: '/', icon: Home, label: 'Dashboard' },
    { path: '/markets', icon: TrendingUp, label: 'Markets' },
    { path: '/positions', icon: FileText, label: 'My Positions' },
    { path: '/wallet', icon: Wallet, label: 'Wallet' },
    { path: '/staking', icon: Lock, label: 'Staking' },
    { path: '/create', icon: PlusCircle, label: 'Create Market' },
  ]

  return (
    <nav className="bg-white shadow-lg sticky top-0 z-50">
      <div className="container mx-auto px-4">
        <div className="flex items-center justify-between h-16 min-w-0">
          {/* Logo */}
          <Link to="/" className="flex items-center space-x-2 group flex-shrink-0" onClick={() => setMobileMenuOpen(false)}>
            <div className="bg-gradient-to-r from-blue-600 to-purple-600 text-white font-bold text-xl px-4 py-2 rounded-lg group-hover:from-blue-700 group-hover:to-purple-700 transition-all duration-200">
              LL&L
            </div>
            <span className="text-xl font-semibold text-gray-800 hidden sm:inline">Futures</span>
          </Link>

          {/* Desktop Navigation Links */}
          <div className="hidden md:flex items-center space-x-1">
            {navItems.map((item) => {
              const Icon = item.icon
              return (
                <Link
                  key={item.path}
                  to={item.path}
                  className={`flex items-center space-x-1 px-4 py-2 rounded-lg transition-all duration-200 ${
                    isActive(item.path)
                      ? 'bg-blue-50 text-blue-600 shadow-sm'
                      : 'text-gray-600 hover:bg-gray-50 hover:text-gray-800'
                  }`}
                >
                  <Icon size={18} />
                  <span>{item.label}</span>
                </Link>
              )
            })}
            {currentUser && currentUser.isAdmin && (
              <Link
                to="/admin"
                className={`flex items-center space-x-1 px-4 py-2 rounded-lg transition-all duration-200 ${
                  isActive('/admin')
                    ? 'bg-purple-50 text-purple-600 shadow-sm'
                    : 'text-gray-600 hover:bg-gray-50 hover:text-gray-800'
                }`}
              >
                <Settings size={18} />
                <span>Admin</span>
              </Link>
            )}
          </div>

          {/* Wallet Connection */}
          <div className="flex items-center space-x-2 sm:space-x-3 flex-shrink-0 min-w-0">
            {/* Auth Buttons - Show when not authenticated */}
            {!currentUser && (
              <>
                <Link
                  to="/signin"
                  className="hidden sm:inline-flex items-center px-4 py-2 text-gray-600 hover:text-gray-800 transition-colors text-sm font-medium"
                >
                  Sign In
                </Link>
                <Link
                  to="/signup"
                  className="hidden sm:inline-flex items-center px-4 py-2 bg-gradient-to-r from-blue-600 to-purple-600 text-white rounded-lg hover:from-blue-700 hover:to-purple-700 transition-all duration-200 text-sm font-medium"
                >
                  Sign Up
                </Link>
              </>
            )}
            
            {/* User Menu - Show when authenticated */}
            {currentUser && (
              <div className="relative" ref={userMenuRef}>
                <button
                  onClick={() => setUserMenuOpen(!userMenuOpen)}
                  className="flex items-center space-x-2 px-3 py-2 rounded-lg hover:bg-gray-100 transition-colors"
                >
                  <div className="w-8 h-8 rounded-full overflow-hidden bg-gradient-to-r from-blue-500 to-purple-500 flex items-center justify-center relative">
                    {/* Always show placeholder as background */}
                    <User className="text-white absolute inset-0 flex items-center justify-center" size={16} />
                    {/* Show profile picture if it exists and loads successfully */}
                    {currentUser.profilePicture && (
                      <img
                        src={getImageUrl(currentUser.profilePicture)}
                        alt={currentUser.username}
                        className="w-full h-full object-cover relative z-10"
                        onError={(e) => {
                          // Hide image if it fails to load, placeholder will show through
                          e.target.style.display = 'none'
                        }}
                      />
                    )}
                  </div>
                  <span className="hidden sm:inline text-sm font-medium text-gray-700">
                    {currentUser.username}
                  </span>
                  <ChevronDown size={16} className="text-gray-500" />
                </button>
                
                {/* Dropdown Menu */}
                {userMenuOpen && (
                  <div className="absolute right-0 mt-2 w-48 bg-white rounded-lg shadow-lg border border-gray-200 py-1 z-50">
                    <Link
                      to="/profile"
                      onClick={() => setUserMenuOpen(false)}
                      className="flex items-center px-4 py-2 text-sm text-gray-700 hover:bg-gray-100"
                    >
                      <User size={16} className="mr-3" />
                      Profile
                    </Link>
                    {currentUser.isAdmin && (
                      <Link
                        to="/admin"
                        onClick={() => setUserMenuOpen(false)}
                        className="flex items-center px-4 py-2 text-sm text-gray-700 hover:bg-gray-100"
                      >
                        <Settings size={16} className="mr-3" />
                        Admin
                      </Link>
                    )}
                    <hr className="my-1" />
                    <button
                      onClick={() => {
                        signOut()
                        setUserMenuOpen(false)
                      }}
                      className="flex items-center w-full px-4 py-2 text-sm text-red-600 hover:bg-red-50"
                    >
                      <LogOut size={16} className="mr-3" />
                      Sign Out
                    </button>
                  </div>
                )}
              </div>
            )}

            {/* Wallet Button */}
            <div className="min-w-0">
              <WalletButton />
            </div>

            {/* Mobile Menu Button */}
            <button
              onClick={() => setMobileMenuOpen(!mobileMenuOpen)}
              className="md:hidden p-2 rounded-lg hover:bg-gray-100 transition-all duration-200"
              aria-label="Toggle menu"
            >
              {mobileMenuOpen ? <X size={24} /> : <Menu size={24} />}
            </button>
          </div>
        </div>

        {/* Mobile Menu Dropdown */}
        {mobileMenuOpen && (
          <div className="md:hidden border-t border-gray-200 py-2 bg-white shadow-lg">
            {/* Mobile Navigation Links */}
            {navItems.map((item) => {
              const Icon = item.icon
              return (
                <Link
                  key={item.path}
                  to={item.path}
                  onClick={() => setMobileMenuOpen(false)}
                  className={`flex items-center space-x-3 px-4 py-3 transition-all duration-200 ${
                    isActive(item.path)
                      ? 'bg-blue-50 text-blue-600 border-l-4 border-blue-600 shadow-sm'
                      : 'text-gray-700 hover:bg-gray-50 hover:text-gray-900'
                  }`}
                >
                  <Icon size={20} />
                  <span className="font-medium">{item.label}</span>
                </Link>
              )
            })}
            
            {/* Auth Links (Mobile) */}
            {!currentUser && (
              <>
                <Link
                  to="/signin"
                  onClick={() => setMobileMenuOpen(false)}
                  className="flex items-center space-x-3 px-4 py-3 transition-all duration-200 text-gray-700 hover:bg-gray-50 hover:text-gray-900"
                >
                  <User size={20} />
                  <span className="font-medium">Sign In</span>
                </Link>
                <Link
                  to="/signup"
                  onClick={() => setMobileMenuOpen(false)}
                  className="flex items-center space-x-3 px-4 py-3 transition-all duration-200 text-gray-700 hover:bg-gray-50 hover:text-gray-900"
                >
                  <User size={20} />
                  <span className="font-medium">Sign Up</span>
                </Link>
              </>
            )}
            
            {/* User Menu (Mobile) */}
            {currentUser && (
              <>
                <Link
                  to="/profile"
                  onClick={() => setMobileMenuOpen(false)}
                  className={`flex items-center space-x-3 px-4 py-3 transition-all duration-200 ${
                    isActive('/profile')
                      ? 'bg-blue-50 text-blue-600 border-l-4 border-blue-600 shadow-sm'
                      : 'text-gray-700 hover:bg-gray-50 hover:text-gray-900'
                  }`}
                >
                  <User size={20} />
                  <span className="font-medium">Profile</span>
                </Link>
                {currentUser.isAdmin && (
                  <Link
                    to="/admin"
                    onClick={() => setMobileMenuOpen(false)}
                    className={`flex items-center space-x-3 px-4 py-3 transition-all duration-200 ${
                      isActive('/admin')
                        ? 'bg-purple-50 text-purple-600 border-l-4 border-purple-600 shadow-sm'
                        : 'text-gray-700 hover:bg-gray-50 hover:text-gray-900'
                    }`}
                  >
                    <Settings size={20} />
                    <span className="font-medium">Admin</span>
                  </Link>
                )}
                <button
                  onClick={() => {
                    signOut()
                    setMobileMenuOpen(false)
                  }}
                  className="flex items-center space-x-3 w-full px-4 py-3 text-red-600 hover:bg-red-50 transition-all duration-200"
                >
                  <LogOut size={20} />
                  <span className="font-medium">Sign Out</span>
                </button>
              </>
            )}
          </div>
        )}
      </div>
    </nav>
  )
}

