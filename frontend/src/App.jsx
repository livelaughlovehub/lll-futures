import { BrowserRouter as Router, Routes, Route } from 'react-router-dom'
import { useState, useEffect } from 'react'
import { WalletProvider } from './context/WalletContext'
import Navbar from './components/Navbar'
import Dashboard from './pages/Dashboard'
import Markets from './pages/Markets'
import MyPositions from './pages/MyPositions'
import Wallet from './pages/Wallet'
import StakingPage from './pages/StakingPage'
import CreateMarket from './pages/CreateMarket'
import AdminPanel from './pages/AdminPanel'
import { getAllUsers } from './api/api'

function App() {
  const [currentUser, setCurrentUser] = useState(null)
  const [users, setUsers] = useState([])

  useEffect(() => {
    loadUsers()
  }, [])

  const loadUsers = async () => {
    try {
      const data = await getAllUsers()
      setUsers(data)
      // Set first non-admin user as default
      const defaultUser = data.find(u => !u.isAdmin) || data[0]
      setCurrentUser(defaultUser)
    } catch (error) {
      console.error('Failed to load users:', error)
    }
  }

  return (
    <WalletProvider>
      <Router>
        <div className="min-h-screen bg-gray-50">
          <Navbar 
            currentUser={currentUser} 
            users={users}
            onUserChange={setCurrentUser}
          />
          <main className="container mx-auto px-4 py-8">
            <Routes>
              <Route path="/" element={<Dashboard currentUser={currentUser} />} />
              <Route path="/markets" element={<Markets currentUser={currentUser} />} />
              <Route path="/positions" element={<MyPositions currentUser={currentUser} />} />
              <Route path="/wallet" element={<Wallet currentUser={currentUser} />} />
              <Route path="/staking" element={<StakingPage />} />
              <Route path="/create" element={<CreateMarket currentUser={currentUser} />} />
              <Route path="/admin" element={<AdminPanel currentUser={currentUser} />} />
            </Routes>
          </main>
        </div>
      </Router>
    </WalletProvider>
  )
}

export default App


