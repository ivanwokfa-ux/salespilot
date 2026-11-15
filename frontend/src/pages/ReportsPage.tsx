import { useState } from 'react'
import { downloadReport } from '../api/client'

function ReportsPage() {
  const [from, setFrom] = useState('2026-01-01')
  const [to, setTo] = useState('2026-01-31')
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState<string | null>(null)

  async function handleDownload() {
    setLoading(true)
    setError(null)
    try {
      const blob = await downloadReport(from, to)

      // On cree une adresse temporaire vers le fichier en memoire, puis on "clique" dessus
      const url = URL.createObjectURL(blob)
      const link = document.createElement('a')
      link.href = url
      link.download = `rapport-ventes_${from}_${to}.xlsx`
      link.click()
      URL.revokeObjectURL(url) // on libere la memoire
    } catch (err) {
      setError((err as Error).message)
    } finally {
      setLoading(false)
    }
  }

  return (
    <section>
      <h1>Rapports</h1>
      <p className="page-subtitle">Téléchargez un rapport Excel prêt à être envoyé.</p>

      <div className="card">
        <h2 className="card-title">Rapport des ventes</h2>
        <p className="report-desc">
          Trois onglets : synthèse par devise, évolution mensuelle et top 10 des produits.
        </p>

        <div className="filters">
          <label className="field">Du<input type="date" value={from} onChange={(e) => setFrom(e.target.value)} /></label>
          <label className="field">Au<input type="date" value={to} onChange={(e) => setTo(e.target.value)} /></label>
          <button type="button" className="btn" onClick={handleDownload} disabled={loading || !from || !to}>
            {loading ? 'Génération…' : 'Télécharger le rapport'}
          </button>
        </div>

        {error && <p className="message error">{error}</p>}
      </div>
    </section>
  )
}

export default ReportsPage