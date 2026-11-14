import { useState } from 'react'
import type { FormEvent } from 'react'
import { uploadImport } from '../api/client'
import type { ImportBatch } from '../api/types'

interface UploadFormProps {
  onImported: () => void
}

function UploadForm({ onImported }: UploadFormProps) {
  const [file, setFile] = useState<File | null>(null)
  const [currency, setCurrency] = useState('XAF')
  const [sending, setSending] = useState(false)
  const [result, setResult] = useState<ImportBatch | null>(null)
  const [error, setError] = useState<string | null>(null)

  async function handleSubmit(event: FormEvent) {
    event.preventDefault()
    if (!file) return
    setSending(true)
    setError(null)
    setResult(null)
    try {
      setResult(await uploadImport(file, currency))
      onImported()
    } catch (err) {
      setError((err as Error).message)
    } finally {
      setSending(false)
    }
  }

  return (
    <div className="card">
      <form className="upload" onSubmit={handleSubmit}>
        <label className="field">
          Fichier de ventes (CSV ou Excel)
          <input type="file" accept=".csv,.xlsx" onChange={(e) => setFile(e.target.files?.[0] ?? null)} />
        </label>
        <label className="field">
          Devise
          <select value={currency} onChange={(e) => setCurrency(e.target.value)}>
            <option value="XAF">XAF (franc CFA)</option>
            <option value="EUR">EUR (euro)</option>
          </select>
        </label>
        <button className="btn" type="submit" disabled={!file || sending}>
          {sending ? 'Import en cours…' : 'Importer'}
        </button>
      </form>

      {result && (
        <p className="message success">
          {result.fileName} : {result.importedRows} ventes importées, {result.rejectedRows} lignes rejetées,{' '}
          {result.duplicateRows} doublons ignorés.
        </p>
      )}
      {error && <p className="message error">{error}</p>}
    </div>
  )
}

export default UploadForm