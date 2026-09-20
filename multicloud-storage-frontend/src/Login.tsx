import { useState } from 'react'
import Register from './Register'

interface LoginProps {
  onLoginSuccess: () => void
}

const API_URL = import.meta.env.VITE_API_URL || 'http://localhost:8080'

function Login({ onLoginSuccess }: LoginProps) {
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(false)
  const [showRegister, setShowRegister] = useState(false)
  const [successMessage, setSuccessMessage] = useState('')

  const handleLogin = async (event: React.FormEvent) => {
    event.preventDefault()

    setError('')
    setSuccessMessage('')
    setLoading(true)

    try {
      const response = await fetch(`${API_URL}/api/auth/login`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({
          email,
          password,
        }),
      })

      const data = await response.text()

      if (!response.ok) {
        throw new Error(data || 'Login failed')
      }

      localStorage.setItem('token', data)

      onLoginSuccess()
    } catch (err) {
      setError(
        err instanceof Error
          ? err.message
          : 'Unable to connect to the server'
      )
    } finally {
      setLoading(false)
    }
  }

  const handleRegistrationSuccess = () => {
    setShowRegister(false)
    setSuccessMessage('Registration successful! Please login.')
    setPassword('')
  }

  if (showRegister) {
    return (
      <Register
        onRegistrationSuccess={handleRegistrationSuccess}
        onBackToLogin={() => {
          setShowRegister(false)
          setError('')
          setSuccessMessage('')
        }}
      />
    )
  }

  return (
    <div className="login-page">
      <div className="login-card">
        <div className="login-icon">☁</div>

        <h1>Cloud of Clouds</h1>

        <p className="login-subtitle">
          Intelligent Multi-Cloud Storage
        </p>

        <h2>Welcome Back</h2>

        <p className="login-description">
          Login to access your secure cloud storage.
        </p>

        <form onSubmit={handleLogin}>
          <div className="form-group">
            <label>Email</label>

            <input
              type="email"
              value={email}
              onChange={(event) => setEmail(event.target.value)}
              placeholder="Enter your email"
              required
            />
          </div>

          <div className="form-group">
            <label>Password</label>

            <input
              type="password"
              value={password}
              onChange={(event) => setPassword(event.target.value)}
              placeholder="Enter your password"
              required
            />
          </div>

          {successMessage && (
            <div className="login-success">
              {successMessage}
            </div>
          )}

          {error && (
            <div className="login-error">
              {error}
            </div>
          )}

          <button
            type="submit"
            className="login-button"
            disabled={loading}
          >
            {loading ? 'Logging in...' : 'Login'}
          </button>
        </form>

        <p className="auth-switch">
          Don't have an account?{' '}
          <button
            type="button"
            onClick={() => {
              setShowRegister(true)
              setError('')
              setSuccessMessage('')
            }}
          >
            Register
          </button>
        </p>
      </div>
    </div>
  )
}

export default Login