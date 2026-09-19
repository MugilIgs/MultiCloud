import { useState } from 'react'

interface RegisterProps {
  onRegistrationSuccess: () => void
  onBackToLogin: () => void
}

const API_URL = 'http://localhost:8080'

function Register({
  onRegistrationSuccess,
  onBackToLogin,
}: RegisterProps) {
 const [name, setName] = useState('')
const [email, setEmail] = useState('')
const [password, setPassword] = useState('')
const [confirmPassword, setConfirmPassword] = useState('')
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(false)

  const handleRegister = async (event: React.FormEvent) => {
    event.preventDefault()

 setError('')

if (password !== confirmPassword) {
  setError('Passwords do not match')
  return
}

setLoading(true)

try {
      const response = await fetch(`${API_URL}/api/auth/register`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
      body: JSON.stringify({
  name,
  email,
  password,
}),
      })

      const data = await response.text()

      if (!response.ok) {
        throw new Error(data || 'Registration failed')
      }

      onRegistrationSuccess()
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

  return (
    <div className="login-page">
      <div className="login-card">
        <div className="login-icon">☁</div>

        <h1>Cloud of Clouds</h1>

        <p className="login-subtitle">
          Intelligent Multi-Cloud Storage
        </p>

        <h2>Create Account</h2>

        <p className="login-description">
          Register to start using your secure cloud storage.
        </p>

        <form onSubmit={handleRegister}>
            <div className="form-group">
  <label>Full Name</label>

  <input
    type="text"
    value={name}
    onChange={(event) => setName(event.target.value)}
    placeholder="Enter your full name"
    required
  />
</div>
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
              placeholder="Create a password"
              required
            />
          </div>
          <div className="form-group">
  <label>Confirm Password</label>

  <input
    type="password"
    value={confirmPassword}
    onChange={(event) => setConfirmPassword(event.target.value)}
    placeholder="Confirm your password"
    required
  />
</div>

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
            {loading ? 'Creating Account...' : 'Register'}
          </button>
        </form>

        <p className="auth-switch">
          Already have an account?{' '}
          <button
            type="button"
            onClick={() => {
              setError('')
              onBackToLogin()
            }}
          >
            Login
          </button>
        </p>
      </div>
    </div>
  )
}

export default Register