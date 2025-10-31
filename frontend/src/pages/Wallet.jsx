import { useState, useEffect } from 'react'
import { getUserById, withdrawToWallet, checkDeposit } from '../api/api'
import { Wallet as WalletIcon, TrendingUp, ArrowUpRight, ArrowDownRight, Send, ExternalLink, CheckCircle, XCircle, Loader, Copy } from 'lucide-react'
import TokenBalanceDisplay from '../components/TokenBalanceDisplay'
import { useAuth } from '../context/AuthContext'

export default function Wallet() {
  const { currentUser } = useAuth()
  const [user, setUser] = useState(null)
  const [loading, setLoading] = useState(true)
  const [withdrawing, setWithdrawing] = useState(false)
  const [phantomWallet, setPhantomWallet] = useState('')
  const [withdrawAmount, setWithdrawAmount] = useState('')
  const [withdrawStatus, setWithdrawStatus] = useState(null) // { type: 'success' | 'error', message: string, tx: string }
  const [depositStatus, setDepositStatus] = useState(null) // { type: 'success' | 'error' | 'info', message: string }

  useEffect(() => {
    loadUser()
  }, [currentUser])

  const loadUser = async () => {
    if (!currentUser) return
    
    try {
      setLoading(true)
      const data = await getUserById(currentUser.id)
      setUser(data)
    } catch (error) {
      console.error('Failed to load user:', error)
    } finally {
      setLoading(false)
    }
  }

  const handleWithdraw = async (e) => {
    e.preventDefault()
    
    if (!currentUser || !user) return
    
    // Validation
    if (!phantomWallet.trim()) {
      setWithdrawStatus({ type: 'error', message: 'Please enter a wallet address' })
      return
    }
    
    const amount = parseFloat(withdrawAmount)
    if (isNaN(amount) || amount <= 0) {
      setWithdrawStatus({ type: 'error', message: 'Please enter a valid amount' })
      return
    }
    
    if (amount > user.tokenBalance) {
      setWithdrawStatus({ type: 'error', message: 'Insufficient balance' })
      return
    }
    
    try {
      setWithdrawing(true)
      setWithdrawStatus(null)
      
      const response = await withdrawToWallet(currentUser.id, phantomWallet.trim(), amount)
      
      if (response.success) {
        setWithdrawStatus({
          type: 'success',
          message: 'Withdrawal successful!',
          tx: response.transaction
        })
        // Refresh user data to get updated balance
        await loadUser()
        // Clear form
        setPhantomWallet('')
        setWithdrawAmount('')
      } else {
        setWithdrawStatus({
          type: 'error',
          message: response.message || 'Withdrawal failed'
        })
      }
    } catch (error) {
      console.error('Withdrawal error:', error)
      setWithdrawStatus({
        type: 'error',
        message: error.response?.data?.message || error.message || 'Failed to process withdrawal'
      })
    } finally {
      setWithdrawing(false)
    }
  }

  if (loading) {
    return (
      <div className="text-center py-12">
        <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-600 mx-auto"></div>
      </div>
    )
  }

  if (!user) {
    return <div>User not found</div>
  }

  return (
    <div className="space-y-4 sm:space-y-6">
      <div>
        <h1 className="text-2xl sm:text-3xl font-bold text-gray-900">Token Wallet</h1>
        <p className="text-sm sm:text-base text-gray-600 mt-1 sm:mt-2">Manage your LL&L Tokens</p>
      </div>

      {/* Token Balance Display */}
      <TokenBalanceDisplay 
        userBalance={user.tokenBalance} 
        stakedAmount={0} // We'll get this from API later
      />

      {/* Balance Card */}
      <div className="bg-gradient-to-br from-blue-600 to-purple-600 rounded-lg shadow-xl p-4 sm:p-8 text-white">
        <div className="flex items-center justify-between mb-4 sm:mb-6">
          <div className="flex items-center space-x-2">
            <WalletIcon size={20} className="sm:w-6 sm:h-6" />
            <span className="text-base sm:text-lg font-medium">LL&L Token Balance</span>
          </div>
          <div className="bg-white/20 backdrop-blur-sm px-2 sm:px-3 py-1 rounded-full text-xs sm:text-sm">
            {user.username}
          </div>
        </div>
        <div>
          <p className="text-3xl sm:text-4xl font-bold mb-2">{user.tokenBalance.toFixed(2)} LLL</p>
          <p className="text-sm sm:text-base text-blue-100">Available for trading</p>
        </div>
      </div>

      {/* Stats Grid */}
      <div className="grid grid-cols-1 sm:grid-cols-3 gap-3 sm:gap-4">
        <div className="bg-white rounded-lg shadow p-4 sm:p-6">
          <div className="flex items-center justify-between">
            <div>
              <p className="text-xs sm:text-sm text-gray-600">Account Type</p>
              <p className="text-lg sm:text-xl font-bold text-gray-900 mt-1">
                {user.isAdmin ? 'Admin' : 'Standard'}
              </p>
            </div>
            <TrendingUp className="text-blue-500" size={28} />
          </div>
        </div>

        <div className="bg-white rounded-lg shadow p-4 sm:p-6">
          <div className="flex items-center justify-between">
            <div>
              <p className="text-xs sm:text-sm text-gray-600">Member Since</p>
              <p className="text-base sm:text-xl font-bold text-gray-900 mt-1">
                {new Date(user.createdAt).toLocaleDateString()}
              </p>
            </div>
            <ArrowUpRight className="text-green-500" size={28} />
          </div>
        </div>

        <div className="bg-white rounded-lg shadow p-4 sm:p-6">
          <div className="flex items-center justify-between">
            <div>
              <p className="text-xs sm:text-sm text-gray-600">Token Symbol</p>
              <p className="text-lg sm:text-xl font-bold text-gray-900 mt-1">LLL</p>
            </div>
            <ArrowDownRight className="text-purple-500" size={28} />
          </div>
        </div>
      </div>

      {/* Deposit Section */}
      <div className="bg-white rounded-lg shadow p-4 sm:p-6">
        <h2 className="text-lg sm:text-xl font-bold text-gray-900 mb-3 sm:mb-4 flex items-center">
          <ArrowDownRight className="mr-2" size={20} />
          Deposit to Your App Wallet
        </h2>
        <p className="text-sm text-gray-600 mb-4">
          Send LL&L tokens from your Phantom or other Solana wallet to this address:
        </p>
        
        <div className="bg-gray-50 border border-gray-200 rounded-lg p-4 mb-4">
          <div className="flex items-center justify-between mb-2">
            <label className="text-xs font-medium text-gray-700">Your App Wallet Address:</label>
            <button
              onClick={() => {
                navigator.clipboard.writeText(user.walletAddress)
                setDepositStatus({ type: 'success', message: 'Wallet address copied to clipboard!' })
                setTimeout(() => setDepositStatus(null), 3000)
              }}
              className="inline-flex items-center text-xs text-blue-600 hover:text-blue-800 font-medium"
            >
              <Copy size={14} className="mr-1" />
              Copy
            </button>
          </div>
          <p className="text-sm font-mono text-gray-900 break-all">{user.walletAddress}</p>
        </div>
        
        <div className="bg-blue-50 border border-blue-200 rounded-lg p-3 sm:p-4 mb-4">
          <p className="text-xs sm:text-sm text-blue-800 font-medium mb-2">
            📋 How to deposit:
          </p>
          <ol className="list-decimal list-inside space-y-1 text-xs sm:text-sm text-blue-700">
            <li>Copy your app wallet address above</li>
            <li>Open your Phantom wallet (or other Solana wallet)</li>
            <li>Send LL&L tokens to this address</li>
            <li>Click "Check for Deposits" below to update your balance</li>
          </ol>
          <p className="text-xs text-blue-600 mt-3">
            ⚠️ Only send LL&L tokens to this address. Other tokens may be lost.
          </p>
        </div>

        <button
          onClick={async () => {
            if (!currentUser || !user) return
            try {
              setDepositStatus({ type: 'info', message: 'Checking for deposits...' })
              const result = await checkDeposit(currentUser.id)
              if (result.success && result.hasDeposit) {
                setDepositStatus({ 
                  type: 'success', 
                  message: `Deposit detected! ${result.difference.toFixed(2)} LLL added. New balance: ${result.newBalance.toFixed(2)} LLL` 
                })
                await loadUser() // Refresh user data
              } else {
                setDepositStatus({ type: 'info', message: 'No new deposits detected. Balance is up to date.' })
              }
            } catch (error) {
              setDepositStatus({ type: 'error', message: 'Failed to check deposit: ' + (error.response?.data?.message || error.message || 'Unknown error') })
            }
          }}
          className="w-full sm:w-auto px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition font-medium text-sm flex items-center justify-center"
        >
          Check for Deposits
        </button>

        {depositStatus && (
          <div className={`mt-4 p-3 rounded-lg ${
            depositStatus.type === 'success' 
              ? 'bg-green-50 border border-green-200' 
              : depositStatus.type === 'error'
              ? 'bg-red-50 border border-red-200'
              : 'bg-blue-50 border border-blue-200'
          }`}>
            <div className="flex items-start">
              {depositStatus.type === 'success' ? (
                <CheckCircle className="text-green-600 mr-2 mt-0.5" size={18} />
              ) : depositStatus.type === 'error' ? (
                <XCircle className="text-red-600 mr-2 mt-0.5" size={18} />
              ) : (
                <Loader className="text-blue-600 mr-2 mt-0.5 animate-spin" size={18} />
              )}
              <p className={`text-sm ${
                depositStatus.type === 'success' ? 'text-green-800' 
                : depositStatus.type === 'error' ? 'text-red-800'
                : 'text-blue-800'
              }`}>
                {depositStatus.message}
              </p>
            </div>
          </div>
        )}
      </div>

      {/* Withdrawal Section */}
      <div className="bg-white rounded-lg shadow p-4 sm:p-6">
        <h2 className="text-lg sm:text-xl font-bold text-gray-900 mb-3 sm:mb-4 flex items-center">
          <Send className="mr-2" size={20} />
          Withdraw to External Wallet
        </h2>
        <p className="text-sm text-gray-600 mb-4">
          Transfer your LL&L tokens to your Phantom or other Solana wallet
        </p>
        
        <form onSubmit={handleWithdraw} className="space-y-4">
          <div>
            <label htmlFor="phantomWallet" className="block text-sm font-medium text-gray-700 mb-2">
              Destination Wallet Address
            </label>
            <input
              type="text"
              id="phantomWallet"
              value={phantomWallet}
              onChange={(e) => setPhantomWallet(e.target.value)}
              placeholder="Enter Solana wallet address (e.g., 8JoNvVbo7Q6hz2x9xmYaisxvEerKKtEyigygw28D7fzq)"
              className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
              disabled={withdrawing}
              required
            />
          </div>
          
          <div>
            <label htmlFor="withdrawAmount" className="block text-sm font-medium text-gray-700 mb-2">
              Amount (LLL)
            </label>
            <input
              type="number"
              id="withdrawAmount"
              value={withdrawAmount}
              onChange={(e) => setWithdrawAmount(e.target.value)}
              placeholder="0.00"
              min="0"
              max={user.tokenBalance}
              step="0.01"
              className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
              disabled={withdrawing}
              required
            />
            <p className="mt-1 text-xs text-gray-500">
              Available: {user.tokenBalance.toFixed(2)} LLL
            </p>
          </div>
          
          {withdrawStatus && (
            <div className={`p-4 rounded-lg ${
              withdrawStatus.type === 'success' 
                ? 'bg-green-50 border border-green-200' 
                : 'bg-red-50 border border-red-200'
            }`}>
              <div className="flex items-start">
                {withdrawStatus.type === 'success' ? (
                  <CheckCircle className="text-green-600 mr-2 mt-0.5" size={20} />
                ) : (
                  <XCircle className="text-red-600 mr-2 mt-0.5" size={20} />
                )}
                <div className="flex-1">
                  <p className={`text-sm font-medium ${
                    withdrawStatus.type === 'success' ? 'text-green-800' : 'text-red-800'
                  }`}>
                    {withdrawStatus.message}
                  </p>
                  {withdrawStatus.tx && (
                    <a
                      href={`https://solscan.io/tx/${withdrawStatus.tx}?cluster=devnet`}
                      target="_blank"
                      rel="noopener noreferrer"
                      className="mt-2 inline-flex items-center text-xs text-blue-600 hover:text-blue-800"
                    >
                      View transaction on Solscan <ExternalLink size={12} className="ml-1" />
                    </a>
                  )}
                </div>
              </div>
            </div>
          )}
          
          <button
            type="submit"
            disabled={withdrawing || !phantomWallet.trim() || !withdrawAmount}
            className="w-full sm:w-auto px-6 py-3 bg-gradient-to-r from-blue-600 to-purple-600 text-white rounded-lg hover:from-blue-700 hover:to-purple-700 transition-all duration-200 font-medium disabled:opacity-50 disabled:cursor-not-allowed flex items-center justify-center"
          >
            {withdrawing ? (
              <>
                <Loader className="animate-spin mr-2" size={18} />
                Processing...
              </>
            ) : (
              <>
                <Send className="mr-2" size={18} />
                Withdraw Tokens
              </>
            )}
          </button>
        </form>
      </div>

      {/* Info Section */}
      <div className="bg-white rounded-lg shadow p-4 sm:p-6">
        <h2 className="text-lg sm:text-xl font-bold text-gray-900 mb-3 sm:mb-4">About LL&L Token</h2>
        <div className="space-y-2 sm:space-y-3 text-sm sm:text-base text-gray-600">
          <p>
            LL&L Token (LLL) is the native currency of the LL&L Futures platform. Use it to:
          </p>
          <ul className="list-disc list-inside space-y-1 sm:space-y-2 ml-2 sm:ml-4 text-sm sm:text-base">
            <li>Place bets on prediction markets</li>
            <li>Create new markets (requires tokens for market creation)</li>
            <li>Earn rewards from successful predictions</li>
            <li>Withdraw to your personal Solana wallet</li>
          </ul>
          <div className="bg-green-50 border border-green-200 rounded-lg p-3 sm:p-4 mt-3 sm:mt-4">
            <p className="text-xs sm:text-sm text-green-800 font-medium">
              ✅ Real Solana Integration: Your LL&L tokens are real SPL tokens on Solana Devnet. 
              You can withdraw them to any Solana wallet!
            </p>
          </div>
        </div>
      </div>
    </div>
  )
}

