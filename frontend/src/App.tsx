import { useEffect, useState } from 'react'
import { fetchImports } from './api/client'
import type { ImportBatch } from './api/types'

function App() {
  const [imports, setImports] = useState<ImportBatch[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)

  // Charge les imports une seule fois, a l'affichage du composant
  useEffect(() => {
    fetchImports()
      .then((page) => setImports(page.content))
      .catch((err: Error) => setError(err.message))
      .finally(() => setLoading(false))
  }, [])

  if (loading) {
    return <p>Chargement…</p>
  }
  if (error) {
    return <p>Impossible de charger les imports : {error}</p>
  }

  return (
    <main>
      <h1>SalesPilot</h1>
      <h2>Historique des imports</h2>

      {imports.length === 0 ? (
        <p>Aucun import pour le moment.</p>
      ) : (
        <table>
          <thead>
            <tr>
              <th>Fichier</th>
              <th>Date</th>
              <th>Lignes</th>
              <th>Importées</th>
              <th>Rejetées</th>
              <th>Doublons</th>
              <th>Statut</th>
            </tr>
          </thead>
          <tbody>
            {imports.map((imp) => (
              <tr key={imp.id}>
                <td>{imp.fileName}</td>
                <td>{new Date(imp.createdAt).toLocaleString('fr-FR')}</td>
                <td>{imp.totalRows}</td>
                <td>{imp.importedRows}</td>
                <td>{imp.rejectedRows}</td>
                <td>{imp.duplicateRows}</td>
                <td>{imp.status}</td>
              </tr>
            ))}
          </tbody>
        </table>
      )}
    </main>
  )
}

export default App