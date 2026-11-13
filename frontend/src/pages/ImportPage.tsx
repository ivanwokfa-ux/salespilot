import { useEffect, useState } from 'react'
import { fetchImports } from '../api/client'
import type { ImportBatch, ImportStatus } from '../api/types'

const STATUS_LABELS: Record<ImportStatus, { label: string; tone: string }> = {
  COMPLETED: { label: 'Terminé', tone: 'good' },
  FAILED: { label: 'Échec', tone: 'bad' },
  PENDING: { label: 'En cours', tone: 'neutral' },
}

function ImportPage() {
  const [imports, setImports] = useState<ImportBatch[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    fetchImports()
      .then((page) => setImports(page.content))
      .catch((err: Error) => setError(err.message))
      .finally(() => setLoading(false))
  }, [])

  return (
    <section>
      <h1>Importer des ventes</h1>
      <p className="page-subtitle">Historique des fichiers importés.</p>

      <div className="card">
        {loading && <p>Chargement…</p>}
        {error && <p>Impossible de charger les imports : {error}</p>}
        {!loading && !error && imports.length === 0 && <p>Aucun import pour le moment.</p>}

        {imports.length > 0 && (
          <table>
            <thead>
              <tr>
                <th>Fichier</th>
                <th>Date</th>
                <th className="num">Lignes</th>
                <th className="num">Importées</th>
                <th className="num">Rejetées</th>
                <th className="num">Doublons</th>
                <th>Statut</th>
              </tr>
            </thead>
            <tbody>
              {imports.map((imp) => {
                const status = STATUS_LABELS[imp.status]
                return (
                  <tr key={imp.id}>
                    <td>{imp.fileName}</td>
                    <td>{new Date(imp.createdAt).toLocaleString('fr-FR')}</td>
                    <td className="num">{imp.totalRows}</td>
                    <td className="num">{imp.importedRows}</td>
                    <td className="num">{imp.rejectedRows}</td>
                    <td className="num">{imp.duplicateRows}</td>
                    <td><span className={`pill ${status.tone}`}>{status.label}</span></td>
                  </tr>
                )
              })}
            </tbody>
          </table>
        )}
      </div>
    </section>
  )
}

export default ImportPage