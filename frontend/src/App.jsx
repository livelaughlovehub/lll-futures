import { BrowserRouter as Router, Routes, Route } from 'react-router-dom'
import { WalletProvider } from './context/WalletContext'
import { AuthProvider, useAuth } from './context/AuthContext'
import BackendWakeup from './components/BackendWakeup'
import Navbar from './components/Navbar'
import Dashboard from './pages/Dashboard'
import Markets from './pages/Markets'
import MyPositions from './pages/MyPositions'
import Wallet from './pages/Wallet'
import StakingPage from './pages/StakingPage'
import CreateMarket from './pages/CreateMarket'
import AdminPanel from './pages/AdminPanel'
import Signup from './pages/Signup'
import SignIn from './pages/SignIn'
import Profile from './pages/Profile'

function AppRoutes() {
  const { currentUser } = useAuth()
  
  return (
    <Router>
      <div className="min-h-screen bg-gray-50">
        <Navbar />
        <main className="container mx-auto px-4 py-8">
          <Routes>
            <Route path="/" element={<Dashboard />} />
            <Route path="/markets" element={<Markets />} />
            <Route path="/positions" element={<MyPositions currentUser={currentUser} />} />
            <Route path="/wallet" element={<Wallet />} />
            <Route path="/staking" element={<StakingPage />} />
            <Route path="/create" element={<CreateMarket />} />
            <Route path="/admin" element={<AdminPanel currentUser={currentUser} />} />
            <Route path="/signup" element={<Signup />} />
            <Route path="/signin" element={<SignIn />} />
            <Route path="/profile" element={<Profile />} />
          </Routes>
        </main>
      </div>
    </Router>
  )
}

function App() {
  return (
    <BackendWakeup>
      <AuthProvider>
        <WalletProvider>
          <AppRoutes />
        </WalletProvider>
      </AuthProvider>
    </BackendWakeup>
  )
}

export default App