import { useState, useEffect } from 'react'
import { Link } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'
import { User, Mail, Camera, FileText, Edit3, Save, X, DollarSign, TrendingUp } from 'lucide-react'
import { getUserById, uploadFile, updateUserProfile } from '../api/api'

export default function Profile() {
  const { currentUser, loading: authLoading } = useAuth()
  const [profile, setProfile] = useState(null)
  const [isEditing, setIsEditing] = useState(false)
  const [editData, setEditData] = useState({
    username: '',
    email: '',
    profilePicture: '',
    bio: ''
  })
  const [loading, setLoading] = useState(false)
  const [uploading, setUploading] = useState(false)
  const [error, setError] = useState('')
  const [success, setSuccess] = useState('')

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

  useEffect(() => {
    if (currentUser) {
      loadProfile()
    }
  }, [currentUser])

  const loadProfile = async () => {
    try {
      const userData = await getUserById(currentUser.id)
      setProfile(userData)
      setEditData({
        username: userData.username || '',
        email: userData.email || '',
        profilePicture: userData.profilePicture || '',
        bio: userData.bio || ''
      })
    } catch (err) {
      console.error('Failed to load profile:', err)
    }
  }

  const handleEdit = () => {
    setIsEditing(true)
    setError('')
    setSuccess('')
  }

  const handleCancel = () => {
    setIsEditing(false)
    setEditData({
      username: profile.username || '',
      email: profile.email || '',
      profilePicture: profile.profilePicture || '',
      bio: profile.bio || ''
    })
  }

  const handleFileUpload = async (e) => {
    const file = e.target.files[0]
    if (!file) return

    // Validate file type
    if (!file.type.startsWith('image/')) {
      setError('Please select an image file')
      return
    }

    // Validate file size (max 5MB)
    if (file.size > 5 * 1024 * 1024) {
      setError('File size must be less than 5MB')
      return
    }

    setUploading(true)
    setError('')

    try {
      const result = await uploadFile(file, 'profiles')
      if (result.success === 'true') {
        setEditData({
          ...editData,
          profilePicture: result.fileUrl
        })
        setSuccess('Image uploaded successfully!')
      } else {
        setError(result.message || 'Failed to upload image')
      }
    } catch (err) {
      setError('Failed to upload image. Please try again.')
      console.error('Upload error:', err)
    } finally {
      setUploading(false)
    }
  }

  const handleSave = async () => {
    setLoading(true)
    setError('')
    setSuccess('')

    try {
      const updatedProfile = await updateUserProfile(currentUser.id, editData)
      setProfile(updatedProfile)
      setSuccess('Profile updated successfully!')
      setIsEditing(false)
    } catch (err) {
      setError('Failed to update profile. Please try again.')
      console.error('Profile update error:', err)
    } finally {
      setLoading(false)
    }
  }

  const handleChange = (e) => {
    setEditData({
      ...editData,
      [e.target.name]: e.target.value
    })
  }

  if (!currentUser) {
    return (
      <div className="min-h-screen bg-gray-50 flex items-center justify-center">
        <div className="text-center">
          <div className="bg-blue-100 rounded-full p-4 w-16 h-16 mx-auto mb-4">
            <User className="text-blue-600 mx-auto" size={32} />
          </div>
          <h2 className="text-xl font-semibold text-gray-900 mb-2">Please Sign In</h2>
          <p className="text-gray-600 mb-4">Sign in to view your profile</p>
          <Link to="/signin" className="inline-flex items-center px-6 py-3 bg-gradient-to-r from-blue-600 to-purple-600 text-white rounded-lg hover:from-blue-700 hover:to-purple-700 transition-all duration-200 font-medium">
            Sign In
          </Link>
        </div>
      </div>
    )
  }

  if (!profile) {
    return (
      <div className="min-h-screen bg-gray-50 flex items-center justify-center">
        <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-600"></div>
      </div>
    )
  }

  return (
    <div className="min-h-screen bg-gray-50 py-8">
      <div className="container mx-auto px-4 max-w-4xl">
        {/* Header */}
        <div className="mb-8">
          <h1 className="text-3xl font-bold text-gray-900">Profile</h1>
          <p className="text-gray-600 mt-2">Manage your account and trading preferences</p>
        </div>

        {/* Success/Error Messages */}
        {success && (
          <div className="bg-green-50 border border-green-200 rounded-lg p-4 mb-6">
            <p className="text-green-800">{success}</p>
          </div>
        )}
        {error && (
          <div className="bg-red-50 border border-red-200 rounded-lg p-4 mb-6">
            <p className="text-red-800">{error}</p>
          </div>
        )}

        <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
          {/* Profile Card */}
          <div className="lg:col-span-2">
            <div className="bg-white rounded-xl shadow-lg p-8">
              <div className="flex items-center justify-between mb-6">
                <h2 className="text-xl font-semibold text-gray-900">Profile Information</h2>
                {!isEditing && (
                  <button
                    onClick={handleEdit}
                    className="flex items-center px-4 py-2 text-blue-600 hover:text-blue-700 transition-colors"
                  >
                    <Edit3 size={16} className="mr-2" />
                    Edit Profile
                  </button>
                )}
              </div>

              <div className="space-y-6">
                {/* Profile Picture */}
                <div className="flex items-center space-x-6">
                  <div className="relative">
                    {editData.profilePicture ? (
                      <img
                        src={getImageUrl(editData.profilePicture)}
                        alt="Profile"
                        className="w-20 h-20 rounded-full object-cover border-4 border-gray-200"
                        onError={(e) => {
                          console.error('Failed to load image:', editData.profilePicture)
                        }}
                      />
                    ) : (
                      <div className="w-20 h-20 rounded-full bg-gradient-to-r from-blue-500 to-purple-500 flex items-center justify-center">
                        <User className="text-white" size={32} />
                      </div>
                    )}
                    {isEditing && (
                      <label className="absolute -bottom-2 -right-2 bg-blue-600 rounded-full p-2 cursor-pointer hover:bg-blue-700 transition-colors">
                        <Camera className="text-white" size={16} />
                        <input
                          type="file"
                          accept="image/*"
                          onChange={handleFileUpload}
                          className="hidden"
                          disabled={uploading}
                        />
                      </label>
                    )}
                    {uploading && (
                      <div className="absolute inset-0 bg-black bg-opacity-50 rounded-full flex items-center justify-center">
                        <div className="animate-spin rounded-full h-6 w-6 border-b-2 border-white"></div>
                      </div>
                    )}
                  </div>
                  <div className="flex-1">
                    <h3 className="text-lg font-semibold text-gray-900">
                      {isEditing ? (
                        <input
                          type="text"
                          name="username"
                          value={editData.username}
                          onChange={handleChange}
                          className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
                        />
                      ) : (
                        profile.username
                      )}
                    </h3>
                    <p className="text-gray-600">
                      {isEditing ? (
                        <input
                          type="email"
                          name="email"
                          value={editData.email}
                          onChange={handleChange}
                          className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
                        />
                      ) : (
                        profile.email
                      )}
                    </p>
                  </div>
                </div>

                {/* Bio */}
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-2">Bio</label>
                  {isEditing ? (
                    <textarea
                      name="bio"
                      value={editData.bio}
                      onChange={handleChange}
                      rows={4}
                      className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 resize-none"
                      placeholder="Tell us about yourself..."
                    />
                  ) : (
                    <p className="text-gray-700 bg-gray-50 p-4 rounded-lg">
                      {profile.bio || 'No bio provided'}
                    </p>
                  )}
                </div>

                {/* Profile Picture URL */}
                {isEditing && (
                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-2">
                      Profile Picture URL (optional)
                    </label>
                    <input
                      type="url"
                      name="profilePicture"
                      value={editData.profilePicture}
                      onChange={handleChange}
                      className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
                      placeholder="https://example.com/your-photo.jpg or upload a file using the camera icon above"
                    />
                    <p className="text-xs text-gray-500 mt-1">
                      You can either upload an image file or paste a URL to an existing image
                    </p>
                  </div>
                )}

                {/* Action Buttons */}
                {isEditing && (
                  <div className="flex space-x-4">
                    <button
                      onClick={handleSave}
                      disabled={loading || uploading}
                      className="flex items-center px-6 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 disabled:opacity-50 transition-colors"
                    >
                      <Save size={16} className="mr-2" />
                      {uploading ? 'Uploading...' : loading ? 'Saving...' : 'Save Changes'}
                    </button>
                    <button
                      onClick={handleCancel}
                      className="flex items-center px-6 py-2 bg-gray-200 text-gray-700 rounded-lg hover:bg-gray-300 transition-colors"
                    >
                      <X size={16} className="mr-2" />
                      Cancel
                    </button>
                  </div>
                )}
              </div>
            </div>
          </div>

          {/* Stats Card */}
          <div className="space-y-6">
            {/* Token Balance */}
            <div className="bg-white rounded-xl shadow-lg p-6">
              <div className="flex items-center justify-between mb-4">
                <h3 className="text-lg font-semibold text-gray-900">Token Balance</h3>
                <DollarSign className="text-yellow-500" size={24} />
              </div>
              <div className="text-3xl font-bold text-gray-900 mb-2">
                {profile.tokenBalance?.toFixed(2) || '0.00'} LLL
              </div>
              <p className="text-sm text-gray-600">Available for trading</p>
            </div>

            {/* Trading Stats */}
            <div className="bg-white rounded-xl shadow-lg p-6">
              <div className="flex items-center justify-between mb-4">
                <h3 className="text-lg font-semibold text-gray-900">Trading Stats</h3>
                <TrendingUp className="text-green-500" size={24} />
              </div>
              <div className="space-y-3">
                <div className="flex justify-between">
                  <span className="text-gray-600">Total Trades</span>
                  <span className="font-semibold">0</span>
                </div>
                <div className="flex justify-between">
                  <span className="text-gray-600">Win Rate</span>
                  <span className="font-semibold">-</span>
                </div>
                <div className="flex justify-between">
                  <span className="text-gray-600">Total Earned</span>
                  <span className="font-semibold text-green-600">0.00 LLL</span>
                </div>
              </div>
            </div>

            {/* Wallet Info */}
            <div className="bg-white rounded-xl shadow-lg p-6">
              <h3 className="text-lg font-semibold text-gray-900 mb-4">Wallet Information</h3>
              <div className="space-y-3">
                <div>
                  <span className="text-sm text-gray-600">Wallet Address</span>
                  <p className="font-mono text-sm bg-gray-50 p-2 rounded break-all">
                    {profile.walletAddress || 'Not connected'}
                  </p>
                </div>
                <div>
                  <span className="text-sm text-gray-600">Member Since</span>
                  <p className="text-sm">
                    {profile.createdAt ? new Date(profile.createdAt).toLocaleDateString() : 'Unknown'}
                  </p>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  )
}
