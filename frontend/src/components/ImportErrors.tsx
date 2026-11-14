import { useEffect, useState } from 'react'
import { fetchImportErrors } from '../api/client'
import type { ImportErrorLine } from '../api/types'

interface ImportErrorsProps {
  importId: number
  fileName: string
}

function ImportErrors({ importId, fileName }: ImportErrorsProps) {
  const [errors, setErrors] = useState<ImportErrorLine[]>([])
  const [loading, setLoading] = useState(true)
  const [failure, setFailure] = useState<string | null>(null)

  useEffect(() => {
    let cancelled = false
    setLoading(true)
    setFailure(null)

    fetchImportErrors(importId)
      .then((page) => { if (!cancelled) setErrors(page.content) })
      .catch((err: Error) => { if (!cancelled) setFailure(err.message) })
      .finally(() => { if (!cancelled) setLoading(false) })

    // Si l'utilisateur clique sur un autre import avant la reponse, on ignore l'ancienne
    return () => { cancelled = true }
  }, [importId])

  return (
    <div className="card">
      <h2 className="card-title">Lignes rejetées · {fileName}</h2>

      {loading && <p>Chargement…</p>}
      {failure && <p className="message error">{failure}</p>}
      {!loading && !failure && errors.length === 0 && (
        <p className="message success">Aucune ligne rejetée : toutes les lignes de ce fichier étaient valides.</p>
      )}

      {!loading && errors.length > 0 && (
        <ul className="error-list">
          {errors.map((e) => (
            <li key={e.lineNumber}>
              <span className="line-number">ligne {e.lineNumber}</span>
              <span>
                {e.errorMessage}
                {e.rawLine && <code className="raw-line">{e.rawLine}</code>}
              </span>
            </li>
          ))}
        </ul>
      )}
    </div>
  )
}

export default ImportErrors