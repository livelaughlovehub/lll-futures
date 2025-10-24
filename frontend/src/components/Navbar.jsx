import { Link, useLocation } from 'react-router-dom'
import { Home, TrendingUp, FileText, Wallet, PlusCircle, Settings, Menu, X, Lock } from 'lucide-react'
import { useState } from 'react'
import WalletButton from './WalletButton'

export default function Navbar({ currentUser, users, onUserChange }) {
  const location = useLocation()
  const [mobileMenuOpen, setMobileMenuOpen] = useState(false)

  const isActive = (path) => location.pathname === path

  const navItems = [
    { path: '/', icon: Home, label: 'Dashboard' },
    { path: '/markets', icon: TrendingUp, label: 'Markets' },
    { path: '/positions', icon: FileText, label: 'My Positions' },
    { path: '/wallet', icon: Wallet, label: 'Wallet' },
    { path: '/staking', icon: Lock, label: 'Staking' },
    { path: '/create', icon: PlusCircle, label: 'Create Market' },
  ]

  return (
    <nav className="bg-white shadow-lg">
      <div className="container mx-auto px-4">
        <div className="flex items-center justify-between h-16">
          {/* Logo */}
          <Link to="/" className="flex items-center space-x-2" onClick={() => setMobileMenuOpen(false)}>
            <div className="bg-gradient-to-r from-blue-600 to-purple-600 text-white font-bold text-xl px-4 py-2 rounded-lg">
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
                  className={`flex items-center space-x-1 px-4 py-2 rounded-lg transition ${
                    isActive(item.path)
                      ? 'bg-blue-50 text-blue-600'
                      : 'text-gray-600 hover:bg-gray-50'
                  }`}
                >
                  <Icon size={18} />
                  <span>{item.label}</span>
                </Link>
              )
            })}
            {currentUser?.isAdmin && (
              <Link
                to="/admin"
                className={`flex items-center space-x-1 px-4 py-2 rounded-lg transition ${
                  isActive('/admin')
                    ? 'bg-purple-50 text-purple-600'
                    : 'text-gray-600 hover:bg-gray-50'
                }`}
              >
                <Settings size={18} />
                <span>Admin</span>
              </Link>
            )}
          </div>

          {/* Wallet Button & User Selector */}
          <div className="flex items-center space-x-2 sm:space-x-4">
            {/* Wallet Button */}
            <WalletButton />
            
            {/* Mobile Balance (always visible) */}
            {currentUser && (
              <div className="bg-gradient-to-r from-yellow-400 to-yellow-500 text-white px-2 sm:px-4 py-2 rounded-lg font-semibold text-sm sm:text-base">
                <span className="hidden xs:inline">{currentUser.tokenBalance.toFixed(2)} </span>
                <span className="xs:hidden">{Math.round(currentUser.tokenBalance)} </span>
                LLL
              </div>
            )}
            
            {/* Mobile User Selector - Simplified */}
            <div className="hidden sm:flex items-center space-x-2">
              <span className="text-sm text-gray-600">User:</span>
              <select
                value={currentUser?.id || ''}
                onChange={(e) => {
                  const user = users.find(u => u.id === parseInt(e.target.value))
                  onUserChange(user)
                }}
                className="px-3 py-1 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
              >
                {users.map(user => (
                  <option key={user.id} value={user.id}>
                    {user.username}
                  </option>
                ))}
              </select>
            </div>

            {/* Mobile Menu Button */}
            <button
              onClick={() => setMobileMenuOpen(!mobileMenuOpen)}
              className="md:hidden p-2 rounded-lg hover:bg-gray-100 transition"
              aria-label="Toggle menu"
            >
              {mobileMenuOpen ? <X size={24} /> : <Menu size={24} />}
            </button>
          </div>
        </div>

        {/* Mobile Menu Dropdown */}
        {mobileMenuOpen && (
          <div className="md:hidden border-t border-gray-200 py-2">
            {/* Mobile User Selector */}
            <div className="px-4 py-3 border-b border-gray-200 sm:hidden">
              <label className="block text-xs text-gray-600 mb-1">Switch User:</label>
              <select
                value={currentUser?.id || ''}
                onChange={(e) => {
                  const user = users.find(u => u.id === parseInt(e.target.value))
                  onUserChange(user)
                }}
                className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
              >
                {users.map(user => (
                  <option key={user.id} value={user.id}>
                    {user.username} ({user.tokenBalance.toFixed(0)} LLL)
                  </option>
                ))}
              </select>
            </div>

            {/* Mobile Navigation Links */}
            {navItems.map((item) => {
              const Icon = item.icon
              return (
                <Link
                  key={item.path}
                  to={item.path}
                  onClick={() => setMobileMenuOpen(false)}
                  className={`flex items-center space-x-3 px-4 py-3 transition ${
                    isActive(item.path)
                      ? 'bg-blue-50 text-blue-600 border-l-4 border-blue-600'
                      : 'text-gray-700 hover:bg-gray-50'
                  }`}
                >
                  <Icon size={20} />
                  <span className="font-medium">{item.label}</span>
                </Link>
              )
            })}
            
            {/* Admin Link (Mobile) */}
            {currentUser?.isAdmin && (
              <Link
                to="/admin"
                onClick={() => setMobileMenuOpen(false)}
                className={`flex items-center space-x-3 px-4 py-3 transition ${
                  isActive('/admin')
                    ? 'bg-purple-50 text-purple-600 border-l-4 border-purple-600'
                    : 'text-gray-700 hover:bg-gray-50'
                }`}
              >
                <Settings size={20} />
                <span className="font-medium">Admin</span>
              </Link>
            )}
          </div>
        )}
      </div>
    </nav>
  )
}

