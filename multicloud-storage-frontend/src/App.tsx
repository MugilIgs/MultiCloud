import { useEffect, useState } from 'react'
import './App.css'
import Login from './Login'

const API_URL = 'http://localhost:8080'
const formatFileSize = (bytes: number) => {
  if (bytes < 1024) {
    return `${bytes} B`
  }

  const units = ['KB', 'MB', 'GB', 'TB']
  let size = bytes / 1024
  let unitIndex = 0

  while (size >= 1024 && unitIndex < units.length - 1) {
    size /= 1024
    unitIndex++
  }

  return `${size.toFixed(1)} ${units[unitIndex]}`
}
const REPLICA_PROVIDER = 'AWS_S3'
interface CloudStatus {
  healthy: boolean
  latencyMs?: number
  score?: number
}

interface CloudScores {
  [key: string]: CloudStatus
}

interface FileMetadata {
  id: number
  originalFilename: string
  size: number
  primaryProvider: string
  replicaProvider: string
}

function App() {
  const handleDownload = async (id: number, filename: string) => {
  const token = localStorage.getItem('token')

  if (!token) {
    alert('Please login again.')
    return
  }

  try {
    const response = await fetch(`${API_URL}/api/files/${id}/download`, {
      headers: {
        Authorization: `Bearer ${token}`,
      },
    })

  if (response.status === 401 || response.status === 403) {
  handleAuthenticationFailure()
  throw new Error('Authentication expired')
}

if (!response.ok) {
  throw new Error('Download failed')
}

    const blob = await response.blob()
    const url = window.URL.createObjectURL(blob)

    const link = document.createElement('a')
    link.href = url
    link.download = filename
    document.body.appendChild(link)
    link.click()
    link.remove()

    window.URL.revokeObjectURL(url)
  } catch (error) {
    console.error('Download error:', error)
    alert('Failed to download file.')
  }
}
const handleDelete = (id: number, filename: string) => {
  setDeleteFileId(id)
  setDeleteFileName(filename)
}
const confirmDelete = async () => {
  if (deleteFileId === null) {
    return
  }

  const token = localStorage.getItem('token')

  if (!token) {
    alert('Please login again.')
    return
  }

  try {
    const response = await fetch(
      `${API_URL}/api/files/${deleteFileId}`,
      {
        method: 'DELETE',
        headers: {
          Authorization: `Bearer ${token}`,
        },
      }
    )

   if (response.status === 401 || response.status === 403) {
  handleAuthenticationFailure()
  throw new Error('Authentication expired')
}

if (!response.ok) {
  throw new Error('Delete failed')
}

    setFiles((currentFiles) =>
      currentFiles.filter(
        (file) => file.id !== deleteFileId
      )
    )

    setDeleteFileId(null)
    setDeleteFileName('')
  } catch (error) {
    console.error('Delete error:', error)
    alert('Failed to delete file.')
  }
}
const handleUpload = async (file: File) => {
  setUploadMessage('')
    const token = localStorage.getItem('token')

    if (!token) {
        alert('Please login again.')
        return
    }

    const formData = new FormData()
    formData.append('file', file)

    try {
        const response = await fetch(`${API_URL}/api/files/upload`, {
            method: 'POST',
            headers: {
                Authorization: `Bearer ${token}`,
            },
            body: formData,
        })

    if (response.status === 401 || response.status === 403) {
    handleAuthenticationFailure()
    throw new Error('Authentication expired')
}

if (!response.ok) {
    const errorText = await response.text()
    throw new Error(errorText || 'Upload failed')
}

        const uploadedFile = await response.json()

        setFiles((currentFiles) => [
            ...currentFiles,
            uploadedFile,
        ])

       setUploadMessage('File uploaded successfully!')
    } catch (error) {
        console.error('Upload error:', error)
        alert('Failed to upload file.')
    }
}
  const [isAuthenticated, setIsAuthenticated] = useState(
  !!localStorage.getItem('token')
)


const [cloudStatus, setCloudStatus] = useState<CloudScores>({})
const [bestCloud, setBestCloud] = useState<string>('Loading...')
const [files, setFiles] = useState<FileMetadata[]>([])
const [deleteFileId, setDeleteFileId] = useState<number | null>(null)
const [deleteFileName, setDeleteFileName] = useState('')
const [uploadMessage, setUploadMessage] = useState('')
const [historicalPerformance, setHistoricalPerformance] =
  useState<Record<string, number>>({})

const handleAuthenticationFailure = () => {
  localStorage.removeItem('token')
  localStorage.removeItem('user')
  setIsAuthenticated(false)
}
  useEffect(() => {
    const token = localStorage.getItem('token')

    if (!token) {
      console.warn('No JWT token found in localStorage')
      return
    }

    const headers = {
      Authorization: `Bearer ${token}`,
    }
fetch(`${API_URL}/api/files`, {
  headers,
})
.then(async (response) => {
  if (response.status === 401 || response.status === 403) {
    handleAuthenticationFailure()
    throw new Error('Authentication expired')
  }

  if (!response.ok) {
    throw new Error('Failed to fetch files')
  }

  return response.json()
})
  .then((data) => {
    setFiles(data)
  })
  .catch((error) => {
    console.error('Files API error:', error)
  })

Promise.all([
  fetch(`${API_URL}/api/health/analysis`, {
    headers,
  }),
  fetch(`${API_URL}/api/health/historical`, {
    headers,
  }),
])
  .then(async ([
    analysisResponse,
    historicalResponse,
  ]) => {

    if (!analysisResponse.ok) {
      throw new Error('Failed to fetch adaptive cloud analysis')
    }

    if (!historicalResponse.ok) {
      throw new Error('Failed to fetch historical performance')
    }

    const analysisData = await analysisResponse.json()
    const historicalData = await historicalResponse.json()

    const combinedData: CloudScores = {}

    Object.keys(analysisData).forEach((provider) => {

      if (
        provider === 'selectedProvider'
      ) {
        return
      }

      const providerData = analysisData[provider]

      combinedData[provider] = {
        healthy: providerData.healthy,
        latencyMs: providerData.latencyMs,
        score: providerData.score,
      }
    })

    setCloudStatus(combinedData)
    setBestCloud(analysisData.selectedProvider)
    setHistoricalPerformance(historicalData)
  })
  .catch((error) => {
    console.error('Dashboard API error:', error)
  })
  }, [isAuthenticated])

  const getCloud = (provider: string): CloudStatus => {
    return cloudStatus[provider] || {
      healthy: false,
    }
  }
 if (!isAuthenticated) {
  return (
    <Login
      onLoginSuccess={() => setIsAuthenticated(true)}
    />
  )
}
  return (
    <div className="app">
      <header className="topbar">
        <div className="brand">
          <div className="brand-icon">☁</div>

          <div>
            <h1>Cloud of Clouds</h1>
            <p>Intelligent Multi-Cloud Storage</p>
          </div>
        </div>

        <div className="user-section">
          <span>Cloud Storage</span>

        <button
  className="logout-button"
  onClick={() => {
    localStorage.removeItem('token')
    localStorage.removeItem('user')
    window.location.reload()
  }}
>
  Logout
</button>
        </div>
      </header>

      <main className="dashboard">

        {/* Welcome */}

        <section className="welcome-section">
          <div>
            <p className="eyebrow">
              MULTI-CLOUD DASHBOARD
            </p>

            <h2>
              Welcome to Cloud of Clouds
            </h2>

            <p className="subtitle">
              Securely store, replicate and manage your files
              across multiple cloud providers.
            </p>
          </div>
        </section>


        {/* Cloud Health */}

        <section className="cloud-section">

          <div className="section-heading">
            <div>
              <h3>Cloud Health</h3>

              <p>
                Real-time status and performance
              </p>
            </div>
          </div>


          <div className="cloud-grid">

            {/* AWS */}

            <div className="cloud-card">

              <div className="cloud-card-header">

                <div className="provider-icon aws">
                  AWS
                </div>

                <span
                  className={`status ${
                    getCloud('AWS_S3').healthy
                      ? 'healthy'
                      : ''
                  }`}
                >
                  ●{' '}
                  {cloudStatus.AWS_S3
                    ? getCloud('AWS_S3').healthy
                      ? 'Healthy'
                      : 'Unavailable'
                    : 'Loading...'}
                </span>

              </div>

              <h4>AWS S3</h4>

<p>
  Amazon Simple Storage Service
</p>

{REPLICA_PROVIDER === 'AWS_S3' && (
  <span className="cloud-role replica">
    Replica
  </span>
)}

              <div className="cloud-metrics">

                <div>
                  <span>Latency</span>

                  <strong>
                    {getCloud('AWS_S3').latencyMs !== undefined
                      ? `${getCloud('AWS_S3').latencyMs} ms`
                      : '-- ms'}
                  </strong>
                </div>

                <div>
                  <span>Score</span>

                  <strong>
  {getCloud('AWS_S3').score !== undefined
    ? getCloud('AWS_S3').score?.toFixed(2)
    : '--'}
</strong>
                </div>

              </div>
            </div>


            {/* GCP */}

            <div className="cloud-card">

              <div className="cloud-card-header">

                <div className="provider-icon gcp">
                  GCP
                </div>

                <span
                  className={`status ${
                    getCloud('GCP').healthy
                      ? 'healthy'
                      : ''
                  }`}
                >
                  ●{' '}
                  {cloudStatus.GCP
                    ? getCloud('GCP').healthy
                      ? 'Healthy'
                      : 'Unavailable'
                    : 'Loading...'}
                </span>

              </div>

              <h4>Google Cloud</h4>

              <p>
                Google Cloud Storage
              </p>
              {bestCloud === 'GCP' ? (
  <div className="cloud-role selected">
    Selected
  </div>
) : (
  <div className="cloud-role candidate">
    Candidate
  </div>
)}

              <div className="cloud-metrics">

                <div>
                  <span>Latency</span>

                  <strong>
                    {getCloud('GCP').latencyMs !== undefined
                      ? `${getCloud('GCP').latencyMs} ms`
                      : '-- ms'}
                  </strong>
                </div>

                <div>
                  <span>Score</span>

                  <strong>
                    {getCloud('GCP').score !== undefined
                      ? getCloud('GCP').score?.toFixed(2)
                      : '--'}
                  </strong>
                </div>

              </div>
            </div>


            {/* Azure */}

            <div className="cloud-card">

              <div className="cloud-card-header">

                <div className="provider-icon azure">
                  AZ
                </div>

                <span
                  className={`status ${
                    getCloud('AZURE').healthy
                      ? 'healthy'
                      : ''
                  }`}
                >
                  ●{' '}
                  {cloudStatus.AZURE
                    ? getCloud('AZURE').healthy
                      ? 'Healthy'
                      : 'Unavailable'
                    : 'Loading...'}
                </span>

              </div>

              <h4>Microsoft Azure</h4>

              <p>
                Azure Blob Storage
              </p>
{bestCloud === 'AZURE' ? (
  <div className="cloud-role selected">
    Selected
  </div>
) : (
  <div className="cloud-role candidate">
    Candidate
  </div>
)}
              <div className="cloud-metrics">

                <div>
                  <span>Latency</span>

                  <strong>
                    {getCloud('AZURE').latencyMs !== undefined
                      ? `${getCloud('AZURE').latencyMs} ms`
                      : '-- ms'}
                  </strong>
                </div>

                <div>
                  <span>Score</span>

                  <strong>
                    {getCloud('AZURE').score !== undefined
                      ? getCloud('AZURE').score?.toFixed(2)
                      : '--'}
                  </strong>
                </div>

              </div>
            </div>

          </div>
        </section>


        {/* Adaptive Selection */}

        <section className="selection-card">

          <div className="selection-icon">
            🧠
          </div>

          <div className="selection-content">

            <p>
              ADAPTIVE CLOUD SELECTION
            </p>

            <h3>
              Best Cloud:{' '}
              <span>{bestCloud}</span>
            </h3>

            <small>
              Selected using cloud health, latency and
              intelligent scoring.
            </small>

          </div>

        </section>
<section className="learning-section">

  <div className="section-heading">
    <div>
      <h3>Adaptive Learning</h3>

      <p>
        Historical cloud performance learned from runtime observations
      </p>
    </div>
  </div>

  <div className="learning-grid">

    <div className="learning-card">
      <span>AWS S3</span>
      <strong>
        {historicalPerformance.AWS_S3 !== undefined
          ? historicalPerformance.AWS_S3.toFixed(2)
          : '--'}
      </strong>
    </div>

    <div className="learning-card">
      <span>Google Cloud</span>
      <strong>
        {historicalPerformance.GCP !== undefined
          ? historicalPerformance.GCP.toFixed(2)
          : '--'}
      </strong>
    </div>

    <div className="learning-card">
      <span>Microsoft Azure</span>
      <strong>
        {historicalPerformance.AZURE !== undefined
          ? historicalPerformance.AZURE.toFixed(2)
          : '--'}
      </strong>
    </div>

  </div>

</section>

        {/* Files */}

        <section className="files-section">

          <div className="section-heading">

            <div>
              <h3>My Files</h3>

              <p>
                Manage your encrypted multi-cloud files
              </p>
              {uploadMessage && (
  <div className="upload-success">
    {uploadMessage}
  </div>
)}
            </div>

            <>
    <input
        type="file"
        id="file-upload"
        style={{ display: 'none' }}
        onChange={(event) => {
            const selectedFile = event.target.files?.[0]

            if (selectedFile) {
                handleUpload(selectedFile)
            }
        }}
    />

    <button
        className="upload-button"
        onClick={() =>
            document.getElementById('file-upload')?.click()
        }
    >
        + Upload File
    </button>
</>

          </div>


          <div className="files-table">

            <div className="table-header">

              <span>File Name</span>
              <span>Size</span>
              <span>Primary</span>
              <span>Replica</span>
              <span>Actions</span>

            </div>


            {files.length === 0 ? (
  <div className="empty-state">

    <div className="empty-icon">
      📁
    </div>

    <h4>
      No files to display
    </h4>

    <p>
      Your uploaded files will appear here.
    </p>

  </div>
) : (
files.map((file) => (
  <div className="file-row" key={file.id}>
    <span className="file-name">
      {file.originalFilename}
    </span>

   <span className="file-size">
  {formatFileSize(file.size)}
</span>

    <span className="file-provider">
  {file.primaryProvider || '--'}
</span>

<span className="file-provider">
  {file.replicaProvider || '--'}
</span>

    <span className="file-actions">
      <button
        onClick={() => handleDownload(file.id, file.originalFilename)}
      >
        Download
      </button>

      <button
  onClick={() => handleDelete(file.id, file.originalFilename)}
>
  Delete
</button>
    </span>
  </div>
  ))
)}
{deleteFileId !== null && (
  <div className="delete-overlay">
    <div className="delete-dialog">
      <h3>Delete File?</h3>

      <p>
        Are you sure you want to delete{' '}
        <strong>{deleteFileName}</strong>?
      </p>

      <div className="delete-dialog-actions">
        <button
          type="button"
          className="cancel-delete"
          onClick={() => {
            setDeleteFileId(null)
            setDeleteFileName('')
          }}
        >
          Cancel
        </button>

        <button
          type="button"
          className="confirm-delete"
          onClick={confirmDelete}
        >
          Delete
        </button>
      </div>
    </div>
  </div>
)}
          </div>

        </section>

      </main>
    </div>
  )
}

export default App