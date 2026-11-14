import { useEffect, useState } from 'react'
import type { ImportBatch, ImportStatus } from '../api/types'
import UploadForm from '../components/UploadForm'
import ImportErrors from '../components/ImportErrors'
import { deleteImport, fetchImports } from '../api/client'

const STATUS_LABELS: Record<ImportStatus, { label: string; tone: string }> = {
  COMPLETED: { label: 'Terminé', tone: 'good' },
  FAILED: { label: 'Échec', tone: 'bad' },
  PENDING: { label: 'En cours', tone: 'neutral' },
}

function ImportPage() {
  const [imports, setImports] = useState<ImportBatch[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const [refreshKey, setRefreshKey] = useState(0)
  const [selected, setSelected] = useState<ImportBatch | null>(null)

  // Se relance a chaque changement de refreshKey : apres un import ou une suppression
  useEffect(() => {
    fetchImports()
      .then((page) => setImports(page.content))
      .catch((err: Error) => setError(err.message))
      .finally(() => setLoading(false))
  }, [refreshKey])

  async function handleDelete(imp: ImportBatch) {
    const confirmed = window.confirm(
      `Supprimer l'import « ${imp.fileName} » et ses ${imp.importedRows} ventes ? Cette action est définitive.`)
    if (!confirmed) return
    try {
      await deleteImport(imp.id)
      if (selected?.id === imp.id) setSelected(null)
      setRefreshKey((k) => k + 1)
    } catch (err) {
      window.alert((err as Error).message)
    }
  }

  return (
    <section>
      <h1>Importer des ventes</h1>
      <p className="page-subtitle">Historique des fichiers importés.</p>
      <UploadForm onImported={() => setRefreshKey((k) => k + 1)} />

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
                <th></th>
              </tr>
            </thead>
            <tbody>
              {imports.map((imp) => {
                const status = STATUS_LABELS[imp.status]
                return (
                  <tr
                    key={imp.id}
                    className={`clickable ${selected?.id === imp.id ? 'selected' : ''}`}
                    onClick={() => setSelected(imp)}
                    tabIndex={0}
                    onKeyDown={(e) => { if (e.key === 'Enter') setSelected(imp) }}
                  >
                    <td>{imp.fileName}</td>
                    <td>{new Date(imp.createdAt).toLocaleString('fr-FR')}</td>
                    <td className="num">{imp.totalRows}</td>
                    <td className="num">{imp.importedRows}</td>
                    <td className="num">{imp.rejectedRows}</td>
                    <td className="num">{imp.duplicateRows}</td>
                    <td><span className={`pill ${status.tone}`}>{status.label}</span></td>
                    <td>
                      <button
                        type="button"
                        className="btn-link danger"
                        onClick={(e) => { e.stopPropagation(); handleDelete(imp) }}
                      >
                        Supprimer
                      </button>
                    </td>
                  </tr>
                )
              })}
            </tbody>
          </table>
        )}
      </div>

      {selected && <ImportErrors importId={selected.id} fileName={selected.fileName} />}
    </section>
  )
}

export default ImportPage